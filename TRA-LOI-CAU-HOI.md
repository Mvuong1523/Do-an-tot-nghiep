# ❓ Trả lời câu hỏi: "Đặt hàng xong không thấy có gì thay đổi"

## 🎯 Câu trả lời ngắn gọn

**Đơn GHN CHỈ được tạo khi giao hàng NGOÀI nội thành Hà Nội.**

Nếu bạn đặt hàng ở các quận:
- Ba Đình, Hoàn Kiếm, Đống Đa, Hai Bà Trưng, Cầu Giấy, Tây Hồ, Thanh Xuân

→ **KHÔNG có mã GHN** (giao hàng nội bộ, miễn phí ship)

---

## ✅ Code đã đầy đủ

Tích hợp GHN đã hoàn chỉnh:
- ✅ Tạo đơn GHN tự động
- ✅ Xem trạng thái vận chuyển
- ✅ Webhook auto-update từ GHN

**KHÔNG thiếu phần xử lý nào!**

---

## 🧪 Test để thấy mã GHN

### Đặt hàng với địa chỉ ngoài HN:

```json
POST /api/orders
{
  "province": "Bắc Ninh",
  "district": "Từ Sơn",
  "address": "123 Test",
  "shippingFee": 30000,
  "paymentMethod": "COD"
}
```

**Response sẽ có:**
```json
{
  "ghnOrderCode": "GHNABCD1234",
  "ghnShippingStatus": "created"
}
```

---

## 🔍 Kiểm tra nhanh

### 1. Xem đơn hàng vừa tạo:

```sql
SELECT 
    order_code,
    shipping_address,
    shipping_fee,
    ghn_order_code
FROM orders 
ORDER BY created_at DESC 
LIMIT 1;
```

### 2. Kiểm tra kết quả:

**Nếu địa chỉ nội thành HN:**
- `shipping_fee` = 0
- `ghn_order_code` = NULL ← **Đúng behavior**

**Nếu địa chỉ ngoài HN:**
- `shipping_fee` > 0
- `ghn_order_code` = "GHNABCD1234" ← **Có mã GHN**

---

## 📊 Về việc update trạng thái

### Có 2 cách update:

#### 1. GHN tự động update (Webhook) ✅
- GHN push thông báo về server khi có thay đổi
- Endpoint: `POST /api/webhooks/ghn`
- Tự động cập nhật `ghnShippingStatus` và `Order.status`

**Cần đăng ký webhook URL trên GHN Dashboard:**
```
https://your-domain.com/api/webhooks/ghn
```

#### 2. Tự query thủ công ✅
- API: `GET /api/orders/{id}/shipping-status`
- Gọi GHN API để lấy status mới nhất
- Tự động cập nhật vào database

---

## 🎯 Kết luận

### Không thiếu phần xử lý!

Code đã đầy đủ cả 3 bước:
1. ✅ Tạo đơn GHN khi đặt hàng
2. ✅ API xem trạng thái
3. ✅ Webhook auto-update

### Vấn đề thực sự:

**Đặt hàng nội thành HN → Không tạo GHN (đây là behavior đúng)**

### Giải pháp:

Test với địa chỉ ngoài HN (Bắc Ninh, Hải Phòng...) để thấy mã GHN được tạo.

---

## 📁 Tài liệu hỗ trợ

- **QUICK-CHECK-GHN.md** - Checklist 3 phút
- **GHN-TROUBLESHOOTING.md** - Debug chi tiết
- **GHN-STATUS-CHECK.md** - Tổng hợp tình trạng
- **test-ghn-integration.http** - Test cases
- **check-ghn-orders.sql** - SQL queries

---

**TL;DR:** Code đã đủ. Đơn GHN chỉ tạo khi giao ngoài nội thành HN. Test với địa chỉ Bắc Ninh để thấy mã GHN.
