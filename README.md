# ChitSmart — GenAI Chit Fund Management Platform

A complete, production-oriented chit fund management platform for India, built with AI-SDLC methodology.

**Status:** All 63 Jira stories Done · TypeScript 0 errors · Vite build clean

---

## What is ChitSmart?

ChitSmart is a three-application platform for managing chit funds — rotating savings and credit associations regulated under the Chit Funds Act, 1982. Members contribute a fixed installment each month and one member receives the full pooled amount each cycle.

The platform includes:
- **Android customer app** — 27 screens for members to pay installments, view memberships, access AI assistance, and manage their profile
- **Admin dashboard** — 24 pages for operations staff to manage members, chit groups, installments, payments, reconciliation, ledger, reports, and RBAC
- **Backend API** — FastAPI with PostgreSQL, pgvector RAG, JWT auth, and AI chat with SSE streaming

---

## Quick start

```bash
# Start the database
docker-compose up -d db

# Backend
cd backend && pip install -r requirements.txt
cp .env.example .env  # fill in SECRET_KEY, OPENAI_API_KEY
alembic upgrade head
uvicorn app.main:app --reload --port 8000

# Admin dashboard (new terminal)
cd admin && npm install && npm run dev
```

See **[docs/RUNNING.md](docs/RUNNING.md)** for the complete setup guide including Android.

---

## Technology stack

| Layer | Technology |
|---|---|
| Android | Kotlin, Jetpack Compose, Material 3, Hilt, Retrofit, Navigation Compose |
| Admin | React 18, TypeScript, Vite, Tailwind CSS, TanStack Query, Recharts |
| Backend | Python 3.12, FastAPI, SQLAlchemy 2.0 async, Alembic, PostgreSQL 16 |
| AI | OpenAI gpt-4o + text-embedding-3-small, pgvector cosine RAG, SSE streaming |
| Payments | UPI, Net Banking, Debit Card, NEFT/RTGS (internal order management; gateway integration planned) |
| Infrastructure | Docker Compose, GitHub Actions CI/CD |

---

## Documentation

| Document | Contents |
|---|---|
| [docs/RUNNING.md](docs/RUNNING.md) | How to run all three apps — step by step |
| [docs/LEARNING.md](docs/LEARNING.md) | Complete learning doc — AI-SDLC, patterns, lessons |
| [docs/PROJECT-STATUS.md](docs/PROJECT-STATUS.md) | Jira status, build verification, what's production-ready |
| [docs/scope-decisions.md](docs/scope-decisions.md) | What is in scope, deferred, and removed — and why |
| [docs/architecture/system-overview.md](docs/architecture/system-overview.md) | System diagram, data flows, technology decisions |
| [docs/security/security-checklist.md](docs/security/security-checklist.md) | Pre-production security review checklist |
| [docs/deployment/local-setup.md](docs/deployment/local-setup.md) | Full developer onboarding guide |
| [docs/design/google-stitch-master-prompt.md](docs/design/google-stitch-master-prompt.md) | UI generation prompt (Google Stitch, Batches A–D) |
| [docs/design/figma-mcp-implementation.md](docs/design/figma-mcp-implementation.md) | Figma MCP setup and screen-to-code node ID mapping |

---

## Repository structure

```
Chit-Fund/
├── app/                    Android app
│   └── src/main/java/com/chitsmart/
│       ├── ui/             27 screens organised by feature
│       ├── utils/          CurrencyUtils (Indian number words)
│       └── MainActivity.kt
│
├── admin/                  Admin dashboard
│   └── src/
│       ├── pages/          24 pages
│       ├── components/     AdminLayout (sidebar + header)
│       ├── api/            Axios client with JWT
│       └── types/          TypeScript domain types
│
├── backend/                REST API
│   └── app/
│       ├── ai/             RAG pipeline, embeddings, tools, chat
│       ├── api/v1/         15 endpoint modules
│       ├── core/           config, database, deps, security
│       ├── models/         SQLAlchemy ORM models
│       ├── repositories/   DB query layer
│       ├── schemas/        Pydantic request/response models
│       └── services/       Business logic
│
├── docs/                   All documentation
├── .kiro/
│   ├── steering/           Always-loaded project constraints (scope + architecture)
│   └── skills/             Reusable implementation guides (5 skills)
├── docker-compose.yml
└── README.md
```

---

## Scope boundaries

Three explicit scope decisions were made before implementation:

| Decision | Status | Rationale |
|---|---|---|
| Gold Saver 50K | **Removed** | Not a chit fund product |
| KYC (Aadhaar/PAN) | **Deferred** | Regulatory prerequisites not met |
| Auctions/Bidding | **Deferred** | Real-time infrastructure not built |

Database schema for deferred features exists (tables created, no API endpoints). Activating these features requires only endpoint and UI work — no schema migration.

See [docs/scope-decisions.md](docs/scope-decisions.md) for full rationale.

---

## Figma design file

`https://www.figma.com/design/lLqFOeduJnNpINYH7fQth7/Untitled`  
File key: `lLqFOeduJnNpINYH7fQth7`

---

## Jira project

`https://androidassistant.atlassian.net/jira/software/projects/CHIT/boards/36`  
Project key: `CHIT` · 63/63 stories Done
