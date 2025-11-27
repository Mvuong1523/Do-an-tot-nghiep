@echo off
echo ========================================
echo   TEST WEBHOOK NHANH
echo ========================================
echo.
echo Test 1: Goi truc tiep localhost (nen thanh cong)
echo.
curl -X POST http://localhost:8080/api/payment/sepay/webhook ^
  -H "Content-Type: application/json" ^
  -d "{\"transactionId\":\"TEST123\",\"amount\":30007,\"content\":\"PAY20241127001\",\"bankCode\":\"MBBank\",\"accountNumber\":\"3333315012003\",\"status\":\"SUCCESS\"}"

echo.
echo.
echo ========================================
echo Test 2: Goi qua ngrok URL (giong nhu SePay goi)
echo.
curl -X POST https://buffy-cellular-uncontradictably.ngrok-free.dev/api/payment/sepay/webhook ^
  -H "Content-Type: application/json" ^
  -d "{\"transactionId\":\"TEST456\",\"amount\":30007,\"content\":\"PAY20241127001\",\"bankCode\":\"MBBank\",\"accountNumber\":\"3333315012003\",\"status\":\"SUCCESS\"}"

echo.
echo.
echo ========================================
echo Kiem tra ket qua:
echo 1. Neu thanh cong, ban se thay response: {"success":true,...}
echo 2. Kiem tra backend logs de xem co nhan webhook khong
echo 3. Mo ngrok dashboard: http://127.0.0.1:4040 de xem requests
echo ========================================
echo.
pause
