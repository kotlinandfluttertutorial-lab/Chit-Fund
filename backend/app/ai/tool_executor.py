"""
Tool executor — dispatches AI assistant tool calls to read-only DB queries.

CRITICAL CONSTRAINT: This module NEVER writes, updates, or deletes data.
All functions are read-only SELECT queries.

Enforced constraints:
- Every query is scoped to the authenticated member_id.
- KYC fields are never returned.
- Auction and bid data are never returned.
- No INSERT, UPDATE, or DELETE statements anywhere in this module.
"""

import json
import logging
import uuid
from datetime import datetime, timezone
from typing import Any

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.chit_group import ChitGroup, ChitMembership, MembershipStatus
from app.models.payment import InstallmentSchedule, Payment, PaymentStatus

logger = logging.getLogger(__name__)

# ── Dispatcher ─────────────────────────────────────────────────────────────────


async def execute_tool(
    tool_name: str,
    arguments: dict[str, Any],
    member_id: str,
    db: AsyncSession,
) -> str:
    """
    Dispatch an AI tool call to the appropriate read-only DB handler.

    Args:
        tool_name:  Name of the function tool (must match a key in ACCOUNT_TOOLS).
        arguments:  Parsed JSON arguments from the OpenAI tool call.
        member_id:  UUID string of the authenticated member. All queries are
                    scoped to this member — never crosses member boundaries.
        db:         Async SQLAlchemy session.

    Returns:
        JSON string to pass back to the OpenAI messages as the tool result.

    Raises:
        ValueError: If tool_name is not a recognised tool.
    """
    member_uuid = uuid.UUID(member_id)

    handlers = {
        "get_outstanding_balance": _get_outstanding_balance,
        "get_next_installment_due": _get_next_installment_due,
        "get_active_memberships": _get_active_memberships,
        "get_payment_history": _get_payment_history,
    }

    handler = handlers.get(tool_name)
    if handler is None:
        logger.warning("Unknown tool requested: %r", tool_name)
        raise ValueError(f"Unknown tool: {tool_name!r}")

    logger.info("Executing tool %r for member %s", tool_name, member_id)
    result = await handler(member_uuid, arguments, db)
    return json.dumps(result, default=str)


# ── Read-only handlers ─────────────────────────────────────────────────────────


async def _get_outstanding_balance(
    member_id: uuid.UUID,
    _arguments: dict,
    db: AsyncSession,
) -> dict:
    """Return total pending payment amount for the member."""
    # Only PENDING and PROCESSING payments count as outstanding
    stmt = select(Payment).where(
        Payment.member_id == member_id,
        Payment.status.in_([PaymentStatus.PENDING, PaymentStatus.PROCESSING]),
    )
    result = await db.execute(stmt)
    payments = result.scalars().all()

    total = sum(float(p.total_amount) for p in payments)
    return {
        "outstanding_balance": total,
        "currency": "INR",
        "pending_payment_count": len(payments),
        "note": "Includes installments with PENDING or PROCESSING status.",
    }


async def _get_next_installment_due(
    member_id: uuid.UUID,
    _arguments: dict,
    db: AsyncSession,
) -> dict:
    """Return the nearest upcoming installment due date and amount."""
    now = datetime.now(timezone.utc)

    # Find active memberships
    mem_stmt = select(ChitMembership).where(
        ChitMembership.member_id == member_id,
        ChitMembership.status == MembershipStatus.ACTIVE,
    )
    mem_result = await db.execute(mem_stmt)
    memberships = mem_result.scalars().all()

    if not memberships:
        return {"message": "No active chit group memberships found."}

    group_ids = [m.chit_group_id for m in memberships]

    # Find next upcoming installment schedule
    sched_stmt = (
        select(InstallmentSchedule, ChitGroup)
        .join(ChitGroup, InstallmentSchedule.chit_group_id == ChitGroup.id)
        .where(
            InstallmentSchedule.chit_group_id.in_(group_ids),
            InstallmentSchedule.due_date >= now,
        )
        .order_by(InstallmentSchedule.due_date)
        .limit(1)
    )
    sched_result = await db.execute(sched_stmt)
    row = sched_result.first()

    if row is None:
        return {"message": "No upcoming installments found."}

    schedule, group = row
    return {
        "group_name": group.name,
        "due_date": schedule.due_date.isoformat(),
        "amount": float(schedule.amount_per_member),
        "currency": "INR",
        "month_number": schedule.month_number,
    }


async def _get_active_memberships(
    member_id: uuid.UUID,
    _arguments: dict,
    db: AsyncSession,
) -> dict:
    """List all active ChitMembership records for the member."""
    stmt = (
        select(ChitMembership, ChitGroup)
        .join(ChitGroup, ChitMembership.chit_group_id == ChitGroup.id)
        .where(
            ChitMembership.member_id == member_id,
            ChitMembership.status == MembershipStatus.ACTIVE,
        )
        .order_by(ChitGroup.name)
    )
    result = await db.execute(stmt)
    rows = result.all()

    memberships = [
        {
            "group_name": group.name,
            "chit_value": float(group.chit_value),
            "monthly_installment": float(group.monthly_installment),
            "duration_months": group.duration_months,
            "enrollment_date": membership.enrollment_date.isoformat(),
            "member_number": membership.member_number,
            "payout_received": membership.payout_received,
            "currency": "INR",
        }
        for membership, group in rows
    ]

    return {
        "active_memberships": memberships,
        "count": len(memberships),
    }


async def _get_payment_history(
    member_id: uuid.UUID,
    arguments: dict,
    db: AsyncSession,
) -> dict:
    """Return the last N payment transactions for the member."""
    limit = min(int(arguments.get("limit", 10)), 50)

    stmt = (
        select(Payment, ChitGroup)
        .join(ChitGroup, Payment.chit_group_id == ChitGroup.id)
        .where(Payment.member_id == member_id)
        .order_by(Payment.created_at.desc())
        .limit(limit)
    )
    result = await db.execute(stmt)
    rows = result.all()

    payments = [
        {
            "receipt_number": payment.receipt_number,
            "group_name": group.name,
            "amount": float(payment.amount),
            "late_fee": float(payment.late_fee),
            "total_amount": float(payment.total_amount),
            "payment_method": payment.payment_method.value,
            "status": payment.status.value,
            "transaction_date": payment.created_at.isoformat(),
            "currency": "INR",
        }
        for payment, group in rows
    ]

    return {
        "payment_history": payments,
        "count": len(payments),
        "limit": limit,
    }
