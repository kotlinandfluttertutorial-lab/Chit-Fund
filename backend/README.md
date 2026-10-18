# ChitSmart Backend

Python 3.12 · FastAPI · PostgreSQL 16 · Alembic · SQLAlchemy 2.0

## Setup

```bash
# 1. Create virtual environment
python -m venv .venv
.venv\Scripts\activate        # Windows
# source .venv/bin/activate   # macOS/Linux

# 2. Install dependencies
pip install -r requirements.txt

# 3. Configure environment
copy .env.example .env
# Edit .env with your database URL, secret key, and API keys

# 4. Start PostgreSQL (Docker)
docker run -d \
  --name chitsmart-db \
  -e POSTGRES_USER=chitsmart \
  -e POSTGRES_PASSWORD=password \
  -e POSTGRES_DB=chitsmart_dev \
  -p 5432:5432 \
  postgres:16
  
  docker run -d \ --name chitsmart-db \ -e POSTGRES_USER=chitsmart \ -e POSTGRES_PASSWORD=password \ -ee POSTGRES_DB=chitsmart_dev \  -p 5432:5432 \ postgres:16


# 5. Run migrations
alembic upgrade head

# 6. Start development server
uvicorn app.main:app --reload --port 8000
```

## API docs

Available at http://localhost:8000/docs (DEBUG mode only)

## Project structure

```
backend/
├── app/
│   ├── main.py              FastAPI app, CORS, router registration
│   ├── api/v1/
│   │   ├── router.py        All endpoint modules registered here
│   │   └── endpoints/       One file per domain
│   ├── core/
│   │   └── config.py        Pydantic settings from .env
│   ├── models/              SQLAlchemy ORM models
│   │   ├── base.py          Base, UUIDMixin, TimestampMixin
│   │   ├── member.py        Member (customer account)
│   │   ├── chit_group.py    ChitGroup, ChitMembership
│   │   ├── payment.py       Payment, InstallmentSchedule, LedgerEntry
│   │   └── deferred_schema.py  KYCDocument, Auction, Bid, Payout (schema only)
│   ├── schemas/             Pydantic request/response schemas
│   ├── services/            Business logic
│   ├── repositories/        DB queries
│   └── ai/                  OpenAI / LangChain RAG integration
├── alembic/                 Database migrations
├── tests/
├── requirements.txt
└── .env.example
```

## Deferred features

The following schemas exist in the database but have no API endpoints in this release:

- `kyc_documents` — KYC deferred (regulatory prerequisites)
- `auctions` — Auctions deferred (infrastructure)
- `bids` — Deferred with auctions
- `payouts` — Read-only GET only; approval workflow deferred

See `docs/scope-decisions.md` for full rationale.
