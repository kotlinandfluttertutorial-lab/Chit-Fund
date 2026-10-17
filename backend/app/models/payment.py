"""
Payment and financial models.

Financial records are NEVER hard-deleted. Use status = VOIDED with a reason.
See docs/scope-decisions.md and .kiro/steering/architecture.md.
"""

import enum

from sqlalchemy import Column, DateTime, Enum, ForeignKey, Integer, Numeric, String
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.models.base import Base, TimestampMixin, UUIDMixin


class PaymentMethod(str, enum.Enum):
    UPI = "UPI"
    NET_BANKING = "NET_BANKING"
    DEBIT_CARD = "DEBIT_CARD"
    NEFT = "NEFT"
    RTGS = "RTGS"
    CASH = "CASH"  # admin offline marking


class PaymentStatus(str, enum.Enum):
    PENDING = "PENDING"
    PROCESSING = "PROCESSING"
    SUCCESSFUL = "SUCCESSFUL"
    FAILED = "FAILED"
    REFUNDED = "REFUNDED"
    VOIDED = "VOIDED"  # never deleted — mark as voided with reason


class LedgerEntryType(str, enum.Enum):
    INSTALLMENT = "INSTALLMENT"
    LATE_FEE = "LATE_FEE"
    PAYOUT = "PAYOUT"
    FOREMAN_COMMISSION = "FOREMAN_COMMISSION"
    REFUND = "REFUND"
    ADJUSTMENT = "ADJUSTMENT"


class InstallmentSchedule(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "installment_schedules"

    chit_group_id = Column(UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False)
    month_number = Column(Integer, nullable=False)  # 1-based (month 1, 2, …24)
    due_date = Column(DateTime(timezone=True), nullable=False)
    amount_per_member = Column(Numeric(12, 2), nullable=False)

    chit_group = relationship("ChitGroup", back_populates="installment_schedules")
    payments = relationship("Payment", back_populates="installment_schedule")


class Payment(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "payments"

    member_id = Column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=False, index=True)
    chit_group_id = Column(UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False, index=True)
    installment_schedule_id = Column(UUID(as_uuid=True), ForeignKey("installment_schedules.id"), nullable=False)
    amount = Column(Numeric(12, 2), nullable=False)
    late_fee = Column(Numeric(12, 2), default=0, nullable=False)
    total_amount = Column(Numeric(12, 2), nullable=False)  # amount + late_fee
    payment_method = Column(Enum(PaymentMethod), nullable=False)
    status = Column(Enum(PaymentStatus), default=PaymentStatus.PENDING, nullable=False)
    gateway_reference = Column(String(255), nullable=True)
    bank_reference = Column(String(255), nullable=True)
    upi_vpa = Column(String(255), nullable=True)
    failure_reason = Column(String(512), nullable=True)
    void_reason = Column(String(512), nullable=True)  # if VOIDED
    receipt_number = Column(String(100), unique=True, nullable=True)

    member = relationship("Member")
    chit_group = relationship("ChitGroup")
    installment_schedule = relationship("InstallmentSchedule", back_populates="payments")


class LedgerEntry(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "ledger_entries"

    chit_group_id = Column(UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False, index=True)
    member_id = Column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=True)
    payment_id = Column(UUID(as_uuid=True), ForeignKey("payments.id"), nullable=True)
    entry_type = Column(Enum(LedgerEntryType), nullable=False)
    narration = Column(String(512), nullable=False)
    debit = Column(Numeric(12, 2), default=0, nullable=False)
    credit = Column(Numeric(12, 2), default=0, nullable=False)
    running_balance = Column(Numeric(14, 2), nullable=False)
    # Financial records are NEVER deleted — this table is append-only
