# ChitSmart — Local Development Setup

Complete step-by-step setup for a developer joining the project.

---

## Quick start (5 minutes)

```bash
# 1. Clone the repository
git clone <repo-url>
cd Chit-Fund

# 2. Start the database
docker-compose up -d db

# 3. Backend setup
cd backend
python -m venv .venv
.venv\Scripts\activate          # Windows
# source .venv/bin/activate     # macOS/Linux
pip install -r requirements.txt
copy .env.example .env          # fill in SECRET_KEY and OPENAI_API_KEY
alembic upgrade head
uvicorn app.main:app --reload --port 8000

# 4. Admin dashboard setup (new terminal)
cd admin
npm install
npm run dev
```

API: http://localhost:8000  
Admin: http://localhost:5173

---

## Environment variables reference

### backend/.env

```env
# Application
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

# OTP
OTP_EXPIRE_MINUTES=10
OTP_LENGTH=6

# SMS — in DEBUG mode, OTP is logged to console instead of SMS
SMS_PROVIDER=twilio
SMS_API_KEY=
SMS_SENDER_ID=CHTSMT

# OpenAI — required for AI assistant
OPENAI_API_KEY=sk-...
OPENAI_MODEL=gpt-4o
OPENAI_EMBEDDING_MODEL=text-embedding-3-small

# Payment gateway — no third-party gateway is integrated in this release.
# PAYMENT_KEY_ID and PAYMENT_KEY_SECRET are reserved for a future integration.
# PAYMENT_KEY_ID=
# PAYMENT_KEY_SECRET=

# CORS — comma-separated origins allowed
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

### admin/.env.local

```env
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
# Backend — run with auto-reload
uvicorn app.main:app --reload --port 8000

# Backend — run tests
pytest tests/ -v --asyncio-mode=auto

# Admin — TypeScript check
npx tsc --noEmit

# Admin — production build
npm run build

# Admin — run tests
npm run test

# Docker — view logs
docker-compose logs -f db

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
