# Hướng dẫn cấu hình SePay

## 1. Thông tin cần có

Để tích hợp thanh toán SePay, bạn cần có:

### Từ SePay:
- **Bank Code**: Mã ngân hàng (VD: VCB, TCB, MB, ACB, etc.)
- **Account Number**: Số tài khoản nhận tiền
- **Account Name**: Tên chủ tài khoản (viết hoa, không dấu)
- **API Secret**: Key để verify webhook (nếu SePay cung cấp)

### Từ Ngrok (để test webhook):
- **Ngrok URL**: URL public để SePay gọi webhook
  ```bash
  ngrok http 8080
  # Sẽ có URL dạng: https://abc123.ngrok.io
  ```

## 2. Cấu hình trong application.properties

Thêm vào file `src/main/resources/application.properties`:

```properties
# SePay Configuration
sepay.bank.code=VCB
sepay.bank.account.number=1234567890
sepay.bank.account.name=CONG TY TNHH TECHMART
sepay.api.secret=your_secret_key_here

# Webhook URL (để log, không bắt buộc)
sepay.webhook.url=https://your-ngrok-url.ngrok.io/api/payment/sepay/webhook
```

## 3. Đăng ký Webhook với SePay

Sau khi có Ngrok URL, đăng ký webhook với SePay:

**Webhook URL**: `https://your-ngrok-url.ngrok.io/api/payment/sepay/webhook`

**Method**: POST

**Payload mẫu** (SePay sẽ gửi):
```json
{
  "transactionId": "TXN123456",
  "amount": 30020000,
  "content": "PAY20231119001",
  "bankCode": "VCB",
  "accountNumber": "1234567890",
  "timestamp": "2023-11-19T10:30:00",
  "signature": "abc123..."
}
```

## 4. Test Flow

### Bước 1: Tạo Payment
```bash
POST http://localhost:8080/api/payment/create
Authorization: Bearer <token>
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
    "paymentCode": "PAY20231119001",
    "qrCodeUrl": "https://img.vietqr.io/image/VCB-1234567890-compact.png?amount=30020000&addInfo=PAY20231119001",
    "bankCode": "VCB",
    "accountNumber": "1234567890",
    "accountName": "CONG TY TNHH TECHMART",
    "content": "PAY20231119001",
    "expiredAt": "2023-11-19T10:45:00"
  }
}
```

### Bước 2: Khách hàng chuyển khoản
- Quét QR Code HOẶC
- Chuyển khoản thủ công với nội dung: `PAY20231119001`

### Bước 3: SePay gọi Webhook
SePay tự động gọi webhook khi phát hiện giao dịch:
```bash
POST https://your-ngrok-url.ngrok.io/api/payment/sepay/webhook
Content-Type: application/json

{
  "transactionId": "TXN123456",
  "amount": 30020000,
  "content": "PAY20231119001",
  "bankCode": "VCB",
  "accountNumber": "1234567890"
}
```

### Bước 4: Hệ thống xử lý
- Verify signature (nếu có)
- Kiểm tra amount, content
- Update payment status → SUCCESS
- Update order status → CONFIRMED
- Gửi email xác nhận (TODO)

### Bước 5: Frontend polling
Frontend tự động check status mỗi 3 giây:
```bash
GET http://localhost:8080/api/payment/PAY20231119001/status
```

Khi status = SUCCESS → Redirect đến trang success

## 5. QR Code Format

Sử dụng VietQR API (miễn phí):
```
https://img.vietqr.io/image/{BANK}-{ACCOUNT}-compact.png?amount={AMOUNT}&addInfo={CONTENT}
```

**Ví dụ**:
```
https://img.vietqr.io/image/VCB-1234567890-compact.png?amount=30020000&addInfo=PAY20231119001
```

## 6. Ngrok Setup

### Cài đặt Ngrok:
```bash
# Download từ https://ngrok.com/download
# Hoặc dùng chocolatey (Windows)
choco install ngrok

# Hoặc npm
npm install -g ngrok
```

### Chạy Ngrok:
```bash
ngrok http 8080
```

**Output**:
```
Forwarding  https://abc123.ngrok.io -> http://localhost:8080
```

→ Webhook URL: `https://abc123.ngrok.io/api/payment/sepay/webhook`

## 7. Test Webhook (Manual)

Nếu chưa có SePay thật, test bằng Postman/curl:

```bash
curl -X POST http://localhost:8080/api/payment/sepay/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TEST123",
    "amount": 30020000,
    "content": "PAY20231119001",
    "bankCode": "VCB",
    "accountNumber": "1234567890"
  }'
```

## 8. Troubleshooting

### Webhook không được gọi:
- Kiểm tra Ngrok đang chạy
- Kiểm tra URL đã đăng ký đúng với SePay
- Check firewall/antivirus

### Payment không update:
- Check logs: `tail -f logs/spring.log`
- Verify content chuyển khoản đúng
- Check amount khớp

### QR Code không hiển thị:
- Kiểm tra URL format
- Test URL trực tiếp trên browser
- Thử bank code khác (VCB, TCB, MB)

## 9. Production Checklist

- [ ] Đổi `sepay.api.secret` thành key thật
- [ ] Đổi account number thật
- [ ] Setup domain thật (không dùng Ngrok)
- [ ] Enable HTTPS
- [ ] Implement signature verification
- [ ] Setup email notification
- [ ] Setup monitoring/alerting
- [ ] Test với số tiền nhỏ trước

## 10. Liên hệ SePay

- Website: https://sepay.vn
- Email: support@sepay.vn
- Hotline: 1900 xxxx
- Docs: https://docs.sepay.vn
