#!/usr/bin/env bash
# ============================================================
#  ChitSmart — Pre-Push Quality Gate
#  Run this manually before pushing:  bash check-before-push.sh
#
#  Checks:
#    admin/   → ESLint + TypeScript + Vitest + Vite build
#    backend/ → Ruff lint (pytest if DATABASE_URL is set)
#    app/     → reminder to run Gradle checks
# ============================================================
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FAILED=false
ERRORS=()

print_header() {
  echo ""
  echo -e "${BLUE}${BOLD}══════════════════════════════════════════${NC}"
  echo -e "${BLUE}${BOLD}  $1${NC}"
  echo -e "${BLUE}${BOLD}══════════════════════════════════════════${NC}"
}

pass() { echo -e "  ${GREEN}✓ $1${NC}"; }
fail() { echo -e "  ${RED}✗ $1${NC}"; FAILED=true; ERRORS+=("$1"); }
warn() { echo -e "  ${YELLOW}⚠ $1${NC}"; }
info() { echo -e "  $1"; }

# ─────────────────────────────────────────────────────────────
# 0. Uncommitted changes warning
# ─────────────────────────────────────────────────────────────
print_header "Git Status"
if ! git -C "$REPO_ROOT" diff --quiet HEAD 2>/dev/null; then
  warn "You have uncommitted changes. These will NOT be included in the push."
  git -C "$REPO_ROOT" diff --name-only HEAD | sed 's/^/    /'
else
  pass "Working tree is clean"
fi

# ─────────────────────────────────────────────────────────────
# 1. Admin Dashboard — React / TypeScript / Vitest
# ─────────────────────────────────────────────────────────────
print_header "Admin Dashboard (React + TypeScript + Vite)"

ADMIN_DIR="$REPO_ROOT/admin"

if [ ! -d "$ADMIN_DIR/node_modules" ]; then
  info "node_modules not found — running npm ci..."
  if ! (cd "$ADMIN_DIR" && npm ci --silent); then
    fail "npm ci failed — fix dependency installation first"
  else
    pass "npm ci"
  fi
else
  pass "node_modules present"
fi

info "Running ESLint..."
if (cd "$ADMIN_DIR" && npm run lint --silent 2>&1); then
  pass "ESLint"
else
  fail "ESLint — run: cd admin && npm run lint"
fi

info "Running TypeScript type check..."
if (cd "$ADMIN_DIR" && npm run type-check --silent 2>&1); then
  pass "TypeScript type-check"
else
  fail "TypeScript — run: cd admin && npm run type-check"
fi

info "Running Vitest unit tests..."
if (cd "$ADMIN_DIR" && npm test --silent 2>&1); then
  pass "Vitest tests"
else
  fail "Vitest — run: cd admin && npm test"
fi

info "Running production build..."
if (cd "$ADMIN_DIR" && npm run build --silent 2>&1); then
  pass "Vite production build"
else
  fail "Build failed — run: cd admin && npm run build"
fi

# ─────────────────────────────────────────────────────────────
# 2. Backend — Python / FastAPI / Ruff
# ─────────────────────────────────────────────────────────────
print_header "Backend (Python / FastAPI)"

BACKEND_DIR="$REPO_ROOT/backend"

if ! command -v ruff &>/dev/null; then
  warn "ruff not found — install with: pip install ruff"
  warn "Skipping backend lint"
else
  info "Running Ruff lint..."
  if (cd "$BACKEND_DIR" && ruff check . 2>&1); then
    pass "Ruff lint"
  else
    fail "Ruff lint — run: cd backend && ruff check . --fix  (auto-fixes 22/34 violations)"
  fi

  info "Running Ruff format check..."
  if (cd "$BACKEND_DIR" && ruff format --check . 2>&1); then
    pass "Ruff format"
  else
    fail "Ruff format — run: cd backend && ruff format ."
  fi
fi

if ! command -v mypy &>/dev/null; then
  warn "mypy not found — install with: pip install mypy"
else
  info "Running Mypy type check..."
  if (cd "$BACKEND_DIR" && mypy app --ignore-missing-imports --no-error-summary 2>&1); then
    pass "Mypy type check"
  else
    fail "Mypy — run: cd backend && mypy app --ignore-missing-imports"
  fi
fi

# pytest only if DATABASE_URL is set
if [ -n "${DATABASE_URL:-}" ]; then
  info "DATABASE_URL found — running pytest..."
  if (cd "$BACKEND_DIR" && pytest tests/ -v --asyncio-mode=auto -x 2>&1); then
    pass "pytest"
  else
    fail "pytest — run: cd backend && pytest tests/ -v --asyncio-mode=auto -x"
  fi
else
  warn "DATABASE_URL not set — skipping pytest (requires PostgreSQL)"
  warn "To run: export DATABASE_URL=postgresql+asyncpg://user:pass@localhost:5432/chitsmart_dev"
  warn "        cd backend && pytest tests/ -v --asyncio-mode=auto -x"
fi

# ─────────────────────────────────────────────────────────────
# 3. Android — Kotlin / Jetpack Compose
# ─────────────────────────────────────────────────────────────
print_header "Android (Kotlin / Jetpack Compose)"

GRADLEW="$REPO_ROOT/gradlew"

if [ ! -f "$GRADLEW" ]; then
  warn "gradlew not found at repo root — skipping Android checks"
  warn "Gradle checks run automatically in GitHub Actions CI (android.yml)"
else
  info "Running Android Lint..."
  if (cd "$REPO_ROOT" && ./gradlew lint --quiet 2>&1); then
    pass "Android Lint"
  else
    fail "Android Lint — run: ./gradlew lint"
  fi

  info "Running unit tests..."
  if (cd "$REPO_ROOT" && ./gradlew testDebugUnitTest --quiet 2>&1); then
    pass "Android unit tests"
  else
    fail "Android unit tests — run: ./gradlew testDebugUnitTest"
  fi
fi

# ─────────────────────────────────────────────────────────────
# 4. Security — sensitive file check
# ─────────────────────────────────────────────────────────────
print_header "Security — Sensitive File Check"

SENSITIVE_PATTERNS=(".env" "*.pem" "*.key" "*.p12" "*.jks" "keystore.jks")
SENSITIVE_FOUND=false

for pattern in "${SENSITIVE_PATTERNS[@]}"; do
  # Check if any such file is tracked by git
  if git -C "$REPO_ROOT" ls-files "$pattern" 2>/dev/null | grep -q .; then
    fail "Sensitive file tracked by git: $pattern"
    SENSITIVE_FOUND=true
  fi
done

# Check for .env files specifically
if git -C "$REPO_ROOT" ls-files | grep -E '^\.env$|^backend/\.env$|^admin/\.env$' | grep -q .; then
  fail ".env file is tracked by git — remove it: git rm --cached .env"
else
  pass "No .env files committed"
fi

if [ "$SENSITIVE_FOUND" = false ]; then
  pass "No sensitive files tracked by git"
fi

# ─────────────────────────────────────────────────────────────
# 5. npm dependency audit (admin)
# ─────────────────────────────────────────────────────────────
print_header "Dependency Audit"

if [ -d "$ADMIN_DIR/node_modules" ]; then
  info "Running npm audit (admin)..."
  # Only fail on critical/high — moderate advisories in dev deps are expected
  AUDIT_OUTPUT=$(cd "$ADMIN_DIR" && npm audit --audit-level=high 2>&1 || true)
  if echo "$AUDIT_OUTPUT" | grep -qE "critical|high"; then
    fail "npm audit found critical/high vulnerabilities:"
    echo "$AUDIT_OUTPUT" | grep -E "critical|high" | sed 's/^/    /'
  else
    pass "npm audit — no critical/high vulnerabilities"
  fi
fi

# ─────────────────────────────────────────────────────────────
# Final result
# ─────────────────────────────────────────────────────────────
echo ""
echo -e "${BLUE}${BOLD}══════════════════════════════════════════${NC}"

if [ "$FAILED" = true ]; then
  echo -e "${RED}${BOLD}  ✗ Quality gate FAILED — do not push yet${NC}"
  echo -e "${BLUE}${BOLD}══════════════════════════════════════════${NC}"
  echo ""
  echo -e "${RED}Failed checks:${NC}"
  for err in "${ERRORS[@]}"; do
    echo -e "  ${RED}•${NC} $err"
  done
  echo ""
  echo -e "${YELLOW}Fix all failures above, then re-run:${NC}"
  echo -e "  bash check-before-push.sh"
  echo ""
  exit 1
else
  echo -e "${GREEN}${BOLD}  ✓ All checks passed — safe to push${NC}"
  echo -e "${BLUE}${BOLD}══════════════════════════════════════════${NC}"
  echo ""
  echo -e "  Run:  git push"
  echo ""
  exit 0
fi
