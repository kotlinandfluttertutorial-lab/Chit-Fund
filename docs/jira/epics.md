# ChitSmart — Jira Epics

All epics reflect the current release scope.
Deferred epics (KYC, Auctions/Bidding) are listed but marked DEFERRED — do not start.

## Active Epics

| Epic ID | Name | Description | Priority |
|---|---|---|---|
| KAN-241 | Authentication & Registration | Mobile OTP login, user registration (name/mobile/email), session management | P0 |
| KAN-239 | Chit Group Management (Customer) | Browse groups, view details, enroll, view memberships | P0 |
| KAN-240 | Installment Payments | Payment flow (UPI/NB/Card/NEFT), history, statements, receipts | P0 |
| KAN-237 | GenAI Assistant | Multilingual chat, policy RAG, policy reference viewer, human escalation | P1 |
| KAN-236 | Customer Profile & Settings | Personal info, bank accounts, nominees, security, preferences, help | P1 |
| KAN-238 | Notifications | Push, SMS, in-app notification centre | P2 |
| KAN-242 | Admin — Member Management | Member search, details, create/edit, suspend | P0 |
| KAN-243 | Admin — Chit Group Management | Group list, create/edit, detail view | P0 |
| KAN-244 | Admin — Installment Management | Schedule, collection view, reminders | P0 |
| KAN-245 | Admin — Payment Operations | Transactions, reconciliation, refunds, disputes | P0 |
| KAN-246 | Admin — Ledger & Statements | Double-entry ledger, member account statements | P1 |
| KAN-247 | Admin — Payout Records | Read-only payout history, certificates | P1 |
| KAN-248 | Admin — Reports & Exports | Collections, member activity, payouts, AI usage reports | P1 |
| KAN-249 | Admin — AI Tools | Collection summary, natural-language reporting | P2 |
| KAN-250 | Admin — Settings & RBAC | 4 roles, permission matrix, admin user management | P1 |
| KAN-251 | Backend — Core API | FastAPI setup, auth, shared models, Alembic migrations | P0 |
| KAN-252 | Infrastructure | PostgreSQL setup, CI/CD, environment configs, Docker | P0 |
| KAN-317 | Observability | Structured logging, health/readiness/metrics endpoints, AI cost tracking, audit trail, alerts, crash reporting | P1 |

## Deferred Epics (do not start in this release)

| Epic ID | Name | Deferral Reason |
|---|---|---|
| CS-EP-D01 | KYC — Customer | Regulatory prerequisites not met (PMLA, RBI KYC) |
| CS-EP-D02 | KYC — Admin Review | Deferred with CS-EP-D01 |
| CS-EP-D03 | Auctions — Customer Bidding | Real-time infrastructure not ready |
| CS-EP-D04 | Auctions — Admin Management | Deferred with CS-EP-D03 |
| CS-EP-D05 | Payout Approval Workflow | Deferred with auctions |

Reference: `docs/scope-decisions.md`
