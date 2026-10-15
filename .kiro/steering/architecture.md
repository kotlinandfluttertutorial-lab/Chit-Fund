---
inclusion: always
---

# ChitSmart — Technology Stack and Architecture

**Last updated:** 15/10/2026

## Technology decisions (fixed — do not substitute)

| Layer | Technology |
|---|---|
| Android app | Kotlin, Jetpack Compose, Material Design 3 |
| Admin dashboard | React, TypeScript, Vite |
| Backend API | Python, FastAPI |
| Database | PostgreSQL |
| Database migrations | Alembic |
| GenAI | OpenAI API — approved-document RAG + authorised read-only account tools |

## Database safety rules

- Never drop tables or columns without an explicit migration plan agreed with the user.
- Schema for deferred features (KYC, auctions, bids) must be created in Alembic migrations but left empty and unexposed by the API. This allows future releases to add data without schema surgery.
- `kyc_status` column on `members` table: include in schema, default `NOT_REQUIRED`, not exposed via customer API in this release.
- `kyc_documents` table: create schema in migration, no API endpoints in this release.
- `auctions` table: create schema in migration, no API endpoints in this release.
- `bids` table: create schema in migration, no API endpoints in this release.
- `payouts` table: include with read-only API endpoint in this release. Approval workflow endpoints are deferred.
- Financial records (ledger entries, transactions, payouts) must never be deleted by application code. Mark records as voided/reversed with a reason; never hard-delete.

## Backend API rules

- All endpoints require authentication (JWT Bearer).
- All mutation endpoints require RBAC role check.
- Do not expose KYC endpoints in this release.
- Do not expose auction or bid endpoints in this release.
- Payout endpoint is read-only (GET) in this release.

## GenAI rules

- The GenAI assistant may only read from approved policy documents and the authenticated user's own account data (via authorised read-only tools).
- The assistant must never write data, initiate transactions, or approve workflows.
- All policy document citations must include document name and section reference.
- The assistant must support at minimum: English, Hindi. Additional Indian languages are a plus.
