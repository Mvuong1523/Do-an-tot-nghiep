@echo off
echo Testing webhook via ngrok URL...
echo.
set /p PAYMENT_CODE="Nhap payment code (vi du: PAY20241127001): "
echo.
echo Calling webhook for payment: %PAYMENT_CODE%
echo.

curl -X POST https://buffy-cellular-uncontradictably.ngrok-free.dev/api/payment/sepay/webhook ^
  -H "Content-Type: application/json" ^
  -d "{\"transactionId\":\"TEST_%PAYMENT_CODE%\",\"amount\":30007,\"content\":\"%PAYMENT_CODE%\",\"bankCode\":\"MBBank\",\"accountNumber\":\"3333315012003\",\"status\":\"SUCCESS\"}"

echo.
echo.
pause
