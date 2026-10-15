---
inclusion: always
---

# ChitSmart — Current Release Scope

**Last updated:** 15/10/2026  
**Source of truth:** `docs/scope-decisions.md`

## What is IN scope for this release

- Mobile OTP login and basic registration (name, mobile, email — no Aadhaar/PAN).
- Chit group browsing, enrollment, and membership management.
- Installment payment flow (UPI, Net Banking, Debit Card, NEFT/RTGS).
- Payment history, account statements, and digital receipts.
- Payout history as a read-only record (no approval workflow).
- Double-entry ledger and payment reconciliation.
- Refunds and disputes.
- Financial reports and exports (Collections, Member Activity, Payouts, AI Usage).
- GenAI assistant with multilingual support and policy references.
- Admin: member management, chit group management, installment tracking, payment operations.
- Admin: AI collection summary and natural-language reporting.
- Role-based access control (4 roles: Super Admin, Finance Manager, Support Agent, Auditor).

## What is DEFERRED — do not implement

### KYC (regulatory prerequisites not ready)
- KYC Document Upload screen (customer app 1.6).
- KYC Status screen (customer app 1.7).
- Aadhaar/PAN fields on registration.
- KYC status badge and Documents section in Profile.
- Admin KYC sidebar, KYC Pending Queue, KYC Review Workspace.
- AI Document Review tool.
- KYC Officer admin role.

### Bidding and Auctions (infrastructure not ready)
- Customer app Auctions tab and all auction screens (4.1–4.6 from original spec).
- Bid submission, live auction monitoring, bid history.
- Admin Auctions sidebar and all auction management screens.
- Payout approval workflow (payout records are read-only in this release).
- WebSocket infrastructure for real-time bidding.

## What is REMOVED entirely

### Gold Saver 50K
- Not a chit fund product. Removed from all UI, mock data, sample data, and copy.
- Do not use "Gold Saver 50K" as a group name anywhere. Use realistic chit fund group names (e.g. "Family Savings 50K", "Community Chit 1L").

## Navigation (customer app)

Bottom navigation: **Home | My Chits | Payments | Assistant | Profile**  
(The Auctions tab does not exist in this release.)

## Admin sidebar navigation

Dashboard | Members | Chit Groups | Installments | Payments | Ledger | Reports | AI Tools | Settings  
(KYC and Auctions sidebar items do not exist in this release.)


---

## Available skills

When implementing features, activate the relevant skill for step-by-step guidance:

| Task | Skill to use |
|---|---|
| Implementing a Jetpack Compose screen | `android-screen` |
| Implementing a FastAPI endpoint | `fastapi-endpoint` |
| Creating an Alembic migration | `database-migration` |
| Implementing a React admin dashboard page/component | `admin-dashboard-component` |
| Working on the AI assistant, RAG pipeline, or OpenAI tools | `ai-rag-assistant` |

Skills are in `.kiro/skills/`. To use one, reference it in your prompt: "Use the android-screen skill to implement the Login screen."
