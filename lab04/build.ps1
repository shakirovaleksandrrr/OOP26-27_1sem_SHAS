$ErrorActionPreference = "Stop"
Set-Location -LiteralPath $PSScriptRoot
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$env:Path = "C:\Program Files\Java\jdk-21\bin;" + $env:Path

Write-Host ">>> Cleaning out directory..."
if (Test-Path out) {
    Remove-Item -Recurse -Force out
}
New-Item -ItemType Directory -Path out | Out-Null

Write-Host ">>> Compiling source files..."
$sources = Get-ChildItem -Recurse -Filter *.java -Path src | Select-Object -ExpandProperty FullName
javac -encoding UTF-8 -d out $sources

Write-Host ">>> Compiling test files..."
javac -encoding UTF-8 -cp out -d out test/edu/course/lab04/WalletServiceVerificationTest.java

Write-Host "`n>>> Running verification tests..."
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -cp out edu.course.lab04.WalletServiceVerificationTest

Write-Host "`n>>> Running WalletApp..."
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -cp out edu.course.lab04.WalletApp

Write-Host "`n>>> Creating JAR file..."
jar --create --file lab04.jar --main-class edu.course.lab04.WalletApp -C out .

Write-Host "`n>>> Running packaged JAR file..."
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar lab04.jar

Write-Host "`n>>> Bytecode inspection of WalletService (transfer method exception handling)..."
javap -c -p out/edu/course/lab04/service/WalletService.class
