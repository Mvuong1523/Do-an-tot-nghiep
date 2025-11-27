@echo off
echo ========================================
echo   RESTART SPRING BOOT SERVER
echo ========================================
echo.
echo Dang dung server hien tai...
echo.

REM Find and kill Java process running on port 8080
for /f "tokens=5" %%a in ('netstat -aon ^| findstr :8080 ^| findstr LISTENING') do (
    echo Killing process %%a...
    taskkill /F /PID %%a
)

echo.
echo Server da dung. Vui long khoi dong lai server bang cach chay:
echo   mvnw spring-boot:run
echo.
echo Hoac nhan F5 trong IDE de restart
echo.
pause
