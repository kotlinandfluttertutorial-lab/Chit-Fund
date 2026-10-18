# =============================================================================
# start-admin.ps1 — ChitSmart admin dashboard local dev launcher
#
# What this script does:
#   1. Checks Node.js is available
#   2. Creates .env.local from .env.local.example if it doesn't exist yet
#   3. Installs npm dependencies if node_modules is missing or package.json
#      is newer than node_modules (i.e. after a git pull)
#   4. Optionally runs TypeScript type-check before starting
#   5. Starts the Vite dev server on port 5173
#
# Usage:
#   .\start-admin.ps1
#   .\start-admin.ps1 -SkipTypeCheck    # skip tsc --noEmit (faster startup)
#   .\start-admin.ps1 -Port 3000        # use a different port
# =============================================================================

param(
    [switch]$SkipTypeCheck,
    [int]$Port = 5173
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# ── Helpers ───────────────────────────────────────────────────────────────────

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

# ── Step 1: Node.js check ─────────────────────────────────────────────────────

Write-Step "Checking Node.js..."

$nodeVersion = node --version 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Fail "Node.js not found. Install it from https://nodejs.org (LTS recommended)."
    exit 1
}
Write-OK "Node.js $nodeVersion"

# ── Step 2: .env.local ────────────────────────────────────────────────────────

Write-Step "Checking .env.local..."

$envFile    = Join-Path $ScriptDir ".env.local"
$envExample = Join-Path $ScriptDir ".env.local.example"

if (-not (Test-Path $envFile)) {
    if (Test-Path $envExample) {
        Copy-Item $envExample $envFile
        Write-OK ".env.local created from .env.local.example."
        Write-Warn "Review $envFile and update VITE_API_BASE_URL if your backend runs on a different port."
    } else {
        Write-Fail ".env.local.example not found. Cannot create .env.local."
        exit 1
    }
} else {
    Write-OK ".env.local already exists."
}

# ── Step 3: npm install (if needed) ──────────────────────────────────────────

Write-Step "Checking npm dependencies..."

$nodeModules  = Join-Path $ScriptDir "node_modules"
$packageJson  = Join-Path $ScriptDir "package.json"
$needsInstall = $false

if (-not (Test-Path $nodeModules)) {
    Write-Warn "node_modules not found."
    $needsInstall = $true
} elseif ((Get-Item $packageJson).LastWriteTime -gt (Get-Item $nodeModules).LastWriteTime) {
    Write-Warn "package.json is newer than node_modules — running npm install."
    $needsInstall = $true
} else {
    Write-OK "node_modules up to date."
}

if ($needsInstall) {
    Push-Location $ScriptDir
    try {
        npm install
        if ($LASTEXITCODE -ne 0) {
            Write-Fail "npm install failed."
            exit 1
        }
        Write-OK "Dependencies installed."
    } finally {
        Pop-Location
    }
}

# ── Step 4: TypeScript type-check ─────────────────────────────────────────────

if ($SkipTypeCheck) {
    Write-Warn "Skipping TypeScript check (-SkipTypeCheck flag set)."
} else {
    Write-Step "Running TypeScript type-check..."
    Push-Location $ScriptDir
    try {
        npx tsc --noEmit
        if ($LASTEXITCODE -ne 0) {
            Write-Fail "TypeScript errors found. Fix them or re-run with -SkipTypeCheck to start anyway."
            exit 1
        }
        Write-OK "No TypeScript errors."
    } finally {
        Pop-Location
    }
}

# ── Step 5: Start Vite dev server ─────────────────────────────────────────────

Write-Step "Starting Vite dev server on port $Port..."
Write-Host ""
Write-Host "   Admin:  http://localhost:$Port" -ForegroundColor White
Write-Host "   API:    $(Select-String 'VITE_API_BASE_URL' $envFile | ForEach-Object { ($_ -split '=',2)[1] })" -ForegroundColor White
Write-Host ""
Write-Host "   Press Ctrl+C to stop." -ForegroundColor DarkGray
Write-Host ""

Push-Location $ScriptDir
npx vite --port $Port
Pop-Location
