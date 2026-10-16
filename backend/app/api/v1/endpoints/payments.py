"""
Payments endpoints — CHIT-39.

POST /payments              Create Razorpay order (requires auth)
POST /payments/webhook      Razorpay webhook (NO auth — server-to-server)
GET  /payments              List payments with pagination + filters (requires auth)
GET  /payments/{id}/receipt Digital receipt (requires auth)
"""
from __future__ import annotations

import uuid
from datetime import datetime
from typing import Optional

from fastapi import APIRouter, Depends, Header, HTTPException, Request, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.deps import get_current_member
from app.models.member import Member
from app.models.payment import PaymentStatus
from app.repositories.installment_repo import InstallmentRepository
from app.repositories.payment_repo import PaymentRepository
from app.schemas.payment import (
    PaymentCreateRequest,
    PaymentListResponse,
    PaymentResponse,
    ReceiptResponse,
)
from app.services.payment_service import PaymentService

router = APIRouter()


# ── Dependency factory ────────────────────────────────────────────────────────

def _get_payment_service(db: AsyncSession = Depends(get_db)) -> PaymentService:
    return PaymentService(
        payment_repo=PaymentRepository(db),
        installment_repo=InstallmentRepository(db),
    )


# ── Endpoints ─────────────────────────────────────────────────────────────────

@router.post("", response_model=PaymentResponse, status_code=status.HTTP_201_CREATED)
async def create_payment(
    request: PaymentCreateRequest,
    db: AsyncSession = Depends(get_db),
    current_member: Member = Depends(get_current_member),
    service: PaymentService = Depends(_get_payment_service),
) -> PaymentResponse:
    """
    Create a Razorpay payment order for an installment.
    Returns the payment record with status=PENDING and the gateway order_id.
    """
    return await service.create_payment_intent(db, current_member, request)


@router.post("/webhook", status_code=status.HTTP_200_OK)
async def razorpay_webhook(
    request: Request,
    db: AsyncSession = Depends(get_db),
    service: PaymentService = Depends(_get_payment_service),
    x_razorpay_signature: Optional[str] = Header(None),
) -> dict:
    """
    Razorpay payment.captured webhook.
    This endpoint is intentionally unauthenticated — it is called by Razorpay servers.
    Signature verification is performed inside the service layer.
    """
    payload_bytes = await request.body()

    if x_razorpay_signature is None:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Missing X-Razorpay-Signature header",
        )

    # Parse JSON to extract order/payment ids
    import json
    try:
        payload = json.loads(payload_bytes)
    except json.JSONDecodeError:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Invalid JSON payload",
        )

    event = payload.get("event", "")
    if event != "payment.captured":
        # Acknowledge other events without processing them
        return {"status": "ignored", "event": event}

    payment_entity = payload.get("payload", {}).get("payment", {}).get("entity", {})
    order_id = payment_entity.get("order_id", "")
    razorpay_payment_id = payment_entity.get("id", "")

    await service.process_webhook(
        db=db,
        order_id=order_id,
        razorpay_payment_id=razorpay_payment_id,
        signature=x_razorpay_signature,
        payload_bytes=payload_bytes,
    )

    return {"status": "ok"}


@router.get("", response_model=PaymentListResponse)
async def list_payments(
    page: int = 1,
    size: int = 20,
    group_id: Optional[uuid.UUID] = None,
    payment_status: Optional[PaymentStatus] = None,
    from_date: Optional[datetime] = None,
    to_date: Optional[datetime] = None,
    db: AsyncSession = Depends(get_db),
    current_member: Member = Depends(get_current_member),
) -> PaymentListResponse:
    """
    List the authenticated member's payments with optional filters.
    Pagination: page (1-based), size (max 100).
    """
    size = min(size, 100)
    repo = PaymentRepository(db)
    payments, total = await repo.list_by_member(
        member_id=current_member.id,
        group_id=group_id,
        status=payment_status,
        from_date=from_date,
        to_date=to_date,
        page=page,
        size=size,
    )
    return PaymentListResponse(
        items=[PaymentResponse.model_validate(p) for p in payments],
        total=total,
        page=page,
        size=size,
    )


@router.get("/{payment_id}/receipt", response_model=ReceiptResponse)
async def get_receipt(
    payment_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
    current_member: Member = Depends(get_current_member),
    service: PaymentService = Depends(_get_payment_service),
) -> ReceiptResponse:
    """Return the digital receipt for a completed payment owned by the current member."""
    return await service.get_receipt(db, payment_id, current_member)
