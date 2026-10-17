# ChitSmart — Build Variants

This document describes every supported build configuration across the three application
layers (Android app, admin dashboard, backend API).

**Last updated:** Added `local` / `cloud` product flavors to Android and `ENV=local|cloud` variant system to the Python backend.

---

## Environment overview

| Environment | Purpose | Who uses it |
|---|---|---|
| **Local dev** | Fast iteration — hot reload, no Docker for app code | Individual developers |
| **Local Docker** | Full stack in containers — mirrors production networking | Integration testing, QA |
| **Staging** | Cloud-hosted pre-production — real infrastructure, test data | QA, stakeholder review |
| **Production** | Live customer traffic | Operations |

---

## Android app

### Product flavor system

The app uses a single flavor dimension called `environment` with two flavors:

| Flavor | Application ID | App name on device | `BUILD_VARIANT` | `IS_LOGGING_ENABLED` |
|---|---|---|---|---|
| `local` | `com.chitsmart.local` | ChitSmart Dev | `"local"` | `true` |
| `cloud` | `com.chitsmart` | ChitSmart | `"cloud"` | `false` (release) / `true` (debug) |

Combined with the two standard build types (`debug` / `release`), this produces four variants:

| Variant | Gradle task | Output | Environment |
|---|---|---|---|
| `localDebug` | `assembleLocalDebug` | `.apk` | Local emulator / developer machine |
| `localRelease` | `assembleLocalRelease` | `.apk` | Local device, release-signed |
| `cloudDebug` | `assembleCloudDebug` | `.apk` | Cloud staging, debug |
| `cloudRelease` | `assembleCloudRelease` / `bundleCloudRelease` | `.apk` / `.aab` | Cloud production |

### API URL configuration

URLs are read from `local.properties` — never hard-coded in `build.gradle.kts` or committed to git.

```properties
# local.properties
local.api.base.url=http://10.0.2.2:8000/api/v1/   # emulator → host loopback
cloud.api.base.url=https://api.chitsmart.app/api/v1/

# Physical device on same Wi-Fi:
# local.api.base.url=http://192.168.1.x:8000/api/v1/
```

`10.0.2.2` is the Android emulator's built-in alias for the host machine's `localhost`.

For CI / release builds, inject via Gradle project properties:

```bash
./gradlew assembleCloudRelease \
  -Pcloud.api.base.url=https://api.chitsmart.app/api/v1/
```

### BuildConfig fields injected per variant

| Field | Type | local value | cloud value |
|---|---|---|---|
| `API_BASE_URL` | `String` | `local.api.base.url` from `local.properties` | `cloud.api.base.url` from `local.properties` |
| `BUILD_VARIANT` | `String` | `"local"` | `"cloud"` |
| `IS_LOGGING_ENABLED` | `Boolean` | `true` | `false` (release) / `true` (debug override) |
| `DEBUG` | `Boolean` | `true` (debug) / `false` (release) | same |

### AppConfig — runtime access

`AppConfig.kt` (`utils/AppConfig.kt`) is the single place to read these at runtime:

```kotlin
AppConfig.apiBaseUrl                  // String — base URL for Retrofit
AppConfig.buildVariant                // "local" | "cloud"
AppConfig.isLoggingEnabled            // Boolean — controls OkHttp HttpLoggingInterceptor
AppConfig.isLocal / AppConfig.isCloud // Boolean convenience helpers
AppConfig.connectTimeoutMs            // 10_000 local / 30_000 cloud
AppConfig.readTimeoutMs               // 30_000 local / 60_000 cloud
AppConfig.retryOnConnectionFailure    // false local / true cloud
```

`NetworkModule.kt` reads `AppConfig` for timeouts, retry policy, and logging level — no manual changes needed when switching variants.

### Source sets

Flavor-specific code lives in these directories (created, empty, ready to use):

```
app/src/local/java/com/chitsmart/   — local-flavor-only code
app/src/local/res/values/           — local-flavor-only resources
app/src/cloud/java/com/chitsmart/   — cloud-flavor-only code
app/src/cloud/res/values/           — cloud-flavor-only resources
```

AGP also automatically merges combined variant source sets — just create the directory:
```
app/src/localDebug/java/…    — local + debug only
app/src/cloudRelease/java/…  — cloud + release only
```

### Signing

| Variant | Signing |
|---|---|
| `*Debug` | Android Studio auto-generated debug keystore |
| `localRelease` | Same debug keystore acceptable for local testing |
| `cloudRelease` | Production keystore (stored as `KEYSTORE_BASE64` GitHub secret) |

Keystore secrets required in GitHub Actions for `cloudRelease`:

```
KEYSTORE_BASE64      base64-encoded .jks file
KEYSTORE_PASSWORD    keystore password
KEY_ALIAS            key alias
KEY_PASSWORD         key password
```

---

## Backend API (Python / FastAPI)

### Variant system

The backend variant is controlled by the `ENV` environment variable:

| `ENV` | `DEBUG` default | Workers | Swagger `/docs` | `env_file` loaded |
|---|---|---|---|---|
| `local` (default) | `true` | 1 (hot-reload) | ✅ | `.env` + `.env.local` (`.env.local` wins) |
| `cloud` | `false` | `WEB_CONCURRENCY` (≥2) | ❌ | none — all secrets from environment |

`DEBUG` can be overridden explicitly (`DEBUG=false ENV=local`) but is auto-derived when not set.

### config.py variant fields

`app/core/config.py` exposes these runtime helpers:

```python
settings.ENV              # "local" | "cloud"
settings.is_local         # bool
settings.is_cloud         # bool
settings.cors_origins     # List[str] — parsed from ALLOWED_ORIGINS
settings.DEBUG            # auto True for local, False for cloud
```

### Environment files

| File | Purpose | Committed |
|---|---|---|
| `backend/.env.local.example` | Template for local dev | ✅ Yes |
| `backend/.env.local` | Actual local secrets | ❌ No (.gitignore) |
| `backend/.env.cloud.example` | Reference doc for cloud env vars | ✅ Yes |
| `backend/.env` | Legacy fallback (still loaded if present) | ❌ No (.gitignore) |

New developers should copy `.env.local.example → .env.local` and fill in their keys.  
Cloud deployments set all variables via the cloud provider's secret manager — no file on disk.

### Dockerfile stages

`backend/Dockerfile` has three stages:

| Stage | `FROM` | Used by | Source | Reload |
|---|---|---|---|---|
| `base` | `python:3.12-slim` | Both | — | — |
| `dev` | `base` | `docker-compose.yml` | Volume-mounted | ✅ `--reload` |
| `prod` | `base` | `docker-compose.cloud.yml` | Baked in at build time | ❌ multi-worker |

The `prod` stage runs `alembic upgrade head` before starting uvicorn, so migrations are always applied on deployment.

### Docker Compose files

| File | Stage | DB | Use for |
|---|---|---|---|
| `docker-compose.yml` | `dev` | Local container | Local development |
| `docker-compose.yml` + `docker-compose.cloud.yml` | `prod` | External managed DB | Staging / production |

```bash
# Local development (hot-reload, local DB)
docker compose up --build

# Cloud (prod stage, external DB, secrets from env)
docker compose -f docker-compose.yml -f docker-compose.cloud.yml up -d
```

### Local variant — running without Docker

```bash
cd backend
python -m venv .venv
.venv\Scripts\activate          # Windows
source .venv/bin/activate       # macOS/Linux
pip install -r requirements.txt
cp .env.local.example .env.local   # fill in keys
alembic upgrade head
ENV=local uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### Cloud variant — minimum required environment variables

```env
ENV=cloud
DATABASE_URL=postgresql+asyncpg://USER:PASSWORD@HOST:5432/chitsmart_prod
SECRET_KEY=<64-char random hex>
FIREBASE_SERVICE_ACCOUNT_JSON={"type":"service_account",...}   # inline JSON — no file
OPENAI_API_KEY=sk-...
PAYMENT_KEY_ID=rzp_live_...
PAYMENT_KEY_SECRET=...
ALLOWED_ORIGINS=https://admin.yourdomain.com
STORAGE_BACKEND=s3
AWS_BUCKET_NAME=chitsmart-uploads
AWS_REGION=ap-south-1
WEB_CONCURRENCY=4
```

### Build variant matrix

| Variant | How to run | DB | Hot reload | Workers |
|---|---|---|---|---|
| Local dev | `ENV=local uvicorn --reload` | Docker `db` container | ✅ | 1 |
| Local Docker | `docker compose up` | Docker `db` container | ✅ (bind mount) | 1 |
| CI test | GitHub Actions + Postgres service | Ephemeral | ❌ | 1 |
| Cloud staging | `docker compose -f … -f …cloud.yml` | Managed PostgreSQL | ❌ | `WEB_CONCURRENCY` |
| Cloud production | same as staging | Managed PostgreSQL | ❌ | `WEB_CONCURRENCY` |

---

## Admin dashboard (React / Vite)

### Local development (hot reload)

```bash
cd admin
npm install
npm run dev
```

Vite dev server: `http://localhost:5173`

Environment file: `admin/.env.local` (gitignored)

```env
VITE_API_BASE_URL=http://localhost:8000/api/v1
```

### Staging / production build (CI)

`VITE_API_BASE_URL` is baked into the static bundle at build time — it is not read at runtime.

```bash
# Staging
VITE_API_BASE_URL=https://api.staging.chitsmart.in/api/v1 npm run build

# Production
VITE_API_BASE_URL=https://api.chitsmart.in/api/v1 npm run build
```

Output: `admin/dist/` — serve via Vercel, Netlify, S3 + CloudFront, or Nginx.

### Build variant matrix

| Variant | Command | `VITE_API_BASE_URL` | Hosting |
|---|---|---|---|
| Local dev | `npm run dev` | `http://localhost:8000` | Vite dev server |
| Local Docker | `npm run build` (inside Docker) | `http://api:8000` | Nginx container |
| Staging | `npm run build` (CI) | `https://api.staging.chitsmart.in` | Vercel / Netlify |
| Production | `npm run build` (CI) | `https://api.chitsmart.in` | Vercel / Netlify |

---

## Local Docker — full stack

```bash
# DB only (recommended for active development)
docker compose up -d db

# DB + API — dev stage, hot-reload
docker compose up -d

# DB + API — with forced rebuild
docker compose up --build -d

# Cloud variant — prod stage, baked source
docker compose -f docker-compose.yml -f docker-compose.cloud.yml up -d

# Logs
docker compose logs -f api
docker compose logs -f db

# Stop all
docker compose down

# Stop and delete all data
docker compose down -v
```

### Health checks after `docker compose up -d`

```powershell
docker exec chitsmart_db pg_isready -U chitsmart
Invoke-RestMethod http://localhost:8000/health
# {"status":"ok","version":"0.1.0"}
```

### Run migrations in Docker

```bash
# Inside the running container
docker exec chitsmart_api alembic upgrade head

# Or from host against the Docker DB
cd backend
$env:DATABASE_URL = "postgresql+asyncpg://chitsmart:password@localhost:5432/chitsmart_dev"
alembic upgrade head
```

---

## Cloud deployment (staging and production)

### Backend deployment options

| Option | When to use |
|---|---|
| **Railway / Render** | Simple deployments, no Kubernetes |
| **Google Cloud Run** | Serverless containers, auto-scaling to zero |
| **AWS ECS Fargate** | Full AWS ecosystem |
| **VM + Docker Compose** | Full control, predictable cost |

Minimum resources: 0.5 vCPU / 512 MB (1 vCPU / 1 GB recommended).  
pgvector extension is required — verify your managed PostgreSQL provider supports it.

### Android distribution

| Channel | When |
|---|---|
| Google Play internal track | Every `main` push (CI produces `.aab`) |
| Google Play closed testing | Pre-release QA |
| Google Play production | After sign-off |
| Firebase App Distribution | Internal testers outside Play |

### Required GitHub Actions secrets

#### Repository-level

| Secret | Description |
|---|---|
| `KEYSTORE_BASE64` | Base64 Android signing keystore |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |
| `GOOGLE_SERVICES_JSON_BASE64` | Base64 `google-services.json` for Firebase |

#### Environment: `staging`

| Secret | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://api.staging.chitsmart.in/api/v1` |
| `cloud.api.base.url` | `https://api.staging.chitsmart.in/api/v1/` |
| `DEPLOY_HOST` | Staging server hostname |
| `DEPLOY_USER` | SSH user |
| `DEPLOY_PATH` | Remote path for admin dist |
| `SSH_PRIVATE_KEY` | SSH private key |

#### Environment: `production`

| Secret | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://api.chitsmart.in/api/v1` |
| `cloud.api.base.url` | `https://api.chitsmart.in/api/v1/` |
| `DEPLOY_HOST` | Production server hostname |
| `DEPLOY_USER` | SSH user |
| `DEPLOY_PATH` | Remote path |
| `SSH_PRIVATE_KEY` | SSH private key |

---

## Build troubleshooting

### Android: build variant not visible in Studio

**Build → Select Build Variant** → if only `debug`/`release` appear, run **File → Sync Project with Gradle Files**.

### Android: "API_BASE_URL not found in BuildConfig"

Ensure `buildFeatures { buildConfig = true }` is set in `app/build.gradle.kts` (it is by default). Then clean and rebuild.

### Android: `local.properties` keys not picked up

Verify the key names exactly match what `build.gradle.kts` reads:
```properties
local.api.base.url=...
cloud.api.base.url=...
```

### Backend: `/docs` not accessible in cloud variant

Swagger is intentionally disabled when `ENV=cloud`. Switch to `ENV=local` or set `DEBUG=true` temporarily.

### Backend: `env_file .env.local: file not found`

Create the file: `cp backend/.env.local.example backend/.env.local` and fill in at minimum `SECRET_KEY` and `DATABASE_URL`. The `env_file` list silently skips missing files, so this usually only shows up in strict Docker runs.

### "pgvector extension not found"

The `docker-compose.yml` uses `pgvector/pgvector:pg16` which includes the extension. If using a plain `postgres:16` image:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### "Cannot connect to api:8000 from admin container"

When both are in Docker Compose the API hostname is `api` (the service name), not `localhost`:
```env
VITE_API_BASE_URL=http://api:8000/api/v1
```

### "Alembic target database is not up to date"

```bash
alembic current
alembic upgrade head
```

---

## Related documents

| Document | Content |
|---|---|
| `docs/deployment/local-setup.md` | First-time developer setup walkthrough |
| `docs/deployment/cicd.md` | GitHub Actions workflow reference |
| `docs/deployment/check-before-push.md` | Pre-push quality gate guide |
| `docker-compose.yml` | Local Docker Compose (dev stage) |
| `docker-compose.cloud.yml` | Cloud override (prod stage) |
| `backend/Dockerfile` | Multi-stage Dockerfile (base / dev / prod) |
| `backend/.env.local.example` | Local variant env template |
| `backend/.env.cloud.example` | Cloud variant env reference |
| `app/build.gradle.kts` | Android product flavors + BuildConfig fields |
| `app/src/main/java/com/chitsmart/utils/AppConfig.kt` | Runtime variant config singleton |
