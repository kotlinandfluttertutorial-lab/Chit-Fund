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

from sqlalchemy import Boolean, Column, DateTime, Enum, ForeignKey, Integer, Numeric, String, Text
from sqlalchemy.dialects.postgresql import UUID

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

    member_id = Column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=False, index=True)
    document_type = Column(Enum(KYCDocumentType), nullable=False)
    file_url = Column(String(1024), nullable=True)
    status = Column(Enum(KYCDocumentStatus), default=KYCDocumentStatus.UPLOADED, nullable=False)
    extracted_data = Column(Text, nullable=True)  # JSON of AI-extracted fields
    reviewer_notes = Column(Text, nullable=True)
    reviewed_by_admin_id = Column(UUID(as_uuid=True), nullable=True)


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

    chit_group_id = Column(UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False, index=True)
    month_number = Column(Integer, nullable=False)
    scheduled_start = Column(DateTime(timezone=True), nullable=False)
    scheduled_end = Column(DateTime(timezone=True), nullable=False)
    min_bid = Column(Numeric(12, 2), nullable=False)
    max_bid = Column(Numeric(12, 2), nullable=False)
    status = Column(Enum(AuctionStatus), default=AuctionStatus.SCHEDULED, nullable=False)
    winner_member_id = Column(UUID(as_uuid=True), nullable=True)
    winning_bid_amount = Column(Numeric(12, 2), nullable=True)
    result_approved_by = Column(UUID(as_uuid=True), nullable=True)


class Bid(Base, UUIDMixin, TimestampMixin):
    """Schema created but no API endpoints in this release."""

    __tablename__ = "bids"

    auction_id = Column(UUID(as_uuid=True), ForeignKey("auctions.id"), nullable=False, index=True)
    member_id = Column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=False, index=True)
    bid_amount = Column(Numeric(12, 2), nullable=False)
    is_winner = Column(Boolean, default=False, nullable=False)
    is_voided = Column(Boolean, default=False, nullable=False)
    void_reason = Column(String(512), nullable=True)


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

    member_id = Column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=False, index=True)
    chit_group_id = Column(UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False, index=True)
    auction_id = Column(UUID(as_uuid=True), ForeignKey("auctions.id"), nullable=True)
    month_number = Column(Integer, nullable=False)
    gross_amount = Column(Numeric(12, 2), nullable=False)
    foreman_commission = Column(Numeric(12, 2), nullable=False)
    net_amount = Column(Numeric(12, 2), nullable=False)
    bank_account_id = Column(UUID(as_uuid=True), ForeignKey("bank_accounts.id"), nullable=True)
    bank_reference = Column(String(255), nullable=True)
    status = Column(Enum(PayoutStatus), default=PayoutStatus.PENDING, nullable=False)
    credit_date = Column(DateTime(timezone=True), nullable=True)
    approved_by_admin_id = Column(UUID(as_uuid=True), nullable=True)
