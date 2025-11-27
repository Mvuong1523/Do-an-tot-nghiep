@echo off
echo ========================================
echo   KHOI DONG NGROK CHO SEPAY WEBHOOK
echo ========================================
echo.
echo Buoc 1: Kiem tra ngrok da cai dat chua...
where ngrok >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Ngrok chua duoc cai dat!
    echo.
    echo Vui long tai va cai dat ngrok:
    echo 1. Truy cap: https://ngrok.com/download
    echo 2. Tai ban Windows
    echo 3. Giai nen va them vao PATH hoac copy vao thu muc nay
    echo.
    pause
    exit /b 1
)

echo [OK] Ngrok da duoc cai dat
echo.
echo Buoc 2: Khoi dong ngrok...
echo.
echo ========================================
echo   QUAN TRONG!
echo ========================================
echo.
echo Sau khi ngrok khoi dong, ban se thay URL nhu:
echo   Forwarding: https://abc123.ngrok-free.app -^> http://localhost:8080
echo.
echo HAY COPY URL NAY VA:
echo 1. Truy cap: https://my.sepay.vn/developer/webhook
echo 2. Dang nhap voi tai khoan SePay
echo 3. Them webhook URL: https://abc123.ngrok-free.app/api/payment/sepay/webhook
echo 4. Luu lai
echo.
echo SAU DO MOI CHUYEN KHOAN THAT, SEPAY SE TU DONG GOI WEBHOOK!
echo.
echo ========================================
echo.
pause

ngrok http 8080
