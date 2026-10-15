# ChitSmart — GenAI Chit Fund Management Platform

A modern, AI-powered chit fund management platform for India.  
Built with Kotlin/Compose (Android), FastAPI/PostgreSQL (backend), and React/TypeScript (admin dashboard).

## Repository structure

```
Chit-Fund/
├── app/                    Android customer application (Kotlin, Jetpack Compose)
├── backend/                REST API (Python, FastAPI, PostgreSQL, Alembic)
├── admin/                  Admin dashboard (React, TypeScript, Vite)
├── docs/
│   ├── design/             Google Stitch prompt, Figma handoff, design traceability
│   ├── jira/               Epics, tickets, Kiro prompts, requirements catalog
│   └── scope-decisions.md  Canonical scope: what is in/deferred/removed
└── .kiro/
    ├── steering/           Always-on rules for every Kiro session
    └── specs/              Per-feature specifications (requirements, design, tasks)
```

## Current release scope

- Mobile OTP login and basic registration
- Chit group browsing, enrollment, membership management
- Installment payment flow (UPI, Net Banking, Debit Card, NEFT)
- Payment history, statements, digital receipts
- Payout history (read-only)
- Ledger, reconciliation, refunds
- Financial reports and exports
- GenAI assistant (multilingual, policy-aware)
- Admin dashboard: members, chit groups, installments, payments, AI tools

**Deferred:** KYC, bidding/auctions, payout approval workflow.  
See `docs/scope-decisions.md` for full rationale.

## Tech stack

| Layer | Technology |
|---|---|
| Android | Kotlin, Jetpack Compose, Material 3, Hilt, Retrofit, Room |
| Backend | Python 3.12, FastAPI, PostgreSQL 16, Alembic, SQLAlchemy |
| Admin | React 18, TypeScript, Vite, TanStack Query, Tailwind CSS |
| GenAI | OpenAI API, LangChain (RAG), pgvector |

## Figma design file

URL: https://www.figma.com/design/H3sDKKBcj9dveyJdgs7Jn5/Untitled  
File key: `H3sDKKBcj9dveyJdgs7Jn5`

## Getting started

See individual README files in `app/`, `backend/`, and `admin/` once scaffolding is complete.
