# Kiro Prompt — KAN-324: Audit Log Atomicity

**Ticket:** KAN-324  
**Epic:** KAN-317 — Observability  
**Priority:** Highest  
**Skill:** Use the `database-migration` skill and `fastapi-endpoint` skill  
**Spec:** `.kiro/specs/observability/requirements.md` — REQ-OBS-014  
**Steering:** `.kiro/steering/financial-integrity.md`  
**Files:**
- `backend/migrations/versions/003_add_audit_log.py` (new Alembic migration)
- `backend/app/models/audit_log.py` (new model)
- `backend/app/core/audit.py` (new helper)
- `backend/app/services/admin_service.py` (instrument every admin mutation)

---

## Objective

Create the `audit_log` table and a helper that writes an audit row in the **same
database transaction** as every admin action that creates, modifies, or changes the
status of a financial record. If the audit write fails, the business operation must
roll back. The table must be immutable — no DELETE or UPDATE endpoint may ever touch it.

## Repository inspection

Before writing any code:
1. Read `backend/app/models/` — understand base models and existing table patterns.
2. Read `backend/migrations/versions/001_initial_schema.py` — migration conventions.
3. Read `backend/app/services/admin_service.py` (or equivalent) — identify every
   function that mutates member data, payment data, reconciliation records, or group data.
4. Read `backend/app/api/v1/endpoints/` — confirm there is no existing DELETE or UPDATE
   route that touches `audit_log`. If one exists, remove it.

## Model — `backend/app/models/audit_log.py`

```python
import json
from sqlalchemy import Column, String, Text, ForeignKey
from sqlalchemy.dialects.postgresql import UUID, JSONB
from app.models.base import Base, UUIDMixin, TimestampMixin

class AuditLog(Base, UUIDMixin, TimestampMixin):
    __tablename__ = "audit_log"

    admin_id = Column(UUID(as_uuid=True), ForeignKey("admin_users.id"), nullable=False)
    action = Column(String(128), nullable=False)       # e.g. "suspend_member"
    entity_type = Column(String(64), nullable=False)   # e.g. "member", "payment"
    entity_id = Column(String(64), nullable=False)     # UUID or other identifier
    before_state = Column(JSONB, nullable=True)        # snapshot before change
    after_state = Column(JSONB, nullable=True)         # snapshot after change
    ip_address = Column(String(45), nullable=True)     # IPv4 or IPv6
    correlation_id = Column(String(64), nullable=True)

    # Enforce immutability at DB level: no UPDATE or DELETE
    # Add a PostgreSQL trigger in the migration if your DB supports it,
    # or enforce via application-level access control.
```

## Audit helper — `backend/app/core/audit.py`

```python
import logging
from sqlalchemy.ext.asyncio import AsyncSession
from app.models.audit_log import AuditLog

logger = logging.getLogger("app.audit")

async def write_audit_log(
    session: AsyncSession,
    *,
    admin_id: str,
    action: str,
    entity_type: str,
    entity_id: str,
    before_state: dict | None = None,
    after_state: dict | None = None,
    ip_address: str | None = None,
    correlation_id: str | None = None,
) -> None:
    """
    Write an audit log entry using the EXISTING session/transaction.
    The caller must commit the session — this function does NOT commit.
    If this write raises, the caller's transaction will roll back, which
    is the intended behaviour (audit failure = business operation failure).
    """
    # Scrub secrets from snapshots before storing
    before_clean = _scrub_snapshot(before_state)
    after_clean = _scrub_snapshot(after_state)

    entry = AuditLog(
        admin_id=admin_id,
        action=action,
        entity_type=entity_type,
        entity_id=str(entity_id),
        before_state=before_clean,
        after_state=after_clean,
        ip_address=ip_address,
        correlation_id=correlation_id,
    )
    session.add(entry)
    # Do NOT call await session.commit() here — the caller controls the transaction.

    logger.info("admin_action", extra={
        "event": "admin_action",
        "admin_id": str(admin_id),
        "action": action,
        "entity_type": entity_type,
        "entity_id": str(entity_id),
        "correlation_id": correlation_id,
    })


_SCRUB_KEYS = {"token", "access_token", "refresh_token", "password",
               "otp", "secret", "account_number", "pan", "aadhaar"}

def _scrub_snapshot(snapshot: dict | None) -> dict | None:
    if snapshot is None:
        return None
    return {k: "***REDACTED***" if k in _SCRUB_KEYS else v
            for k, v in snapshot.items()}
```

## Usage pattern in admin services

Every admin mutation must follow this pattern:

```python
async def suspend_member(
    session: AsyncSession,
    member_id: str,
    admin_user,
    request: Request,
) -> Member:
    # 1. Load current state (for before_state snapshot)
    member = await session.get(Member, member_id)
    before = {"status": member.account_status, "name": member.full_name}

    # 2. Apply the business change
    member.account_status = "SUSPENDED"
    session.add(member)

    # 3. Write audit log IN THE SAME session (same transaction)
    await write_audit_log(
        session,
        admin_id=str(admin_user.id),
        action="suspend_member",
        entity_type="member",
        entity_id=member_id,
        before_state=before,
        after_state={"status": "SUSPENDED"},
        ip_address=request.client.host,
        correlation_id=getattr(request.state, "correlation_id", None),
    )

    # 4. Commit — both the business change and audit log commit together
    await session.commit()
    return member
```

Apply this pattern to all admin mutations:
- Member suspend / reactivate
- Member profile edit
- Payment reconciliation approval
- Dispute approve / reject
- Chit group publish / archive
- Installment due date edit
- Admin user deactivate / role change

## Migration — `backend/migrations/versions/003_add_audit_log.py`

Create the `audit_log` table. Add indexes on:
- `(admin_id, created_at)` for per-admin queries
- `(entity_type, entity_id)` for per-entity history
- `(created_at,)` for time-range queries

Optional: Add a PostgreSQL rule to block `DELETE` at the DB level:
```sql
CREATE RULE no_delete_audit_log AS ON DELETE TO audit_log DO INSTEAD NOTHING;
```

## Acceptance criteria

- [ ] `alembic upgrade head` creates `audit_log` table with all columns and indexes
- [ ] After a member suspension, one row exists in `audit_log` with correct fields
- [ ] `before_state` contains the status before the change; `after_state` after
- [ ] No `access_token`, `otp`, `pan`, or `account_number` values appear in `before_state` or `after_state`
- [ ] If `write_audit_log` raises, the business operation is rolled back (no orphaned record without audit entry)
- [ ] No `DELETE /audit-log` or `UPDATE /audit-log` endpoint exists in the API
- [ ] Admin audit log entries are visible in the Admin → Audit Log screen (read-only)
- [ ] Unit tests: successful audit write, transaction rollback on audit failure, snapshot scrubbing

## Definition of done

- `alembic upgrade head` and `alembic downgrade -1` succeed
- `pytest tests/test_audit_log.py -v` passes, including the atomicity test
- `ruff check` and `mypy` pass on new files
