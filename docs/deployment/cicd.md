# ChitSmart — CI/CD Reference

This document describes every GitHub Actions workflow in `.github/workflows/`, how they
relate to each other, what triggers them, what each job does, and how to maintain them.

---

## Workflow files overview

| File | Trigger | Purpose |
|---|---|---|
| `ci.yml` | Push + PR to `main`/`develop` | Consolidated quality gate (all layers, path-filtered) |
| `admin.yml` | Push + PR when `admin/**` changes | Admin dashboard lint, test, build, deploy |
| `backend.yml` | Push + PR when `backend/**` changes | Backend lint, test, Docker image build + push |
| `android.yml` | Push + PR when `app/**` changes | Android lint, unit test, debug APK, signed release AAB |
| `pr-gate.yml` | PR to `main`/`develop` | Aggregated required status check for branch protection |

All workflows run on `ubuntu-latest`. Android jobs use JDK 17 (Temurin). Backend jobs
use Python 3.12. Admin jobs use Node.js 20.

---

## `ci.yml` — Consolidated quality gate

**File:** `.github/workflows/ci.yml`  
**Triggers:** `push` and `pull_request` on `main` and `develop`

This is the primary single-file gate. It mirrors the per-layer workflows but runs all
three layers in one file using path filters so each job only runs when its layer changes.
Use the individual per-layer workflows (`admin.yml`, `backend.yml`, `android.yml`) for
more granular control and deploy steps.

### Jobs

#### `admin` — Admin: Lint, Type-Check, Test & Build

Runs when `admin/**` files change (or on any push event).

| Step | What it does |
|---|---|
| `actions/setup-node@v4` | Installs Node.js 20 with npm cache |
| `npm ci` | Clean install of exact dependency versions |
| `npm run lint` | ESLint — fails on any error |
| `npm run type-check` | `tsc --noEmit` — fails on any TS error |
| `npm test` | Vitest unit tests — fails on any test failure |
| `npm run build` | Vite production build with `VITE_API_BASE_URL` |
| `upload-artifact` | Uploads `admin/dist/` for 7 days (push events only) |

#### `backend` — Backend: Lint & Test

Runs when `backend/**` files change (or on any push event).

| Step | What it does |
|---|---|
| `actions/setup-python@v5` | Installs Python 3.12 with pip cache |
| `pip install -r requirements.txt ruff` | Installs dependencies |
| `ruff check .` | Lint — fails on any violation |
| `ruff format --check .` | Format check — fails if unformatted |
| `alembic upgrade head` | Applies migrations to ephemeral test DB |
| `pytest tests/ -v --asyncio-mode=auto` | All tests including DB tests |

Uses a PostgreSQL 16 service container (`postgres:16-alpine`) with health checks.

Environment variables injected:

```yaml
DATABASE_URL: postgresql+asyncpg://chitsmart:chitsmart@localhost:5432/chitsmart_test
SECRET_KEY: test-secret-key-not-for-production
OPENAI_API_KEY: sk-test-placeholder
DEBUG: "true"
```

#### `android` — Android: Lint & Unit Test

Runs when `app/**`, `*.gradle*`, `gradle/**`, or `gradlew*` files change.

| Step | What it does |
|---|---|
| `actions/setup-java@v4` | Installs JDK 17 (Temurin) |
| `actions/cache@v4` | Caches `~/.gradle/caches` and `~/.gradle/wrapper` |
| `./gradlew lint` | Android Lint — fails on configured error-level issues |
| `./gradlew testDebugUnitTest` | Kotlin unit tests |
| `upload-artifact` | Uploads lint HTML report for 7 days |

Gradle daemon is disabled in CI (`-Dorg.gradle.daemon=false`) to avoid daemon leaks.
Parallel execution and build caching are enabled.

---

## `admin.yml` — Admin dashboard CI

**File:** `.github/workflows/admin.yml`  
**Triggers:** Push + PR on `main`/`develop` when `admin/**` or the workflow file changes

### Jobs

#### `quality` — Lint, Type Check & Test

Runs on every push and PR.

```
npm ci → ESLint → TypeScript → Vitest
```

#### `build` — Production Build

Runs after `quality` passes (`needs: quality`).

```
npm ci → npm run build
```

`VITE_API_BASE_URL` is read from the `VITE_API_BASE_URL` repository secret at build
time. If the secret is not set, it defaults to `http://localhost:8000`.

Uploads `admin/dist/` as artifact `admin-dist-<sha>` (7-day retention).

#### `deploy` — Deploy Admin Dashboard

Runs only on `push` to `main` or `develop` after `build` passes.

Uses GitHub Environments:
- `main` → `production` environment
- `develop` → `staging` environment

The deploy step in the workflow is a **placeholder** using rsync over SSH. Replace it
with your actual hosting provider:

| Provider | Recommended action |
|---|---|
| Vercel | `amondnet/vercel-action@v25` |
| Netlify | `nwtgck/actions-netlify@v3` |
| AWS S3 + CloudFront | `aws-actions/configure-aws-credentials` + `aws s3 sync` |
| Nginx VM | rsync step (already in the file — fill in secrets) |

Required secrets for rsync deploy:

```
DEPLOY_HOST       server hostname
DEPLOY_USER       SSH user
DEPLOY_PATH       remote directory path
SSH_PRIVATE_KEY   private key (no passphrase)
```

---

## `backend.yml` — Backend CI

**File:** `.github/workflows/backend.yml`  
**Triggers:** Push + PR on `main`/`develop` when `backend/**` or the workflow file changes

### Jobs

#### `lint` — Lint & Type Check

```
pip install → ruff check → ruff format --check → mypy app
```

No database needed. Runs fast (~60 s).

#### `test` — Unit + Integration Tests

```
pip install → alembic upgrade head → pytest tests/ -v --asyncio-mode=auto
```

Requires PostgreSQL 16 service container. Runs all tests including DB integration tests.

#### `docker` — Build & Push Docker Image

Runs only on `push` to `main` or `develop` after both `lint` and `test` pass
(`needs: [lint, test]`).

```
docker/setup-buildx-action → docker/login-action (GHCR) → docker/build-push-action
```

**Image registry:** `ghcr.io/<github-owner>/chitsmart-backend`

**Tags:**

| Branch | Tags |
|---|---|
| `main` | `latest`, `<sha8>` |
| `develop` | `develop`, `develop-<sha8>` |

Pushes to GitHub Container Registry using `GITHUB_TOKEN` (no secret configuration
required — the token is automatically available to Actions).

**Build cache:** Uses GitHub Actions cache (`type=gha`) to speed up layer rebuilds.

To pull and run the image locally:

```bash
docker pull ghcr.io/<owner>/chitsmart-backend:latest
docker run -p 8000:8000 \
  -e DATABASE_URL="postgresql+asyncpg://..." \
  -e SECRET_KEY="..." \
  -e OPENAI_API_KEY="sk-..." \
  ghcr.io/<owner>/chitsmart-backend:latest
```

---

## `android.yml` — Android CI

**File:** `.github/workflows/android.yml`  
**Triggers:** Push + PR on `main`/`develop` when Android source or Gradle files change

### Jobs

#### `lint` — Android Lint

```
setup-java → cache Gradle → chmod +x gradlew → ./gradlew lint → upload lint report
```

Lint report uploaded as `lint-report-<sha>` (7-day retention).

#### `unit-test` — Unit Tests

```
setup-java → cache Gradle → ./gradlew testDebugUnitTest → upload test results
```

Test results uploaded as `unit-test-results-<sha>` (7-day retention).

#### `build-debug` — Build Debug APK

Runs after both `lint` and `unit-test` pass (`needs: [lint, unit-test]`). Runs on
every push and PR.

```
setup-java → cache Gradle → ./gradlew assembleDebug → upload APK
```

Debug APK uploaded as `chitsmart-debug-<sha>` (14-day retention). Useful for manual
testing without needing Android Studio.

#### `build-release` — Build Signed Release AAB

Runs only on `push` to `main` after lint and unit tests pass.

```
setup-java → cache Gradle → decode keystore → ./gradlew bundleRelease → upload AAB
```

Required secrets:

| Secret | Description |
|---|---|
| `KEYSTORE_BASE64` | `base64 < keystore.jks` — the signing keystore |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias (as configured in `build.gradle`) |
| `KEY_PASSWORD` | Key password |

Release AAB uploaded as `chitsmart-release-<sha>` (30-day retention).

To automate Google Play upload, uncomment the `r0adkll/upload-google-play` step and add:

```
PLAY_SERVICE_ACCOUNT_JSON   Google Play service account credentials JSON
```

---

## `pr-gate.yml` — Aggregated PR gate

**File:** `.github/workflows/pr-gate.yml`  
**Triggers:** PR to `main` or `develop`

This workflow exists so branch protection rules only need to require **one** status
check ("PR Gate / All checks passed") instead of every individual workflow job. The job
runs a summary step and always succeeds if the workflow is triggered.

> The actual gate is enforced by adding the individual workflow jobs as required status
> checks in GitHub branch protection settings **in addition to** the PR gate, or by
> relying on GitHub's "Require status checks to pass before merging" pointing to the
> individual `ci.yml` job names.

### Recommended branch protection settings for `main`

In **GitHub → Settings → Branches → main → Branch protection rule**:

- ✅ Require a pull request before merging
- ✅ Require approvals: 1
- ✅ Require status checks to pass before merging:
  - `Admin — Lint, Type-Check, Test & Build`
  - `Backend — Lint & Test`
  - `Android — Lint & Unit Test`
  - `PR Gate / All checks passed`
- ✅ Require branches to be up to date before merging
- ✅ Do not allow bypassing the above settings (for non-admin users)

---

## Job dependency and parallelism

```
PR pushed
    ├── ci.yml / admin job        (parallel)
    ├── ci.yml / backend job      (parallel)
    └── ci.yml / android job      (parallel)

Push to main/develop
    admin.yml:
        quality ──► build ──► deploy (staging or production)

    backend.yml:
        lint ──┐
               ├──► docker (GHCR push)
        test ──┘

    android.yml:
        lint ──┐
               ├──► build-debug    (all branches)
        unit ──┤
               └──► build-release  (main only)
```

---

## Caching strategy

| Cache | Key | Content | Hit rate |
|---|---|---|---|
| npm (admin) | `package-lock.json` hash | `~/.npm` | High — lock file rarely changes |
| pip (backend) | `requirements.txt` hash | pip download cache | High |
| Gradle (Android) | `*.gradle*` + wrapper properties hash | `~/.gradle/caches` + `~/.gradle/wrapper` | High |
| Docker layers | GitHub Actions cache (`type=gha`) | Docker build layers | Medium — varies by change |

Cache restore key fallbacks are configured on Gradle and pip so a partial cache hit
(same OS, different hash) is used rather than a full re-download.

---

## Secrets inventory

### Repository-level secrets

| Secret | Used by | Description |
|---|---|---|
| `KEYSTORE_BASE64` | `android.yml` | Base64-encoded `.jks` signing keystore |
| `KEYSTORE_PASSWORD` | `android.yml` | Keystore password |
| `KEY_ALIAS` | `android.yml` | Signing key alias |
| `KEY_PASSWORD` | `android.yml` | Key password |
| `GITHUB_TOKEN` | `backend.yml` | Auto-provided — GHCR login |

### Environment secrets — `staging`

| Secret | Used by | Description |
|---|---|---|
| `VITE_API_BASE_URL` | `admin.yml build` | Staging API URL |
| `DEPLOY_HOST` | `admin.yml deploy` | Staging server hostname |
| `DEPLOY_USER` | `admin.yml deploy` | SSH user |
| `DEPLOY_PATH` | `admin.yml deploy` | Remote path |
| `SSH_PRIVATE_KEY` | `admin.yml deploy` | SSH private key |

### Environment secrets — `production`

| Secret | Used by | Description |
|---|---|---|
| `VITE_API_BASE_URL` | `admin.yml build` | Production API URL |
| `DEPLOY_HOST` | `admin.yml deploy` | Production server hostname |
| `DEPLOY_USER` | `admin.yml deploy` | SSH user |
| `DEPLOY_PATH` | `admin.yml deploy` | Remote path |
| `SSH_PRIVATE_KEY` | `admin.yml deploy` | SSH private key |

---

## Adding a new CI check

### Adding a backend lint rule

1. Update `backend/pyproject.toml` or `ruff.toml` with the new rule.
2. Fix all violations: `cd backend && py -m ruff check . --fix`.
3. The `ruff check .` step in both `ci.yml` and `backend.yml` will enforce it on the
   next push automatically.
4. Add the rule to `docs/deployment/check-before-push.md` if developers need to know
   about it locally.

### Adding a new workflow job

1. Add the job to the relevant `.github/workflows/*.yml` file.
2. If the job must block PRs, add its name to the branch protection required status
   checks list on GitHub.
3. If it is a new step in an existing job, add the equivalent block to
   `check-before-push.ps1` and `check-before-push.sh`.
4. Update `docs/deployment/cicd.md` (this file) with the new job description.

---

## Debugging failing CI jobs

### View run logs

```
GitHub → Actions tab → select workflow run → select failing job → expand step
```

### Re-run a single failed job

```
GitHub → Actions → select run → Re-run failed jobs
```

### Download build artifacts

```
GitHub → Actions → select run → Artifacts section → download
```

### Common failure causes

| Symptom | Likely cause | Fix |
|---|---|---|
| `npm ci` fails | `package-lock.json` out of sync | Run `npm install` locally and commit lock file |
| `ruff check` fails | New lint violations introduced | `py -m ruff check . --fix` |
| `mypy` fails | Missing type annotations or stubs | Add annotations or `# type: ignore` with justification |
| `alembic upgrade head` fails | Migration conflict or missing migration | Generate missing migration: `alembic revision --autogenerate -m "description"` |
| `pytest` fails | Test failure or broken import | Run locally: `pytest tests/ -v --asyncio-mode=auto -x` |
| Gradle lint fails | New Android Lint issue | `.\gradlew lint` locally, read HTML report |
| Docker build fails | Dockerfile change or dependency issue | Build locally: `docker build backend/` |
| Keystore decode fails | `KEYSTORE_BASE64` secret empty or wrong | Re-encode: `base64 -i keystore.jks` and update the secret |

---

## Branching model and when jobs run

```
feature/* ──► develop ──► main
                │               │
                │               └── android release AAB
                │               └── backend Docker image (latest)
                │               └── admin deploy (production)
                └── backend Docker image (develop)
                └── admin deploy (staging)
```

| Branch | PR to `develop` | Push to `develop` | Push to `main` |
|---|---|---|---|
| ci.yml admin | ✅ lint/test/build | ✅ lint/test/build | ✅ lint/test/build |
| ci.yml backend | ✅ lint/test | ✅ lint/test | ✅ lint/test |
| ci.yml android | ✅ lint/unit | ✅ lint/unit | ✅ lint/unit |
| admin deploy | ✅ staging preview | ✅ staging | ✅ production |
| backend Docker | — | ✅ `develop` tag | ✅ `latest` tag |
| Android debug APK | ✅ | ✅ | ✅ |
| Android signed AAB | — | — | ✅ |

---

## Pre-push vs CI relationship

The pre-push scripts (`check-before-push.ps1` / `.sh`) and the GitHub Actions workflows
check the same things but serve different purposes:

| Aspect | Pre-push script | GitHub Actions |
|---|---|---|
| When it runs | Before `git push`, on the developer's machine | After push, in a clean cloud VM |
| Speed | Fast (uses local caches) | Slower (installs from scratch) |
| Database | Optional (skipped if `DATABASE_URL` not set) | Always available (service container) |
| Docker build | Not run | Run on `main`/`develop` push |
| Android keystore | Not run (no release signing locally) | Run on `main` push |
| Purpose | Catch obvious failures before wasting CI minutes | Authoritative pass/fail for branch protection |

The pre-push script is a local development aid. The GitHub Actions workflows are the
authoritative source of truth for whether code is releasable.

---

## Related documents

| Document | Content |
|---|---|
| `docs/deployment/local-setup.md` | Developer onboarding and local run commands |
| `docs/deployment/build-variants.md` | Build configurations for all environments |
| `docs/deployment/check-before-push.md` | Pre-push script detailed reference |
| `.github/workflows/ci.yml` | Consolidated CI workflow source |
| `.github/workflows/admin.yml` | Admin dashboard CI source |
| `.github/workflows/backend.yml` | Backend CI source |
| `.github/workflows/android.yml` | Android CI source |
| `.github/workflows/pr-gate.yml` | PR gate aggregator source |
