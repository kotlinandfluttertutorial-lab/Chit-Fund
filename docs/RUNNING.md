# ChitSmart — How to Run the Platform

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

Open `.env` and set at minimum:

```env
DATABASE_URL=postgresql+asyncpg://chitsmart:password@localhost:5432/chitsmart_dev
SECRET_KEY=your-random-secret-key-here
DEBUG=true
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

Expected output:
```
INFO  [alembic.runtime.migration] Running upgrade  -> 001_initial_schema, Initial schema...
```

### Step 5 — Start the API server

```bash
uvicorn app.main:app --reload --port 8000
```

**API is running at:** http://localhost:8000

**Swagger docs:** http://localhost:8000/docs  
*(only available when `DEBUG=true`)*

### Verify it works

```bash
curl http://localhost:8000/health
# {"status":"ok","version":"0.1.0"}
```

### Test OTP flow (DEBUG mode — no real SMS)

```bash
# Send OTP (check terminal for the 6-digit code)
curl -X POST http://localhost:8000/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"mobile_number": "+919876543210"}'

# Look in the API terminal for:
# [DEV OTP] +919876543210 → 123456
```

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

### What you'll see

| URL | Page |
|---|---|
| http://localhost:5173/dashboard | Executive Dashboard — KPIs, collection chart, group table |
| http://localhost:5173/members | Member Search — searchable table with filters |
| http://localhost:5173/members/member-1 | Member Details — 4-tab view |
| http://localhost:5173/chit-groups | Chit Groups List |
| http://localhost:5173/payments | Payment Transactions |
| http://localhost:5173/ai-tools | AI Collection Summary + Natural-Language Reporting |

### Build for production

```bash
npm run build
# Output: admin/dist/
```

TypeScript check (should show 0 errors):
```bash
npm run type-check
```

---

## 3. Android App

### Prerequisites

1. Open **Android Studio**
2. Make sure you have:
   - **Android SDK** platform 35 installed
   - An **AVD (emulator)** created — recommended: Pixel 6 API 34 (1080×2400, 390dp width)

### Step 1 — Open the project

In Android Studio: **File → Open** → select the `Chit-Fund` folder (the repository root).

Android Studio will detect the `app/` module and sync Gradle automatically.

### Step 2 — Add Inter font files

The app uses Inter font. Add these files to `app/src/main/res/font/`:

1. Download Inter from https://fonts.google.com/specimen/Inter
2. Copy these files into `app/src/main/res/font/`:
   - `inter_regular.ttf`
   - `inter_medium.ttf`
   - `inter_semibold.ttf` (rename `Inter-SemiBold.ttf`)
   - `inter_bold.ttf`

> If you skip this step, Android Studio will show a compile error in `Type.kt`. You can temporarily change the font to the system default by removing the `FontFamily` references in `Type.kt`.

### Step 3 — Configure the API URL

The app points to `http://10.0.2.2:8000` by default (Android emulator loopback to host machine).

If using a physical device, update `BuildConfig.API_BASE_URL` in `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "API_BASE_URL", "\"http://YOUR_PC_IP:8000/api/v1/\"")
```

### Step 4 — Run on emulator

1. Select your AVD in the device dropdown (top toolbar)
2. Click ▶ **Run** (or `Shift+F10`)
3. The app will build and install — takes ~2 minutes on first build

### What you'll see

The app launches to the **Splash Screen** → **Onboarding** (first launch) → **Login**.

All 18 screens are navigable:

| Screen | How to reach |
|---|---|
| Splash → Onboarding | App launch (first time) |
| Login | After onboarding |
| OTP Verification | Enter mobile + Send OTP |
| Home Dashboard | After login |
| My Chits | Bottom nav tab |
| Browse Groups | My Chits → Browse |
| Group Details | Browse → Know More |
| Enrollment | Browse → Enroll Now |
| Active Memberships | My Chits tab |
| Group Member View | Active Memberships → tap a card |
| Payments Home | Bottom nav tab |
| Payment Flow (5 steps) | Payments Home → Pay |
| Payment History | Payments Home → View History |
| Payout History | Payments Home → View Payouts |
| AI Chat | Assistant tab |
| Profile | Profile tab |
| Notifications | Home → bell icon |
| Registration | Login → Register link |

> **Note:** API calls will fail with connection errors until the backend is running. The UI still renders — you'll see loading states and empty states instead of real data.

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

**Android Studio:** Run the app on the emulator.

---

## Common issues

### `alembic upgrade head` fails — "relation already exists"

The database already has tables. Reset:
```bash
docker-compose down -v       # removes the pgdata volume
docker-compose up -d db
alembic upgrade head
```

### Android build fails — "Cannot find font resource"

The Inter font files are missing from `res/font/`. See Step 2 above.

### Admin dashboard — blank page / routing errors

Make sure the Vite dev server is running (`npm run dev`) and you're visiting `http://localhost:5173` (not 5174 or another port).

### OTP not received on phone

In `DEBUG=true` mode, the OTP is logged to the API terminal console — no real SMS is sent. Look for the line:
```
[DEV OTP] +91XXXXXXXXXX → 123456
```

### API returns 500 — "no such table"

Migrations haven't run. Run `alembic upgrade head` from the `backend/` directory with the virtualenv activated.

---

## Environment summary

| Service | URL | Default credentials |
|---|---|---|
| Backend API | http://localhost:8000 | — |
| API Swagger | http://localhost:8000/docs | — |
| Admin Dashboard | http://localhost:5173 | — |
| PostgreSQL | localhost:5432 | user: `chitsmart` / pass: `password` / db: `chitsmart_dev` |
| Android emulator | localhost:5554 (AVD) | — |

---

## Scope reminders

The following features are **not implemented** in this release and have no working UI or API:

- **KYC** — Aadhaar/PAN collection, document upload, admin review
- **Auctions / Bidding** — Auction scheduling, bid submission, live monitoring
- **Payout approval** — Payout records are read-only; approval workflow not built

See `docs/scope-decisions.md` for full rationale.
