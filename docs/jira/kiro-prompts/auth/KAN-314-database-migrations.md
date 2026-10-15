# Kiro Prompt — KAN-314: Database Migrations — Initial Schema

**Ticket:** KAN-314  
**Epic:** KAN-251 — Backend — Core API  
**Priority:** Highest  
**Skill:** Use the `database-migration` skill  
**File:** `backend/alembic/versions/001_initial_schema.py`

---

## Task

Create the initial Alembic migration that sets up the complete database schema for the ChitSmart platform. This is the first migration and must be run before any API endpoints are started.

## Tables to create (in this order — respect foreign keys)

### Active tables (with API endpoints)
1. `members` — customer accounts
2. `admin_users` — admin dashboard users
3. `otp_sessions` — OTP storage for auth
4. `member_sessions` — JWT refresh tokens
5. `chit_groups` — chit fund groups
6. `chit_memberships` — member ↔ group enrollments
7. `bank_accounts` — linked bank accounts
8. `nominees` — nominee details per membership
9. `installment_schedules` — monthly payment schedule per group
10. `payments` — all payment transactions
11. `ledger_entries` — double-entry accounting records
12. `policy_chunks` — AI RAG: approved policy document chunks + embeddings (requires pgvector)
13. `payouts` — payout records (read-only GET endpoint in this release)

### Deferred tables (schema created, NO API endpoints)
14. `kyc_documents` — deferred (KYC regulatory prerequisites not met)
15. `auctions` — deferred (real-time bidding infrastructure not ready)
16. `bids` — deferred with auctions

## Special requirements

### pgvector extension
```python
def upgrade() -> None:
    op.execute("CREATE EXTENSION IF NOT EXISTS vector;")
    # then create tables...
```

### Financial records constraint
Add a PostgreSQL check in the migration comment and in code review: `payments`, `ledger_entries`, and `payouts` must never have DELETE statements. The application enforces `status = VOIDED` instead.

### Index requirements
- `members.mobile_number` — unique index
- `otp_sessions.mobile_number` — index (for rate-limit queries)
- `chit_memberships.(member_id, chit_group_id)` — unique composite index
- `payments.member_id` — index
- `payments.chit_group_id` — index
- `ledger_entries.chit_group_id` — index
- `policy_chunks.embedding` — IVFFlat index for pgvector cosine search

### pgvector index
```python
op.execute("""
    CREATE INDEX ix_policy_chunks_embedding
    ON policy_chunks
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);
""")
```

## docker-compose.yml (also needed for this ticket)
Create `docker-compose.yml` at the repo root:
```yaml
version: "3.9"
services:
  db:
    image: pgvector/pgvector:pg16
    environment:
      POSTGRES_USER: chitsmart
      POSTGRES_PASSWORD: password
      POSTGRES_DB: chitsmart_dev
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U chitsmart"]
      interval: 5s
      retries: 5

volumes:
  pgdata:
```

## Acceptance criteria
- [ ] `alembic upgrade head` runs without errors on a fresh PostgreSQL instance
- [ ] `alembic downgrade -1` runs without errors (all tables dropped cleanly)
- [ ] pgvector extension is enabled
- [ ] All 16 tables exist after migration
- [ ] Deferred tables (kyc_documents, auctions, bids) exist but no API endpoints reference them
- [ ] All indexes created including the IVFFlat index on `policy_chunks.embedding`
- [ ] `docker-compose up` starts PostgreSQL and migration runs successfully against it
