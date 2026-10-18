"""
Payment and financial models.

Financial records are NEVER hard-deleted. Use status = VOIDED with a reason.
See docs/scope-decisions.md and .kiro/steering/architecture.md.
"""

import enum
import uuid
from datetime import datetime
from decimal import Decimal
from typing import Optional

from sqlalchemy import DateTime, Enum, ForeignKey, Integer, Numeric, String
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

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

    chit_group_id: Mapped[uuid.UUID] = mapped_column(UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False)
    month_number: Mapped[int] = mapped_column(Integer, nullable=False)  # 1-based (month 1, 2, …24)
    due_date: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    amount_per_member: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)

    chit_group = relationship("ChitGroup", back_populates="installment_schedules")
    payments = relationship("Payment", back_populates="installment_schedule")


class Payment(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "payments"

    member_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("members.id"), nullable=False, index=True
    )
    chit_group_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False, index=True
    )
    installment_schedule_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("installment_schedules.id"), nullable=False
    )
    amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    late_fee: Mapped[Decimal] = mapped_column(Numeric(12, 2), default=0, nullable=False)
    total_amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)  # amount + late_fee
    payment_method: Mapped[PaymentMethod] = mapped_column(Enum(PaymentMethod), nullable=False)
    status: Mapped[PaymentStatus] = mapped_column(
        Enum(PaymentStatus),
        default=PaymentStatus.PENDING,
        nullable=False,
    )
    gateway_reference: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    bank_reference: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    upi_vpa: Mapped[Optional[str]] = mapped_column(String(255), nullable=True)
    failure_reason: Mapped[Optional[str]] = mapped_column(String(512), nullable=True)
    void_reason: Mapped[Optional[str]] = mapped_column(String(512), nullable=True)  # if VOIDED
    receipt_number: Mapped[Optional[str]] = mapped_column(String(100), unique=True, nullable=True)

    member = relationship("Member")
    chit_group = relationship("ChitGroup")
    installment_schedule = relationship("InstallmentSchedule", back_populates="payments")


class LedgerEntry(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "ledger_entries"

    chit_group_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), ForeignKey("chit_groups.id"), nullable=False, index=True
    )
    member_id: Mapped[Optional[uuid.UUID]] = mapped_column(UUID(as_uuid=True), ForeignKey("members.id"), nullable=True)
    payment_id: Mapped[Optional[uuid.UUID]] = mapped_column(
        UUID(as_uuid=True), ForeignKey("payments.id"), nullable=True
    )
    entry_type: Mapped[LedgerEntryType] = mapped_column(Enum(LedgerEntryType), nullable=False)
    narration: Mapped[str] = mapped_column(String(512), nullable=False)
    debit: Mapped[Decimal] = mapped_column(Numeric(12, 2), default=0, nullable=False)
    credit: Mapped[Decimal] = mapped_column(Numeric(12, 2), default=0, nullable=False)
    running_balance: Mapped[Decimal] = mapped_column(Numeric(14, 2), nullable=False)
    # Financial records are NEVER deleted — this table is append-only
