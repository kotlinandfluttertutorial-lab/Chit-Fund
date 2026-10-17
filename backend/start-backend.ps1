# =============================================================================
# start-backend.ps1 — ChitSmart backend local dev launcher
#
# What this script does:
#   1. Ensures the chitsmart-db Docker container is running
#   2. Activates the Python virtual environment
#   3. Checks that .env.local exists and SECRET_KEY has been set
#   4. Overrides DATABASE_URL to use localhost (not the docker-compose hostname)
#   5. Runs Alembic migrations
#   6. Starts uvicorn with hot-reload on port 8000
#
# Usage:
#   .\start-backend.ps1
#   .\start-backend.ps1 -SkipMigrations   # skip alembic upgrade head
#   .\start-backend.ps1 -Port 8080        # use a different port
# =============================================================================

param(
    [switch]$SkipMigrations,
    [int]$Port = 8000
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# ── Helpers ──────────────────────────────────────────────────────────────────

function Write-Step([string]$msg) {
    Write-Host "`n▶  $msg" -ForegroundColor Cyan
}

function Write-OK([string]$msg) {
    Write-Host "   ✓ $msg" -ForegroundColor Green
}

function Write-Warn([string]$msg) {
    Write-Host "   ⚠  $msg" -ForegroundColor Yellow
}

function Write-Fail([string]$msg) {
    Write-Host "`n   ✗ $msg" -ForegroundColor Red
}

# ── Step 1: Docker container ──────────────────────────────────────────────────

Write-Step "Checking Docker database container..."

$containerName = "chitsmart-db"
$containerState = docker inspect --format "{{.State.Status}}" $containerName 2>$null

if ($LASTEXITCODE -ne 0) {
    Write-Warn "Container '$containerName' not found. Creating and starting it..."
    docker run -d `
        --name $containerName `
        -e POSTGRES_USER=chitsmart `
        -e POSTGRES_PASSWORD=password `
        -e POSTGRES_DB=chitsmart_dev `
        -p 5432:5432 `
        postgres:16 | Out-Null

    if ($LASTEXITCODE -ne 0) {
        Write-Fail "Failed to start Docker container. Is Docker Desktop running?"
        exit 1
    }
    Write-OK "Container created and started."
    Write-Host "   Waiting 3 seconds for PostgreSQL to be ready..." -ForegroundColor DarkGray
    Start-Sleep -Seconds 3
}
elseif ($containerState -eq "exited" -or $containerState -eq "stopped") {
    Write-Warn "Container exists but is stopped. Starting it..."
    docker start $containerName | Out-Null
    Write-OK "Container started."
    Start-Sleep -Seconds 2
}
else {
    Write-OK "Container is $containerState."
}

# ── Step 2: Virtual environment ───────────────────────────────────────────────

Write-Step "Activating virtual environment..."

$venvActivate = Join-Path $ScriptDir ".venv\Scripts\Activate.ps1"
if (-not (Test-Path $venvActivate)) {
    Write-Fail ".venv not found at $ScriptDir\.venv"
    Write-Host "   Run: python -m venv .venv  then  pip install -r requirements.txt" -ForegroundColor Yellow
    exit 1
}

& $venvActivate
Write-OK "Virtual environment active."

# ── Step 3: Check .env.local ──────────────────────────────────────────────────

Write-Step "Checking .env.local..."

$envFile = Join-Path $ScriptDir ".env.local"
if (-not (Test-Path $envFile)) {
    Write-Fail ".env.local not found."
    Write-Host "   Run: Copy-Item .env.local.example .env.local  then fill in SECRET_KEY and OPENAI_API_KEY" -ForegroundColor Yellow
    exit 1
}

$envContent = Get-Content $envFile -Raw
if ($envContent -match "CHANGE_ME" -or $envContent -match "SECRET_KEY=$" ) {
    Write-Warn "SECRET_KEY still has placeholder value in .env.local"
    Write-Host "   Generate one with: python -c `"import secrets; print(secrets.token_hex(32))`"" -ForegroundColor Yellow
    Write-Host "   Continuing anyway — auth endpoints will not work correctly.`n" -ForegroundColor DarkGray
} else {
    Write-OK ".env.local looks configured."
}

# ── Step 4: Override DATABASE_URL for localhost access ────────────────────────
#
# .env.local.example ships with @db:5432 (docker-compose hostname).
# When running uvicorn directly (outside compose) we need @localhost:5432.

$env:DATABASE_URL = "postgresql+asyncpg://chitsmart:password@localhost:5432/chitsmart_dev"
$env:ENV = "local"
Write-OK "DATABASE_URL set to localhost:5432"

# ── Step 5: Alembic migrations ────────────────────────────────────────────────

if ($SkipMigrations) {
    Write-Warn "Skipping migrations (-SkipMigrations flag set)."
} else {
    Write-Step "Running Alembic migrations..."
    Push-Location $ScriptDir
    try {
        alembic upgrade head
        if ($LASTEXITCODE -ne 0) {
            Write-Fail "Alembic migration failed. Fix the error above before starting the server."
            exit 1
        }
        Write-OK "Migrations applied."
    } finally {
        Pop-Location
    }
}

# ── Step 6: Start uvicorn ─────────────────────────────────────────────────────

Write-Step "Starting uvicorn on port $Port..."
Write-Host ""
Write-Host "   API:    http://localhost:$Port" -ForegroundColor White
Write-Host "   Docs:   http://localhost:$Port/docs" -ForegroundColor White
Write-Host "   Redoc:  http://localhost:$Port/redoc" -ForegroundColor White
Write-Host ""
Write-Host "   Press Ctrl+C to stop." -ForegroundColor DarkGray
Write-Host ""

Push-Location $ScriptDir
uvicorn app.main:app --reload --port $Port --env-file .env.local
Pop-Location
