# ChitSmart — Local Development Setup

Complete step-by-step setup for a developer joining the project.

**Updated:** Added `local`/`cloud` build variants to the backend (`ENV=local|cloud`) and Android product flavors (`localDebug` / `cloudRelease`).

---

## Quick start (5 minutes)

```bash
# 1. Clone the repository
git clone <repo-url>
cd Chit-Fund

# 2. Start the database
docker compose up -d db

# 3. Backend setup (local variant)
cd backend
python -m venv .venv
.venv\Scripts\activate           # Windows
# source .venv/bin/activate      # macOS/Linux
pip install -r requirements.txt
copy .env.local.example .env.local   # fill in SECRET_KEY and OPENAI_API_KEY
alembic upgrade head
uvicorn app.main:app --reload --port 8000

# 4. Admin dashboard setup (new terminal)
cd admin
npm install
npm run dev
```

API: http://localhost:8000 · Swagger docs: http://localhost:8000/docs (local variant only)  
Admin: http://localhost:5173

### Android Firebase configuration

The Android app uses Firebase Phone Auth. Download the configuration for the
Firebase Android app with package name `com.chitsmart` and save it as
`app/google-services.json`; this file is git-ignored and must not be committed.
CI lint, unit-test, and debug APK jobs pass `-PskipGoogleServices=true` because
those checks do not contact Firebase. Release builds require the real
configuration. The Android release workflow restores it from the
base64-encoded `GOOGLE_SERVICES_JSON_BASE64` repository secret.

**Note on build variants:** The Android app has two product flavors — `local`
(targets `local.api.base.url` from `local.properties`, app ID suffix `.local`) and
`cloud` (targets `cloud.api.base.url`, plain `com.chitsmart` ID). In Android Studio,
select the active variant via **Build → Select Build Variant** and pick `localDebug`
for day-to-day development.

---

## Environment variables reference

### backend/.env.local (local variant)

Copy from `.env.local.example`:

```bash
cp backend/.env.local.example backend/.env.local
```

```env
# Build variant — tells config.py which defaults to apply
ENV=local
APP_NAME=ChitSmart API
APP_VERSION=0.1.0
DEBUG=true

# Database — PostgreSQL via Docker Compose
DATABASE_URL=postgresql+asyncpg://chitsmart:password@localhost:5432/chitsmart_dev
DATABASE_POOL_SIZE=10
DATABASE_MAX_OVERFLOW=20

# Security — generate with: python -c "import secrets; print(secrets.token_hex(32))"
SECRET_KEY=CHANGE_ME_32_BYTE_HEX_STRING
ACCESS_TOKEN_EXPIRE_MINUTES=1440
REFRESH_TOKEN_EXPIRE_DAYS=30
ALGORITHM=HS256

# Firebase — path to local service account JSON file
FIREBASE_SERVICE_ACCOUNT_PATH=firebase-service-account.json

# OpenAI — required for AI assistant
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4o
OPENAI_EMBEDDING_MODEL=text-embedding-3-small

# Payment gateway (use test keys locally)
PAYMENT_KEY_ID=rzp_test_REPLACE_ME
PAYMENT_KEY_SECRET=REPLACE_ME

# CORS — comma-separated origins allowed
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000

# File storage
STORAGE_BACKEND=local
STORAGE_LOCAL_PATH=./uploads
```

> **Why `.env.local` instead of `.env`?**  
> `config.py` loads both files (`.env` first as legacy fallback, then `.env.local` which takes precedence).
> `.env.cloud.example` documents cloud variables — in cloud deployments all vars are set via the
> cloud provider's secret manager, no file needed.

### backend/.env.cloud.example (cloud variant reference)

This file is **documentation only** — do not use it as a runtime file. In cloud deployments, set all variables as environment variables in your cloud provider:

```env
ENV=cloud
DATABASE_URL=postgresql+asyncpg://USER:PASSWORD@HOST:5432/chitsmart_prod
SECRET_KEY=<64-char hex>
FIREBASE_SERVICE_ACCOUNT_JSON={"type":"service_account",...}  # inline JSON, no file
OPENAI_API_KEY=sk-...
PAYMENT_KEY_ID=rzp_live_...
ALLOWED_ORIGINS=https://admin.yourdomain.com
STORAGE_BACKEND=s3
AWS_BUCKET_NAME=chitsmart-uploads
WEB_CONCURRENCY=4
```

### admin/.env.local

```env
VITE_API_BASE_URL=http://localhost:8000/api/v1
```
VITE_API_BASE_URL=http://localhost:8000/api/v1
```

---

## Database management

```bash
# View current migration state
alembic current

# Apply all pending migrations
alembic upgrade head

# Create a new migration (after changing a model)
alembic revision --autogenerate -m "add_bank_accounts_verified_field"

# Roll back one migration (dev only)
alembic downgrade -1

# Reset everything (dev only — destroys all data)
docker-compose down -v
docker-compose up -d db
alembic upgrade head
```

---

## Useful dev commands

```bash
# Backend — run with auto-reload (local variant)
ENV=local uvicorn app.main:app --reload --port 8000

# Backend — run tests
pytest tests/ -v --asyncio-mode=auto

# Android — build local debug APK
.\gradlew assembleLocalDebug

# Android — build cloud release APK
.\gradlew assembleCloudRelease

# Admin — TypeScript check
npx tsc --noEmit

# Admin — production build
npm run build

# Admin — run tests
npm run test

# Docker — view logs (updated command)
docker compose logs -f db

# Docker — connect to PostgreSQL directly
docker exec -it chitsmart_db psql -U chitsmart -d chitsmart_dev
```

---

## Figma design file

URL: https://www.figma.com/design/lLqFOeduJnNpINYH7fQth7/Untitled  
File key: `lLqFOeduJnNpINYH7fQth7`

To use Figma MCP in Kiro:
1. Get a Figma Personal Access Token: Figma → Profile → Settings → Security → Personal Access Tokens
2. Add to `~/.kiro/settings/mcp.json`:
```json
"figma": {
  "url": "https://mcp.figma.com/mcp",
  "headers": { "X-Figma-Token": "YOUR_TOKEN" },
  "disabled": false
}
```
3. Restart Kiro or reconnect MCP from the command palette

---

## VS Code / Android Studio setup

### VS Code (recommended for backend + admin)

Install these extensions:
- Python (Microsoft)
- Pylance
- ESLint
- Prettier
- Tailwind CSS IntelliSense
- ES7+ React/Redux/React-Native snippets

### Android Studio (required for Android app)

1. Install Android Studio Hedgehog 2023.1+
2. Install Android SDK platform 35
3. Create AVD: Pixel 6, API 34, 1080×2400 px
4. Add Inter font files to `app/src/main/res/font/` (see RUNNING.md)

---

## Branching strategy

```
main           — production-ready code
dev            — integration branch
feature/*      — individual features (e.g. feature/kyc-upload)
fix/*          — bug fixes
```

All PRs target `dev`. `dev` → `main` only after:
- All tests pass
- Security checklist reviewed
- At least one approver

---

## First things to do after setup

1. Run `alembic upgrade head` and confirm it completes without errors
2. Visit http://localhost:8000/docs — confirm all endpoints are listed
3. Visit http://localhost:5173 — confirm the admin dashboard loads
4. Call `POST /api/v1/auth/send-otp` with a test mobile number — confirm OTP appears in the API terminal
5. Read `docs/scope-decisions.md` — understand what is deferred and why before adding any code
6. Read `docs/LEARNING.md` — understand the patterns before writing any code

---

## Before pushing code

Run the pre-push quality gate. It checks lint, types, tests, and secrets before your
changes reach GitHub Actions.

```powershell
# Windows
.\check-before-push.ps1
```

```bash
# macOS / Linux / WSL
bash check-before-push.sh
```

Full guide: `docs/deployment/check-before-push.md`

---

## Further reading

| Document | What it covers |
|---|---|
| `docs/deployment/check-before-push.md` | Pre-push script — all sections, fix shortcuts, skip rules |
| `docs/deployment/build-variants.md` | Local dev vs local Docker vs staging vs production builds |
| `docs/deployment/cicd.md` | GitHub Actions workflows — every job, trigger, secret, and cache |
