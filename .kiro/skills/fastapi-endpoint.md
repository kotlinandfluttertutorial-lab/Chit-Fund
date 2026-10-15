---
name: fastapi-endpoint
description: Step-by-step instructions for implementing a FastAPI endpoint in the ChitSmart backend — router, schema, service, repository, and test.
---

# Skill: Implement a ChitSmart FastAPI Endpoint

Use this skill whenever you are adding or updating a backend API endpoint.

## Stack

- **Framework:** FastAPI (async)
- **ORM:** SQLAlchemy 2.0 (async, `AsyncSession`)
- **Migrations:** Alembic
- **Auth:** JWT Bearer via `python-jose`, dependency `get_current_member` or `get_current_admin`
- **Validation:** Pydantic v2
- **DB:** PostgreSQL 16 via `asyncpg`

---

## File structure

```
app/
├── api/v1/endpoints/<domain>.py   — FastAPI router, request/response wiring
├── schemas/<domain>.py            — Pydantic request + response models
├── services/<domain>_service.py   — Business logic
├── repositories/<domain>_repo.py  — DB queries (SQLAlchemy)
└── models/<domain>.py             — SQLAlchemy ORM model (already defined for core domains)
```

---

## Step 1 — Pydantic schemas

```python
# app/schemas/payment.py
from pydantic import BaseModel, UUID4
from decimal import Decimal
from datetime import datetime
from app.models.payment import PaymentMethod, PaymentStatus


class PaymentCreateRequest(BaseModel):
    installment_schedule_id: UUID4
    payment_method: PaymentMethod
    upi_vpa: str | None = None


class PaymentResponse(BaseModel):
    id: UUID4
    installment_schedule_id: UUID4
    amount: Decimal
    late_fee: Decimal
    total_amount: Decimal
    payment_method: PaymentMethod
    status: PaymentStatus
    gateway_reference: str | None
    receipt_number: str | None
    created_at: datetime

    model_config = {"from_attributes": True}
```

---

## Step 2 — Repository

```python
# app/repositories/payment_repo.py
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from app.models.payment import Payment
import uuid


class PaymentRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def create(self, payment: Payment) -> Payment:
        self.db.add(payment)
        await self.db.commit()
        await self.db.refresh(payment)
        return payment

    async def get_by_id(self, payment_id: uuid.UUID) -> Payment | None:
        result = await self.db.execute(
            select(Payment).where(Payment.id == payment_id)
        )
        return result.scalar_one_or_none()

    async def list_by_member(
        self, member_id: uuid.UUID, page: int = 1, page_size: int = 20
    ) -> list[Payment]:
        result = await self.db.execute(
            select(Payment)
            .where(Payment.member_id == member_id)
            .order_by(Payment.created_at.desc())
            .offset((page - 1) * page_size)
            .limit(page_size)
        )
        return list(result.scalars().all())
```

---

## Step 3 — Service

Business logic lives here, not in the router or repo.

```python
# app/services/payment_service.py
from app.repositories.payment_repo import PaymentRepository
from app.repositories.installment_repo import InstallmentRepository
from app.schemas.payment import PaymentCreateRequest, PaymentResponse
from app.models.payment import Payment, PaymentStatus
from app.core.exceptions import NotFoundError, ValidationError
import uuid


class PaymentService:
    def __init__(self, payment_repo: PaymentRepository, installment_repo: InstallmentRepository):
        self.payment_repo = payment_repo
        self.installment_repo = installment_repo

    async def create_payment_intent(
        self,
        member_id: uuid.UUID,
        request: PaymentCreateRequest,
    ) -> PaymentResponse:
        schedule = await self.installment_repo.get_by_id(request.installment_schedule_id)
        if not schedule:
            raise NotFoundError("Installment schedule not found")

        payment = Payment(
            member_id=member_id,
            chit_group_id=schedule.chit_group_id,
            installment_schedule_id=schedule.id,
            amount=schedule.amount_per_member,
            late_fee=0,      # calculate late fee logic here
            total_amount=schedule.amount_per_member,
            payment_method=request.payment_method,
            status=PaymentStatus.PENDING,
            upi_vpa=request.upi_vpa,
        )
        saved = await self.payment_repo.create(payment)
        return PaymentResponse.model_validate(saved)
```

---

## Step 4 — Router

```python
# app/api/v1/endpoints/payments.py
from fastapi import APIRouter, Depends, status
from sqlalchemy.ext.asyncio import AsyncSession
from app.core.deps import get_db, get_current_member
from app.schemas.payment import PaymentCreateRequest, PaymentResponse
from app.services.payment_service import PaymentService
from app.repositories.payment_repo import PaymentRepository
from app.repositories.installment_repo import InstallmentRepository
from app.models.member import Member

router = APIRouter()


def get_payment_service(db: AsyncSession = Depends(get_db)) -> PaymentService:
    return PaymentService(
        payment_repo=PaymentRepository(db),
        installment_repo=InstallmentRepository(db),
    )


@router.post("", response_model=PaymentResponse, status_code=status.HTTP_201_CREATED)
async def create_payment(
    request: PaymentCreateRequest,
    current_member: Member = Depends(get_current_member),
    service: PaymentService = Depends(get_payment_service),
):
    """Create a payment intent for an installment."""
    return await service.create_payment_intent(current_member.id, request)


@router.get("", response_model=list[PaymentResponse])
async def list_payments(
    page: int = 1,
    page_size: int = 20,
    current_member: Member = Depends(get_current_member),
    service: PaymentService = Depends(get_payment_service),
):
    """Get payment history for the authenticated member."""
    return await service.list_payments(current_member.id, page, page_size)
```

---

## Auth dependency pattern

```python
# app/core/deps.py
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from app.core.security import decode_access_token

security = HTTPBearer()

async def get_current_member(
    credentials: HTTPAuthorizationCredentials = Depends(security),
    db: AsyncSession = Depends(get_db),
) -> Member:
    token = credentials.credentials
    payload = decode_access_token(token)  # raises 401 if invalid
    member = await member_repo.get_by_id(db, payload["sub"])
    if not member or member.account_status != AccountStatus.ACTIVE:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED)
    return member
```

---

## Security rules

- Every endpoint **must** use `get_current_member` or `get_current_admin` dependency
- Admin endpoints **must** also check `get_current_admin` role against RBAC
- Financial records are **never** hard-deleted — use `status = VOIDED` + `void_reason`
- Never log or return sensitive fields (mobile number in full, account numbers)
- Always use parameterised queries (SQLAlchemy handles this automatically)

---

## Scope guards

- **Do NOT** create endpoints for: `/kyc/*`, `/auctions/*`, `/bids/*`, `/admin/kyc/*`, `/admin/auctions/*`, or payout approval (`POST /payouts/*`)
- **Payouts** endpoint is read-only `GET` only in this release
- See `docs/scope-decisions.md` and `app/api/v1/router.py` comments

---

## Test pattern

```python
# tests/api/test_payments.py
import pytest
from httpx import AsyncClient
from app.main import app

@pytest.mark.asyncio
async def test_create_payment_requires_auth():
    async with AsyncClient(app=app, base_url="http://test") as client:
        response = await client.post("/api/v1/payments", json={})
    assert response.status_code == 403  # no bearer token

@pytest.mark.asyncio
async def test_create_payment_success(auth_headers, installment_schedule_factory):
    schedule = await installment_schedule_factory()
    async with AsyncClient(app=app, base_url="http://test") as client:
        response = await client.post(
            "/api/v1/payments",
            json={"installment_schedule_id": str(schedule.id), "payment_method": "UPI"},
            headers=auth_headers,
        )
    assert response.status_code == 201
    assert response.json()["status"] == "PENDING"
```
