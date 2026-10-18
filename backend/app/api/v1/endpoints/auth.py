"""
Auth endpoints — verify Firebase token, register, refresh token, logout.

/verify-firebase-token is unauthenticated (caller has no ChitSmart JWT yet).
/register requires a valid JWT issued by verify-firebase-token.
/refresh and /logout use refresh tokens directly.
"""

import logging
import secrets
from datetime import datetime, timedelta, timezone

import firebase_admin
from fastapi import APIRouter, Depends, HTTPException, status
from firebase_admin import auth as firebase_auth
from firebase_admin import credentials
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

logger = logging.getLogger(__name__)

router = APIRouter()


# ── Firebase initialisation (idempotent) ────────────────────────────────────────────


def _get_firebase_app() -> firebase_admin.App:
    """Return the already-initialised Firebase app, or initialise it on first call."""
    try:
        return firebase_admin.get_app()
    except ValueError:
        if settings.FIREBASE_SERVICE_ACCOUNT_JSON:
            # Inline JSON supplied (cloud / CI deployments)
            import json as _json

            cred = credentials.Certificate(_json.loads(settings.FIREBASE_SERVICE_ACCOUNT_JSON))
        else:
            # File path (local dev — put the downloaded key at FIREBASE_SERVICE_ACCOUNT_PATH)
            cred = credentials.Certificate(settings.FIREBASE_SERVICE_ACCOUNT_PATH)
        return firebase_admin.initialize_app(cred)


# ── Schemas ──────────────────────────────────────────────────────────────────────────


class VerifyFirebaseTokenRequest(BaseModel):
    firebase_id_token: str
    mobile_number: str

    @field_validator("mobile_number")
    @classmethod
    def validate_mobile(cls, v: str) -> str:
        import re

        if not re.match(r"^\+91[6-9]\d{9}$", v):
            raise ValueError("Invalid Indian mobile number. Must be +91 followed by 10 digits starting 6-9.")
        return v


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


# ── Endpoints ──────────────────────────────────────────────────────────────────────────


@router.post("/verify-firebase-token", response_model=AuthTokenResponse)
async def verify_firebase_token(
    request: VerifyFirebaseTokenRequest,
    db: AsyncSession = Depends(get_db),
) -> AuthTokenResponse:
    """
    Verify a Firebase Phone Auth ID token.

    Flow:
    1. Android app completes Firebase phone OTP → receives Firebase ID token.
    2. App sends token + mobile_number here.
    3. We verify with Firebase Admin SDK and issue ChitSmart JWT tokens.
    4. Member record is created if the mobile number is new.
    """
    from app.models.member import AccountStatus, KYCStatus, Member
    from app.models.member_session import MemberSession

    # Initialise Firebase Admin SDK (idempotent)
    _get_firebase_app()

    # Verify the Firebase ID token
    try:
        decoded = firebase_auth.verify_id_token(request.firebase_id_token)
    except Exception as exc:
        logger.warning("Firebase token verification failed: %s", exc)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail=f"Firebase token verification failed: {exc}",
        )

    # Confirm phone number in token matches what the app sent
    token_phone: str = decoded.get("phone_number", "")
    if token_phone != request.mobile_number:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Phone number mismatch between Firebase token and request",
        )

    # Find or create member
    member_result = await db.execute(select(Member).where(Member.mobile_number == request.mobile_number))
    member = member_result.scalar_one_or_none()
    is_new_user = member is None

    if member is None:
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

    # Issue ChitSmart JWT + refresh token
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
    Update member profile after Firebase verification (called when is_new_user=true).
    Requires a valid JWT from verify-firebase-token.
    NO Aadhaar or PAN fields — KYC is deferred.
    """
    from datetime import date

    from app.models.member import Gender

    current_member.full_name = request.full_name

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

    access_token = create_access_token(subject=str(current_member.id))

    return AuthTokenResponse(
        is_new_user=False,
        access_token=access_token,
        refresh_token="",
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
    result = await db.execute(select(MemberSession).where(MemberSession.refresh_token_hash == refresh_hash))
    session = result.scalar_one_or_none()

    if session:
        session.is_revoked = True
        await db.commit()

    return {"message": "Logged out successfully"}
