"""
Deferred feature schemas — KYC, Auctions, Bids.

These tables are created by Alembic migrations in this release but:
- Have NO API endpoints registered
- Have NO application code that reads or writes them
- Exist so future releases can add data without schema surgery

See docs/scope-decisions.md for the deferral rationale.
DO NOT add API endpoints for these models in this release.
"""

import enum
import uuid
from datetime import datetime
from decimal import Decimal
from typing import Optional

from sqlalchemy import Boolean, DateTime, Enum, ForeignKey, Integer, Numeric, String, Text
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base, TimestampMixin, UUIDMixin

# ── KYC (deferred — Decision 2) ───────────────────────────────────────────────


class KYCDocumentType(str, enum.Enum):
    AADHAAR_FRONT = "AADHAAR_FRONT"
    AADHAAR_BACK = "AADHAAR_BACK"
    PAN_CARD = "PAN_CARD"
    SELFIE = "SELFIE"
    LIVENESS_VIDEO = "LIVENESS_VIDEO"


class KYCDocumentStatus(str, enum.Enum):
    UPLOADED = "UPLOADED"
    AUTO_VERIFIED = "AUTO_VERIFIED"
    UNDER_REVIEW = "UNDER_REVIEW"
    VERIFIED = "VERIFIED"
    REJECTED = "REJECTED"


class KYCDocument(Base, UUIDMixin, TimestampMixin):
    """Schema created but no API endpoints in this release."""

    __tablename__ = "kyc_documents"

    member_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("members.id"), nullable=False, index=True
    )
    document_type: Mapped[KYCDocumentType] = mapped_column(Enum(KYCDocumentType), nullable=False)
    file_url: Mapped[Optional[str]] = mapped_column(String(1024), nullable=True)
    status: Mapped[KYCDocumentStatus] = mapped_column(
        Enum(KYCDocumentStatus),
        default=KYCDocumentStatus.UPLOADED,
        nullable=False,
    )
    extracted_data: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    reviewer_notes: Mapped[Optional[str]] = mapped_column(Text, nullable=True)
    reviewed_by_admin_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), nullable=True)


# ── Auctions (deferred — Decision 3) ─────────────────────────────────────────


class AuctionStatus(str, enum.Enum):
    SCHEDULED = "SCHEDULED"
    OPEN = "OPEN"
    CLOSED = "CLOSED"
    RESULT_APPROVED = "RESULT_APPROVED"
    CANCELLED = "CANCELLED"


class Auction(Base, UUIDMixin, TimestampMixin):
    """Schema created but no API endpoints in this release."""

    __tablename__ = "auctions"

    chit_group_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False, index=True
    )
    month_number: Mapped[int] = mapped_column(Integer, nullable=False)
    scheduled_start: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    scheduled_end: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    min_bid: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    max_bid: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    status: Mapped[AuctionStatus] = mapped_column(
        Enum(AuctionStatus),
        default=AuctionStatus.SCHEDULED,
        nullable=False,
    )
    winner_member_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), nullable=True)
    winning_bid_amount: Mapped[Optional[Decimal]] = mapped_column(Numeric(12, 2), nullable=True)
    result_approved_by: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), nullable=True)


class Bid(Base, UUIDMixin, TimestampMixin):
    """Schema created but no API endpoints in this release."""

    __tablename__ = "bids"

    auction_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("auctions.id"), nullable=False, index=True
    )
    member_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("members.id"), nullable=False, index=True
    )
    bid_amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    is_winner: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    is_voided: Mapped[bool] = mapped_column(Boolean, default=False, nullable=False)
    void_reason: Mapped[Optional[str]] = mapped_column(String(512), nullable=True)


# ── Payouts (read-only GET in this release — approval workflow deferred) ──────


class PayoutStatus(str, enum.Enum):
    PENDING = "PENDING"
    APPROVED = "APPROVED"
    PROCESSING = "PROCESSING"
    CREDITED = "CREDITED"
    FAILED = "FAILED"


class Payout(Base, UUIDMixin, TimestampMixin):
    """
    Read-only GET endpoint available in this release.
    POST/PATCH approval workflow endpoints are deferred with auctions.
    """

    __tablename__ = "payouts"

    member_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("members.id"), nullable=False, index=True
    )
    chit_group_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False, index=True
    )
    auction_id: Mapped[Optional[uuid.UUID]] = mapped_column(
        UUID(as_uuid=True), ForeignKey("auctions.id"), nullable=True
    )
    month_number: Mapped[int] = mapped_column(Integer, nullable=False)
    gross_amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    foreman_commission: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    net_amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    bank_account_id: Mapped[Optional[uuid.UUID]] = mapped_column(
        UUID(as_uuid=True), nullable=True  # FK to bank_accounts kept in DB schema; model removed
    )
    bank_reference: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    status: Mapped[PayoutStatus] = mapped_column(
        Enum(PayoutStatus),
        default=PayoutStatus.PENDING,
        nullable=False,
    )
    credit_date: Mapped[Optional[datetime]] = mapped_column(DateTime(timezone=True), nullable=True)
    approved_by_admin_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), nullable=True)
