# 🔐 PHÂN QUYỀN FRONTEND

## Tổng quan quyền truy cập các trang

### ✅ CUSTOMER - Khách hàng
**Layout:** Header + Footer đầy đủ (giỏ hàng, yêu thích, tìm kiếm)

**Các trang:**
- `/` - Trang chủ ✅
- `/products` - Danh sách sản phẩm ✅
- `/products/[id]` - Chi tiết sản phẩm ✅
- `/cart` - Giỏ hàng ✅
- `/checkout` - Thanh toán ✅
- `/orders` - Lịch sử đơn hàng ✅
- `/profile` - Thông tin cá nhân ✅
- `/wishlist` - Yêu thích ✅

---

### 📦 WAREHOUSE - Nhân viên kho
**Layout:** EmployeeHeader (không có giỏ hàng, yêu thích)

**Dashboard:** `/warehouse`

**Các trang có quyền:**
- `/warehouse/inventory` - Xem tồn kho ✅
- `/warehouse/import` → redirect `/admin/inventory/transactions/create?type=IMPORT` ✅
- `/warehouse/export` → redirect `/admin/inventory/transactions/create?type=EXPORT` ✅
- `/warehouse/transactions` → redirect `/admin/inventory` ✅
- `/warehouse/suppliers` → redirect `/admin/suppliers` ✅
- `/warehouse/reports` - Báo cáo kho ✅
- `/admin/inventory` - Quản lý kho ✅
- `/admin/inventory/transactions/create` - Tạo phiếu nhập/xuất ✅
- `/admin/suppliers` - Quản lý nhà cung cấp ✅

**Các trang KHÔNG có quyền:**
- `/admin/employee-approval` ❌
- `/admin/products` ❌
- `/admin/categories` ❌
- `/product-manager/*` ❌

---

### 🏷️ PRODUCT_MANAGER - Quản lý sản phẩm
**Layout:** EmployeeHeader (không có giỏ hàng, yêu thích)

**Dashboard:** `/product-manager`

**Các trang có quyền:**
- `/product-manager/inventory` - Xem tồn kho (read-only) ✅
- `/product-manager/products/publish` - Đăng bán sản phẩm ✅
- `/product-manager/products` - Quản lý sản phẩm đã đăng ✅
- `/product-manager/categories` - Quản lý danh mục ✅
- `/admin/categories` - Quản lý danh mục (shared) ✅
- `/admin/products` - Quản lý sản phẩm (shared) ✅

**Các trang KHÔNG có quyền:**
- `/admin/inventory` (chỉ xem, không nhập/xuất) ⚠️
- `/admin/inventory/transactions/create` ❌
- `/admin/suppliers` ❌
- `/admin/employee-approval` ❌
- `/warehouse/*` ❌

---

### 👑 ADMIN - Quản trị viên
**Layout:** EmployeeHeader (không có giỏ hàng, yêu thích)

**Dashboard:** `/admin`

**Các trang có quyền:** TẤT CẢ ✅
- Tất cả quyền của WAREHOUSE ✅
- Tất cả quyền của PRODUCT_MANAGER ✅
- `/admin/employee-approval` - Duyệt nhân viên ✅
- `/admin/customers` - Quản lý người dùng ✅
- `/admin/reports` - Báo cáo tổng hợp ✅
- Xóa sản phẩm/danh mục ✅

---

## 🔧 Cấu hình phân quyền

### Backend (Spring Security)
```java
// InventoryController
@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
GET /api/inventory/stock - WAREHOUSE có thể xem

// ProductController  
@PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
POST /api/products/warehouse/publish - PRODUCT_MANAGER có thể đăng bán

// CategoryController
@PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
POST /api/categories - PRODUCT_MANAGER có thể tạo danh mục
```

### Frontend (React/Next.js)
```typescript
// Kiểm tra quyền trong component
if (user?.role !== 'ADMIN' && user?.role !== 'WAREHOUSE') {
  toast.error('Chỉ quản trị viên và nhân viên kho mới có quyền truy cập')
  router.push('/')
  return
}
```

---

## 📝 Checklist đã sửa

- [x] `/admin/inventory/transactions/create` - Cho phép WAREHOUSE
- [x] `/admin/inventory` - Cho phép WAREHOUSE
- [x] `/admin/suppliers` - Cho phép WAREHOUSE
- [x] `/warehouse/*` - Layout riêng không có giỏ hàng
- [x] `/product-manager/*` - Layout riêng không có giỏ hàng
- [x] `/admin/*` - Layout riêng không có giỏ hàng
- [x] Backend `/api/inventory/stock` - Cho phép PRODUCT_MANAGER xem (read-only)

---

## 🚨 Lưu ý quan trọng

1. **WAREHOUSE** không được:
   - Đăng bán sản phẩm
   - Quản lý danh mục
   - Duyệt nhân viên

2. **PRODUCT_MANAGER** không được:
   - Nhập/xuất kho
   - Quản lý nhà cung cấp
   - Duyệt nhân viên

3. **Layout riêng biệt:**
   - Customer: Header + Footer đầy đủ
   - Employee: EmployeeHeader đơn giản, không Footer
   - Tự động phát hiện theo đường dẫn

4. **Role mapping:**
   - Backend: `Role.EMPLOYEE` + `Position.WAREHOUSE` 
   - Frontend: Chuyển thành `role: "WAREHOUSE"`
