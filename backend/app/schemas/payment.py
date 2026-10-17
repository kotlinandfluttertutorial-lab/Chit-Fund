"""
Payment Pydantic schemas — request/response models for CHIT-39.

Uses Pydantic v2 model_config = ConfigDict(from_attributes=True) for
ORM-to-schema mapping.
"""

from __future__ import annotations

import uuid
from datetime import datetime
from decimal import Decimal
from typing import List, Optional

from pydantic import BaseModel, ConfigDict

from app.models.deferred_schema import PayoutStatus
from app.models.payment import PaymentMethod, PaymentStatus

# ── Payment schemas ────────────────────────────────────────────────────────────


class PaymentCreateRequest(BaseModel):
    """Request body for POST /payments."""

    installment_schedule_id: uuid.UUID
    payment_method: PaymentMethod
    upi_vpa: Optional[str] = None  # required when payment_method == UPI


class PaymentResponse(BaseModel):
    """Single payment record returned to the client."""

    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    member_id: uuid.UUID
    chit_group_id: uuid.UUID
    installment_schedule_id: uuid.UUID
    amount: Decimal
    late_fee: Decimal
    total_amount: Decimal
    payment_method: PaymentMethod
    status: PaymentStatus
    gateway_reference: Optional[str] = None
    receipt_number: Optional[str] = None
    created_at: datetime


class PaymentListResponse(BaseModel):
    """Paginated list of payment records."""

    items: List[PaymentResponse]
    total: int
    page: int
    size: int


# ── Receipt schema ─────────────────────────────────────────────────────────────


class ReceiptResponse(BaseModel):
    """Digital receipt returned by GET /payments/{id}/receipt."""

    receipt_number: str
    payment_id: uuid.UUID
    member_name: str
    group_name: str
    month_number: int
    amount: Decimal
    late_fee: Decimal
    total_amount: Decimal
    payment_method: PaymentMethod
    status: PaymentStatus
    paid_at: Optional[datetime] = None


# ── Payout schemas ─────────────────────────────────────────────────────────────


class PayoutResponse(BaseModel):
    """Single payout record — read-only in this release."""

    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    member_id: uuid.UUID
    chit_group_id: uuid.UUID
    month_number: int
    gross_amount: Decimal
    foreman_commission: Decimal
    net_amount: Decimal
    status: PayoutStatus
    credit_date: Optional[datetime] = None
    bank_reference: Optional[str] = None
    created_at: datetime


class PayoutListResponse(BaseModel):
    """Paginated list of payout records."""

    items: List[PayoutResponse]
    total: int
    page: int
    size: int


# ── Webhook schema ─────────────────────────────────────────────────────────────


class WebhookPayload(BaseModel):
    """Payment gateway webhook payload (key fields only)."""

    order_id: str
    gateway_payment_id: str
    signature: str
