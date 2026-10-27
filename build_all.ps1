# Build and test all laboratory works (3, 4, 5, 6, 7)
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "   BUILD AND VERIFICATION OF ALL LABS (3, 4, 5, 6, 7)             " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

$root = $PSScriptRoot
$labs = @("lab03", "lab04", "lab05", "lab06", "lab07")
$allSuccess = $true

foreach ($lab in $labs) {
    Write-Host "------------------------------------------------------------------" -ForegroundColor Yellow
    Write-Host "[*] Building and testing: $lab..." -ForegroundColor Yellow
    Write-Host "------------------------------------------------------------------" -ForegroundColor Yellow
    
    $labDir = Join-Path $root $lab
    $buildScript = Join-Path $labDir "build.ps1"
    
    if (Test-Path $buildScript) {
        & powershell -ExecutionPolicy Bypass -File $buildScript
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[FAIL] Error while building $lab (Exit code: $LASTEXITCODE)!" -ForegroundColor Red
            $allSuccess = $false
            break
        }
    } else {
        Write-Host "[WARN] Script $buildScript not found." -ForegroundColor Red
        $allSuccess = $false
        break
    }
    Write-Host ""
}

Write-Host "==================================================================" -ForegroundColor Cyan
if ($allSuccess) {
    Write-Host " [SUCCESS] ALL LABS (3-7) COMPILED AND VERIFIED SUCCESSFULLY!" -ForegroundColor Green
} else {
    Write-Host " [ERROR] Build failed for one or more laboratory works." -ForegroundColor Red
}
Write-Host "==================================================================" -ForegroundColor Cyan

if (-not $allSuccess) {
    exit 1
}
