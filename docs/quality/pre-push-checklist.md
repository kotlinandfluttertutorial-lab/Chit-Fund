# ChitSmart — Pre-Push Checklist

**Last updated:** 2026-10-15

Use this checklist before pushing to a shared branch or opening a pull request.

---

## 1. Enable the pre-push hook (once per machine)

```sh
git config core.hooksPath .githooks
```

This registers `.githooks/pre-push` as the hook directory. It runs automatically on every `git push`.

---

## 2. Manual pre-push checks

Run these manually if you need to verify before the hook runs, or when investigating a failure.

### Admin dashboard

```sh
cd admin
npm run lint          # ESLint — must report 0 errors
npm run type-check    # TypeScript — must exit 0
npm test              # Vitest — all tests must pass
npm run build         # Vite production build — must succeed
npm run check         # Runs all four above in sequence
cd ..
```

### Backend API

```sh
cd backend
ruff check .          # Lint — must exit 0
ruff format --check . # Format check — must exit 0
pytest tests/ -v --asyncio-mode=auto   # Requires DATABASE_URL set
cd ..
```

### Android app

```sh
./gradlew lint                  # Android Lint — check app/build/reports/
./gradlew testDebugUnitTest     # Kotlin unit tests
```

> Android checks are advisory in the pre-push hook (Gradle is too slow for interactive use). They run in full in GitHub Actions CI.

---

## 3. GitHub Branch Protection Setup

Configure these settings in **GitHub → repository → Settings → Branches → Add branch protection rule** for `main` (and separately for `develop`).

### Required status checks

Under **Require status checks to pass before merging**, search for and add:

| Check name | Source workflow |
|---|---|
| `Admin — Lint, Type-Check, Test & Build` | `.github/workflows/ci.yml` |
| `Backend — Lint & Test` | `.github/workflows/ci.yml` |
| `Android — Lint & Unit Test` | `.github/workflows/ci.yml` |
| `Lint, Type Check & Test` | `.github/workflows/admin.yml` |
| `Production Build` | `.github/workflows/admin.yml` |
| `Lint & Type Check` | `.github/workflows/backend.yml` |
| `Android Lint` | `.github/workflows/android.yml` |
| `Unit Tests` | `.github/workflows/android.yml` |

> The individual workflow files (`admin.yml`, `backend.yml`, `android.yml`) provide granular required checks. The consolidated `ci.yml` adds path-filtered jobs under the `CI` workflow name.

### Additional recommended settings

- **Require branches to be up to date before merging** — prevents stale PRs from merging.
- **Require a pull request before merging** with **1 required approver** for `main`.
- **Do not allow bypassing the above settings** — applies to admins too on `main`.

> **Do not rely solely on `pr-gate.yml` / "All checks passed"** as a required status check. That job runs a no-op echo step and always passes immediately — it cannot wait on other workflows. Use the individual check names listed above instead.

---

## 4. How to enable 1-approver PR reviews

In **Settings → Branches → Branch protection rule** for `main`:

1. Enable **Require a pull request before merging**.
2. Set **Required number of approvals before merging** to `1`.
3. Enable **Dismiss stale pull request approvals when new commits are pushed**.

---

## 5. Common failure fixes

### ESLint errors (`npm run lint` fails)

```sh
cd admin
npm run lint          # read the error lines
# Fix the reported files manually, then:
npm run lint          # must exit 0 before re-pushing
```

### TypeScript errors (`npm run type-check` fails)

```sh
cd admin
npm run type-check    # reads tsconfig.json — strict mode is on
# Fix the type errors in the reported files
```

### Test failures (`npm test` fails)

```sh
cd admin
npm test -- --reporter=verbose    # see which test failed and why
# Fix the failing test or the application code
```

### Build failures (`npm run build` fails)

```sh
cd admin
npm run build 2>&1    # Vite + tsc — look for import errors or missing types
```

### Ruff lint failures (`ruff check .` fails)

```sh
cd backend
ruff check . --fix    # auto-fix safe issues (imports, formatting)
ruff check .          # verify no remaining errors
# Fix remaining issues manually
```

### Ruff format failures (`ruff format --check .` fails)

```sh
cd backend
ruff format .         # auto-format all files
git add -p            # review the formatting changes
```

### pytest failures

```sh
cd backend
pytest tests/ -v --asyncio-mode=auto -x   # -x stops at first failure
# Read the assertion error and fix the code or test
```

---

## 6. How to view GitHub Actions results

1. Go to the repository on GitHub.
2. Click **Actions** in the top navigation.
3. Select the workflow run from the list.
4. Expand the failing job and step to see the full log output.
5. Fix the reported issue locally, push the fix — CI re-runs automatically.

---

## 7. Bypass the pre-push hook (emergency only)

```sh
git push --no-verify
```

Use only when CI is broken by an infrastructure issue unrelated to code. CI checks still run on the pushed commit and will catch failures.

---

## 8. Disable the hook

```sh
git config --unset core.hooksPath
```

This removes the hook path setting from your local `.git/config`. The `.githooks/` directory is not deleted.
