# 🎭 PHÂN CHIA GIAO DIỆN THEO VAI TRÒ

## 📋 Tổng quan

Hệ thống đã được phân chia giao diện riêng biệt cho từng vai trò với các chức năng phù hợp.

---

## 👤 CUSTOMER - Khách hàng

### Dashboard: `/`
**Chức năng:**
- Xem sản phẩm
- Tìm kiếm sản phẩm
- Thêm vào giỏ hàng
- Mua hàng
- Xem lịch sử đơn hàng

**Các trang:**
- `/` - Trang chủ
- `/products` - Danh sách sản phẩm
- `/products/[id]` - Chi tiết sản phẩm
- `/cart` - Giỏ hàng
- `/checkout` - Thanh toán
- `/orders` - Lịch sử đơn hàng
- `/profile` - Thông tin cá nhân

---

## 📦 WAREHOUSE - Nhân viên kho

### Dashboard: `/warehouse`
**Chức năng:**
- ✅ Quản lý tồn kho (xem, nhập, xuất)
- ✅ Tạo phiếu nhập kho
- ✅ Tạo phiếu xuất kho
- ✅ Xem lịch sử giao dịch
- ✅ Quản lý nhà cung cấp
- ❌ KHÔNG được đăng bán sản phẩm
- ❌ KHÔNG được quản lý danh mục

**Các trang:**
- `/warehouse` - Dashboard kho hàng
- `/warehouse/inventory` - Tồn kho
- `/warehouse/import/create` → redirect to `/admin/inventory/transactions/create?type=IMPORT`
- `/warehouse/export/create` → redirect to `/admin/inventory/transactions/create?type=EXPORT`
- `/warehouse/transactions` → redirect to `/admin/inventory`
- `/warehouse/suppliers` - Quản lý nhà cung cấp (TODO)
- `/warehouse/reports` - Báo cáo kho (TODO)

**Thống kê hiển thị:**
- Tổng sản phẩm trong kho
- Sản phẩm sắp hết hàng
- Phiếu nhập chờ xử lý
- Phiếu xuất chờ xử lý

---

## 🏷️ PRODUCT_MANAGER - Quản lý sản phẩm

### Dashboard: `/product-manager`
**Chức năng:**
- ✅ Xem tồn kho (read-only)
- ✅ Đăng bán sản phẩm từ kho
- ✅ Chỉnh sửa thông tin sản phẩm
- ✅ Quản lý danh mục (CRUD)
- ❌ KHÔNG được nhập/xuất kho
- ❌ KHÔNG được xóa sản phẩm (chỉ ADMIN)

**Các trang:**
- `/product-manager` - Dashboard sản phẩm
- `/product-manager/inventory` - Xem tồn kho (read-only)
- `/product-manager/products/publish` - Đăng bán sản phẩm
- `/product-manager/products` - Quản lý sản phẩm đã đăng
- `/product-manager/categories` - Quản lý danh mục

**Thống kê hiển thị:**
- Sản phẩm trong kho (chưa đăng bán)
- Sản phẩm đã đăng bán
- Số lượng danh mục
- Sản phẩm sắp hết hàng

---

## 👑 ADMIN - Quản trị viên

### Dashboard: `/admin`
**Chức năng:**
- ✅ TẤT CẢ quyền của WAREHOUSE
- ✅ TẤT CẢ quyền của PRODUCT_MANAGER
- ✅ Duyệt nhân viên
- ✅ Quản lý người dùng
- ✅ Xóa sản phẩm/danh mục
- ✅ Xem tất cả báo cáo

**Các trang:**

### Quản trị hệ thống:
- `/admin` - Dashboard tổng quan
- `/admin/employee-approval` - Duyệt nhân viên
- `/admin/customers` - Quản lý người dùng
- `/admin/reports` - Báo cáo tổng hợp

### Quản lý kho (như WAREHOUSE):
- `/admin/inventory` - Quản lý kho
- `/admin/inventory/transactions/create` - Tạo phiếu nhập/xuất
- `/admin/suppliers` - Nhà cung cấp

### Quản lý sản phẩm (như PRODUCT_MANAGER):
- `/admin/products/publish` - Đăng bán sản phẩm
- `/admin/products` - Quản lý sản phẩm
- `/admin/categories` - Quản lý danh mục
- `/admin/orders` - Quản lý đơn hàng

**Thống kê hiển thị:**
- Tổng đơn hàng
- Doanh thu
- Tổng sản phẩm
- Tổng khách hàng
- Yêu cầu duyệt nhân viên
- Sản phẩm sắp hết

---

## 🔄 Auto Redirect

Component `RoleBasedRedirect` tự động điều hướng người dùng đến dashboard phù hợp:

```typescript
ADMIN → /admin
WAREHOUSE → /warehouse
PRODUCT_MANAGER → /product-manager
CUSTOMER → /
```

---

## 🎨 Phân biệt giao diện

### WAREHOUSE Dashboard
- **Màu chủ đạo:** Xanh lá (nhập), Xanh dương (xuất)
- **Icon:** FiPackage, FiDownload, FiUpload
- **Focus:** Nhập xuất tồn kho

### PRODUCT_MANAGER Dashboard
- **Màu chủ đạo:** Đỏ (đăng bán), Tím (danh mục)
- **Icon:** FiTag, FiTrendingUp, FiEye
- **Focus:** Đăng bán và phân loại sản phẩm

### ADMIN Dashboard
- **Màu chủ đạo:** Đa dạng (tất cả chức năng)
- **Icon:** Tất cả
- **Focus:** Quản trị toàn diện
- **Layout:** Chia thành 3 nhóm chức năng rõ ràng

---

## 📝 Lưu ý

1. **Warehouse** không thể truy cập `/product-manager/*`
2. **Product Manager** không thể truy cập `/warehouse/import` hoặc `/warehouse/export`
3. **Admin** có thể truy cập tất cả
4. Các trang redirect được tạo để tái sử dụng code giữa các role
5. Mỗi dashboard có thống kê riêng phù hợp với vai trò

---

## ✅ Đã hoàn thành

- [x] Dashboard WAREHOUSE
- [x] Dashboard PRODUCT_MANAGER
- [x] Dashboard ADMIN (cập nhật)
- [x] Trang tồn kho cho WAREHOUSE
- [x] Trang tồn kho (read-only) cho PRODUCT_MANAGER
- [x] Trang đăng bán sản phẩm cho PRODUCT_MANAGER
- [x] Trang quản lý danh mục cho PRODUCT_MANAGER
- [x] Component RoleBasedRedirect
- [x] Các trang redirect

## 🚧 Cần làm tiếp

- [ ] Trang quản lý nhà cung cấp cho WAREHOUSE
- [ ] Trang báo cáo cho WAREHOUSE
- [ ] Trang báo cáo tổng hợp cho ADMIN
- [ ] Trang quản lý người dùng cho ADMIN
- [ ] Trang quản lý đơn hàng cho ADMIN

