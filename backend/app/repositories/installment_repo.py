"""
Installment repository — DB queries for installment schedules.
"""
from __future__ import annotations

import uuid
from datetime import datetime, timezone
from typing import List, Optional, Tuple

from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.payment import InstallmentSchedule


class InstallmentRepository:
    def __init__(self, db: AsyncSession) -> None:
        self.db = db

    async def get_by_id(self, schedule_id: uuid.UUID) -> Optional[InstallmentSchedule]:
        result = await self.db.execute(
            select(InstallmentSchedule).where(InstallmentSchedule.id == schedule_id)
        )
        return result.scalar_one_or_none()

    async def list_by_group(
        self,
        group_id: uuid.UUID,
        page: int = 1,
        size: int = 50,
    ) -> Tuple[List[InstallmentSchedule], int]:
        stmt = (
            select(InstallmentSchedule)
            .where(InstallmentSchedule.chit_group_id == group_id)
            .order_by(InstallmentSchedule.month_number)
        )
        count_stmt = select(func.count()).select_from(stmt.subquery())
        total = (await self.db.execute(count_stmt)).scalar_one()

        stmt = stmt.offset((page - 1) * size).limit(size)
        rows = (await self.db.execute(stmt)).scalars().all()
        return list(rows), total

    async def get_current_due(self, group_id: uuid.UUID) -> Optional[InstallmentSchedule]:
        """Return the most recent installment that is due on or before now."""
        now = datetime.now(timezone.utc)
        result = await self.db.execute(
            select(InstallmentSchedule)
            .where(InstallmentSchedule.chit_group_id == group_id)
            .where(InstallmentSchedule.due_date <= now)
            .order_by(InstallmentSchedule.due_date.desc())
            .limit(1)
        )
        return result.scalar_one_or_none()
