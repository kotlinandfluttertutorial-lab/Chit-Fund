"""
OTP session model — stores OTP hashes for mobile verification.
Records are never deleted; expired/used records are kept for audit.
"""
import uuid
from datetime import datetime, timezone

from sqlalchemy import Boolean, Column, DateTime, Integer, String
from sqlalchemy.dialects.postgresql import UUID

from app.models.base import Base


class OtpSession(Base):
    __tablename__ = "otp_sessions"

    id = Column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        nullable=False,
    )
    mobile_number = Column(String(15), nullable=False, index=True)
    otp_hash = Column(String(64), nullable=False)   # SHA-256 hex digest (64 chars)
    expires_at = Column(DateTime(timezone=True), nullable=False)
    is_used = Column(Boolean, default=False, nullable=False)
    attempt_count = Column(Integer, default=0, nullable=False)
    created_at = Column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        nullable=False,
    )
