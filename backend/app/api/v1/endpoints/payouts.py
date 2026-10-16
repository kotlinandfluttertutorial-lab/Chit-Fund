"""
Payouts endpoint — CHIT-39 (read-only in this release).

GET /payouts    Paginated list of payouts for the authenticated member.

Approval workflow (POST /payouts, PATCH /payouts/{id}/approve) is deferred
with the auction infrastructure. See docs/scope-decisions.md.
"""
from __future__ import annotations

from fastapi import APIRouter, Depends
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.core.deps import get_current_member
from app.models.deferred_schema import Payout
from app.models.member import Member
from app.schemas.payment import PayoutListResponse, PayoutResponse

router = APIRouter()


@router.get("", response_model=PayoutListResponse)
async def list_payouts(
    page: int = 1,
    size: int = 20,
    db: AsyncSession = Depends(get_db),
    current_member: Member = Depends(get_current_member),
) -> PayoutListResponse:
    """
    List payouts for the authenticated member (read-only).
    Approval workflow is deferred to a future release.
    """
    size = min(size, 100)

    stmt = (
        select(Payout)
        .where(Payout.member_id == current_member.id)
        .order_by(Payout.created_at.desc())
    )

    count_stmt = select(func.count()).select_from(stmt.subquery())
    total = (await db.execute(count_stmt)).scalar_one()

    stmt = stmt.offset((page - 1) * size).limit(size)
    payouts = list((await db.execute(stmt)).scalars().all())

    return PayoutListResponse(
        items=[PayoutResponse.model_validate(p) for p in payouts],
        total=total,
        page=page,
        size=size,
    )
