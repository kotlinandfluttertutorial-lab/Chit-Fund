"""
Payment repository — all DB queries for payments and ledger entries.

Financial records are NEVER hard-deleted. Use status=VOIDED with reason.
"""
from __future__ import annotations

import uuid
from datetime import datetime
from decimal import Decimal
from typing import List, Optional, Tuple

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.payment import (
    LedgerEntry,
    LedgerEntryType,
    Payment,
    PaymentMethod,
    PaymentStatus,
)


class PaymentRepository:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def create(
        self,
        *,
        member_id: uuid.UUID,
        chit_group_id: uuid.UUID,
        installment_schedule_id: uuid.UUID,
        amount: Decimal,
        late_fee: Decimal,
        total_amount: Decimal,
        payment_method: PaymentMethod,
        gateway_reference: Optional[str] = None,
        upi_vpa: Optional[str] = None,
    ) -> Payment:
        payment = Payment(
            member_id=member_id,
            chit_group_id=chit_group_id,
            installment_schedule_id=installment_schedule_id,
            amount=amount,
            late_fee=late_fee,
            total_amount=total_amount,
            payment_method=payment_method,
            status=PaymentStatus.PENDING,
            gateway_reference=gateway_reference,
            upi_vpa=upi_vpa,
        )
        self.db.add(payment)
        await self.db.commit()
        await self.db.refresh(payment)
        return payment

    async def get_by_id(
        self,
        payment_id: uuid.UUID,
        member_id: Optional[uuid.UUID] = None,
    ) -> Optional[Payment]:
        stmt = select(Payment).where(Payment.id == payment_id)
        if member_id is not None:
            stmt = stmt.where(Payment.member_id == member_id)
        result = await self.db.execute(stmt)
        return result.scalar_one_or_none()

    async def get_by_gateway_reference(self, gateway_reference: str) -> Optional[Payment]:
        result = await self.db.execute(
            select(Payment).where(Payment.gateway_reference == gateway_reference)
        )
        return result.scalar_one_or_none()

    async def list_by_member(
        self,
        member_id: uuid.UUID,
        group_id: Optional[uuid.UUID] = None,
        status: Optional[PaymentStatus] = None,
        from_date: Optional[datetime] = None,
        to_date: Optional[datetime] = None,
        page: int = 1,
        size: int = 20,
    ) -> Tuple[List[Payment], int]:
        stmt = select(Payment).where(Payment.member_id == member_id)

        if group_id is not None:
            stmt = stmt.where(Payment.chit_group_id == group_id)
        if status is not None:
            stmt = stmt.where(Payment.status == status)
        if from_date is not None:
            stmt = stmt.where(Payment.created_at >= from_date)
        if to_date is not None:
            stmt = stmt.where(Payment.created_at <= to_date)

        # Count total matching rows
        count_stmt = select(func.count()).select_from(stmt.subquery())
        total = (await self.db.execute(count_stmt)).scalar_one()

        # Apply pagination and ordering
        stmt = stmt.order_by(Payment.created_at.desc()).offset((page - 1) * size).limit(size)
        rows = (await self.db.execute(stmt)).scalars().all()
        return list(rows), total

    async def update_status(
        self,
        payment_id: uuid.UUID,
        status: PaymentStatus,
        gateway_reference: Optional[str] = None,
        receipt_number: Optional[str] = None,
        failure_reason: Optional[str] = None,
        void_reason: Optional[str] = None,
    ) -> Optional[Payment]:
        payment = await self.get_by_id(payment_id)
        if payment is None:
            return None
        payment.status = status
        if gateway_reference is not None:
            payment.gateway_reference = gateway_reference
        if receipt_number is not None:
            payment.receipt_number = receipt_number
        if failure_reason is not None:
            payment.failure_reason = failure_reason
        if void_reason is not None:
            payment.void_reason = void_reason
        await self.db.commit()
        await self.db.refresh(payment)
        return payment

    async def get_running_balance(self, chit_group_id: uuid.UUID) -> Decimal:
        """Return the latest running_balance for the given chit group's ledger."""
        result = await self.db.execute(
            select(LedgerEntry.running_balance)
            .where(LedgerEntry.chit_group_id == chit_group_id)
            .order_by(LedgerEntry.created_at.desc())
            .limit(1)
        )
        balance = result.scalar_one_or_none()
        return balance if balance is not None else Decimal("0.00")

    async def create_ledger_entry(
        self,
        *,
        chit_group_id: uuid.UUID,
        member_id: Optional[uuid.UUID],
        payment_id: Optional[uuid.UUID],
        entry_type: LedgerEntryType,
        narration: str,
        debit: Decimal,
        credit: Decimal,
        running_balance: Decimal,
    ) -> LedgerEntry:
        entry = LedgerEntry(
            chit_group_id=chit_group_id,
            member_id=member_id,
            payment_id=payment_id,
            entry_type=entry_type,
            narration=narration,
            debit=debit,
            credit=credit,
            running_balance=running_balance,
        )
        self.db.add(entry)
        await self.db.commit()
        await self.db.refresh(entry)
        return entry
