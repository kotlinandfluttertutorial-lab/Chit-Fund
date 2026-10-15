"""
Chit group and membership models.

Auction-related fields exist in the schema (next_auction_date on ChitGroup)
but are informational only — no auction API endpoints are registered in this
release. See docs/scope-decisions.md Decision 3.
"""
import enum
from decimal import Decimal
from sqlalchemy import Column, String, Integer, Numeric, Date, Boolean, Enum, ForeignKey, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship
from app.models.base import Base, UUIDMixin, TimestampMixin


class ChitGroupStatus(str, enum.Enum):
    PENDING = "PENDING"       # created, not yet open for enrollment
    ACTIVE = "ACTIVE"         # open and running
    COMPLETED = "COMPLETED"   # all cycles done
    CLOSED = "CLOSED"         # manually closed


class MembershipStatus(str, enum.Enum):
    PENDING_ENROLLMENT = "PENDING_ENROLLMENT"
    ACTIVE = "ACTIVE"
    COMPLETED = "COMPLETED"
    SUSPENDED = "SUSPENDED"


class ChitGroup(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "chit_groups"

    name = Column(String(255), nullable=False, unique=True)
    chit_value = Column(Numeric(12, 2), nullable=False)       # e.g. 50000.00
    monthly_installment = Column(Numeric(12, 2), nullable=False)
    duration_months = Column(Integer, nullable=False)          # e.g. 24
    max_members = Column(Integer, nullable=False)
    foreman_commission_pct = Column(Numeric(5, 2), nullable=False)  # e.g. 5.00
    start_date = Column(Date, nullable=False)
    status = Column(Enum(ChitGroupStatus), default=ChitGroupStatus.PENDING, nullable=False)
    terms_document_url = Column(String(1024), nullable=True)
    description = Column(Text, nullable=True)

    # Auction schema fields — present but unused in this release
    # next_auction_date is stored for informational display only
    next_auction_date = Column(Date, nullable=True)

    # Relationships
    memberships = relationship("ChitMembership", back_populates="chit_group")
    installment_schedules = relationship("InstallmentSchedule", back_populates="chit_group")


class ChitMembership(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "chit_memberships"

    member_id = Column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=False)
    chit_group_id = Column(UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False)
    member_number = Column(Integer, nullable=False)   # member's number within the group
    enrollment_date = Column(Date, nullable=False)
    status = Column(Enum(MembershipStatus), default=MembershipStatus.ACTIVE, nullable=False)

    # Payout tracking — read-only in this release
    payout_received = Column(Boolean, default=False, nullable=False)
    payout_month = Column(Integer, nullable=True)  # which month the member received payout

    # Relationships
    member = relationship("Member", back_populates="memberships")
    chit_group = relationship("ChitGroup", back_populates="memberships")
