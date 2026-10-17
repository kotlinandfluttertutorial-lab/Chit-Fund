"""
Payment service — business logic for payment creation, webhook processing,
receipt generation, and ledger double-entry.

No third-party payment gateway is integrated in this release.
Payment orders are created internally; gateway_reference holds a synthetic
order ID that will be replaced when a gateway is wired in a future release.

Financial records are NEVER deleted — use status=VOIDED with a reason.
"""

from __future__ import annotations

import hashlib
import hmac
import uuid
from datetime import datetime, timezone
from decimal import Decimal

from fastapi import HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.models.member import Member
from app.models.payment import LedgerEntryType, PaymentStatus
from app.repositories.installment_repo import InstallmentRepository
from app.repositories.payment_repo import PaymentRepository
from app.schemas.payment import PaymentCreateRequest, PaymentResponse, ReceiptResponse


class PaymentService:
    def __init__(
        self,
        payment_repo: PaymentRepository,
        installment_repo: InstallmentRepository,
    ) -> None:
        self.payment_repo = payment_repo
        self.installment_repo = installment_repo

    # ── Create payment intent ─────────────────────────────────────────────────

    async def create_payment_intent(
        self,
        db: AsyncSession,
        member: Member,
        request: PaymentCreateRequest,
    ) -> PaymentResponse:
        """
        Create a payment order and save a PENDING Payment record.
        Returns PaymentResponse with gateway_reference = internal order_id.

        No external gateway call is made in this release.  When a payment
        gateway is integrated, replace _create_internal_order with the
        gateway's order-creation call and update process_webhook accordingly.
        """
        schedule = await self.installment_repo.get_by_id(request.installment_schedule_id)
        if schedule is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Installment schedule not found",
            )

        amount = Decimal(str(schedule.amount_per_member))
        late_fee = Decimal("0.00")
        total_amount = amount + late_fee

        # Generate an internal order reference (no external gateway call)
        order_id = self._create_internal_order(member_id=member.id)

        payment = await self.payment_repo.create(
            member_id=member.id,
            chit_group_id=schedule.chit_group_id,
            installment_schedule_id=schedule.id,
            amount=amount,
            late_fee=late_fee,
            total_amount=total_amount,
            payment_method=request.payment_method,
            gateway_reference=order_id,
            upi_vpa=request.upi_vpa,
        )

        return PaymentResponse.model_validate(payment)

    def _create_internal_order(self, member_id: uuid.UUID) -> str:
        """
        Generate a unique internal order reference.
        Format: order_<16 hex chars>
        Replace this method when integrating a payment gateway.
        """
        return f"order_{uuid.uuid4().hex[:16]}"

    # ── Webhook ───────────────────────────────────────────────────────────────

    def verify_webhook_signature(
        self,
        payload_bytes: bytes,
        signature: str,
    ) -> bool:
        """
        Verify a webhook payload signature using HMAC-SHA256.
        key = PAYMENT_KEY_SECRET, message = raw payload bytes.

        Returns False when no PAYMENT_KEY_SECRET is configured (dev mode).
        Replace the signature scheme to match whichever gateway is integrated.
        """
        if not settings.PAYMENT_KEY_SECRET:
            return False
        expected = hmac.new(
            settings.PAYMENT_KEY_SECRET.encode(),
            payload_bytes,
            hashlib.sha256,
        ).hexdigest()
        return hmac.compare_digest(expected, signature)

    async def process_webhook(
        self,
        db: AsyncSession,
        order_id: str,
        gateway_payment_id: str,
        signature: str,
        payload_bytes: bytes,
    ) -> None:
        """
        Process a payment.captured webhook event.
        1. Verify signature.
        2. Find payment by order_id (gateway_reference).
        3. Mark SUCCESSFUL, generate receipt_number.
        4. Write double-entry LedgerEntry.

        The field name gateway_payment_id is gateway-agnostic.
        """
        if not self.verify_webhook_signature(payload_bytes, signature):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid webhook signature",
            )

        payment = await self.payment_repo.get_by_gateway_reference(order_id)
        if payment is None:
            # Unknown order — log and ignore (may be from a different environment)
            return

        if payment.status == PaymentStatus.SUCCESSFUL:
            # Idempotent — already processed
            return

        # Generate receipt number: CS-YYYYMM-XXXXXX
        now = datetime.now(timezone.utc)
        seq = str(payment.id).replace("-", "")[-6:].upper()
        receipt_number = f"CS-{now.strftime('%Y%m')}-{seq}"

        await self.payment_repo.update_status(
            payment.id,
            status=PaymentStatus.SUCCESSFUL,
            gateway_reference=gateway_payment_id,
            receipt_number=receipt_number,
        )

        # Reload updated payment
        payment = await self.payment_repo.get_by_id(payment.id)

        # Double-entry ledger: credit the group collection account
        current_balance = await self.payment_repo.get_running_balance(payment.chit_group_id)
        new_balance = current_balance + Decimal(str(payment.total_amount))

        await self.payment_repo.create_ledger_entry(
            chit_group_id=payment.chit_group_id,
            member_id=payment.member_id,
            payment_id=payment.id,
            entry_type=LedgerEntryType.INSTALLMENT,
            narration=f"Installment payment received — receipt {receipt_number}",
            debit=Decimal("0.00"),
            credit=Decimal(str(payment.total_amount)),
            running_balance=new_balance,
        )

    # ── Receipt ───────────────────────────────────────────────────────────────

    async def get_receipt(
        self,
        db: AsyncSession,
        payment_id: uuid.UUID,
        member: Member,
    ) -> ReceiptResponse:
        """Return receipt data for a completed payment owned by the member."""
        from sqlalchemy import select

        from app.models.chit_group import ChitGroup
        from app.models.payment import InstallmentSchedule

        payment = await self.payment_repo.get_by_id(payment_id, member_id=member.id)
        if payment is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Payment not found",
            )

        if payment.receipt_number is None:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Receipt not yet available — payment is not completed",
            )

        # Load related chit group and schedule
        group_result = await db.execute(select(ChitGroup).where(ChitGroup.id == payment.chit_group_id))
        group = group_result.scalar_one_or_none()

        schedule_result = await db.execute(
            select(InstallmentSchedule).where(InstallmentSchedule.id == payment.installment_schedule_id)
        )
        schedule = schedule_result.scalar_one_or_none()

        return ReceiptResponse(
            receipt_number=payment.receipt_number,
            payment_id=payment.id,
            member_name=member.full_name,
            group_name=group.name if group else "Unknown Group",
            month_number=schedule.month_number if schedule else 0,
            amount=Decimal(str(payment.amount)),
            late_fee=Decimal(str(payment.late_fee)),
            total_amount=Decimal(str(payment.total_amount)),
            payment_method=payment.payment_method,
            status=payment.status,
            paid_at=payment.updated_at,
        )
