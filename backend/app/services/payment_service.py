"""
Payment service — business logic for payment creation, webhook processing,
receipt generation, and ledger double-entry.

Uses httpx to call the Razorpay REST API directly (no razorpay SDK).
Financial records are NEVER deleted — use status=VOIDED with a reason.
"""

from __future__ import annotations

import hashlib
import hmac
import uuid
from datetime import datetime, timezone
from decimal import Decimal

import httpx
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
        Create a Razorpay order and save a PENDING Payment record.
        Returns PaymentResponse with gateway_reference = Razorpay order_id.
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

        # Create Razorpay order via REST API
        order_id = await self._create_razorpay_order(
            amount_paise=int(total_amount * 100),
            receipt_hint=f"member_{member.id}",
        )

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

    async def _create_razorpay_order(
        self,
        amount_paise: int,
        receipt_hint: str,
    ) -> str:
        """
        POST https://api.razorpay.com/v1/orders via httpx with Basic auth.
        Returns the Razorpay order_id string.

        Falls back to a synthetic order_id when payment keys are not configured
        (development / test mode).
        """
        if not settings.PAYMENT_KEY_ID or not settings.PAYMENT_KEY_SECRET:
            # No payment keys configured — return a synthetic order id for dev
            return f"order_dev_{uuid.uuid4().hex[:16]}"

        async with httpx.AsyncClient(timeout=10.0) as client:
            response = await client.post(
                "https://api.razorpay.com/v1/orders",
                auth=(settings.PAYMENT_KEY_ID, settings.PAYMENT_KEY_SECRET),
                json={
                    "amount": amount_paise,
                    "currency": "INR",
                    "receipt": receipt_hint[:40],  # Razorpay receipt max 40 chars
                },
            )

        if response.status_code != 200:
            raise HTTPException(
                status_code=status.HTTP_502_BAD_GATEWAY,
                detail=f"Payment gateway error: {response.text}",
            )

        return response.json()["id"]

    # ── Webhook ───────────────────────────────────────────────────────────────

    def verify_webhook_signature(
        self,
        payload_bytes: bytes,
        signature: str,
    ) -> bool:
        """
        Verify Razorpay webhook signature.
        HMAC-SHA256: key = PAYMENT_KEY_SECRET, message = raw payload bytes.
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
        razorpay_payment_id: str,
        signature: str,
        payload_bytes: bytes,
    ) -> None:
        """
        Process a Razorpay payment.captured webhook event.
        1. Verify signature.
        2. Find payment by order_id (gateway_reference).
        3. Mark SUCCESSFUL, generate receipt_number.
        4. Write double-entry LedgerEntry.
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
        # Use payment id last 6 hex chars as sequence proxy (no sequence table yet)
        seq = str(payment.id).replace("-", "")[-6:].upper()
        receipt_number = f"CS-{now.strftime('%Y%m')}-{seq}"

        await self.payment_repo.update_status(
            payment.id,
            status=PaymentStatus.SUCCESSFUL,
            gateway_reference=razorpay_payment_id,
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
