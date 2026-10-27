# Run tests across all laboratory works (3, 4, 5, 6, 7)
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "   RUNNING ALL VERIFICATION TESTS (LABS 3, 4, 5, 6, 7)            " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host ""

$root = $PSScriptRoot
$tests = @(
    @{ Lab = "lab03"; Class = "edu.course.lab03.TransactionVerificationTest" },
    @{ Lab = "lab04"; Class = "edu.course.lab04.WalletServiceVerificationTest" },
    @{ Lab = "lab05"; Class = "edu.course.lab05.CollectionsVerificationTest" },
    @{ Lab = "lab06"; Class = "edu.course.lab06.FileIoVerificationTest" },
    @{ Lab = "lab07"; Class = "edu.course.lab07.StreamAnalyticsVerificationTest" }
)

$allPassed = $true

foreach ($item in $tests) {
    $lab = $item.Lab
    $class = $item.Class
    Write-Host "------------------------------------------------------------------" -ForegroundColor Yellow
    Write-Host "[*] Running tests: $lab ($class)..." -ForegroundColor Yellow
    Write-Host "------------------------------------------------------------------" -ForegroundColor Yellow

    $outDir = Join-Path $root "$lab\out"
    if (Test-Path $outDir) {
        & java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -cp $outDir $class
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[FAIL] Tests for $lab failed (Exit code: $LASTEXITCODE)!" -ForegroundColor Red
            $allPassed = $false
        }
    } else {
        Write-Host "[ERROR] Directory $outDir not found. Run build_all.bat first." -ForegroundColor Red
        $allPassed = $false
    }
    Write-Host ""
}

Write-Host "==================================================================" -ForegroundColor Cyan
if ($allPassed) {
    Write-Host " [SUCCESS] ALL 5 LABS PASSED VERIFICATION SUITES (100% SUCCESS)!" -ForegroundColor Green
} else {
    Write-Host " [ERROR] Some tests failed." -ForegroundColor Red
}
Write-Host "==================================================================" -ForegroundColor Cyan

if (-not $allPassed) {
    exit 1
}
