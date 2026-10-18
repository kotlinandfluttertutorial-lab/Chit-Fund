# ChitSmart — Jira Epics

All epics reflect the current release scope.
Deferred epics (KYC, Auctions/Bidding) are listed but marked DEFERRED — do not start.

## Active Epics

| Epic ID | Name | Description | Priority |
|---|---|---|---|
| CHIT-241 | Authentication & Registration | Mobile OTP login, user registration (name/mobile/email), session management | P0 |
| CHIT-239 | Chit Group Management (Customer) | Browse groups, view details, enroll, view memberships | P0 |
| CHIT-240 | Installment Payments | Payment flow (UPI/NB/Card/NEFT), history, statements, receipts | P0 |
| CHIT-237 | GenAI Assistant | Multilingual chat, policy RAG, policy reference viewer, human escalation | P1 |
| CHIT-236 | Customer Profile & Settings | Personal info, bank accounts, nominees, security, preferences, help | P1 |
| CHIT-238 | Notifications | Push, SMS, in-app notification centre | P2 |
| CHIT-242 | Admin — Member Management | Member search, details, create/edit, suspend | P0 |
| CHIT-243 | Admin — Chit Group Management | Group list, create/edit, detail view | P0 |
| CHIT-244 | Admin — Installment Management | Schedule, collection view, reminders | P0 |
| CHIT-245 | Admin — Payment Operations | Transactions, reconciliation, refunds, disputes | P0 |
| CHIT-246 | Admin — Ledger & Statements | Double-entry ledger, member account statements | P1 |
| CHIT-247 | Admin — Payout Records | Read-only payout history, certificates | P1 |
| CHIT-248 | Admin — Reports & Exports | Collections, member activity, payouts, AI usage reports | P1 |
| CHIT-249 | Admin — AI Tools | Collection summary, natural-language reporting | P2 |
| CHIT-250 | Admin — Settings & RBAC | 4 roles, permission matrix, admin user management | P1 |
| CHIT-251 | Backend — Core API | FastAPI setup, auth, shared models, Alembic migrations | P0 |
| CHIT-252 | Infrastructure | PostgreSQL setup, CI/CD, environment configs, Docker | P0 |
| CHIT-317 | Observability | Structured logging, health/readiness/metrics endpoints, AI cost tracking, audit trail, alerts, crash reporting | P1 |

## Deferred Epics (do not start in this release)

| Epic ID | Name | Deferral Reason |
|---|---|---|
| CS-EP-D01 | KYC — Customer | Regulatory prerequisites not met (PMLA, RBI KYC) |
| CS-EP-D02 | KYC — Admin Review | Deferred with CS-EP-D01 |
| CS-EP-D03 | Auctions — Customer Bidding | Real-time infrastructure not ready |
| CS-EP-D04 | Auctions — Admin Management | Deferred with CS-EP-D03 |
| CS-EP-D05 | Payout Approval Workflow | Deferred with auctions |

Reference: `docs/scope-decisions.md`
