# ============================================================
#  ChitSmart - Encode secrets to Base64 for GitHub Actions
#
#  Run this ONCE to get the Base64 values, then paste them
#  into GitHub → Settings → Secrets and variables → Actions
#
#  NEVER commit the output of this script to git.
# ============================================================

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  ChitSmart - GitHub Secrets Encoder"      -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# ----------------------------------------------------------
# 1. google-services.json  →  GOOGLE_SERVICES_JSON_BASE64
# ----------------------------------------------------------
$googleServicesPath = "$PSScriptRoot\app\google-services.json"

if (Test-Path $googleServicesPath) {
    $encoded = [Convert]::ToBase64String([IO.File]::ReadAllBytes($googleServicesPath))
    Write-Host "Secret name : GOOGLE_SERVICES_JSON_BASE64" -ForegroundColor Yellow
    Write-Host "Secret value:" -ForegroundColor Yellow
    Write-Host $encoded
    Write-Host ""
} else {
    Write-Host "[SKIP] app\google-services.json not found at:" -ForegroundColor DarkGray
    Write-Host "       $googleServicesPath"                    -ForegroundColor DarkGray
    Write-Host ""
}

# ----------------------------------------------------------
# 2. chitsmart-release.jks  →  KEYSTORE_BASE64
# ----------------------------------------------------------
$keystorePath = "$PSScriptRoot\chitsmart-release.jks"

if (Test-Path $keystorePath) {
    $encoded = [Convert]::ToBase64String([IO.File]::ReadAllBytes($keystorePath))
    Write-Host "Secret name : KEYSTORE_BASE64" -ForegroundColor Yellow
    Write-Host "Secret value:" -ForegroundColor Yellow
    Write-Host $encoded
    Write-Host ""
} else {
    Write-Host "[SKIP] chitsmart-release.jks not found at:" -ForegroundColor DarkGray
    Write-Host "       $keystorePath"                       -ForegroundColor DarkGray
    Write-Host ""
}

# ----------------------------------------------------------
# Instructions
# ----------------------------------------------------------
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  Next steps"                              -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Copy each secret value above (the long Base64 string)."
Write-Host "2. Go to: GitHub repo → Settings → Secrets and variables → Actions"
Write-Host "3. Click 'New repository secret' and paste:"
Write-Host ""
Write-Host "   GOOGLE_SERVICES_JSON_BASE64  ← from step 1 above"
Write-Host "   KEYSTORE_BASE64              ← from step 2 above"
Write-Host "   KEYSTORE_PASSWORD            ← your keystore password"
Write-Host "   KEY_ALIAS                    ← your key alias"
Write-Host "   KEY_PASSWORD                 ← your key password"
Write-Host ""
Write-Host "These are used by .github/workflows/android.yml to sign" 
Write-Host "the release AAB without storing the files in git."
Write-Host ""
