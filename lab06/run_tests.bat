@echo off
cd /d "%~dp0"
chcp 65001 > nul
echo ==================================================================
echo   RUNNING VERIFICATION TESTS (FileIoVerificationTest)
echo ==================================================================
echo.
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp out edu.course.lab06.FileIoVerificationTest
echo.
echo ==================================================================
echo Tests finished. Press any key to close this window...
pause > nul
