# Fix Địa Chỉ Đầy Đủ - Hoàn Chỉnh

## Vấn đề
Địa chỉ đầy đủ (`shippingAddress`) hiển thị sai:
- Hiển thị mã ward (ví dụ: "20308") thay vì tên phường/xã (ví dụ: "Phường Yên Hòa")
- Thứ tự các trường bị sai

**Ví dụ lỗi:**
```
số 19, 470609, Huyện Hàm Tân, Bình Thuận
```

**Đúng phải là:**
```
số 19, Xã Tân Xuân, Huyện Hàm Tân, Bình Thuận
```

## Nguyên nhân
1. **Backend build địa chỉ sai**: Dùng `ward` (mã code) thay vì `wardName` (tên hiển thị)
2. **Dữ liệu cũ**: Các đơn hàng cũ chưa có `wardName`, chỉ có `ward` code

## Giải pháp đã thực hiện

### 1. Fix Backend Code ✅

#### File: `OrderServiceImpl.java`
Sửa cách build `shippingAddress` khi tạo đơn hàng mới:

```java
// OLD - SAI
String fullAddress = String.format("%s, %s, %s, %s",
        request.getAddress(), request.getWard(),  // ❌ Dùng ward code
        request.getDistrict(), request.getProvince());

// NEW - ĐÚNG
String wardDisplay = request.getWardName() != null && !request.getWardName().isEmpty() 
        ? request.getWardName()  // ✅ Ưu tiên wardName
        : request.getWard();     // Fallback to ward code
String fullAddress = String.format("%s, %s, %s, %s",
        request.getAddress(), wardDisplay, 
        request.getDistrict(), request.getProvince());
```

#### File: `InventoryServiceImpl.java`
Sửa cách build địa chỉ khi tạo đơn GHN:

```java
// OLD - SAI
String fullAddress = String.join(", ", 
    order.getAddress(),
    order.getWard() != null ? order.getWard() : "",  // ❌ Dùng ward code
    order.getDistrict(),
    order.getProvince()
);

// NEW - ĐÚNG
String wardDisplay = (order.getWardName() != null && !order.getWardName().isEmpty()) 
        ? order.getWardName()  // ✅ Ưu tiên wardName
        : order.getWard();
String fullAddress = String.join(", ", 
    order.getAddress(),
    wardDisplay != null ? wardDisplay : "",
    order.getDistrict(),
    order.getProvince()
);
```

#### File: `ShippingServiceImpl.java`
Cập nhật method `fixAllWardNames()` để cũng rebuild `shippingAddress`:

```java
if (wardOpt.isPresent()) {
    String wardName = (String) wardOpt.get().get("name");
    order.setWardName(wardName);
    
    // ✅ Rebuild shippingAddress với tên phường/xã đúng
    String newShippingAddress = String.format("%s, %s, %s, %s",
        order.getAddress(), wardName, 
        order.getDistrict(), order.getProvince());
    order.setShippingAddress(newShippingAddress);
    
    orderRepository.save(order);
}
```

### 2. Fix Dữ Liệu Cũ

#### Cách 1: Dùng Admin UI (Khuyến nghị)
1. Đăng nhập với tài khoản admin
2. Truy cập: http://localhost:3000/admin/fix-ward-names
3. Click "Bắt đầu cập nhật"
4. Chờ hoàn thành và xem kết quả

#### Cách 2: Dùng API trực tiếp
```bash
POST http://localhost:8080/api/shipping/fix-ward-names
Authorization: Bearer <ADMIN_TOKEN>
```

**Response:**
```json
{
  "success": true,
  "message": "Đã cập nhật tên phường/xã cho tất cả đơn hàng",
  "data": {
    "total": 10,
    "success": 10,
    "failed": 0,
    "errors": []
  }
}
```

### 3. Kiểm Tra Kết Quả

#### SQL Queries
Sử dụng file `check-shipping-address.sql`:

```sql
-- Xem các đơn hàng gần đây
SELECT 
    order_code,
    shipping_address,
    ward,
    ward_name
FROM orders
ORDER BY created_at DESC
LIMIT 10;

-- So sánh địa chỉ cũ và mới
SELECT 
    order_code,
    shipping_address as old_address,
    CONCAT(address, ', ', 
           COALESCE(ward_name, ward), ', ', 
           district, ', ', 
           province) as new_address
FROM orders
WHERE ward IS NOT NULL
LIMIT 10;
```

#### Kiểm tra trên UI
1. Vào: http://localhost:3000/warehouse/orders
2. Click vào một đơn hàng
3. Xem phần "Địa chỉ đầy đủ"
4. Phải hiển thị tên phường/xã, không phải mã

## Kết Quả Mong Đợi

### Trước khi fix:
```
📦 Địa chỉ đầy đủ:
số 19, 470609, Huyện Hàm Tân, Bình Thuận
```

### Sau khi fix:
```
📦 Địa chỉ đầy đủ:
số 19, Xã Tân Xuân, Huyện Hàm Tân, Bình Thuận
```

## Lưu Ý

1. **Đơn hàng mới**: Từ bây giờ sẽ tự động lưu đúng
2. **Đơn hàng cũ**: Cần chạy API fix một lần
3. **Ward code vẫn được giữ**: Để tích hợp với GHN API
4. **Không ảnh hưởng**: Không ảnh hưởng đến chức năng giao hàng

## Files Đã Thay Đổi

1. ✅ `src/main/java/com/doan/WEB_TMDT/module/order/service/impl/OrderServiceImpl.java`
2. ✅ `src/main/java/com/doan/WEB_TMDT/module/inventory/service/impl/InventoryServiceImpl.java`
3. ✅ `src/main/java/com/doan/WEB_TMDT/module/shipping/service/impl/ShippingServiceImpl.java`
4. ✅ `src/frontend/app/warehouse/orders/[id]/page.tsx`
5. ✅ `src/frontend/app/admin/fix-ward-names/page.tsx` (mới)

## Files Hỗ Trợ

1. `fix-ward-names.http` - API test
2. `check-shipping-address.sql` - SQL queries
3. `FIX-WARD-NAME-DISPLAY.md` - Hướng dẫn chi tiết
4. `FIX-SHIPPING-ADDRESS-COMPLETE.md` - Tài liệu này

## Cách Test

1. **Tạo đơn hàng mới**:
   - Vào checkout
   - Chọn địa chỉ đầy đủ (tỉnh, huyện, xã)
   - Đặt hàng
   - Kiểm tra địa chỉ trong warehouse orders

2. **Fix đơn hàng cũ**:
   - Chạy API fix
   - Kiểm tra lại các đơn hàng cũ
   - Địa chỉ phải hiển thị tên xã, không phải mã

## Troubleshooting

### Vẫn hiển thị mã ward?
- Chạy lại API fix: `POST /api/shipping/fix-ward-names`
- Kiểm tra database xem `ward_name` đã được cập nhật chưa
- Refresh lại trang

### API fix báo lỗi?
- Kiểm tra GHN API có hoạt động không
- Xem log backend để biết chi tiết lỗi
- Có thể một số ward code không tồn tại trong GHN

### Đơn hàng mới vẫn sai?
- Kiểm tra frontend có gửi `wardName` không
- Xem console log khi checkout
- Kiểm tra backend có nhận được `wardName` không
