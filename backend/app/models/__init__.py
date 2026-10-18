"""
Import all models here so SQLAlchemy's mapper registry is fully populated
before any query runs. Without this, string-based relationship() targets
(e.g. "ChitMembership") can fail with KeyError at query time.
"""

from app.models.base import Base, TimestampMixin, UUIDMixin
from app.models.chit_group import ChitGroup, ChitGroupStatus, ChitMembership, MembershipStatus
from app.models.deferred_schema import (
    Auction,
    AuctionStatus,
    Bid,
    KYCDocument,
    KYCDocumentStatus,
    KYCDocumentType,
    Payout,
    PayoutStatus,
)
from app.models.member import AccountStatus, Gender, KYCStatus, Member
from app.models.member_session import MemberSession
from app.models.otp_session import OtpSession
from app.models.payment import (
    InstallmentSchedule,
    LedgerEntry,
    LedgerEntryType,
    Payment,
    PaymentMethod,
    PaymentStatus,
)
from app.models.policy_document import PolicyChunk

__all__ = [
    "Base",
    "TimestampMixin",
    "UUIDMixin",
    "Member",
    "Gender",
    "AccountStatus",
    "KYCStatus",
    "MemberSession",
    "OtpSession",
    "ChitGroup",
    "ChitGroupStatus",
    "ChitMembership",
    "MembershipStatus",
    "Payment",
    "PaymentMethod",
    "PaymentStatus",
    "InstallmentSchedule",
    "LedgerEntry",
    "LedgerEntryType",
    "KYCDocument",
    "KYCDocumentType",
    "KYCDocumentStatus",
    "Auction",
    "AuctionStatus",
    "Bid",
    "Payout",
    "PayoutStatus",
    "PolicyChunk",
]
