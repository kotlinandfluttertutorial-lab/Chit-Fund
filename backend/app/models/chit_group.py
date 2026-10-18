"""
Chit group and membership models.

Auction-related fields exist in the schema (next_auction_date on ChitGroup)
but are informational only — no auction API endpoints are registered in this
release. See docs/scope-decisions.md Decision 3.
"""

import enum
import uuid
from datetime import date
from decimal import Decimal
from typing import Optional

from sqlalchemy import Boolean, Date, Enum, ForeignKey, Integer, Numeric, String, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base import Base, TimestampMixin, UUIDMixin


class ChitGroupStatus(str, enum.Enum):
    PENDING = "PENDING"  # created, not yet open for enrollment
    ACTIVE = "ACTIVE"  # open and running
    COMPLETED = "COMPLETED"  # all cycles done
    CLOSED = "CLOSED"  # manually closed


class MembershipStatus(str, enum.Enum):
    PENDING_ENROLLMENT = "PENDING_ENROLLMENT"
    ACTIVE = "ACTIVE"
    COMPLETED = "COMPLETED"
    SUSPENDED = "SUSPENDED"


class ChitGroup(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "chit_groups"

    name: Mapped[str] = mapped_column(String(255), nullable=False, unique=True)
    chit_value: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    monthly_installment: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    duration_months: Mapped[int] = mapped_column(Integer, nullable=False)
    max_members: Mapped[int] = mapped_column(Integer, nullable=False)
    foreman_commission_pct: Mapped[Decimal] = mapped_column(Numeric(5, 2), nullable=False)
    start_date: Mapped[date] = mapped_column(Date, nullable=False)
    status: Mapped[ChitGroupStatus] = mapped_column(
        Enum(ChitGroupStatus),
        default=ChitGroupStatus.PENDING,
        nullable=False,
    )
    terms_document_url: Mapped[Optional[str]] = mapped_column(String(1024), nullable=True)
    description: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    next_auction_date: Mapped[Optional[date]] = mapped_column(Date, nullable=True)

    # Relationships
    memberships = relationship("ChitMembership", back_populates="chit_group")
    installment_schedules = relationship("InstallmentSchedule", back_populates="chit_group")


class ChitMembership(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "chit_memberships"

    member_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=False)
    chit_group_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False)
    member_number: Mapped[int] = mapped_column(Integer, nullable=False)
    enrollment_date: Mapped[date] = mapped_column(Date, nullable=False)
    status: Mapped[MembershipStatus] = mapped_column(
        Enum(MembershipStatus),
        default=MembershipStatus.ACTIVE,
        nullable=False,
    )

    # Payout tracking — read-only in this release
    payout_received: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    payout_month: Mapped[Optional[int]] = mapped_column(Integer, nullable=True)  # month the member received payout

    # Relationships
    member = relationship("Member", back_populates="memberships")
    chit_group = relationship("ChitGroup", back_populates="memberships")
