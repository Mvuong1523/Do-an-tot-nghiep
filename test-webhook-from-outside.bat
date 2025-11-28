@echo off
echo Testing webhook from outside (simulating SePay)...
echo.

curl -X POST https://buffy-cellular-uncontradictablely.ngrok-free.dev/api/payment/sepay/webhook ^
  -H "Content-Type: application/json" ^
  -d "{\"transactionId\":\"32696922\",\"amount\":30007,\"content\":\"PAY202511277791 FT2533\",\"bankCode\":\"MBBank\",\"accountNumber\":\"3333315012003\",\"status\":\"SUCCESS\"}"

echo.
echo.
echo Kiem tra ngrok dashboard: http://127.0.0.1:4040
echo Kiem tra backend logs
echo.
pause
