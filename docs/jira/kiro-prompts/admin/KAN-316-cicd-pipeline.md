# Kiro Prompt — KAN-316: CI/CD Pipeline Setup

**Ticket:** KAN-316  
**Epic:** KAN-252 — Infrastructure  
**Priority:** High  
**File:** `.github/workflows/ci.yml`

---

## Task

Create a GitHub Actions CI pipeline that runs on every pull request targeting `main`.

## Pipeline jobs

### Job 1: backend-ci
```yaml
backend-ci:
  runs-on: ubuntu-latest
  services:
    postgres:
      image: pgvector/pgvector:pg16
      env:
        POSTGRES_USER: chitsmart
        POSTGRES_PASSWORD: password
        POSTGRES_DB: chitsmart_test
      ports: ["5432:5432"]
      options: --health-cmd pg_isready --health-interval 5s --health-retries 5
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-python@v5
      with: { python-version: "3.12" }
    - run: pip install -r backend/requirements.txt
    - run: flake8 backend/app --max-line-length=120
    - run: alembic upgrade head
      env: { DATABASE_URL: "postgresql+asyncpg://chitsmart:password@localhost/chitsmart_test" }
    - run: pytest backend/tests -v --asyncio-mode=auto
      env: { DATABASE_URL: "...", SECRET_KEY: "test-secret", DEBUG: "true" }
```

### Job 2: admin-ci
```yaml
admin-ci:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-node@v4
      with: { node-version: "20" }
    - run: npm ci
      working-directory: admin
    - run: npm run lint
      working-directory: admin
    - run: npm run type-check
      working-directory: admin
    - run: npm run test
      working-directory: admin
    - run: npm run build
      working-directory: admin
```

### Job 3: android-ci
```yaml
android-ci:
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with: { java-version: "17", distribution: "temurin" }
    - run: ./gradlew lintDebug
      working-directory: app
    - run: ./gradlew testDebugUnitTest
      working-directory: app
    - run: ./gradlew assembleDebug
      working-directory: app
```

## Acceptance criteria
- [ ] All 3 jobs run on pull requests to main
- [ ] Backend: linting, migrations, and tests all pass
- [ ] Admin: ESLint, TypeScript check, tests, and build pass
- [ ] Android: lint, unit tests, and debug build pass
- [ ] Pipeline fails on any test failure or lint error
