# Sửa Lỗi Các Trường Null Trong Response GHN API

## Vấn Đề
Khi gọi API Giao Hàng Nhanh (GHN) để tạo đơn hàng, một số trường trả về bị `null` mặc dù vẫn có giá ship:
- `expectedDeliveryTime` → null
- `sortCode` → null  
- `totalFee` → null (mặc dù có phí ship)

## Nguyên Nhân

### 1. Tên Trường Không Khớp
GHN API có thể sử dụng tên trường khác với những gì code đang expect:
- `total_fee` vs `fee` vs `service_fee`
- `expected_delivery_time` có thể không có trong response tạo đơn

### 2. Định Dạng Dữ Liệu Khác Nhau
- Timestamp có thể là Unix timestamp (số) hoặc ISO string
- Fee có thể là Integer hoặc Double

### 3. Thiếu Xử Lý Null-Safe
Code cũ không kiểm tra null đầy đủ trước khi parse.

## Giải Pháp Đã Áp Dụng

### 1. Cải Thiện Logging
```java
log.info("📦 GHN Response Data Keys: {}", data.keySet());
log.info("📦 GHN Response Data: {}", data);
```
→ Giúp xem chính xác các trường GHN trả về

### 2. Xử Lý Nhiều Tên Trường
```java
// Try multiple field names for fee
Object feeValue = data.get("total_fee");
if (feeValue == null) {
    feeValue = data.get("fee");
}
if (feeValue == null) {
    feeValue = data.get("service_fee");
}
```

### 3. Parse Timestamp An Toàn
```java
if (timeValue instanceof Number) {
    // Unix timestamp
    long timestamp = ((Number) timeValue).longValue();
    expectedDeliveryTime = LocalDateTime.ofInstant(
        Instant.ofEpochSecond(timestamp), 
        ZoneId.systemDefault()
    );
} else if (timeValue instanceof String) {
    // Try ISO format or timestamp string
    // ...
}
```

### 4. Logging Chi Tiết
```java
log.info("✅ GHN order created successfully!");
log.info("   - Order Code: {}", orderCode);
log.info("   - Sort Code: {}", sortCode != null ? sortCode : "N/A");
log.info("   - Total Fee: {}", totalFee != null ? totalFee : "N/A");
log.info("   - Expected Delivery: {}", expectedDeliveryTime != null ? expectedDeliveryTime : "N/A");
```

## Cách Test

### 1. Tạo Đơn Hàng Test
```bash
# Sử dụng file test-ghn-integration.http
# Test case 4 hoặc 5
```

### 2. Kiểm Tra Log
Xem console log để thấy:
```
📦 GHN Response Data Keys: [order_code, sort_code, trans_type, ...]
📦 GHN Response Data: {order_code=GHNXXX, ...}
✅ Parsed total_fee: 30000.0
✅ GHN order created successfully!
   - Order Code: GHNXXX
   - Sort Code: N/A
   - Total Fee: 30000.0
   - Expected Delivery: N/A
```

### 3. Kiểm Tra Database
```sql
SELECT 
    order_code,
    ghn_order_code,
    ghn_shipping_status,
    ghn_expected_delivery_time,
    shipping_fee
FROM orders
WHERE ghn_order_code IS NOT NULL
ORDER BY created_at DESC
LIMIT 5;
```

## Các Trường Hợp Đặc Biệt

### Trường Hợp 1: totalFee Vẫn Null
**Nguyên nhân**: GHN không trả về fee trong response tạo đơn, chỉ trả về khi tính phí.

**Giải pháp**: Sử dụng `shippingFee` từ request ban đầu (đã tính trước đó).

```java
// In OrderServiceImpl
savedOrder.setShippingFee(shippingFee); // Use calculated fee
// Don't rely on ghnResponse.getTotalFee()
```

### Trường Hợp 2: expectedDeliveryTime Null
**Nguyên nhân**: GHN không trả về thời gian dự kiến trong response tạo đơn.

**Giải pháp**: 
- Tính toán dựa trên lead time đã lấy trước đó
- Hoặc gọi API detail sau khi tạo đơn

```java
// Option 1: Use lead time from fee calculation
// Option 2: Call getGHNOrderDetail after creation
```

### Trường Hợp 3: sortCode Null
**Nguyên nhân**: Một số loại dịch vụ GHN không có sort code.

**Giải pháp**: Chấp nhận null, không bắt buộc.

## Checklist Kiểm Tra

- [ ] Log hiển thị đầy đủ response keys từ GHN
- [ ] orderCode không null (bắt buộc)
- [ ] totalFee được parse đúng (hoặc null nếu GHN không trả về)
- [ ] expectedDeliveryTime được parse đúng (hoặc null)
- [ ] sortCode được parse đúng (hoặc null)
- [ ] Không có exception khi parse
- [ ] Order được lưu vào DB với ghn_order_code

## Tài Liệu Tham Khảo

### GHN API Documentation
- Create Order: https://api.ghn.vn/home/docs/detail?id=123
- Response fields có thể thay đổi theo version API

### Code Files
- `ShippingServiceImpl.java` - Line 454-580
- `OrderServiceImpl.java` - Line 170-210
- `CreateGHNOrderResponse.java` - DTO class

## Lưu Ý Quan Trọng

1. **Không dựa vào totalFee từ response tạo đơn**: Sử dụng fee đã tính trước đó
2. **expectedDeliveryTime có thể null**: Tính toán từ lead time hoặc để null
3. **sortCode không quan trọng**: Có thể null
4. **orderCode là duy nhất bắt buộc**: Phải có để tracking

## Next Steps

Nếu vẫn gặp vấn đề:

1. Kiểm tra GHN API version đang dùng
2. Xem GHN documentation mới nhất
3. Test với Postman để xem raw response
4. Liên hệ GHN support để xác nhận response format
