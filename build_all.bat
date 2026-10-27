@echo off
cd /d "%~dp0"
powershell -ExecutionPolicy Bypass -File "%~dp0build_all.ps1"
echo.
pause
