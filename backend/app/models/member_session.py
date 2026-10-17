"""
Member session model — tracks refresh token sessions for authenticated members.
Sessions are revoked (is_revoked=True), never deleted.
"""

import uuid
from datetime import datetime, timezone

from sqlalchemy import Boolean, Column, DateTime, ForeignKey, String
from sqlalchemy.dialects.postgresql import JSONB, UUID
from sqlalchemy.orm import relationship

from app.models.base import Base


class MemberSession(Base):
    __tablename__ = "member_sessions"

    id = Column(
        UUID(as_uuid=True),
        primary_key=True,
        default=uuid.uuid4,
        nullable=False,
    )
    member_id = Column(
        UUID(as_uuid=True),
        ForeignKey("members.id"),
        nullable=False,
        index=True,
    )
    refresh_token_hash = Column(String(64), nullable=False, unique=True)
    device_info = Column(JSONB, nullable=True)  # {"model": ..., "os": ...}
    ip_address = Column(String(45), nullable=True)
    expires_at = Column(DateTime(timezone=True), nullable=False)
    is_revoked = Column(Boolean, default=False, nullable=False)
    created_at = Column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        nullable=False,
    )

    member = relationship("Member", back_populates="sessions")
