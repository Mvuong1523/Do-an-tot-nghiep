# GIẢI THÍCH CHI TIẾT BẢNG PAYMENTS

## Cấu trúc bảng

```sql
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_code VARCHAR(50) UNIQUE NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    method ENUM('SEPAY', 'COD') NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'EXPIRED') NOT NULL,
    sepay_transaction_id VARCHAR(255),
    sepay_bank_code VARCHAR(50),
    sepay_account_number VARCHAR(50),
    sepay_account_name VARCHAR(255),
    sepay_content VARCHAR(500),
    sepay_qr_code VARCHAR(500),
    sepay_response TEXT,
    created_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    expired_at TIMESTAMP,
    failure_reason TEXT
);
```

---

## CHI TIẾT TỪNG CỘT

### 1. **id** (BIGINT, PK, Auto Increment)
**Tác dụng:** 
- Khóa chính của bảng
- Định danh duy nhất cho mỗi giao dịch thanh toán
- Tự động tăng

**Ví dụ:** 1, 2, 3, 4...

---

### 2. **payment_code** (VARCHAR(50), UNIQUE, NOT NULL)
**Tác dụng:**
- Mã thanh toán duy nhất, dễ đọc cho người dùng
- Dùng để tra cứu, hiển thị cho khách hàng
- Format: PAY + YYYYMMDD + số thứ tự

**Ví dụ:** 
- `PAY20240101001` (Thanh toán đầu tiên ngày 01/01/2024)
- `PAY20240101002` (Thanh toán thứ 2 ngày 01/01/2024)

**Tại sao cần:**
- `id` là số thuần túy, khó nhớ
- `payment_code` dễ đọc, dễ tra cứu cho customer service

---

### 3. **order_id** (BIGINT, FK → orders.id, NOT NULL)
**Tác dụng:**
- Liên kết với đơn hàng cần thanh toán
- Một đơn hàng có một payment
- Foreign key đến bảng `orders`

**Ví dụ:** 
- order_id = 123 → Thanh toán cho đơn hàng #123

**Quan hệ:** 
- 1 Order ↔ 1 Payment (One-to-One)

---

### 4. **user_id** (BIGINT, FK → users.id, NOT NULL)
**Tác dụng:**
- Lưu người thực hiện thanh toán
- Dùng để tra cứu lịch sử thanh toán của user
- Báo cáo thống kê theo user

**Ví dụ:**
- user_id = 456 → User #456 đã thanh toán

**Tại sao cần:**
- Có thể khác với user tạo đơn hàng (người khác thanh toán hộ)
- Audit trail - biết ai đã thanh toán

---

### 5. **amount** (DECIMAL(15,2), NOT NULL)
**Tác dụng:**
- Số tiền cần thanh toán (VNĐ)
- Lưu chính xác đến 2 chữ số thập phân
- Thường bằng `orders.total`

**Ví dụ:**
- 1500000.00 (1,500,000 VNĐ)
- 250000.50 (250,000.50 VNĐ)

**Tại sao DECIMAL(15,2):**
- DECIMAL: Chính xác tuyệt đối (không làm tròn như FLOAT)
- 15: Tổng số chữ số (max: 9,999,999,999,999.99)
- 2: Số chữ số thập phân

---

### 6. **method** (ENUM, NOT NULL)
**Tác dụng:**
- Phương thức thanh toán
- Giá trị: `SEPAY` hoặc `COD`

**Chi tiết:**
- **SEPAY**: Chuyển khoản qua SePay Gateway
- **COD**: Cash On Delivery (Thanh toán khi nhận hàng)

**Ví dụ:**
```
method = 'SEPAY' → Thanh toán online
method = 'COD' → Thanh toán khi nhận hàng
```

---

### 7. **status** (ENUM, NOT NULL)
**Tác dụng:**
- Trạng thái thanh toán hiện tại
- Giá trị: `PENDING`, `SUCCESS`, `FAILED`, `EXPIRED`

**Chi tiết:**
- **PENDING**: Đang chờ thanh toán (vừa tạo)
- **SUCCESS**: Thanh toán thành công
- **FAILED**: Thanh toán thất bại
- **EXPIRED**: Hết hạn (quá 15 phút chưa thanh toán)

**Luồng trạng thái:**
```
PENDING → SUCCESS (thanh toán thành công)
PENDING → FAILED (thanh toán thất bại)
PENDING → EXPIRED (quá 15 phút)
```

---

### 8. **sepay_transaction_id** (VARCHAR(255), NULL)
**Tác dụng:**
- Mã giao dịch từ SePay trả về
- Dùng để đối soát với SePay
- Chỉ có khi method = 'SEPAY'

**Ví dụ:**
- `SEPAY_TXN_20240101_ABC123`

**Tại sao cần:**
- Đối soát giao dịch với SePay
- Xử lý tranh chấp
- Hoàn tiền (refund)

---

### 9. **sepay_bank_code** (VARCHAR(50), NULL)
**Tác dụng:**
- Mã ngân hàng khách chuyển khoản
- Do SePay cung cấp

**Ví dụ:**
- `VCB` (Vietcombank)
- `TCB` (Techcombank)
- `MB` (MBBank)

**Tại sao cần:**
- Biết khách dùng ngân hàng nào
- Thống kê phương thức thanh toán phổ biến

---

### 10. **sepay_account_number** (VARCHAR(50), NULL)
**Tác dụng:**
- Số tài khoản shop nhận tiền
- Do SePay cung cấp

**Ví dụ:**
- `1234567890`

**Tại sao cần:**
- Hiển thị cho khách biết chuyển vào tài khoản nào
- Đối soát sổ sách

---

### 11. **sepay_account_name** (VARCHAR(255), NULL)
**Tác dụng:**
- Tên chủ tài khoản nhận tiền
- Do SePay cung cấp

**Ví dụ:**
- `CONG TY TNHH ABC`

**Tại sao cần:**
- Khách xác nhận đúng người nhận
- Tăng độ tin cậy

---

### 12. **sepay_content** (VARCHAR(500), NULL)
**Tác dụng:**
- Nội dung chuyển khoản
- Khách phải ghi đúng nội dung này
- Format: Mã đơn hàng + Mã thanh toán

**Ví dụ:**
- `ORD20240101001 PAY20240101001`
- `Thanh toan don hang ORD123`

**Tại sao cần:**
- SePay dùng để đối chiếu giao dịch
- Tự động xác nhận thanh toán
- Tránh nhầm lẫn

---

### 13. **sepay_qr_code** (VARCHAR(500), NULL)
**Tác dụng:**
- URL của mã QR code
- Khách quét để chuyển khoản nhanh
- Do SePay tạo

**Ví dụ:**
- `https://sepay.vn/qr/abc123xyz`

**Tại sao cần:**
- Tiện lợi cho khách (quét là chuyển)
- Tự động điền đầy đủ thông tin
- Giảm sai sót

---

### 14. **sepay_response** (TEXT, NULL)
**Tác dụng:**
- Lưu toàn bộ response JSON từ SePay
- Dùng để debug, đối soát
- Lưu trữ đầy đủ thông tin giao dịch

**Ví dụ:**
```json
{
  "transaction_id": "SEPAY_TXN_123",
  "status": "SUCCESS",
  "amount": 1500000,
  "bank_code": "VCB",
  "timestamp": "2024-01-01T10:30:00Z"
}
```

**Tại sao cần:**
- Debug khi có lỗi
- Đối soát chi tiết
- Audit trail đầy đủ

---

### 15. **created_at** (TIMESTAMP, NOT NULL)
**Tác dụng:**
- Thời điểm tạo payment
- Tự động set khi insert

**Ví dụ:**
- `2024-01-01 10:00:00`

**Tại sao cần:**
- Biết khi nào khách bắt đầu thanh toán
- Tính thời gian xử lý
- Báo cáo theo thời gian

---

### 16. **paid_at** (TIMESTAMP, NULL)
**Tác dụng:**
- Thời điểm thanh toán thành công
- Chỉ có giá trị khi status = 'SUCCESS'

**Ví dụ:**
- `2024-01-01 10:05:30`

**Tại sao cần:**
- Biết chính xác khi nào tiền về
- Tính thời gian khách thanh toán (paid_at - created_at)
- Đối soát với ngân hàng

---

### 17. **expired_at** (TIMESTAMP, NULL)
**Tác dụng:**
- Thời điểm hết hạn thanh toán
- Thường là created_at + 15 phút
- Sau thời gian này, payment tự động EXPIRED

**Ví dụ:**
- created_at: `2024-01-01 10:00:00`
- expired_at: `2024-01-01 10:15:00`

**Tại sao cần:**
- Tránh payment treo mãi mãi
- Giải phóng tồn kho đã giữ
- Tự động hủy đơn quá hạn

---

### 18. **failure_reason** (TEXT, NULL)
**Tác dụng:**
- Lý do thanh toán thất bại
- Chỉ có khi status = 'FAILED'

**Ví dụ:**
- `Số dư tài khoản không đủ`
- `Ngân hàng từ chối giao dịch`
- `Timeout kết nối SePay`

**Tại sao cần:**
- Khách biết tại sao thất bại
- Customer service hỗ trợ
- Phân tích lỗi thường gặp

---

## LUỒNG DỮ LIỆU

### Khi tạo payment mới:
```java
Payment payment = Payment.builder()
    .paymentCode("PAY20240101001")
    .orderId(123L)
    .userId(456L)
    .amount(1500000.00)
    .method(PaymentMethod.SEPAY)
    .status(PaymentStatus.PENDING)
    .createdAt(LocalDateTime.now())
    .expiredAt(LocalDateTime.now().plusMinutes(15))
    .build();
```

### Khi SePay trả về thông tin:
```java
payment.setSepayTransactionId("SEPAY_TXN_123");
payment.setSepayBankCode("VCB");
payment.setSepayAccountNumber("1234567890");
payment.setSepayAccountName("CONG TY ABC");
payment.setSepayContent("ORD123 PAY001");
payment.setSepayQrCode("https://sepay.vn/qr/abc");
payment.setSepayResponse(jsonResponse);
```

### Khi thanh toán thành công (webhook):
```java
payment.setStatus(PaymentStatus.SUCCESS);
payment.setPaidAt(LocalDateTime.now());
```

### Khi thanh toán thất bại:
```java
payment.setStatus(PaymentStatus.FAILED);
payment.setFailureReason("Số dư không đủ");
```

---

## TẠI SAO CẦN NHIỀU CỘT SEPAY?

**Lý do:**
1. **Đối soát**: Cần đầy đủ thông tin để đối chiếu với SePay
2. **Debug**: Khi có lỗi, cần biết chính xác điều gì đã xảy ra
3. **Customer Service**: Hỗ trợ khách khi có vấn đề
4. **Audit**: Lưu vết đầy đủ mọi giao dịch
5. **Refund**: Cần thông tin để hoàn tiền
6. **Báo cáo**: Thống kê theo ngân hàng, phương thức...

---

## KẾT LUẬN

Bảng `payments` được thiết kế để:
- ✓ Lưu trữ đầy đủ thông tin thanh toán
- ✓ Tích hợp với SePay Gateway
- ✓ Hỗ trợ đối soát và audit
- ✓ Xử lý các trường hợp lỗi
- ✓ Quản lý thời gian thanh toán
- ✓ Hỗ trợ nhiều phương thức thanh toán
