@echo off
echo === Test Login API Directly ===
echo.

curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"ketoan@gmail.com\",\"password\":\"123456\"}"

echo.
echo.
echo === If you see token above, login works! ===
pause
