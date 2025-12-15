# ⚡ Kiểm tra nhanh tích hợp GHN

## 🎯 Vấn đề: Đặt hàng xong không thấy mã GHN

### ✅ Kiểm tra 3 điều này TRƯỚC:

#### 1️⃣ Địa chỉ giao hàng có phải nội thành HN không?

**Nội thành HN (KHÔNG tạo GHN):**
- Ba Đình
- Hoàn Kiếm  
- Đống Đa
- Hai Bà Trưng
- Cầu Giấy
- Tây Hồ
- Thanh Xuân

➡️ **Nếu đặt hàng ở các quận này → KHÔNG có mã GHN (đúng behavior)**

#### 2️⃣ Phí ship có > 0 không?

```sql
SELECT order_code, shipping_fee, ghn_order_code 
FROM orders 
ORDER BY created_at DESC 
LIMIT 5;
```

➡️ **Nếu `shipping_fee = 0` → KHÔNG tạo GHN**

#### 3️⃣ Xem logs khi đặt hàng

Tìm trong console:
```
✅ Thành công:
Creating GHN order for ORD20231212001
✅ GHN order created: GHNABCD1234

❌ Lỗi:
❌ Failed to create GHN order for ORD20231212001: [error]
```

---

## 🧪 Test nhanh (3 phút)

### Test 1: Đặt hàng ngoài HN

```bash
# Dùng Postman hoặc REST Client
POST http://localhost:8080/api/orders
Authorization: Bearer YOUR_TOKEN
Content-Type: application/json

{
  "province": "Bắc Ninh",
  "district": "Từ Sơn",
  "ward": "Đông Ngàn",
  "address": "123 Test Street",
  "note": "Test GHN",
  "shippingFee": 30000,
  "paymentMethod": "COD"
}
```

**Kiểm tra response:**
```json
{
  "data": {
    "ghnOrderCode": "GHNABCD1234",  // ← Phải có giá trị
    "ghnShippingStatus": "created",
    "ghnCreatedAt": "2023-12-12T10:30:00"
  }
}
```

### Test 2: Kiểm tra database

```sql
-- Xem đơn hàng mới nhất
SELECT 
    order_code,
    shipping_fee,
    ghn_order_code,
    ghn_shipping_status,
    created_at
FROM orders 
ORDER BY created_at DESC 
LIMIT 1;
```

**Kết quả mong đợi:**
- `ghn_order_code`: Có giá trị (VD: "GHNABCD1234")
- `ghn_shipping_status`: "created"

### Test 3: Xem trạng thái GHN

```bash
GET http://localhost:8080/api/orders/{orderId}/shipping-status
Authorization: Bearer YOUR_TOKEN
```

**Nếu thành công:**
```json
{
  "success": true,
  "data": {
    "orderCode": "GHNABCD1234",
    "status": "ready_to_pick",
    "statusText": "Chờ lấy hàng"
  }
}
```

**Nếu lỗi:**
```json
{
  "success": false,
  "message": "Đơn hàng này không có mã vận đơn GHN"
}
```

---

## 🔧 Fix nhanh nếu không hoạt động

### Fix 1: Kiểm tra config GHN

File: `src/main/resources/application.properties`

```properties
ghn.api.url=https://dev-online-gateway.ghn.vn/shiip/public-api
ghn.api.token=76016947-d1a8-11f0-a3d6-dac90fb956b5
ghn.shop.id=198347
ghn.pick.district.id=1485
```

### Fix 2: Test token GHN

```bash
curl -X POST https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee \
  -H "Token: 76016947-d1a8-11f0-a3d6-dac90fb956b5" \
  -H "ShopId: 198347" \
  -H "Content-Type: application/json" \
  -d '{"service_type_id":2,"from_district_id":1485,"to_district_id":1542,"weight":1000,"insurance_value":0}'
```

**Nếu trả về `{"code": 200}` → Token OK**

### Fix 3: Restart server

```bash
# Stop server (Ctrl+C)
# Start lại
mvn spring-boot:run
```

---

## 📊 Kết quả mong đợi

### ✅ Đơn hàng ngoài HN (Bắc Ninh, Hải Phòng...)
- `ghnOrderCode`: ✅ Có giá trị
- `ghnShippingStatus`: ✅ "created"
- `shippingFee`: ✅ > 0
- Logs: ✅ "Creating GHN order"

### ✅ Đơn hàng nội thành HN (Ba Đình, Hoàn Kiếm...)
- `ghnOrderCode`: ✅ NULL (đúng)
- `ghnShippingStatus`: ✅ NULL (đúng)
- `shippingFee`: ✅ = 0
- Logs: ✅ Không có "Creating GHN order"

---

## 🆘 Vẫn không hoạt động?

Cung cấp thông tin sau:

1. **Địa chỉ giao hàng đã test:**
   - Province: ?
   - District: ?

2. **Response từ API `/api/orders`:**
   ```json
   // Paste response ở đây
   ```

3. **Logs từ console:**
   ```
   // Paste logs ở đây
   ```

4. **Kết quả query database:**
   ```sql
   SELECT * FROM orders ORDER BY created_at DESC LIMIT 1;
   ```

---

## 📚 Tài liệu chi tiết

- [GHN-TROUBLESHOOTING.md](./GHN-TROUBLESHOOTING.md) - Hướng dẫn debug chi tiết
- [GHN-INTEGRATION-COMPLETE.md](./GHN-INTEGRATION-COMPLETE.md) - Tài liệu tích hợp đầy đủ
- [test-ghn-integration.http](./test-ghn-integration.http) - Test cases
- [check-ghn-orders.sql](./check-ghn-orders.sql) - SQL queries

---

**TL;DR:** Nếu đặt hàng nội thành HN → Không có mã GHN (đúng). Test với địa chỉ ngoài HN (Bắc Ninh, Hải Phòng...) để thấy mã GHN được tạo.
