# ChitSmart — Pre-Push Quality Gate

`check-before-push.ps1` (Windows PowerShell) and `check-before-push.sh` (bash) run the
same five-section quality gate locally before you push to GitHub. Running this saves
CI minutes and catches errors before they become failed GitHub Actions runs.

---

## Quick start

```powershell
# Windows (PowerShell — run from repo root)
.\check-before-push.ps1
```

```bash
# macOS / Linux / WSL
bash check-before-push.sh
```

Exit code `0` = all checks passed, safe to push.  
Exit code `1` = one or more checks failed; do not push until fixed.

---

## What the script checks

### Section 0 — Git status

Warns you if the working tree has uncommitted changes that would not be included in the
push. Not a failure — just a reminder. Commit or stash before pushing if the changed
files are relevant to your PR.

---

### Section 1 — Admin dashboard (React + TypeScript + Vite)

Working directory: `admin/`

| Step | Command | Fails on |
|---|---|---|
| Dependencies | `npm ci` (skipped if `node_modules` present) | Install failure |
| ESLint | `npm run lint` | Any lint error |
| TypeScript | `npm run type-check` | Any type error |
| Unit tests | `npm test` | Any Vitest failure |
| Production build | `npm run build` | Vite build error |

Fix shortcuts:

```bash
cd admin
npm run lint          # see all lint errors
npm run type-check    # see all TS errors
npm test              # see test failures
npm run build         # see build errors
```

---

### Section 2 — Backend (Python / FastAPI)

Working directory: `backend/`

The script auto-detects Python in this order:
1. `backend/.venv/Scripts/python.exe` (preferred — project venv)
2. `py` (Windows Python launcher)
3. `python` (system Python)

| Step | Command | Fails on |
|---|---|---|
| Ruff lint | `py -m ruff check .` | Any lint violation |
| Ruff format | `py -m ruff format .` | Any formatting issue |
| Mypy | `python -m mypy app --ignore-missing-imports` | Any type error |
| pytest (no DB) | `pytest tests/ -m "not db"` | Any non-DB test failure |
| pytest (all) | `pytest tests/ -v --asyncio-mode=auto -x` | DB tests if `DATABASE_URL` is set |

DB-dependent tests (`-m db`) are skipped when `DATABASE_URL` is not set. To run the
full test suite locally:

```powershell
# PowerShell
$env:DATABASE_URL = "postgresql+asyncpg://chitsmart:password@localhost:5432/chitsmart_dev"
.\check-before-push.ps1
```

```bash
# bash
DATABASE_URL="postgresql+asyncpg://chitsmart:password@localhost:5432/chitsmart_dev" \
  bash check-before-push.sh
```

Fix shortcuts:

```bash
cd backend
py -m ruff check . --fix      # auto-fix most lint violations
py -m ruff format .            # auto-fix formatting
python -m mypy app --ignore-missing-imports   # type errors
pytest tests/ -v -m "not db"  # non-DB tests only
```

---

### Section 3 — Android (Kotlin / Jetpack Compose)

Working directory: repo root (requires `gradlew.bat` / `gradlew`)

| Step | Gradle task | Fails on |
|---|---|---|
| Compile | `:app:compileDebugKotlin` | Kotlin compilation error |
| Android Lint (debug) | `lintDebug` | Lint error or warning configured as error |
| Android Lint (all) | `lint` | Lint error |
| ktlint check | `ktlintCheck` | Style violation |
| ktlint format | `ktlintFormat` | Auto-applies format; fails if unformatted |
| Unit tests | `:app:testDebugUnitTest` | Any unit test failure |

If `gradlew.bat` is not found the section is skipped with a warning. Android checks also
run in `android.yml` on every push — this section is a local fast-feedback shortcut.

Fix shortcuts:

```powershell
# PowerShell
.\gradlew ktlintFormat          # auto-format
.\gradlew :app:compileDebugKotlin
.\gradlew :app:testDebugUnitTest
```

---

### Section 4 — Security: sensitive file check

Scans git-tracked files for patterns that should never be committed:

| Pattern | Why it fails |
|---|---|
| `*.env` | Contains secrets |
| `*.pem`, `*.key` | Private keys |
| `*.p12`, `*.jks`, `keystore.jks` | Android signing keystores |
| `backend/.env`, `admin/.env` | Environment-specific secrets |

If any match is found, the script prints the exact file and the `git rm --cached` command
to untrack it. Add the file to `.gitignore` immediately after untracking.

---

### Section 5 — npm audit (admin)

Runs `npm audit --audit-level=high` in `admin/`. Only critical and high vulnerabilities
are treated as failures. Moderate advisories in dev dependencies are expected during
active development and are logged as warnings.

Fix:

```bash
cd admin
npm audit              # see all advisories
npm audit fix          # auto-fix compatible vulnerabilities
npm audit fix --force  # force-fix (may break — review changes carefully)
```

---

## Common failure patterns and fixes

### "npm ci failed"

Usually means `package-lock.json` is out of sync with `package.json`.

```bash
cd admin
npm install   # regenerates lock file
git add package-lock.json
```

### "Ruff lint — N violations"

```bash
cd backend
py -m ruff check . --fix    # fixes ~80 % automatically
py -m ruff check .          # review remaining manual fixes
```

### "mypy type check"

Most mypy errors in this codebase come from missing stubs. Add `# type: ignore` only for
third-party imports with no stubs. Never suppress errors in application code.

### "DATABASE_URL not set — DB tests skipped"

This is a warning, not a failure. Run `docker-compose up -d db` to start PostgreSQL,
set `DATABASE_URL`, then re-run the script.

### "Sensitive file tracked"

```bash
git rm --cached path/to/file.env
echo "path/to/file.env" >> .gitignore
git add .gitignore
git commit -m "chore: untrack sensitive file"
```

### Android section skipped (no gradlew.bat)

The Android module requires Android Studio setup. On Windows, run:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

If `gradlew.bat` does not exist, generate it: open Android Studio → sync project with
Gradle files.

---

## Differences between the two scripts

| Feature | `.ps1` (PowerShell) | `.sh` (bash) |
|---|---|---|
| Platform | Windows native | macOS / Linux / WSL |
| Python detection | Checks `.venv`, then `py`, then `python` | Checks `ruff`/`mypy` in `$PATH` |
| Output colour | `Write-Host -ForegroundColor` | ANSI escape codes |
| Exit code | `exit 0` / `exit 1` | `exit 0` / `exit 1` |
| Gradle | `gradlew.bat` | `./gradlew` |
| Android steps | compile + lintDebug + lint + ktlintCheck + ktlintFormat + unit tests | lint + unit tests |

The PowerShell script runs two extra Android steps (ktlint check and ktlint format)
that the bash script skips. On CI these are covered by `android.yml`.

---

## Running in CI vs locally

The pre-push scripts are **local tools only** — they are not invoked by GitHub Actions.
CI uses dedicated workflow files that replicate the same checks in a clean environment:

| Local check | CI workflow | Job name |
|---|---|---|
| Admin lint + type + test + build | `admin.yml` | `quality` + `build` |
| Backend ruff + mypy + pytest | `backend.yml` | `lint` + `test` |
| Android lint + unit test | `android.yml` | `lint` + `unit-test` |
| Security scan | `ci.yml` | (add `npm audit` step if needed) |

See `docs/deployment/cicd.md` for the full CI/CD reference.

---

## Skipping individual sections (emergency only)

There is no `--skip` flag by design. All sections must pass before pushing. If a section
is consistently broken and blocking unrelated work, fix the broken section first or
open a tracked issue.

The only approved exception is the Android section when `gradlew.bat` is absent on a
machine that only works on backend or admin code. CI still runs Android checks on push.

---

## Pre-push git hook (optional automation)

To run the PowerShell script automatically on every `git push`:

```powershell
# Create .githooks/pre-push (already exists in this repo)
# Then configure git to use it:
git config core.hooksPath .githooks
```

The hook at `.githooks/pre-push` runs `check-before-push.ps1` on Windows or
`check-before-push.sh` on bash. If the script fails, the push is blocked.

To bypass the hook in a genuine emergency (not recommended):

```bash
git push --no-verify
```

---

## Updating the script

When you add a new linter, test runner, or build step to any layer:

1. Add the corresponding `Invoke-Check` block to `check-before-push.ps1`.
2. Add the equivalent block to `check-before-push.sh`.
3. Add a row to the tables in this document.
4. Update `docs/deployment/cicd.md` to add the same step to the CI workflow reference.
