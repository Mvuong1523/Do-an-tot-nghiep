# Hướng dẫn kiểm tra Employee Warehouse Fix

## Bước 1: Đăng xuất và đăng nhập lại
**QUAN TRỌNG:** Bạn PHẢI đăng xuất và đăng nhập lại để token mới có đầy đủ thông tin employee.

1. Click vào avatar/tên người dùng ở góc phải
2. Chọn "Đăng xuất"
3. Đăng nhập lại với tài khoản nhân viên kho

## Bước 2: Kiểm tra trang Tổng quan Kho
1. Truy cập: `http://localhost:3000/employee/warehouse`
2. Bạn sẽ thấy một **Debug Box màu xám** ở đầu trang (chỉ hiện trong development mode)

### Debug Box sẽ hiển thị:
```json
{
  "employee": {
    "fullName": "Tên nhân viên",
    "phone": "0123456789",
    "address": "Địa chỉ",
    "position": "WAREHOUSE",
    "firstLogin": false
  },
  "position": "WAREHOUSE",
  "canImport": true,
  "canExport": true,
  "inventoryCount": 5
}
```

### Kiểm tra:
- ✅ `employee` object có dữ liệu (không phải null)
- ✅ `position` = "WAREHOUSE"
- ✅ `canImport` = true
- ✅ `canExport` = true
- ✅ `inventoryCount` > 0 (nếu có sản phẩm trong kho)

## Bước 3: Kiểm tra Console (F12)
Mở Developer Tools (F12) và xem Console tab:

```
🔍 DEBUG - Employee data: { fullName: "...", position: "WAREHOUSE", ... }
🔍 DEBUG - Employee position: WAREHOUSE
🔍 DEBUG - Can import: true
🔍 DEBUG - Can export: true
🔍 DEBUG - API Response: { success: true, data: [...] }
🔍 DEBUG - Mapped inventory: [...]
```

## Bước 4: Kiểm tra giao diện
### Các nút phải hiển thị:
- ✅ Nút "Đơn cần xuất" (màu cam)
- ✅ Nút "Nhập hàng" (màu xanh lá)
- ✅ Nút "Xuất hàng" (màu xanh dương)

### Bảng tồn kho:
- ✅ Hiển thị danh sách sản phẩm (không còn "Chưa có sản phẩm nào")
- ✅ Các cột: Sản phẩm, SKU, Nhà cung cấp, Tồn kho, Đã giữ, Hỏng, Có thể bán, Trạng thái

## Bước 5: Kiểm tra chức năng
1. Click vào nút "Nhập hàng" → Phải chuyển đến `/employee/warehouse/import/create`
2. Click vào nút "Xuất hàng" → Phải chuyển đến `/employee/warehouse/export/create`
3. Click vào tab "Phiếu xuất nhập" → Phải hiển thị danh sách phiếu

## Nếu vẫn có lỗi

### Lỗi: employee = null
**Nguyên nhân:** Chưa đăng nhập lại
**Giải pháp:** Đăng xuất và đăng nhập lại

### Lỗi: position = undefined
**Nguyên nhân:** Tài khoản không có position trong database
**Giải pháp:** Kiểm tra database:
```sql
SELECT u.id, u.email, u.role, e.position 
FROM users u 
LEFT JOIN employees e ON u.id = e.user_id 
WHERE u.email = 'email@example.com';
```

### Lỗi: canImport = false, canExport = false
**Nguyên nhân:** Position không phải WAREHOUSE
**Giải pháp:** Cập nhật position trong database:
```sql
UPDATE employees 
SET position = 'WAREHOUSE' 
WHERE user_id = (SELECT id FROM users WHERE email = 'email@example.com');
```

### Lỗi: inventoryCount = 0
**Nguyên nhân:** Chưa có dữ liệu trong kho
**Giải pháp:** Tạo phiếu nhập kho hoặc chạy SQL:
```sql
-- Kiểm tra dữ liệu kho
SELECT * FROM warehouse_stock;
SELECT * FROM warehouse_products;
```

## Sau khi kiểm tra xong
Nếu mọi thứ hoạt động tốt, bạn có thể:
1. Xóa debug box trong `src/frontend/app/employee/warehouse/page.tsx`
2. Xóa các dòng `console.log` debug
3. Hoặc giữ lại để debug sau này (chỉ hiện trong development mode)

## Liên hệ
Nếu vẫn gặp vấn đề, cung cấp:
1. Screenshot của Debug Box
2. Console logs (F12)
3. Thông tin tài khoản đang dùng (email, role, position)
