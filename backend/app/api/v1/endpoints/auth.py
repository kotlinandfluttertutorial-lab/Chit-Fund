"""
Auth endpoints — send OTP, verify OTP, register, refresh token, logout.

All endpoints are unauthenticated except /register, /refresh, /logout.
See docs/jira/kiro-prompts/auth/ for detailed specs.
"""
import hashlib
import secrets
from datetime import datetime, timedelta, timezone

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, field_validator
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.database import get_db
from app.core.deps import get_current_member
from app.core.security import (
    create_access_token,
    hash_refresh_token,
)

router = APIRouter()


# ── Schemas ───────────────────────────────────────────────────────────────────

class SendOtpRequest(BaseModel):
    mobile_number: str

    @field_validator("mobile_number")
    @classmethod
    def validate_mobile(cls, v: str) -> str:
        import re
        if not re.match(r"^\+91[6-9]\d{9}$", v):
            raise ValueError(
                "Invalid Indian mobile number. Must be +91 followed by 10 digits starting 6-9."
            )
        return v


class SendOtpResponse(BaseModel):
    message: str
    expires_in_seconds: int


class VerifyOtpRequest(BaseModel):
    mobile_number: str
    otp: str


class AuthTokenResponse(BaseModel):
    is_new_user: bool
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


class RegisterRequest(BaseModel):
    full_name: str
    email: str | None = None
    date_of_birth: str | None = None  # YYYY-MM-DD
    gender: str | None = None

    @field_validator("full_name")
    @classmethod
    def validate_name(cls, v: str) -> str:
        if len(v.strip()) < 2:
            raise ValueError("Full name must be at least 2 characters")
        return v.strip()


class RefreshRequest(BaseModel):
    refresh_token: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"


# ── Rate limiting helper (simple DB-based, no Redis dependency) ───────────────

async def check_otp_rate_limit(mobile_number: str, db: AsyncSession) -> None:
    """Raise 429 if more than 3 OTP requests in the last 60 minutes."""
    from app.models.otp_session import OtpSession
    one_hour_ago = datetime.now(timezone.utc) - timedelta(hours=1)
    result = await db.execute(
        select(OtpSession)
        .where(OtpSession.mobile_number == mobile_number)
        .where(OtpSession.created_at >= one_hour_ago)
    )
    recent_count = len(result.scalars().all())
    if recent_count >= 3:
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Too many OTP requests. Please wait before requesting again.",
        )


# ── Endpoints ─────────────────────────────────────────────────────────────────

@router.post("/send-otp", response_model=SendOtpResponse)
async def send_otp(
    request: SendOtpRequest,
    db: AsyncSession = Depends(get_db),
) -> SendOtpResponse:
    """
    Send a 6-digit OTP to the provided Indian mobile number.
    Rate-limited to 3 requests per mobile per hour.
    In DEBUG mode, OTP is logged to console instead of sending SMS.
    """
    from app.models.otp_session import OtpSession
    from app.services.sms_service import send_sms_otp

    await check_otp_rate_limit(request.mobile_number, db)

    # Generate OTP
    otp = str(secrets.randbelow(900000) + 100000)
    otp_hash = hashlib.sha256(otp.encode()).hexdigest()
    expires_at = datetime.now(timezone.utc) + timedelta(minutes=settings.OTP_EXPIRE_MINUTES)

    # Persist OTP session
    session = OtpSession(
        mobile_number=request.mobile_number,
        otp_hash=otp_hash,
        expires_at=expires_at,
    )
    db.add(session)
    await db.commit()

    # Send SMS (or log in DEBUG)
    await send_sms_otp(mobile_number=request.mobile_number, otp=otp)

    return SendOtpResponse(
        message="OTP sent successfully",
        expires_in_seconds=settings.OTP_EXPIRE_MINUTES * 60,
    )


@router.post("/verify-otp", response_model=AuthTokenResponse)
async def verify_otp(
    request: VerifyOtpRequest,
    db: AsyncSession = Depends(get_db),
) -> AuthTokenResponse:
    """
    Verify the 6-digit OTP. Returns JWT tokens and is_new_user flag.
    Creates a member record with kyc_status=NOT_REQUIRED if the mobile is new.
    """
    from app.models.member import AccountStatus, KYCStatus, Member
    from app.models.member_session import MemberSession
    from app.models.otp_session import OtpSession

    # Find latest valid unused OTP for this mobile
    result = await db.execute(
        select(OtpSession)
        .where(OtpSession.mobile_number == request.mobile_number)
        .where(OtpSession.is_used.is_(False))
        .where(OtpSession.expires_at > datetime.now(timezone.utc))
        .order_by(OtpSession.created_at.desc())
        .limit(1)
    )
    otp_session = result.scalar_one_or_none()

    if not otp_session:
        raise HTTPException(status_code=status.HTTP_410_GONE, detail="OTP expired or not found")

    # Verify hash
    provided_hash = hashlib.sha256(request.otp.encode()).hexdigest()
    if provided_hash != otp_session.otp_hash:
        otp_session.attempt_count += 1
        await db.commit()
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Incorrect OTP")

    # Mark OTP as used
    otp_session.is_used = True
    await db.commit()

    # Find or create member
    member_result = await db.execute(
        select(Member).where(Member.mobile_number == request.mobile_number)
    )
    member = member_result.scalar_one_or_none()
    is_new_user = member is None

    if is_new_user:
        member = Member(
            mobile_number=request.mobile_number,
            full_name="",  # filled in /register
            account_status=AccountStatus.ACTIVE,
            kyc_status=KYCStatus.NOT_REQUIRED,
            is_mobile_verified=True,
        )
        db.add(member)
        await db.commit()
        await db.refresh(member)
    else:
        member.is_mobile_verified = True
        await db.commit()

    # Issue tokens
    access_token = create_access_token(subject=str(member.id))
    refresh_token = secrets.token_urlsafe(64)
    refresh_hash = hash_refresh_token(refresh_token)

    member_session = MemberSession(
        member_id=member.id,
        refresh_token_hash=refresh_hash,
        expires_at=datetime.now(timezone.utc) + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS),
    )
    db.add(member_session)
    await db.commit()

    return AuthTokenResponse(
        is_new_user=is_new_user,
        access_token=access_token,
        refresh_token=refresh_token,
    )


@router.post("/register", response_model=AuthTokenResponse)
async def register(
    request: RegisterRequest,
    db: AsyncSession = Depends(get_db),
    current_member=Depends(get_current_member),
) -> AuthTokenResponse:
    """
    Update member profile after OTP verification (called when is_new_user=true).
    Requires a valid JWT from verify-otp.
    NO Aadhaar or PAN fields — KYC is deferred.
    """
    from datetime import date

    from app.models.member import Gender

    # Update name
    current_member.full_name = request.full_name

    # Update optional fields
    if request.email is not None:
        current_member.email = request.email

    if request.date_of_birth is not None:
        try:
            current_member.date_of_birth = date.fromisoformat(request.date_of_birth)
        except ValueError:
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail="date_of_birth must be in YYYY-MM-DD format",
            )

    if request.gender is not None:
        try:
            current_member.gender = Gender(request.gender.upper())
        except ValueError:
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail=f"Invalid gender value. Allowed: {[g.value for g in Gender]}",
            )

    await db.commit()
    await db.refresh(current_member)

    # Issue a fresh access token; no new refresh token on profile update
    access_token = create_access_token(subject=str(current_member.id))

    return AuthTokenResponse(
        is_new_user=False,
        access_token=access_token,
        refresh_token="",  # refresh token unchanged; client should keep existing one
    )


@router.post("/refresh", response_model=TokenResponse)
async def refresh_token(
    request: RefreshRequest,
    db: AsyncSession = Depends(get_db),
) -> TokenResponse:
    """Exchange a valid refresh token for a new access token."""
    from app.models.member_session import MemberSession

    refresh_hash = hash_refresh_token(request.refresh_token)
    result = await db.execute(
        select(MemberSession)
        .where(MemberSession.refresh_token_hash == refresh_hash)
        .where(MemberSession.is_revoked.is_(False))
        .where(MemberSession.expires_at > datetime.now(timezone.utc))
    )
    session = result.scalar_one_or_none()

    if not session:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired refresh token",
        )

    access_token = create_access_token(subject=str(session.member_id))
    return TokenResponse(access_token=access_token)


@router.post("/logout")
async def logout(
    request: RefreshRequest,
    db: AsyncSession = Depends(get_db),
) -> dict:
    """Revoke the refresh token, ending the session."""
    from app.models.member_session import MemberSession

    refresh_hash = hash_refresh_token(request.refresh_token)
    result = await db.execute(
        select(MemberSession).where(MemberSession.refresh_token_hash == refresh_hash)
    )
    session = result.scalar_one_or_none()

    if session:
        session.is_revoked = True
        await db.commit()

    return {"message": "Logged out successfully"}
