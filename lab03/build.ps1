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
javac -encoding UTF-8 -cp out -d out test/edu/course/lab03/TransactionVerificationTest.java

Write-Host "`n>>> Running verification tests..."
java -cp out edu.course.lab03.TransactionVerificationTest

Write-Host "`n>>> Running TransactionApp..."
java -cp out edu.course.lab03.TransactionApp

Write-Host "`n>>> Creating JAR file..."
jar --create --file lab03.jar --main-class edu.course.lab03.TransactionApp -C out .

Write-Host "`n>>> Running packaged JAR file..."
java -jar lab03.jar

Write-Host "`n>>> Bytecode inspection of TransactionApp (main method invokevirtual & invokespecial)..."
javap -c -p out/edu/course/lab03/TransactionApp.class

Write-Host "`n>>> Bytecode inspection of AccountService (calculateTotalBalanceImpact)..."
javap -c -p out/edu/course/lab03/service/AccountService.class
