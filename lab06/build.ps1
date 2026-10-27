$ErrorActionPreference = "Stop"
Set-Location -LiteralPath $PSScriptRoot

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
javac -encoding UTF-8 -cp out -d out test/edu/course/lab06/FileIoVerificationTest.java

Write-Host "`n>>> Running verification tests..."
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -cp out edu.course.lab06.FileIoVerificationTest

Write-Host "`n>>> Running FileApp..."
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -cp out edu.course.lab06.FileApp

Write-Host "`n>>> Creating JAR file..."
jar --create --file lab06.jar --main-class edu.course.lab06.FileApp -C out .

Write-Host "`n>>> Running packaged JAR file..."
java "-Dfile.encoding=UTF-8" "-Dstdout.encoding=UTF-8" "-Dstderr.encoding=UTF-8" -jar lab06.jar

Write-Host "`n>>> Bytecode inspection of CsvTransactionFileRepository (try-with-resources)..."
javap -c -p out/edu/course/lab06/repository/CsvTransactionFileRepository.class
