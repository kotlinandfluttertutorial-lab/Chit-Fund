"""
Member model — represents a ChitSmart customer account.

KYC fields (aadhaar_number, pan_number, kyc_status, kyc_documents) are
included in the schema for future use but NOT exposed via the customer API
in this release. See docs/scope-decisions.md Decision 2.
"""

import enum

from sqlalchemy import Boolean, Column, Date, Enum, String
from sqlalchemy.orm import relationship

from app.models.base import Base, TimestampMixin, UUIDMixin


class KYCStatus(str, enum.Enum):
    NOT_REQUIRED = "NOT_REQUIRED"  # default for this release
    PENDING = "PENDING"
    UNDER_REVIEW = "UNDER_REVIEW"
    VERIFIED = "VERIFIED"
    REJECTED = "REJECTED"


class Gender(str, enum.Enum):
    MALE = "MALE"
    FEMALE = "FEMALE"
    OTHER = "OTHER"
    PREFER_NOT_TO_SAY = "PREFER_NOT_TO_SAY"


class AccountStatus(str, enum.Enum):
    ACTIVE = "ACTIVE"
    INACTIVE = "INACTIVE"
    SUSPENDED = "SUSPENDED"


class Member(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "members"

    # Core identity — collected at registration
    full_name = Column(String(255), nullable=False)
    mobile_number = Column(String(15), unique=True, nullable=False, index=True)
    email = Column(String(255), unique=True, nullable=True)
    date_of_birth = Column(Date, nullable=True)
    gender = Column(Enum(Gender), nullable=True)

    # Account state
    account_status = Column(
        Enum(AccountStatus),
        default=AccountStatus.ACTIVE,
        nullable=False,
    )
    is_mobile_verified = Column(Boolean, default=False, nullable=False)

    # KYC — schema present, NOT exposed via API in this release
    # kyc_status default NOT_REQUIRED for all members in this release
    kyc_status = Column(
        Enum(KYCStatus),
        default=KYCStatus.NOT_REQUIRED,
        nullable=False,
    )
    # Aadhaar and PAN stored encrypted; NOT collected via customer app in this release
    aadhaar_number_encrypted = Column(String(512), nullable=True)
    pan_number_encrypted = Column(String(512), nullable=True)

    # Preferences
    preferred_language = Column(String(10), default="en", nullable=False)

    # Relationships
    memberships = relationship("ChitMembership", back_populates="member")
    bank_accounts = relationship("BankAccount", back_populates="member")
    nominees = relationship("Nominee", back_populates="member")
    sessions = relationship("MemberSession", back_populates="member")
