# 🐛 DEBUG: Tại sao GHN không được tạo?

## 📊 PHÂN TÍCH DATA

Từ data bạn cung cấp, **TẤT CẢ 6 đơn hàng** đều có:
- ✅ `shippingFee: 20500` (có phí ship)
- ✅ Địa chỉ ngoại thành: Lạng Sơn, Quảng Ninh, Thái Nguyên, Bắc Giang
- ❌ `ghnOrderCode: null`
- ❌ `ghnShippingStatus: null`
- ❌ `ghnExpectedDeliveryTime: null`

## 🔍 NGUYÊN NHÂN

### **Code logic trong `OrderServiceImpl.java`:**

```java
// 8. Create GHN order (if not free ship)
if (shippingFee > 0 && !shippingService.isHanoiInnerCity(request.getProvince(), request.getDistrict())) {
    try {
        log.info("Creating GHN order for {}", orderCode);
        
        // ... GHN API call ...
        
        log.info("✅ GHN order created: {}", ghnResponse.getOrderCode());
        
    } catch (Exception e) {
        log.error("❌ Failed to create GHN order for {}: {}", orderCode, e.getMessage());
        // Don't fail the whole order, just log the error
        // Admin can manually create GHN order later
    }
}
```

**Điều kiện để gọi GHN:**
1. ✅ `shippingFee > 0` → **PASS** (tất cả đơn đều có phí 20500)
2. ✅ `!isHanoiInnerCity()` → **PASS** (Lạng Sơn, Quảng Ninh không phải nội thành HN)

**→ Code PHẢI gọi GHN API!**

Nhưng `ghnOrderCode` vẫn null → **Exception xảy ra và bị catch!**

---

## 🎯 CÁC NGUYÊN NHÂN CÓ THỂ

### **1. GHN API Token không hợp lệ**
```properties
ghn.api.token=76016947-d1a8-11f0-a3d6-dac90fb956b5
```
- Token có thể hết hạn
- Token không có quyền tạo đơn
- Token thuộc môi trường test nhưng gọi production API

### **2. GHN Shop ID không đúng**
```properties
ghn.shop.id=198347
```
- Shop ID không tồn tại
- Shop ID không khớp với Token

### **3. District ID không tìm thấy**

Method `getDistrictIdForGHN()` trong `OrderServiceImpl`:

```java
private Integer getDistrictIdForGHN(String province, String district) {
    try {
        // ... call shippingService.calculateShippingFee() ...
        return 1485; // Default Hà Đông
    } catch (Exception e) {
        log.warn("Could not get district ID, using default: {}", e.getMessage());
        return 1485;
    }
}
```

**Vấn đề:** Method này luôn return `1485` (Hà Đông) cho TẤT CẢ địa chỉ!

**Ví dụ:**
- Lạng Sơn → District ID = 1485 (SAI! Phải là district ID của Lạng Sơn)
- Quảng Ninh → District ID = 1485 (SAI!)

→ **GHN API sẽ reject vì district ID không hợp lệ!**

### **4. GHN API trả về lỗi**

Có thể thiếu thông tin bắt buộc:
- `to_ward_code` (mã phường/xã) - đang để rỗng `""`
- `to_district_id` - sai (luôn là 1485)
- `service_type_id` - không hợp lệ
- `payment_type_id` - không hợp lệ

---

## 🔧 CÁCH KIỂM TRA

### **Bước 1: Xem log backend**

Khi tạo đơn hàng, backend sẽ log:

```
Creating GHN order for ORD202512196983
=== GHN Create Order API Request ===
URL: https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create
Request body: {...}
```

Nếu có lỗi:
```
❌ Failed to create GHN order for ORD202512196983: [Chi tiết lỗi]
```

**→ Hãy tìm log này để biết lỗi chính xác!**

### **Bước 2: Test GHN API thủ công**

Tạo file `test-ghn-create-order.http`:

```http
### Test GHN Create Order API
POST https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create
Content-Type: application/json
Token: 76016947-d1a8-11f0-a3d6-dac90fb956b5
ShopId: 198347

{
  "to_name": "Nguyễn Văn A",
  "to_phone": "0123456789",
  "to_address": "123 Đường ABC, Lộc Bình, Lạng Sơn",
  "to_ward_code": "",
  "to_district_id": 1485,
  "note": "Test order",
  "cod_amount": 50000,
  "weight": 1000,
  "length": 20,
  "width": 20,
  "height": 10,
  "service_type_id": 2,
  "payment_type_id": 2,
  "items": [
    {
      "name": "Test Product",
      "code": "TEST-001",
      "quantity": 1,
      "price": 50000
    }
  ]
}
```

**Chạy request này và xem response:**
- Nếu thành công → Vấn đề ở code
- Nếu lỗi → Xem message lỗi từ GHN

### **Bước 3: Kiểm tra District ID**

Test API lấy District ID của Lạng Sơn:

```http
### Get Province ID
POST https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/province
Content-Type: application/json
Token: 76016947-d1a8-11f0-a3d6-dac90fb956b5

### Get District ID (sau khi có Province ID)
POST https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/district
Content-Type: application/json
Token: 76016947-d1a8-11f0-a3d6-dac90fb956b5

{
  "province_id": 269
}
```

---

## 🛠️ GIẢI PHÁP

### **Giải pháp 1: Fix method `getDistrictIdForGHN()`**

Thay vì luôn return `1485`, phải gọi đúng API GHN:

```java
private Integer getDistrictIdForGHN(String province, String district) {
    try {
        // Call ShippingService to get correct district ID
        return shippingService.getDistrictId(province, district);
    } catch (Exception e) {
        log.error("Could not get district ID for {}, {}: {}", province, district, e.getMessage());
        throw new RuntimeException("Không thể xác định địa chỉ giao hàng");
    }
}
```

**Và thêm method public trong `ShippingService`:**

```java
public interface ShippingService {
    // ... existing methods ...
    
    Integer getDistrictId(String province, String district);
}
```

### **Giải pháp 2: Thêm log chi tiết**

Thay đổi catch block:

```java
} catch (Exception e) {
    log.error("❌ Failed to create GHN order for {}", orderCode, e);
    log.error("Exception type: {}", e.getClass().getName());
    log.error("Exception message: {}", e.getMessage());
    if (e.getCause() != null) {
        log.error("Cause: {}", e.getCause().getMessage());
    }
    // Don't fail the whole order, just log the error
}
```

### **Giải pháp 3: Validate trước khi gọi GHN**

```java
// Validate district ID before calling GHN
Integer districtId = getDistrictIdForGHN(request.getProvince(), request.getDistrict());
if (districtId == null || districtId == 1485) {
    log.warn("⚠️ Invalid district ID for {}, {} - skipping GHN", 
        request.getProvince(), request.getDistrict());
    // Don't create GHN order
} else {
    // Create GHN order with correct district ID
    // ...
}
```

---

## 📝 CHECKLIST DEBUG

- [ ] **Xem log backend** khi tạo đơn hàng
- [ ] **Test GHN API** bằng Postman/HTTP file
- [ ] **Kiểm tra GHN Token** còn hạn không
- [ ] **Kiểm tra GHN Shop ID** có đúng không
- [ ] **Test lấy District ID** của Lạng Sơn, Quảng Ninh
- [ ] **Fix method `getDistrictIdForGHN()`** để trả về đúng ID
- [ ] **Thêm log chi tiết** trong catch block
- [ ] **Test lại** tạo đơn hàng mới

---

## 🎯 KẾT LUẬN

**Nguyên nhân chính:** Method `getDistrictIdForGHN()` luôn return `1485` (Hà Đông) cho mọi địa chỉ, khiến GHN API reject request.

**Giải pháp:** Sửa method này để gọi đúng `shippingService.getDistrictId()` và lấy district ID chính xác.

**Bước tiếp theo:** Xem log backend để xác nhận lỗi chính xác!
