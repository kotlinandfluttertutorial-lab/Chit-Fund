# ChitSmart — AI Context Guide

> Paste this file into a fresh AI session to give it complete context about the ChitSmart codebase.  
> Last updated: 17/10/2026 · Repo root: `j:\Android\AndroidStudioProjects\Kiro\Chit-Fund`

---

## TL;DR

ChitSmart is a three-layer platform for managing chit funds (rotating savings associations regulated under India's Chit Funds Act, 1982). Members pay monthly installments and one member receives the full pooled amount each cycle. The current release covers the full customer lifecycle — OTP login, group browsing and enrollment, installment payments, payout records (read-only), double-entry ledger, financial reports, and a GenAI assistant — with all UI screens (27 Android + 24 Admin) complete and a FastAPI backend with full schema but 9 stub routers still needing real DB query implementations. KYC and auction/bidding features are explicitly deferred. All 63 Jira stories are marked Done (design/UI complete); backend stubs are the main outstanding implementation work.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Android customer app | Kotlin, Jetpack Compose, Material Design 3, Hilt DI, Retrofit, Navigation Compose |
| Admin dashboard | React 18, TypeScript, Vite, Tailwind CSS, TanStack Query, Recharts |
| Backend API | Python 3.12, FastAPI, SQLAlchemy 2.0 async, Alembic, PostgreSQL 16 |
| AI / RAG | OpenAI `gpt-4o` + `text-embedding-3-small`, pgvector cosine similarity, SSE streaming |
| Payments | UPI, Net Banking, Debit Card, NEFT/RTGS — internal order management (no third-party gateway yet) |
| Auth | Firebase Phone Auth (OTP) → ChitSmart JWT (access + refresh tokens) |
| Infrastructure | Docker Compose (local dev + cloud prod), GitHub Actions CI/CD |

---

## Repository Structure

```
Chit-Fund/
├── app/                        Android app (Kotlin / Jetpack Compose)
│   └── src/main/java/com/chitsmart/
│       ├── ui/                 27 screens organised by feature folder
│       ├── di/                 Hilt modules (NetworkModule, DatabaseModule)
│       ├── data/               Repositories, API services, data sources
│       ├── domain/             Use-case layer
│       ├── utils/              AppConfig.kt (build-variant runtime singleton), CurrencyUtils
│       ├── MainActivity.kt     Single-activity host
│       └── ChitSmartApplication.kt
│   ├── src/local/              Product-flavor source set — local-only code / config
│   └── src/cloud/              Product-flavor source set — cloud-only code / config
│
├── admin/                      Admin dashboard (React / TypeScript / Vite)
│   └── src/
│       ├── pages/              24 page components
│       ├── components/layout/  AdminLayout.tsx (sidebar + header shell)
│       ├── features/           Feature sub-folders (ai, chitgroups, members, etc.)
│       ├── api/client.ts       Axios instance with JWT interceptor
│       └── App.tsx             Route definitions (React Router v6)
│
├── backend/                    REST API (Python / FastAPI / PostgreSQL)
│   └── app/
│       ├── ai/                 RAG pipeline, embeddings, tools, chat service, prompts
│       ├── api/v1/endpoints/   15 endpoint modules (auth, members, payments, …)
│       ├── api/v1/router.py    Registers all routers; deferred routers commented out
│       ├── core/               config.py (ENV=local|cloud), database.py, deps.py, security.py
│       ├── models/             SQLAlchemy ORM models (16 tables total)
│       ├── repositories/       DB query layer
│       ├── schemas/            Pydantic request/response models
│       └── services/           Business logic
│   ├── alembic/                Migration history (1 migration: 001_initial_schema)
│   ├── Dockerfile              Multi-stage: base / dev (hot-reload) / prod (baked, multi-worker)
│   ├── .env.local.example      Template for local variant
│   └── .env.cloud.example      Reference for cloud deployment env vars
│
├── docs/
│   ├── PROJECT-STATUS.md       Jira epic progress, build verification, what's production-ready
│   ├── RUNNING.md              Step-by-step run guide for all three apps
│   ├── scope-decisions.md      Canonical scope: IN / DEFERRED / REMOVED decisions
│   ├── LEARNING.md             AI-SDLC methodology and lessons learned
│   ├── architecture/           System overview, API design, DB schema diagrams
│   ├── deployment/             local-setup.md, build-variants.md, cloud deployment notes
│   ├── design/                 Figma handoff, Stitch master prompt, traceability matrix
│   ├── security/               Security checklist
│   └── jira/                   Epics, CSVs, per-ticket Kiro prompts
│
├── .kiro/
│   ├── steering/               Always-loaded project constraints (scope.md, architecture.md)
│   └── skills/                 Reusable implementation guides (5 skills)
│
├── docker-compose.yml          Local dev stack (PostgreSQL + pgvector + API, dev stage, hot-reload)
├── docker-compose.cloud.yml    Cloud override (prod Dockerfile stage, no volume mount)
├── README.md                   Project overview and quick-start
└── build.gradle.kts / settings.gradle.kts   Android Gradle root files
```

---

## Product & Release Scope

Source of truth: `docs/scope-decisions.md`

### IN scope (this release)

- Mobile OTP login (Firebase Phone Auth) and basic registration (name, mobile, email — **no Aadhaar/PAN**)
- Chit group browsing, enrollment, membership management
- Installment payment flow (UPI, Net Banking, Debit Card, NEFT/RTGS)
- Payment history, account statements, digital receipts
- Payout history as a **read-only record** (no approval workflow)
- Double-entry ledger and payment reconciliation
- Refunds and disputes
- Financial reports and exports (Collections, Member Activity, Payouts, AI Usage)
- GenAI assistant with multilingual support (EN + HI) and policy references
- Admin: member management, chit group management, installment tracking, payment operations
- Admin: AI collection summary and natural-language reporting
- Role-based access control (4 roles: Super Admin, Finance Manager, Support Agent, Auditor)

### DEFERRED — do not implement

**KYC (regulatory prerequisites not ready)**
- KYC document upload screen, KYC status screen
- Aadhaar/PAN fields on registration
- KYC status badge and Documents section in Profile
- Admin KYC sidebar, KYC Pending Queue, KYC Review Workspace
- AI Document Review tool
- KYC Officer admin role

**Bidding and Auctions (infrastructure not ready)**
- Customer app Auctions tab and all auction screens
- Bid submission, live auction monitoring, bid history
- Admin Auctions sidebar and all auction management screens
- Payout approval workflow
- WebSocket infrastructure for real-time bidding

### REMOVED entirely

- **Gold Saver 50K** — not a chit fund product. Use realistic names like "Family Savings 50K", "Community Chit 1L".

### Navigation

**Customer app bottom nav:** Home | My Chits | Payments | Assistant | Profile  
*(No Auctions tab)*

**Admin sidebar nav:** Dashboard | Members | Chit Groups | Installments | Payments | Ledger | Reports | AI Tools | Settings  
*(No KYC, no Auctions)*

---

## Data Model

All tables are created in migration `001_initial_schema`. The Alembic file is at `backend/alembic/versions/`.

### Active tables (API endpoints exist or are in scope)

| Table | Model file | Key columns | Notes |
|---|---|---|---|
| `members` | `models/member.py` | `id` (UUID PK), `mobile_number` (unique), `full_name`, `email`, `date_of_birth`, `gender`, `account_status` (ACTIVE/INACTIVE/SUSPENDED), `is_mobile_verified`, `kyc_status` (NOT_REQUIRED default), `preferred_language` | `aadhaar_number_encrypted`, `pan_number_encrypted` exist in schema but are NOT collected via API this release |
| `member_sessions` | `models/member_session.py` | `id`, `member_id` FK, `refresh_token_hash`, `expires_at`, `is_revoked` | Stores hashed refresh tokens; used for JWT refresh/logout |
| `otp_sessions` | `models/otp_session.py` | `id`, `mobile_number`, `otp_hash`, `expires_at`, `is_used` | Legacy OTP flow (pre-Firebase); still in schema |
| `chit_groups` | `models/chit_group.py` | `id`, `name`, `chit_value`, `monthly_installment`, `duration_months`, `max_members`, `foreman_commission_pct`, `start_date`, `status` (PENDING/ACTIVE/COMPLETED/CLOSED), `next_auction_date` | `next_auction_date` stored but unused (auction API deferred) |
| `chit_memberships` | `models/chit_group.py` | `id`, `member_id` FK, `chit_group_id` FK, `member_number`, `enrollment_date`, `status`, `payout_received`, `payout_month` | MembershipStatus: PENDING_ENROLLMENT / ACTIVE / COMPLETED / SUSPENDED |
| `installment_schedules` | `models/payment.py` | `id`, `chit_group_id` FK, `month_number`, `due_date`, `amount_per_member` | One row per month per group |
| `payments` | `models/payment.py` | `id`, `member_id` FK, `chit_group_id` FK, `installment_schedule_id` FK, `amount`, `late_fee`, `total_amount`, `payment_method` (UPI/NET_BANKING/DEBIT_CARD/NEFT/RTGS/CASH), `status` (PENDING/PROCESSING/SUCCESSFUL/FAILED/REFUNDED/VOIDED), `gateway_reference`, `receipt_number`, `void_reason` | Never hard-deleted — mark VOIDED with reason |
| `ledger_entries` | `models/payment.py` | `id`, `chit_group_id` FK, `member_id` FK (nullable), `payment_id` FK (nullable), `entry_type` (INSTALLMENT/LATE_FEE/PAYOUT/FOREMAN_COMMISSION/REFUND/ADJUSTMENT), `narration`, `debit`, `credit`, `running_balance` | Append-only, never deleted |
| `bank_accounts` | (relationship on Member) | `id`, `member_id` FK, bank details | Used for payout credit targeting |
| `nominees` | (relationship on Member) | `id`, `member_id` FK, nominee details | — |
| `policy_documents` | `models/policy_document.py` | `id`, `document_name`, `file_path` | Metadata for ingested PDFs |
| `policy_chunks` | (created in migration) | `id`, `document_name`, `section_reference`, `content`, `language`, `embedding vector(1536)` | pgvector column; used by RAG pipeline |

### Deferred tables (schema exists, zero API endpoints)

| Table | Model file | Purpose |
|---|---|---|
| `kyc_documents` | `models/deferred_schema.py` | KYC doc uploads — deferred (Decision 2) |
| `auctions` | `models/deferred_schema.py` | Auction scheduling — deferred (Decision 3) |
| `bids` | `models/deferred_schema.py` | Bid submissions — deferred with auctions |
| `payouts` | `models/deferred_schema.py` | Payout records — **GET read-only endpoint exists**; approval workflow (POST/PATCH) deferred |

---

## Backend API Surface

Base URL: `/api/v1`  
All endpoints require `Authorization: Bearer <access_token>` except `/auth/verify-firebase-token`.  
Swagger docs available at `http://localhost:8000/docs` when `ENV=local` only.

Router registration: `backend/app/api/v1/router.py`

### Auth — `/api/v1/auth`
File: `endpoints/auth.py`

| Method | Path | Purpose | Auth required |
|---|---|---|---|
| POST | `/verify-firebase-token` | Verify Firebase Phone Auth ID token → issue ChitSmart JWT + refresh token; creates member record if new | No |
| POST | `/register` | Update member profile after first login (name, email, DOB, gender — no KYC fields) | Yes (JWT) |
| POST | `/refresh` | Exchange refresh token for new access token | No (uses refresh token) |
| POST | `/logout` | Revoke refresh token | No (uses refresh token) |

**Auth flow:** Android completes Firebase Phone OTP → sends `firebase_id_token` + `mobile_number` to `/verify-firebase-token` → receives `is_new_user`, `access_token`, `refresh_token`. If `is_new_user=true`, calls `/register` with profile data.

### Members — `/api/v1/members`
File: `endpoints/members.py` *(stub — needs real DB queries)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/me` | Get authenticated member's profile | Any member |
| PATCH | `/me` | Update profile fields | Any member |
| GET | `/me/memberships` | List the member's chit group memberships | Any member |
| GET | `/me/statements` | Member account statement | Any member |

### Chit Groups — `/api/v1/chit-groups`
File: `endpoints/chit_groups.py` *(stub)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/` | List available chit groups (browse) | Any member |
| GET | `/{id}` | Chit group detail | Any member |
| POST | `/{id}/enroll` | Enroll in a chit group | Any member |

### Installments — `/api/v1/installments`
File: `endpoints/installments.py` *(stub)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/` | List upcoming installments for the current member | Any member |
| GET | `/{id}` | Installment detail | Any member |

### Payments — `/api/v1/payments`
File: `endpoints/payments.py` *(full implementation)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| POST | `/initiate` | Create a payment order | Any member |
| POST | `/webhook` | Payment gateway webhook (HMAC-verified) | System |
| GET | `/history` | Member's payment history | Any member |
| GET | `/{id}/receipt` | Download digital receipt | Any member |

### Payouts — `/api/v1/payouts`
File: `endpoints/payouts.py` *(read-only GET only)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/` | Member's payout history (read-only) | Any member |

### Ledger — `/api/v1/ledger`
File: `endpoints/ledger.py` *(stub)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/` | Double-entry ledger entries for current member | Any member |

### Reports — `/api/v1/reports`
File: `endpoints/reports.py` *(stub)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/` | Member-facing report list | Any member |
| GET | `/export` | Export statement as CSV/PDF | Any member |

### AI Assistant — `/api/v1/ai`
File: `endpoints/ai_assistant.py` *(full implementation)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| POST | `/chat` | RAG-powered chat with SSE streaming | Any member |
| GET | `/history` | Conversation history | Any member |

### Admin — Members `/api/v1/admin/members`
File: `endpoints/admin_members.py` *(stub)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/` | Search/list all members | Super Admin, Finance Manager, Support Agent |
| POST | `/` | Create member | Super Admin |
| GET | `/{id}` | Member detail | Super Admin, Finance Manager, Support Agent |
| PATCH | `/{id}` | Update member | Super Admin |
| POST | `/{id}/suspend` | Suspend member | Super Admin |

### Admin — Chit Groups `/api/v1/admin/chit-groups`
File: `endpoints/admin_chit_groups.py` *(stub)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/` | List all chit groups | All admin roles |
| POST | `/` | Create chit group | Super Admin, Finance Manager |
| GET | `/{id}` | Chit group detail | All admin roles |
| PATCH | `/{id}` | Update chit group | Super Admin, Finance Manager |

### Admin — Installments `/api/v1/admin/installments`
File: `endpoints/admin_installments.py` *(stub)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/schedule` | Installment schedule by group | All admin roles |
| GET | `/collection` | Collection status for a month | Finance Manager, Super Admin |
| POST | `/{id}/mark-paid` | Mark installment paid offline (cash) | Finance Manager, Super Admin |

### Admin — Payments `/api/v1/admin/payments`
File: `endpoints/admin_payments.py` *(stub)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/` | All payment transactions | Finance Manager, Super Admin, Auditor |
| POST | `/reconcile` | Mark payments reconciled | Finance Manager, Super Admin |
| POST | `/refund` | Initiate refund | Finance Manager, Super Admin |
| GET | `/disputes` | List open disputes | All admin roles |

### Admin — Reports `/api/v1/admin/reports`
File: `endpoints/admin_reports.py` *(stub)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| GET | `/collections` | Collections report | Finance Manager, Super Admin, Auditor |
| GET | `/member-activity` | Member activity report | All admin roles |
| GET | `/payouts` | Payout report | Finance Manager, Super Admin, Auditor |
| GET | `/ai-usage` | AI assistant usage stats | Super Admin |
| POST | `/export` | Export report as CSV/PDF | Finance Manager, Super Admin, Auditor |

### Admin — AI Tools `/api/v1/admin/ai`
File: `endpoints/admin_ai.py` *(full implementation)*

| Method | Path | Purpose | Role |
|---|---|---|---|
| POST | `/collection-summary` | AI-generated collection health summary | Finance Manager, Super Admin |
| POST | `/natural-language-report` | Natural-language report query | Finance Manager, Super Admin |

---

## Admin Dashboard (React)

### Stack
React 18, TypeScript, Vite, Tailwind CSS, TanStack Query (data fetching), Recharts (charts), React Router v6.

### API client
`admin/src/api/client.ts` — Axios instance; reads `VITE_API_BASE_URL` from env; attaches `Authorization: Bearer` header from localStorage token; auto-redirects to `/login` on 401.

### Route map (from `admin/src/App.tsx`)

| Route | Page component | Description |
|---|---|---|
| `/login` | `Login.tsx` | Admin login (username + password → JWT) |
| `/dashboard` | `Dashboard.tsx` | Executive dashboard — 5 KPI cards, collection chart (Recharts), group status table |
| `/members` | `MemberSearch.tsx` | Searchable / filterable member table with pagination |
| `/members/new` | `MemberEdit.tsx` | 4-step member create form (Personal / Contact / Bank / Nominees) |
| `/members/:id` | `MemberDetails.tsx` | Member detail — 4 tabs: Overview / Memberships / Payments / Activity |
| `/members/:id/edit` | `MemberEdit.tsx` | Edit existing member |
| `/chit-groups` | `ChitGroupsList.tsx` | Chit groups table + search |
| `/chit-groups/new` | `ChitGroupEdit.tsx` | Create group — 3 sections: Group Details / Installment Config / Terms |
| `/chit-groups/:id` | `ChitGroupDetail.tsx` | Group detail — 4 tabs: Overview / Members / Installments / Financials |
| `/chit-groups/:id/edit` | `ChitGroupEdit.tsx` | Edit group |
| `/installments` | `InstallmentSchedule.tsx` | Group selector + monthly schedule table |
| `/installments/collection` | `InstallmentCollection.tsx` | Month picker + member status table + bulk mark-paid |
| `/payments` | `PaymentTransactions.tsx` | Master payments table + filters + receipt action |
| `/payments/reconciliation` | `Reconciliation.tsx` | 3-column comparison, unmatched list |
| `/payments/refunds` | `Refunds.tsx` | Disputes table + detail panel |
| `/ledger` | `Ledger.tsx` | Double-entry table + running balance |
| `/ledger/account-statements` | `AccountStatement.tsx` | Member account statement |
| `/payouts` | `PayoutRecords.tsx` | Read-only payout records table |
| `/reports` | `Reports.tsx` | 4 report category cards + Quick Reports + history |
| `/reports/analytics` | `CollectionAnalytics.tsx` | Collection analytics charts |
| `/reports/builder` | `ReportBuilder.tsx` | 4-step report wizard: type / filters / columns / preview + schedule |
| `/ai-tools` | `AITools.tsx` | AI collection summary + natural-language reporting |
| `/settings` | `Settings.tsx` | Settings hub — links to RBAC and Admin Users |
| `/settings/rbac` | `RBAC.tsx` | 4 roles × 9 modules permission matrix |
| `/settings/users` | `AdminUsers.tsx` | Invite / deactivate / role-edit admin users |
| `/audit` | `AuditLog.tsx` | Chronological audit log + filters |

### RBAC roles

| Role | Description |
|---|---|
| Super Admin | Full access to all modules |
| Finance Manager | Payments, ledger, reports, installments — no user management |
| Support Agent | Read members, raise disputes — no financial mutations |
| Auditor | Read-only access to all modules |

---

## Android App (27 Screens)

### Stack
Kotlin, Jetpack Compose, Material Design 3, Hilt (dependency injection), Retrofit (HTTP), Navigation Compose (single `NavGraph.kt`), Firebase Auth SDK.

Nav graph entry point: `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt`

### Build variants (product flavors)

| Flavor | App ID | API target |
|---|---|---|
| `local` | `com.chitsmart.local` | `local.api.base.url` from `local.properties` → `http://10.0.2.2:8000/api/v1/` |
| `cloud` | `com.chitsmart` | `cloud.api.base.url` from `local.properties` → `https://api.chitsmart.app/api/v1/` |

**Build commands:**
```bash
.\gradlew assembleLocalDebug      # dev emulator
.\gradlew assembleCloudDebug      # staging
.\gradlew assembleCloudRelease    # production (requires signing keystore)
```

`AppConfig.kt` (in `utils/`) exposes runtime variant values: `apiBaseUrl`, `buildVariant`, `isLoggingEnabled`, `connectTimeoutMs`, `retryOnConnectionFailure`. `NetworkModule` (Hilt) uses these to configure OkHttp.

### Screen directory

**Auth & Onboarding** (`ui/auth/`, `ui/onboarding/`, `ui/splash/`)
| Screen | File | Notes |
|---|---|---|
| Splash Screen | `splash/SplashScreen.kt` | Decides nav: onboarding (first launch) or login |
| Onboarding Carousel | `onboarding/` | 3 slides |
| Login | `auth/LoginScreen.kt` | Mobile number entry → Firebase OTP |
| OTP Verification | `auth/OTPScreen.kt` | 6-digit OTP entry + timer |
| Registration | `auth/RegistrationScreen.kt` | Name, email, DOB, gender — no Aadhaar/PAN |

**Home** (`ui/home/`)
| Screen | File |
|---|---|
| Home Dashboard | `HomeScreen.kt` |
| Notifications | `notifications/` |

**My Chits** (`ui/chits/`)
| Screen | File |
|---|---|
| Active Memberships | `ActiveMembershipsScreen.kt` |
| Browse Chit Groups | `BrowseGroupsScreen.kt` |
| Group Details | `GroupDetailsScreen.kt` (4 tabs) |
| Enrollment Flow | `EnrollmentScreen.kt` (4 steps) |
| Group Member View | `GroupMemberViewScreen.kt` |

**Payments** (`ui/payment/`)
| Screen | File |
|---|---|
| Payments Home | `PaymentsHomeScreen.kt` |
| Payment Flow | `PaymentFlowScreen.kt` (Summary → Method → Processing → Success/Failure) |
| Digital Receipt | `ReceiptScreen.kt` |
| Payment History | `PaymentHistoryScreen.kt` |
| Payout History | `PayoutHistoryScreen.kt` (read-only) |

**AI Assistant** (`ui/assistant/`)
| Screen | File |
|---|---|
| Assistant Home | `AssistantHomeScreen.kt` |
| AI Chat | `AIChatScreen.kt` (policy reference modal, SSE streaming) |
| Human Support | `HumanSupportScreen.kt` |

**Profile** (`ui/profile/`)
| Screen | File |
|---|---|
| Profile | `ProfileScreen.kt` |
| Personal Information | `PersonalInfoScreen.kt` |
| Bank Accounts | `BankAccountsScreen.kt` |
| Security Settings | `SecuritySettingsScreen.kt` |
| Help Centre | `HelpCentreScreen.kt` |
| Language & Preferences | `PreferencesScreen.kt` |

**Bottom navigation tabs:** Home | My Chits | Payments | Assistant | Profile  
*(No Auctions tab in this release)*

---

## GenAI Assistant

Files: `backend/app/ai/`

| File | Purpose |
|---|---|
| `rag_pipeline.py` | Embeds user query → pgvector cosine search on `policy_chunks` → returns top-5 chunks |
| `embeddings.py` | `text-embedding-3-small` via OpenAI API |
| `tools.py` | Read-only account tools (member profile, membership list, payment history, payout history, installment status) |
| `tool_executor.py` | Dispatches tool calls; enforces read-only constraint |
| `chat_service.py` | Orchestrates RAG + tool calls + GPT-4o chat completion with SSE streaming |
| `prompts.py` | System prompt templates (English, Hindi) |

### How it works
1. User sends a message to `POST /api/v1/ai/chat`.
2. `chat_service.py` embeds the query and runs `rag_pipeline.retrieve_relevant_chunks()`.
3. Retrieved policy chunks are injected into the GPT-4o system prompt.
4. GPT-4o may call read-only account tools (balance, payment history, etc.) via `tool_executor.py`.
5. Response is streamed back as SSE.

### Rules
- The assistant **may only read** approved policy documents and the authenticated user's own account data.
- It **must never write data**, initiate transactions, or approve workflows.
- All policy citations must include document name and section reference.
- Minimum supported languages: **English (en), Hindi (hi)**.
- The `policy_chunks` table must be populated via the document ingestion script before policy citations work. Without ingestion, account queries still work.

---

## Conventions & Guardrails

### Database safety
- **Financial records (payments, ledger_entries, payouts) are never hard-deleted.** Mark as `VOIDED` or `REFUNDED` with a `void_reason`. Application code has no DELETE for these tables.
- Deferred feature tables (`kyc_documents`, `auctions`, `bids`) exist in the schema — never drop them.
- `payouts` has a read-only GET endpoint; POST/PATCH approval workflow is deferred.

### Auth & RBAC
- Every non-auth endpoint requires `Authorization: Bearer <jwt>`.
- JWT is issued by ChitSmart after Firebase Phone OTP verification — not issued directly by Firebase.
- Admin endpoints check role via `Depends(require_role([...]))` in FastAPI deps.
- Customer endpoints check `Depends(get_current_member)`.

### Backend config
- `ENV=local` (default): `DEBUG=True`, hot-reload, Swagger UI enabled at `/docs`, loads `.env.local`.
- `ENV=cloud`: `DEBUG=False`, multi-worker, Swagger UI disabled, all secrets via environment variables (no `.env` file on disk).
- `settings` singleton: `backend/app/core/config.py` (pydantic-settings v2).

### Payment flow
- Internal order management only — no third-party gateway integrated yet.
- Gateway webhook at `POST /api/v1/payments/webhook` uses HMAC signature verification.
- Supported methods: `UPI`, `NET_BANKING`, `DEBIT_CARD`, `NEFT`, `RTGS`, `CASH` (admin offline).

### Known non-blocking issues
- `AuditLog.tsx` (admin) is missing `key` prop on a fragment loop — React dev warning only.
- RBAC page has a sticky column hover glitch (cosmetic scroll artefact).
- A few inline `style={{ color }}` props in Reports/Settings for dynamic colours.

---

## How to Run Locally

Full details in `docs/RUNNING.md`. Summary:

### Prerequisites
Python 3.12+, Node.js 20+, Docker Desktop, Android Studio (Hedgehog 2023.1+).

### Step 1 — Start the database
```bash
# From repo root
docker compose up -d db
# PostgreSQL 16 + pgvector on port 5432
# user: chitsmart / pass: password / db: chitsmart_dev
```

### Step 2 — Backend API
```bash
cd backend
python -m venv .venv
.venv\Scripts\activate                  # Windows (use source .venv/bin/activate on macOS/Linux)
pip install -r requirements.txt
copy .env.local.example .env.local      # then fill in SECRET_KEY and OPENAI_API_KEY
alembic upgrade head                    # creates all 16 tables
uvicorn app.main:app --reload --port 8000
# API: http://localhost:8000
# Swagger: http://localhost:8000/docs
```

In `ENV=local` / `DEBUG=true`, OTP is printed to the terminal — no real SMS is sent.

### Step 3 — Admin dashboard
```bash
cd admin
npm install
# Create admin/.env.local with: VITE_API_BASE_URL=http://localhost:8000/api/v1
npm run dev
# Admin: http://localhost:5173
```

### Step 4 — Android app
1. Open the `Chit-Fund` repo root in Android Studio.
2. Add Inter font files to `app/src/main/res/font/` (inter_regular.ttf, inter_medium.ttf, inter_semibold.ttf, inter_bold.ttf) — build fails without them.
3. In `local.properties` set `local.api.base.url=http://10.0.2.2:8000/api/v1/`.
4. Select **Build Variant → localDebug** and run on Pixel 6 emulator (API 34).

### Full Docker stack (alternative)
```bash
cp backend/.env.local.example backend/.env.local   # fill in keys
docker compose up --build
# DB + API on one command
```

### Cloud Docker stack
```bash
docker compose -f docker-compose.yml -f docker-compose.cloud.yml up -d
# Prod Dockerfile stage, no volume mount, multi-worker, alembic runs on startup
```

---

## Testing & CI

### Build verification (current state)
| Check | Command | Status |
|---|---|---|
| TypeScript | `cd admin && npx tsc --noEmit` | ✅ 0 errors |
| Vite production build | `cd admin && npx vite build` | ✅ Clean |
| Python AST | `python -c "import ast; ast.parse(open('app/main.py').read())"` | ✅ Clean |
| Android build | `.\gradlew assembleLocalDebug` | ⬜ Requires Android Studio + font files |

### Automated test suite
Not yet written (P1 priority before production launch). No pytest, Vitest, or Compose tests exist yet.

### CI/CD (GitHub Actions)
Workflow files in `.github/workflows/`:

| File | Trigger | What it does |
|---|---|---|
| `ci.yml` | Push / PR to main | Full CI gate: lint + typecheck + build for all three layers |
| `backend.yml` | Changes to `backend/` | Python lint (ruff), type check (mypy), pytest (when tests exist) |
| `admin.yml` | Changes to `admin/` | npm install, tsc, vite build, Vitest (when tests exist) |
| `android.yml` | Changes to `app/` | Gradle build (assembleLocalDebug) |
| `pr-gate.yml` | Pull requests | Runs all checks; blocks merge on failure |

---

## Key File Index

| File | Description |
|---|---|
| `docs/scope-decisions.md` | **Start here.** Canonical scope: IN / DEFERRED / REMOVED decisions with rationale |
| `docs/PROJECT-STATUS.md` | Jira epics, build verification, what's production-ready, known issues |
| `docs/RUNNING.md` | Complete step-by-step run guide for all three apps |
| `backend/app/main.py` | FastAPI app entry point — CORS, middleware, router registration, health endpoint |
| `backend/app/api/v1/router.py` | All router registrations; deferred routers listed and commented out |
| `backend/app/core/config.py` | `Settings` class (pydantic-settings v2) — all env vars, `ENV=local\|cloud` variant logic |
| `backend/app/core/deps.py` | FastAPI dependencies: `get_current_member`, `require_role`, `get_db` |
| `backend/app/models/member.py` | `Member` ORM model — KYC fields present but unexposed |
| `backend/app/models/chit_group.py` | `ChitGroup` + `ChitMembership` ORM models |
| `backend/app/models/payment.py` | `InstallmentSchedule`, `Payment`, `LedgerEntry` ORM models |
| `backend/app/models/deferred_schema.py` | `KYCDocument`, `Auction`, `Bid`, `Payout` — schema exists, no endpoints this release |
| `backend/app/ai/rag_pipeline.py` | pgvector cosine RAG retrieval (top-5 policy chunks per query) |
| `backend/app/ai/chat_service.py` | GPT-4o orchestration with RAG + read-only tools + SSE streaming |
| `backend/app/api/v1/endpoints/auth.py` | Firebase token verification → ChitSmart JWT issuance; full implementation |
| `backend/app/api/v1/endpoints/payments.py` | Payment initiation, webhook handler, history, receipt; full implementation |
| `admin/src/App.tsx` | All 24 admin routes — single source of truth for admin navigation |
| `admin/src/components/layout/AdminLayout.tsx` | Sidebar + header shell for all admin pages |
| `admin/src/api/client.ts` | Axios instance with JWT interceptor |
| `app/src/main/java/com/chitsmart/ui/navigation/NavGraph.kt` | Android navigation graph — all 27 screens and transitions |
| `app/src/main/java/com/chitsmart/utils/AppConfig.kt` | Runtime variant singleton (API URL, logging, timeouts) |
| `app/src/main/java/com/chitsmart/di/NetworkModule.kt` | Hilt module — configures Retrofit + OkHttp from AppConfig |
| `docker-compose.yml` | Local dev stack (PostgreSQL + pgvector + API dev stage) |
| `docker-compose.cloud.yml` | Cloud prod override (prod Dockerfile stage, no volume) |
| `.kiro/steering/` | Always-loaded constraints: `scope.md` and `architecture.md` — read before implementing anything |
