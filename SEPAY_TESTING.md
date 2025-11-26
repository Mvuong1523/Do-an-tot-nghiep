# Hướng dẫn Test SePay Payment

## ✅ Đã cấu hình:

### Thông tin tài khoản:
- **Merchant ID**: SP-TEST-LM76B74B
- **Secret Key**: spsk_test_AUq3SEFB3PKAjM1VoivHPAoE2GE884mr
- **Bank**: ACB (Ngân hàng Á Châu)
- **Account Number**: 3260749581
- **Account Name**: LE MINH VUONG

### Tài khoản ảo (để test):
- **Virtual Account**: SBPAY5328490167
- **Account Name**: LE MINH VUONG

## 🚀 Bước 1: Setup Ngrok

```bash
# Cài đặt Ngrok (nếu chưa có)
# Download từ: https://ngrok.com/download

# Chạy Ngrok
ngrok http 8080
```

**Output sẽ có dạng**:
```
Forwarding  https://abc123.ngrok.io -> http://localhost:8080
```

→ **Webhook URL**: `https://abc123.ngrok.io/api/payment/sepay/webhook`

## 🔧 Bước 2: Đăng ký Webhook với SePay

1. Truy cập: https://dashboard.sepay.vn (hoặc staging dashboard)
2. Login với account: SP-TEST-LM76B74B
3. Vào **Settings** → **Webhooks**
4. Thêm webhook URL: `https://your-ngrok-url.ngrok.io/api/payment/sepay/webhook`
5. Chọn events: `payment.success`, `payment.failed`
6. Save

## 🧪 Bước 3: Test Flow

### 3.1. Tạo đơn hàng (nếu chưa có)

```bash
POST http://localhost:8080/api/orders
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "province": "Hà Nội",
  "district": "Cầu Giấy",
  "ward": "Dịch Vọng",
  "address": "123 Đường ABC",
  "shippingFee": 20000,
  "note": "Giao giờ hành chính"
}
```

**Response**: Lấy `orderId` và `total`

### 3.2. Tạo Payment

```bash
POST http://localhost:8080/api/payment/create
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "orderId": 1,
  "amount": 30020000
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "paymentCode": "PAY20231126001",
    "qrCodeUrl": "https://img.vietqr.io/image/ACB-3260749581-compact.png?amount=30020000&addInfo=PAY20231126001",
    "bankCode": "ACB",
    "accountNumber": "3260749581",
    "accountName": "LE MINH VUONG",
    "content": "PAY20231126001",
    "expiredAt": "2023-11-26T15:30:00"
  }
}
```

### 3.3. Mở trang Payment

Frontend: `http://localhost:3000/payment/ORD20231126001`

Trang này sẽ:
- Hiển thị QR Code
- Hiển thị thông tin chuyển khoản
- Auto polling check status mỗi 3 giây
- Countdown 15 phút

### 3.4. Chuyển khoản Test

**Cách 1: Quét QR Code**
- Mở app ngân hàng
- Quét QR Code trên màn hình
- Xác nhận thanh toán

**Cách 2: Chuyển khoản thủ công**
- Ngân hàng: ACB
- Số TK: 3260749581 (hoặc ảo: SBPAY5328490167)
- Tên: LE MINH VUONG
- Số tiền: 30,020,000 VND
- Nội dung: **PAY20231126001** (QUAN TRỌNG!)

### 3.5. SePay gọi Webhook

Sau khi chuyển khoản thành công, SePay sẽ tự động gọi webhook:

```bash
POST https://your-ngrok-url.ngrok.io/api/payment/sepay/webhook
Content-Type: application/json

{
  "transactionId": "TXN123456",
  "amount": 30020000,
  "content": "PAY20231126001",
  "bankCode": "ACB",
  "accountNumber": "3260749581",
  "timestamp": "2023-11-26T14:30:00"
}
```

### 3.6. Kiểm tra kết quả

**Backend logs**:
```
INFO: Received SePay webhook for payment: PAY20231126001
INFO: Payment processed successfully: PAY20231126001
```

**Frontend**:
- Tự động redirect đến `/orders/ORD20231126001?success=true`
- Hiển thị thông báo "Thanh toán thành công!"

**Database**:
```sql
-- Check payment status
SELECT * FROM payments WHERE payment_code = 'PAY20231126001';
-- status should be 'SUCCESS'

-- Check order status
SELECT * FROM orders WHERE order_code = 'ORD20231126001';
-- payment_status should be 'PAID'
-- status should be 'CONFIRMED'
```

## 🧪 Test Manual Webhook (không cần chuyển khoản thật)

Nếu muốn test nhanh mà không chuyển khoản:

```bash
curl -X POST http://localhost:8080/api/payment/sepay/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TEST123",
    "amount": 30020000,
    "content": "PAY20231126001",
    "bankCode": "ACB",
    "accountNumber": "3260749581"
  }'
```

## 📊 Check Payment Status

```bash
GET http://localhost:8080/api/payment/PAY20231126001/status
```

**Response**:
```json
{
  "success": true,
  "data": {
    "paymentCode": "PAY20231126001",
    "status": "SUCCESS",
    "amount": 30020000
  }
}
```

## ⚠️ Troubleshooting

### 1. Webhook không được gọi
- ✅ Kiểm tra Ngrok đang chạy: `ngrok http 8080`
- ✅ Kiểm tra URL đã đăng ký đúng với SePay
- ✅ Check logs Ngrok: `http://localhost:4040` (Ngrok dashboard)
- ✅ Verify webhook URL accessible: `curl https://your-ngrok-url.ngrok.io/api/payment/sepay/webhook`

### 2. Payment không update
- ✅ Check backend logs: Có nhận webhook không?
- ✅ Verify content chuyển khoản: Phải đúng `PAY20231126001`
- ✅ Check amount: Phải khớp với payment amount
- ✅ Check payment chưa expired (< 15 phút)

### 3. QR Code không hiển thị
- ✅ Check URL: `https://img.vietqr.io/image/ACB-3260749581-compact.png?amount=30020000&addInfo=PAY20231126001`
- ✅ Test URL trực tiếp trên browser
- ✅ Thử bank code khác nếu ACB không work

### 4. Frontend không redirect
- ✅ Check polling đang chạy (mỗi 3 giây)
- ✅ Check API `/status` có trả về SUCCESS không
- ✅ Check console logs trong browser

## 🔍 Debug Commands

```bash
# Check Ngrok status
curl http://localhost:4040/api/tunnels

# Check payment in DB
mysql -u root -p web2 -e "SELECT * FROM payments WHERE payment_code = 'PAY20231126001';"

# Check order in DB
mysql -u root -p web2 -e "SELECT * FROM orders WHERE id = 1;"

# Tail backend logs
tail -f logs/spring.log

# Test webhook manually
curl -X POST http://localhost:8080/api/payment/sepay/webhook \
  -H "Content-Type: application/json" \
  -d '{"transactionId":"TEST","amount":30020000,"content":"PAY20231126001","bankCode":"ACB","accountNumber":"3260749581"}'
```

## 📝 Notes

- **Timeout**: Payment hết hạn sau 15 phút
- **Polling**: Frontend check status mỗi 3 giây
- **QR Code**: Sử dụng VietQR API (miễn phí)
- **Test Account**: Dùng tài khoản ảo SBPAY5328490167 để test
- **Staging**: Đang dùng staging environment của SePay

## ✅ Success Criteria

Payment thành công khi:
1. ✅ Payment status = SUCCESS
2. ✅ Order payment_status = PAID
3. ✅ Order status = CONFIRMED
4. ✅ Frontend redirect đến success page
5. ✅ Webhook log có trong backend

Happy testing! 🎉
