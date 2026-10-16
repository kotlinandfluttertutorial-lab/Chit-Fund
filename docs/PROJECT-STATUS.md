# ChitSmart — Project Status

**Last updated:** 15/10/2026  
**Jira project:** CHIT on androidassistant.atlassian.net

---

## Overall progress

| Layer | Total | Done | Remaining |
|---|---|---|---|
| Android screens | 27 | 27 | 0 |
| Admin dashboard pages | 24 | 24 | 0 |
| Backend endpoint modules | 15 | 6 full + 9 stubs | 9 stubs need real DB queries |
| Database migrations | 1 (complete) | 1 | 0 |
| Jira stories | 63 | 63 | 0 |

---

## Jira epics status

| Epic | Key | Stories Done | Status |
|---|---|---|---|
| Authentication & Registration | CHIT-1 | 8/8 | ✅ Done |
| Chit Group Management — Customer | CHIT-2 | 8/8 | ✅ Done |
| Installment Payments | CHIT-3 | 11/11 | ✅ Done |
| GenAI Assistant | CHIT-4 | 7/7 | ✅ Done |
| Customer Profile & Settings | CHIT-5 | 7/7 | ✅ Done |
| Notifications | CHIT-6 | 1/1 | ✅ Done |
| Admin — Member Management | CHIT-7 | 5/5 | ✅ Done |
| Admin — Chit Group Management | CHIT-8 | 3/3 | ✅ Done |
| Admin — Installment Management | CHIT-9 | 2/2 | ✅ Done |
| Admin — Payment Operations | CHIT-10 | 3/3 | ✅ Done |
| Admin — Ledger & Statements | CHIT-11 | 2/2 | ✅ Done |
| Admin — Payout Records | CHIT-12 | 1/1 | ✅ Done |
| Admin — Reports & Exports | CHIT-13 | 3/3 | ✅ Done |
| Admin — AI Tools | CHIT-14 | 2/2 | ✅ Done |
| Admin — Settings & RBAC | CHIT-15 | 2/2 | ✅ Done |
| Backend — Core API | CHIT-16 | 5/5 | ✅ Done (stubs need implementation) |
| Infrastructure | CHIT-17 | 2/2 | ✅ Done |

---

## Build verification

| Check | Command | Result |
|---|---|---|
| TypeScript | `cd admin && npx tsc --noEmit` | ✅ 0 errors |
| Vite build | `cd admin && npx vite build` | ✅ 2265 modules, clean |
| Python AST | `python -c "import ast; ast.parse(open('app/main.py').read())"` | ✅ Clean |
| Android | Requires Android Studio + fonts | ⬜ Not yet run |

---

## What is production-ready

- ✅ All UI screens implemented (Android + Admin)
- ✅ Database schema (all 16 tables, deferred schema for KYC/auctions/bids)
- ✅ Auth API (OTP send/verify, register, refresh, logout)
- ✅ Payments API (Razorpay create order, webhook, history, receipt)
- ✅ AI backend (RAG pipeline, pgvector, read-only tools, SSE streaming)
- ✅ Docker Compose for local dev
- ✅ Alembic migrations
- ✅ CI/CD pipeline definition (`.github/workflows/ci.yml`)
- ✅ Security: JWT auth, webhook signature verification, OTP hashing, append-only financial records
- ✅ Figma design file: `lLqFOeduJnNpINYH7fQth7`

## What needs work before production launch

| Item | Priority | Effort |
|---|---|---|
| Android font files (Inter TTF in res/font/) | P0 | 30 min |
| Real backend endpoint implementations (members, chit_groups, installments, ledger, reports — 9 stub routers) | P0 | ~2 weeks |
| Policy document ingestion into policy_chunks table | P0 | 2 hours |
| Production secrets (Razorpay live key, real OpenAI key, managed PostgreSQL) | P0 | 1 day |
| Automated test suite (backend pytest, admin Vitest, Android Compose tests) | P1 | 1 week |
| Load testing | P1 | 3 days |
| Legal/compliance review (Chit Funds Act, DPDPA, payment provider T&Cs) | P0 | External |
| KYC release (UIDAI, PAN, PMLA) | Future | 4–6 weeks |
| Auctions/bidding release | Future | 6–8 weeks |

---

## Deferred features (by design)

| Feature | Decision | Reference |
|---|---|---|
| KYC document upload | Deferred — regulatory prerequisites | CHIT-D01, docs/scope-decisions.md §Decision 2 |
| Admin KYC review | Deferred with KYC | CHIT-D02 |
| Auction scheduling | Deferred — infrastructure | CHIT-D03, docs/scope-decisions.md §Decision 3 |
| Auction bidding UI | Deferred with auctions | CHIT-D04 |
| Payout approval workflow | Deferred with auctions | CHIT-D05 |
| Gold Saver 50K product | Removed — wrong product category | docs/scope-decisions.md §Decision 1 |

Database schema for all deferred features exists in migration `001_initial_schema`. No schema change needed when these features are activated.

---

## Known non-blocking issues

From semantic review (admin dashboard final batch):

1. **Inline style props** — a few `style={{ color: accentColor }}` in Reports/ReportBuilder/Settings for dynamic colours that Tailwind cannot express. Acceptable for MVP; replace with CSS variables in production hardening.

2. **Missing React key on fragment loop** — `AuditLog.tsx` fragment iteration missing `key` prop. Causes React dev warning only, no runtime impact. Fix: add `key={i}` to the fragment.

3. **RBAC sticky column hover glitch** — cosmetic scroll artefact on the sticky first column. Fix: add `isolate` Tailwind class to the sticky cell.

---

## Repository structure

```
Chit-Fund/
├── app/                    Android app (Kotlin / Jetpack Compose)
├── admin/                  Admin dashboard (React / TypeScript / Vite)
├── backend/                REST API (Python / FastAPI / PostgreSQL)
├── docs/                   All documentation
│   ├── PROJECT-STATUS.md   This file
│   ├── RUNNING.md          How to run all three apps
│   ├── LEARNING.md         Complete learning documentation
│   ├── scope-decisions.md  Canonical scope: what is in/deferred/removed
│   ├── architecture/       System overview, API design, DB schema
│   ├── security/           Security checklist
│   ├── design/             Stitch prompt, Figma handoff, traceability
│   └── jira/               Epics, CSVs, per-ticket Kiro prompts
├── .kiro/
│   ├── steering/           Always-loaded project constraints
│   └── skills/             Reusable implementation guides
├── docker-compose.yml      PostgreSQL + pgvector + API services
└── README.md               Project overview
```
