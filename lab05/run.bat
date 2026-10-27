@echo off
cd /d "%~dp0"
chcp 65001 > nul
echo ==================================================================
echo   RUNNING LAB 05 (RepositoryApp)
echo ==================================================================
echo.
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -jar lab05.jar
echo.
echo ==================================================================
echo Program finished. Press any key to close this window...
pause > nul
