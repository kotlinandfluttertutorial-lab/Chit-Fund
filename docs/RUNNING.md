# ChitSmart — How to Run the Platform

> **Updated:** After full implementation — 27 Android screens, 24 admin pages, 40 backend files.

This document covers running all three applications locally:
1. **Backend API** (Python / FastAPI)
2. **Admin Dashboard** (React / Vite)
3. **Android App** (Kotlin / Jetpack Compose)

---

## Prerequisites

| Tool | Version | Install |
|---|---|---|
| Python | 3.12+ | https://python.org |
| Node.js | 20+ | https://nodejs.org |
| Docker Desktop | Latest | https://docker.com |
| Android Studio | Hedgehog 2023.1+ | https://developer.android.com/studio |
| Java | 17 | Bundled with Android Studio |

Verify:
```bash
python --version    # 3.12.x
node --version      # 20.x.x
docker --version    # 24.x.x or later
```

---

## 1. Backend API

### Step 1 — Start the database

```bash
# From the repository root
docker-compose up -d db
```

This starts **PostgreSQL 16** with **pgvector** on port `5432`.

Wait until healthy:
```bash
docker-compose ps
# State should show: healthy
```

### Step 2 — Configure environment

```bash
cd backend
copy .env.example .env          # Windows
# cp .env.example .env          # macOS/Linux
```

Open `.env` and fill in at minimum:

```env
DATABASE_URL=postgresql+asyncpg://chitsmart:password@localhost:5432/chitsmart_dev
SECRET_KEY=your-random-secret-key-here
DEBUG=true
OPENAI_API_KEY=sk-...           # required for AI assistant
```

Generate a secret key:
```bash
python -c "import secrets; print(secrets.token_hex(32))"
```

### Step 3 — Create virtual environment and install dependencies

```bash
cd backend

# Windows
python -m venv .venv
.venv\Scripts\activate

# macOS/Linux
python -m venv .venv
source .venv/bin/activate

# Install packages
pip install -r requirements.txt
```

### Step 4 — Run database migrations

```bash
alembic upgrade head
```

Expected output (last line):
```
INFO  [alembic.runtime.migration] Running upgrade  -> 001_initial_schema, Initial schema...
```

This creates all 16 tables including deferred schema (kyc_documents, auctions, bids).

### Step 5 — Start the API server

```bash
uvicorn app.main:app --reload --port 8000
```

**API is running at:** http://localhost:8000  
**Swagger docs:** http://localhost:8000/docs *(DEBUG=true only)*

### Verify it works

```bash
curl http://localhost:8000/health
# {"status":"ok","version":"0.1.0"}
```

### Test OTP flow (DEBUG mode — OTP appears in terminal, no real SMS)

```bash
# Request OTP
curl -X POST http://localhost:8000/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"mobile_number": "+919876543210"}'

# Watch the API terminal for:
# [DEV OTP] +919876543210 → 123456

# Verify OTP
curl -X POST http://localhost:8000/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"mobile_number": "+919876543210", "otp": "123456"}'
# Returns: {"is_new_user": true, "access_token": "...", "refresh_token": "..."}
```

### Ingest policy documents for AI assistant (optional)

The AI assistant uses RAG over approved policy documents. To enable policy search:

```bash
# Place PDF files in backend/data/approved_policies/
# Then run the ingestion script:
python -m app.ai.document_ingestion \
  --pdf data/approved_policies/chit_funds_act_1982.pdf \
  --name "Chit Funds Act, 1982"
```

Without ingestion, the assistant still works but returns generic responses without policy citations.

---

## 2. Admin Dashboard

### Step 1 — Install dependencies

```bash
cd admin
npm install
```

### Step 2 — Configure environment

Create `admin/.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8000/api/v1
```

### Step 3 — Start the dev server

```bash
npm run dev
```

**Admin dashboard is running at:** http://localhost:5173

### Step 4 — TypeScript check and production build

```bash
# Type check (should show 0 errors)
npx tsc --noEmit

# Production build (outputs to admin/dist/)
npm run build
```

### Complete page directory

| URL | Page | Status |
|---|---|---|
| /dashboard | Executive Dashboard — 5 KPI cards, Recharts collection chart, group table | ✅ Full |
| /members | Member Search — searchable + filterable table, pagination | ✅ Full |
| /members/:id | Member Details — 4 tabs: Overview/Memberships/Payments/Activity | ✅ Full |
| /members/new | Member Create/Edit — 4-step form (Personal/Contact/Bank/Nominees) | ✅ Full |
| /chit-groups | Chit Groups List — table + search | ✅ Full |
| /chit-groups/new | Create/Edit Chit Group — Group Details + Installment Config + Terms | ✅ Full |
| /chit-groups/:id | Chit Group Detail — 4 tabs: Overview/Members/Installments/Financials | ✅ Full |
| /installments | Installment Schedule — group selector + monthly schedule table | ✅ Full |
| /installments/collection | Installment Collection — month picker + member table + bulk actions | ✅ Full |
| /payments | Payment Transactions — master table + filters + receipt action | ✅ Full |
| /payments/reconciliation | Reconciliation — 3-column comparison, unmatched list | ✅ Full |
| /payments/refunds | Refunds & Disputes — disputes table + detail panel | ✅ Full |
| /ledger | Ledger View — double-entry table + running balance | ✅ Full |
| /ledger/statement | Member Account Statement — member search + statement table | ✅ Full |
| /payouts | Payout Records — read-only table (approval workflow deferred) | ✅ Full |
| /reports | Reports Dashboard — 4 category cards + Quick Reports + history | ✅ Full |
| /reports/builder | Report Builder — 4-step wizard: type/filters/columns/preview + schedule | ✅ Full |
| /audit | Audit Log — chronological log + filters, no delete actions | ✅ Full |
| /ai-tools | AI Tools — collection summary + natural-language reporting | ✅ Full |
| /settings | Settings Hub — links to RBAC and Admin Users | ✅ Full |
| /settings/rbac | RBAC — 4 roles × 9 modules permission matrix | ✅ Full |
| /settings/users | Admin User Management — invite + deactivate + role edit | ✅ Full |

**Not included** (deferred per scope-decisions.md):
- KYC sidebar item and review screens
- Auctions management screens
- Payout approval workflow (payout records are read-only)

---

## 3. Android App

### Prerequisites

1. Open **Android Studio**
2. Make sure you have **Android SDK** platform 35 installed
3. Create an **AVD** — recommended: Pixel 6, API 34, 1080×2400 px (390dp width)

### Step 1 — Open the project

In Android Studio: **File → Open** → select the `Chit-Fund` folder (repository root).

Android Studio detects the `app/` module and syncs Gradle automatically.

### Step 2 — Add Inter font files

The app uses Inter font. Add these files to `app/src/main/res/font/`:

1. Download Inter from https://fonts.google.com/specimen/Inter
2. Rename and copy into `app/src/main/res/font/`:

| Download name | Rename to |
|---|---|
| `Inter-Regular.ttf` | `inter_regular.ttf` |
| `Inter-Medium.ttf` | `inter_medium.ttf` |
| `Inter-SemiBold.ttf` | `inter_semibold.ttf` |
| `Inter-Bold.ttf` | `inter_bold.ttf` |

> If you skip this step, the build fails with "Cannot find font resource". Temporarily comment out the `FontFamily` block in `Type.kt` to build without custom fonts.

### Step 3 — Configure API URL

The app uses `http://10.0.2.2:8000` by default (Android emulator loopback → host machine).

For a physical device, update `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://YOUR_PC_IP:8000/api/v1/\"")
```

### Step 4 — Run on emulator

1. Select your AVD in the device toolbar
2. Click ▶ **Run** (Shift+F10)
3. First build takes ~2 minutes; subsequent builds are faster

### Complete screen directory (27 screens)

| Entry point | Path to reach | Screen |
|---|---|---|
| App launch | — | Splash Screen |
| First launch | After splash | Onboarding Carousel (3 slides) |
| Returning user | After splash | Login Screen |
| Login → | Enter mobile + Send OTP | OTP Verification |
| OTP → new user | — | Registration |
| After auth | — | **Home Dashboard** |
| Home → bell | — | Notifications |
| Bottom nav: My Chits | — | Active Memberships |
| My Chits → Browse | — | Browse Chit Groups |
| Browse → Know More | — | Group Details (4 tabs) |
| Browse → Enroll Now | — | Enrollment Flow (4 steps) |
| My Chits → tap card | — | Group Member View |
| Bottom nav: Payments | — | Payments Home |
| Payments → Pay | — | Payment Flow (Summary → Method → Processing → Success/Failure) |
| Success → Download | — | Digital Receipt |
| Payments → History | — | Payment History |
| Payments → Payouts | — | Payout History |
| Bottom nav: Assistant | — | AI Assistant Home |
| Assistant → chat | — | AI Chat (with policy reference modal) |
| AI Chat → Support | — | Human Support |
| Bottom nav: Profile | — | Profile |
| Profile → Personal Info | — | Personal Information Edit |
| Profile → Bank Accounts | — | Bank Accounts |
| Profile → Security | — | Security Settings |
| Profile → Help | — | Help Centre |
| Profile → Language | — | Language & Preferences |

### Note on API connectivity

API calls fail with connection errors unless the backend is running. The UI renders correctly — loading states and empty states appear. To test with real data, start the backend first.

---

## Running everything together

Open **3 terminal windows**:

**Terminal 1 — Database:**
```bash
docker-compose up db
```

**Terminal 2 — Backend API:**
```bash
cd backend
.venv\Scripts\activate      # Windows
uvicorn app.main:app --reload --port 8000
```

**Terminal 3 — Admin Dashboard:**
```bash
cd admin
npm run dev
```

**Android Studio:** Run the app on the Pixel 6 emulator.

---

## Common issues

### `alembic upgrade head` fails with "relation already exists"

The database has tables from a previous run. Reset:
```bash
docker-compose down -v       # removes the pgdata volume entirely
docker-compose up -d db
alembic upgrade head
```

### Android build fails with "Cannot find font resource"

Inter font files are missing from `res/font/`. See Step 2 above. Quick workaround: comment out `FontFamily(...)` in `Type.kt` and replace with the system default.

### Admin dashboard blank page

Make sure Vite dev server is running (`npm run dev`) and you're visiting `http://localhost:5173` not `localhost:5174` or another port.

### OTP not received on phone

In `DEBUG=true`, the OTP is logged to the API terminal, not sent via SMS. Look for:
```
[DEV OTP] +91XXXXXXXXXX → 123456
```

### API returns 500 "no such table"

Migrations haven't run. Activate the virtualenv and run `alembic upgrade head` from the `backend/` directory.

### AI assistant returns "I don't know" for everything

No policy documents have been ingested into `policy_chunks`. Run the document ingestion script (see Step 5 in the backend section above). The assistant still works for account queries (outstanding balance, next installment) without policy documents.

### `npx tsc --noEmit` fails after pulling changes

Run `npm install` first — new dependencies may have been added. Then re-run the type check.

---

## Environment summary

| Service | URL | Default credentials |
|---|---|---|
| Backend API | http://localhost:8000 | — |
| API Swagger docs | http://localhost:8000/docs | — (DEBUG only) |
| Admin Dashboard | http://localhost:5173 | — |
| PostgreSQL | localhost:5432 | user: `chitsmart` / pass: `password` / db: `chitsmart_dev` |
| Android emulator | localhost:5554 | — |

---

## Scope reminders

The following are **intentionally not implemented** in this release:

| Feature | Status | Why |
|---|---|---|
| KYC document upload | Deferred | UIDAI API, PMLA, DPDPA compliance prerequisites not met |
| Aadhaar/PAN collection | Deferred | With KYC |
| Admin KYC review | Deferred | With KYC |
| Auction scheduling | Deferred | WebSocket real-time infrastructure not built |
| Bid submission | Deferred | With auctions |
| Payout approval workflow | Deferred | With auctions |

Database tables for all deferred features exist (schema created in migration `001_initial_schema`) but have no API endpoints registered. See `docs/scope-decisions.md` for full rationale.
