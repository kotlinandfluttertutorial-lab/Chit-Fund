# ChitSmart — System Architecture Overview

## Components

```
┌─────────────────────────────────────────────────────────────────┐
│                        USERS                                     │
│   Customer (Android app)          Operations staff (Web admin)   │
└────────────┬───────────────────────────────┬────────────────────┘
             │ HTTPS                          │ HTTPS
             ▼                                ▼
┌─────────────────────┐           ┌─────────────────────┐
│  Android App        │           │  Admin Dashboard     │
│  Kotlin/Compose     │           │  React/TypeScript    │
│  Material Design 3  │           │  Vite/Tailwind       │
│  27 screens         │           │  24 pages            │
└────────────┬────────┘           └──────────┬──────────┘
             │                               │
             └──────────────┬────────────────┘
                            │ REST + SSE
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    FastAPI Backend                                │
│                                                                   │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────────┐ │
│  │  Auth API    │  │ Business API │  │  AI Assistant API      │ │
│  │ OTP, JWT     │  │ Members      │  │  POST /ai/chat (SSE)   │ │
│  │ Sessions     │  │ Chit Groups  │  │  RAG + tool calls      │ │
│  └──────────────┘  │ Payments     │  └────────────────────────┘ │
│                    │ Ledger       │           │                   │
│                    └──────────────┘           │                   │
└───────────┬──────────────────────────────────┼───────────────────┘
            │ asyncpg                           │ HTTPS
            ▼                                   ▼
┌─────────────────────┐           ┌─────────────────────┐
│  PostgreSQL 16      │           │  OpenAI API          │
│  + pgvector         │           │  gpt-4o              │
│  16 tables          │           │  text-embedding-     │
│  Financial records  │           │  3-small             │
│  Policy chunks      │           └─────────────────────┘
└─────────────────────┘

External integrations:
  ┌──────────────┐  ┌──────────────┐
  │  Razorpay    │  │  SMS Provider│
  │  Payment GW  │  │  Twilio/MSG91│
  └──────────────┘  └──────────────┘
```

## Data flow — member pays an installment

```
1. Android: Member taps "Pay Now" → PaymentSummaryScreen
2. Android: Selects UPI method → POST /api/v1/payments
3. Backend: Validates installment, creates Payment record (PENDING)
4. Backend: Creates Razorpay order → returns order_id to app
5. Android: Opens Razorpay SDK → user authenticates + authorises
6. Razorpay: Sends webhook to POST /api/v1/payments/webhook
7. Backend: Verifies HMAC-SHA256 signature
8. Backend: Updates Payment.status = SUCCESSFUL
9. Backend: Appends LedgerEntry (credit) — immutable
10. Backend: Sends push notification to member
11. Android: Shows PaymentSuccessScreen
```

## Data flow — AI assistant query

```
1. Android: Member types "What is my outstanding balance?"
2. Android: POST /api/v1/ai/chat {message, history}
3. Backend: Embeds message (text-embedding-3-small, 1536 dims)
4. Backend: pgvector cosine search → top 5 policy chunks
5. Backend: Builds system prompt (policy context + member ID + guardrails)
6. Backend: Calls gpt-4o with ACCOUNT_TOOLS + streaming=true
7. OpenAI: Returns tool call: get_outstanding_balance({})
8. Backend: Executes read-only DB query for this member only
9. Backend: Returns tool result to OpenAI
10. OpenAI: Streams response tokens
11. Backend: Forwards tokens as Server-Sent Events
12. Android: Renders text incrementally in chat bubble
```

## Security boundaries

| Boundary | Mechanism |
|---|---|
| Android → Backend | JWT Bearer token (HS256, 24h expiry) |
| Refresh token | Random URL-safe 64-byte token, SHA-256 hashed in DB |
| OTP | 6-digit, SHA-256 hashed in DB, 10-minute expiry, rate-limited 3/hour |
| Payment webhook | HMAC-SHA256 signature verification before any processing |
| AI tools | Read-only DB queries only — no write tool exists |
| Admin → Backend | Separate JWT with role claim (Super Admin/Finance Manager/Support Agent/Auditor) |

## Database design

16 tables in PostgreSQL 16 with pgvector extension:

**Active tables (have API endpoints):**
- `members` — customer accounts
- `admin_users` — operations staff
- `otp_sessions` — one-time passwords (hashed)
- `member_sessions` — JWT refresh tokens (hashed)
- `chit_groups` — chit fund groups
- `chit_memberships` — member ↔ group enrollments
- `bank_accounts` — linked bank accounts (account numbers encrypted)
- `nominees` — nominee details per membership
- `installment_schedules` — monthly payment schedule per group
- `payments` — all payment transactions (append-only)
- `ledger_entries` — double-entry financial records (append-only)
- `policy_chunks` — AI RAG policy document segments + embeddings (vector(1536))
- `payouts` — payout records (read-only GET, approval workflow deferred)

**Deferred tables (schema exists, zero API endpoints):**
- `kyc_documents` — KYC deferred (regulatory)
- `auctions` — Auctions deferred (infrastructure)
- `bids` — Deferred with auctions

## Technology decisions

| Decision | Choice | Rationale |
|---|---|---|
| Android UI | Jetpack Compose + Material 3 | Declarative UI, first-party Google support, composable architecture |
| Admin UI | React + TypeScript + Vite | Fastest dev experience, TanStack ecosystem, Recharts for charts |
| Backend | FastAPI + async | Python ecosystem for AI/ML, async for I/O throughput, OpenAPI built-in |
| Database | PostgreSQL 16 + pgvector | ACID compliance for financial data, native vector search for RAG |
| ORM | SQLAlchemy 2.0 async | Mature Python ORM, full async support, Alembic migrations |
| Auth | JWT HS256 + OTP | Stateless, mobile-friendly, no password to forget |
| AI | OpenAI API + LangChain | Best-in-class models, manageable cost, serverless |
| Payment | Razorpay | India-first gateway, UPI native support, good webhook support |
| Containerisation | Docker + Docker Compose | Reproducible dev environment, PostgreSQL with pgvector |

## What is NOT in this architecture

By explicit scope decision (`docs/scope-decisions.md`):

- **No WebSocket server** — auctions require real-time bidding (deferred)
- **No UIDAI/DigiLocker integration** — KYC requires regulatory approval (deferred)
- **No CKYC registry** — with KYC
- **No Redis** — not needed at current scale; add if rate limiting or caching requires it
- **No microservices** — modular monolith appropriate for current scale
- **No message queue** — synchronous webhooks sufficient; add if webhook reliability becomes an issue
