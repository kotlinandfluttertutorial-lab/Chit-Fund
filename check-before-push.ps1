# ============================================================
#  ChitSmart - Pre-Push Quality Gate (PowerShell)
#  Run: .\check-before-push.ps1
# ============================================================

$ErrorActionPreference = "Continue"
$REPO_ROOT = $PSScriptRoot
$script:FAILED = $false
$script:ERRORS = [System.Collections.Generic.List[string]]::new()

function Pass([string]$msg)   { Write-Host "  [PASS] $msg" -ForegroundColor Green }
function Fail([string]$msg)   { Write-Host "  [FAIL] $msg" -ForegroundColor Red; $script:FAILED = $true; $script:ERRORS.Add($msg) }
function Warn([string]$msg)   { Write-Host "  [WARN] $msg" -ForegroundColor Yellow }
function Info([string]$msg)   { Write-Host "  $msg" }
function Header([string]$msg) {
    Write-Host ""
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host "  $msg"                                    -ForegroundColor Cyan
    Write-Host "==========================================" -ForegroundColor Cyan
}

function Invoke-Check([string]$Label, [scriptblock]$Cmd, [string]$FixHint) {
    Info "Running $Label..."
    $output = & $Cmd 2>&1
    if ($LASTEXITCODE -ne 0) {
        $output | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }
        Fail "$Label  |  fix: $FixHint"
        return $false
    }
    Pass $Label
    return $true
}

# ----------------------------------------------------------
# 0. Git - uncommitted changes warning
# ----------------------------------------------------------
Header "Git Status"
$dirty = git -C $REPO_ROOT diff --name-only HEAD 2>$null
if ($dirty) {
    Warn "Uncommitted changes - these will NOT be included in the push:"
    $dirty | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkYellow }
} else {
    Pass "Working tree is clean"
}

# ----------------------------------------------------------
# 1. Admin Dashboard - React / TypeScript / Vite
# ----------------------------------------------------------
Header "Admin Dashboard  (React + TypeScript + Vite)"

$adminDir = Join-Path $REPO_ROOT "admin"
Push-Location $adminDir

if (-not (Test-Path "node_modules")) {
    Info "node_modules not found - running npm ci..."
    npm ci --silent
    if ($LASTEXITCODE -ne 0) {
        Fail "npm ci failed - fix dependency installation first"
    } else {
        Pass "npm ci"
    }
} else {
    Pass "node_modules present"
}

Invoke-Check "ESLint"       { npm run lint       2>&1 } "cd admin; npm run lint"
Invoke-Check "TypeScript"   { npm run type-check 2>&1 } "cd admin; npm run type-check"
Invoke-Check "Vitest tests" { npm test           2>&1 } "cd admin; npm test"
Invoke-Check "Vite build"   { npm run build      2>&1 } "cd admin; npm run build"

Pop-Location

# ----------------------------------------------------------
# 2. Backend - Python / FastAPI
# ----------------------------------------------------------
Header "Backend  (Python / FastAPI)"

$backendDir = Join-Path $REPO_ROOT "backend"
Push-Location $backendDir

$pyCmd = if (Get-Command py -ErrorAction SilentlyContinue) { "py" }
         elseif (Get-Command python -ErrorAction SilentlyContinue) { "python" }
         else { $null }

if ($pyCmd) {
    # Check ruff is importable (py -m ruff works even without ruff on PATH)
    $ruffCheck = & $pyCmd -m ruff --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Invoke-Check "Ruff lint"   { & $pyCmd -m ruff check .          2>&1 } "cd backend; $pyCmd -m ruff check . --fix"
        Invoke-Check "Ruff format" { & $pyCmd -m ruff format --check .  2>&1 } "cd backend; $pyCmd -m ruff format ."
    } else {
        Warn "ruff not installed - run: $pyCmd -m pip install ruff"
    }

    $mypyCheck = & $pyCmd -m mypy --version 2>$null
    if ($LASTEXITCODE -eq 0) {
        Invoke-Check "Mypy type check" { & $pyCmd -m mypy app --ignore-missing-imports --no-error-summary 2>&1 } "cd backend; $pyCmd -m mypy app --ignore-missing-imports"
    } else {
        Warn "mypy not installed - run: $pyCmd -m pip install mypy"
    }
} else {
    Warn "Python not found - skipping backend lint checks"
    Warn "Install Python from https://python.org and run: py -m pip install ruff mypy"
}

if ($env:DATABASE_URL) {
    if ($pyCmd) {
        Invoke-Check "pytest" { & $pyCmd -m pytest tests/ -v --asyncio-mode=auto -x 2>&1 } "cd backend; $pyCmd -m pytest tests/ -v --asyncio-mode=auto -x"
    }
}

if (-not $env:DATABASE_URL) {
    Warn "DATABASE_URL not set - skipping pytest (requires PostgreSQL)"
    Warn 'Set it with: $env:DATABASE_URL = "postgresql+asyncpg://user:pass@localhost:5432/chitsmart_dev"'
}

Pop-Location

# ----------------------------------------------------------
# 3. Android - Kotlin / Jetpack Compose
# ----------------------------------------------------------
Header "Android  (Kotlin / Jetpack Compose)"

$gradlew = Join-Path $REPO_ROOT "gradlew.bat"
if (Test-Path $gradlew) {
    Invoke-Check "Android Lint"       { & $gradlew lint              --quiet 2>&1 } ".\gradlew lint"
    Invoke-Check "Android unit tests" { & $gradlew testDebugUnitTest --quiet 2>&1 } ".\gradlew testDebugUnitTest"
} else {
    Warn "gradlew.bat not found at repo root - skipping Android checks"
    Warn "These run automatically in GitHub Actions (android.yml)"
    Warn "Run manually once gradlew exists: .\gradlew lint testDebugUnitTest"
}

# ----------------------------------------------------------
# 4. Security - sensitive file check
# ----------------------------------------------------------
Header "Security - Sensitive File Check"

$sensitivePatterns = @("*.env", "*.pem", "*.key", "*.p12", "*.jks", "keystore.jks")
$sensitiveFound = $false

foreach ($pattern in $sensitivePatterns) {
    $tracked = git -C $REPO_ROOT ls-files $pattern 2>$null
    if ($tracked) {
        Fail "Sensitive file tracked by git: $tracked  |  fix: git rm --cached $tracked"
        $sensitiveFound = $true
    }
}

$envTracked = git -C $REPO_ROOT ls-files 2>$null | Select-String -Pattern "^\.env$|^backend/\.env$|^admin/\.env$"
if ($envTracked) {
    Fail ".env file committed to git  |  fix: git rm --cached .env"
} else {
    Pass "No .env files committed"
}

if (-not $sensitiveFound) {
    Pass "No sensitive files tracked by git"
}

# ----------------------------------------------------------
# 5. npm audit - admin (high/critical only)
# ----------------------------------------------------------
Header "Dependency Audit  (npm)"

$adminNodeModules = Join-Path $REPO_ROOT "admin\node_modules"
if (Test-Path $adminNodeModules) {
    Push-Location (Join-Path $REPO_ROOT "admin")
    Info "Running npm audit (high/critical only)..."
    $auditOut = npm audit --audit-level=high 2>&1
    if ($LASTEXITCODE -ne 0) {
        $auditOut | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }
        Fail "npm audit found critical/high vulnerabilities  |  fix: cd admin; npm audit"
    } else {
        Pass "npm audit - no critical/high vulnerabilities"
    }
    Pop-Location
} else {
    Warn "admin/node_modules not present - skipping audit"
}

# ----------------------------------------------------------
# Final result
# ----------------------------------------------------------
Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan

if ($script:FAILED) {
    Write-Host "  FAILED - do not push yet" -ForegroundColor Red
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Failed checks:" -ForegroundColor Red
    foreach ($e in $script:ERRORS) {
        Write-Host "  * $e" -ForegroundColor Red
    }
    Write-Host ""
    Write-Host "Fix all failures above, then re-run:" -ForegroundColor Yellow
    Write-Host "  .\check-before-push.ps1"            -ForegroundColor Yellow
    Write-Host ""
    exit 1
} else {
    Write-Host "  ALL CHECKS PASSED - safe to push" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Run: git push" -ForegroundColor Green
    Write-Host ""
    exit 0
}
