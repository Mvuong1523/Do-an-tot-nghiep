# 💳 Tích hợp SePay

## 📋 Giới thiệu

SePay là cổng thanh toán chuyển khoản ngân hàng tự động tại Việt Nam. Hệ thống sẽ:
1. Tạo QR Code thanh toán
2. Khách hàng quét mã và chuyển khoản
3. SePay tự động xác nhận và gửi webhook
4. Hệ thống cập nhật trạng thái đơn hàng

## 🔧 Cấu hình

### 1. Đăng ký tài khoản SePay
- Website: https://sepay.vn
- Đăng ký tài khoản doanh nghiệp
- Lấy API Key và Secret Key

### 2. Cấu hình trong application.properties
```properties
# SePay Configuration
sepay.api.url=https://api.sepay.vn
sepay.api.key=YOUR_API_KEY
sepay.api.secret=YOUR_SECRET_KEY
sepay.webhook.url=https://yourdomain.com/api/payment/sepay/webhook
sepay.return.url=https://yourdomain.com/payment/result

# Bank Account Info (Tài khoản nhận tiền)
sepay.bank.code=VCB
sepay.bank.account.number=1234567890
sepay.bank.account.name=CONG TY TNHH TECHMART
```

## 🔄 Quy trình thanh toán

### Bước 1: Tạo thanh toán
```
POST /api/payment/create
{
  "orderId": 123,
  "amount": 1000000,
  "returnUrl": "https://yourdomain.com/orders/123"
}

Response:
{
  "paymentId": 456,
  "paymentCode": "PAY20231119001",
  "amount": 1000000,
  "status": "PENDING",
  "bankCode": "VCB",
  "accountNumber": "1234567890",
  "accountName": "CONG TY TNHH TECHMART",
  "content": "PAY20231119001",
  "qrCodeUrl": "https://api.sepay.vn/qr/...",
  "expiredAt": "2023-11-19T10:30:00"
}
```

### Bước 2: Khách hàng thanh toán
- Quét QR Code bằng app ngân hàng
- Hoặc chuyển khoản thủ công với nội dung: `PAY20231119001`

### Bước 3: SePay gửi webhook
```
POST /api/payment/sepay/webhook
{
  "transactionId": "SEP123456",
  "bankCode": "VCB",
  "accountNumber": "1234567890",
  "amount": 1000000,
  "content": "PAY20231119001",
  "transactionDate": "2023-11-19T10:25:00",
  "status": "SUCCESS",
  "signature": "abc123..."
}
```

### Bước 4: Hệ thống xử lý
1. Xác thực signature
2. Tìm Payment theo content (paymentCode)
3. Cập nhật trạng thái Payment → SUCCESS
4. Cập nhật trạng thái Order → PAID
5. Gửi email xác nhận
6. Trả về response cho SePay

## 🔐 Bảo mật

### Xác thực Webhook
```java
public boolean verifySignature(SepayWebhookRequest request, String signature) {
    String data = request.getTransactionId() + 
                  request.getAmount() + 
                  request.getContent() + 
                  sepaySecretKey;
    String calculatedSignature = DigestUtils.sha256Hex(data);
    return calculatedSignature.equals(signature);
}
```

### Kiểm tra trùng lặp
- Lưu transactionId để tránh xử lý trùng
- Kiểm tra amount khớp với đơn hàng
- Kiểm tra thời gian hết hạn

## 📱 Giao diện thanh toán

### Desktop
```
┌─────────────────────────────────────┐
│  Thanh toán đơn hàng #ORD123        │
├─────────────────────────────────────┤
│  Tổng tiền: 1.000.000đ              │
│                                     │
│  ┌─────────────┐                   │
│  │             │  Quét mã QR        │
│  │   QR CODE   │  để thanh toán     │
│  │             │                    │
│  └─────────────┘                   │
│                                     │
│  Hoặc chuyển khoản:                 │
│  Ngân hàng: Vietcombank             │
│  STK: 1234567890                    │
│  Tên: CONG TY TNHH TECHMART         │
│  Nội dung: PAY20231119001           │
│                                     │
│  ⏱️ Hết hạn sau: 14:32              │
└─────────────────────────────────────┘
```

### Mobile
```
┌─────────────────┐
│ Thanh toán      │
├─────────────────┤
│ 1.000.000đ      │
│                 │
│  ┌───────────┐  │
│  │           │  │
│  │  QR CODE  │  │
│  │           │  │
│  └───────────┘  │
│                 │
│ [Mở app ngân hàng]│
│                 │
│ Thông tin CK:   │
│ VCB - 1234567890│
│ PAY20231119001  │
│                 │
│ ⏱️ 14:32        │
└─────────────────┘
```

## 🧪 Test Mode

### Tài khoản test
```
Bank: VCB Test
Account: 9999999999
Amount: Bất kỳ
Content: PAY[CODE]
```

### Webhook test
```bash
curl -X POST http://localhost:8080/api/payment/sepay/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "transactionId": "TEST123",
    "bankCode": "VCB",
    "accountNumber": "9999999999",
    "amount": 1000000,
    "content": "PAY20231119001",
    "transactionDate": "2023-11-19T10:25:00",
    "status": "SUCCESS",
    "signature": "test_signature"
  }'
```

## 📊 Báo cáo

### Dashboard
- Tổng giao dịch hôm nay
- Tổng tiền thu
- Tỷ lệ thành công
- Giao dịch đang chờ

### Export
- Excel: Danh sách giao dịch
- PDF: Báo cáo doanh thu

## 🔔 Thông báo

### Email
- Gửi email xác nhận khi thanh toán thành công
- Gửi email nhắc nhở nếu chưa thanh toán sau 10 phút

### SMS (Optional)
- Gửi SMS xác nhận đơn hàng
- Gửi SMS khi giao hàng

## 🐛 Xử lý lỗi

### Lỗi thường gặp
1. **Webhook không nhận được**
   - Kiểm tra firewall
   - Kiểm tra SSL certificate
   - Kiểm tra log SePay

2. **Signature không khớp**
   - Kiểm tra secret key
   - Kiểm tra format data

3. **Thanh toán trùng**
   - Kiểm tra transactionId
   - Kiểm tra thời gian

### Retry mechanism
- Retry webhook 3 lần nếu thất bại
- Delay 5s giữa các lần retry
- Log tất cả các lần retry

## 📞 Hỗ trợ

**SePay Support:**
- Hotline: 1900 xxxx
- Email: support@sepay.vn
- Docs: https://docs.sepay.vn

**Technical Support:**
- Email: tech@techmart.vn
- Slack: #payment-support
