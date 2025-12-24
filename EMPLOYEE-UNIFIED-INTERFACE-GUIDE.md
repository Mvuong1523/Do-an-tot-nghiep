# ✅ Hướng dẫn Giao diện Nhân viên Thống nhất

## 📋 Tổng quan

Hệ thống đã được chuyển đổi sang **giao diện nhân viên thống nhất** - tất cả nhân viên (bao gồm PRODUCT_MANAGER, WAREHOUSE, ACCOUNTANT, SALE, CSKH, SHIPPER) đều sử dụng chung một giao diện tại `/employee`.

## 🎯 Thay đổi chính

### 1. Xóa route riêng `/product-manager`
- ❌ **Trước đây**: PRODUCT_MANAGER có route riêng `/product-manager`
- ✅ **Bây giờ**: Tất cả nhân viên dùng chung `/employee`

### 2. Unified Employee Interface
- Tất cả nhân viên đăng nhập → redirect về `/employee`
- Sidebar menu hiển thị tất cả chức năng
- Permission check để ẩn/hiện nút action

### 3. Xóa code legacy
- Đã xóa thư mục `src/frontend/app/product-manager/`
- Đã xóa case `PRODUCT_MANAGER` trong navigation components
- Đã xóa tất cả reference đến `/product-manager`

## 📂 Files đã sửa

### 1. Navigation Components

#### `src/frontend/components/layout/HorizontalNav.tsx`
**Thay đổi:**
- ❌ Xóa case `PRODUCT_MANAGER` trong `getMenuItems()`
- ❌ Xóa case `PRODUCT_MANAGER` trong `getRoleName()`
- ✅ Cập nhật interface: `role: 'WAREHOUSE' | 'ADMIN' | 'ACCOUNTANT' | 'SALES'`

**Trước:**
```typescript
case 'PRODUCT_MANAGER':
  return [
    { name: 'Dashboard', icon: FiHome, path: '/product-manager' },
    {
      name: 'Sản phẩm',
      icon: FiPackage,
      children: [
        { name: 'Đăng bán', path: '/product-manager/products/publish' },
        { name: 'Danh sách', path: '/product-manager/products' },
      ]
    },
    { name: 'Danh mục', icon: FiTag, path: '/product-manager/categories' },
  ]
```

**Sau:**
```typescript
// Case PRODUCT_MANAGER đã bị xóa hoàn toàn
```

#### `src/frontend/components/layout/EmployeeHeader.tsx`
**Thay đổi:**
- ❌ Xóa case `PRODUCT_MANAGER` trong `getNavigationLinks()`
- ❌ Xóa case `PRODUCT_MANAGER` trong `getRoleName()`
- ✅ Cập nhật interface: `role: 'WAREHOUSE' | 'ADMIN'`

**Trước:**
```typescript
case 'PRODUCT_MANAGER':
  return [
    { name: 'Dashboard', href: '/product-manager', icon: FiHome },
    { name: 'Đăng bán', href: '/product-manager/products/publish', icon: FiPackage },
    { name: 'Sản phẩm', href: '/product-manager/products', icon: FiPackage },
    { name: 'Danh mục', href: '/product-manager/categories', icon: FiTag },
  ]
```

**Sau:**
```typescript
// Case PRODUCT_MANAGER đã bị xóa hoàn toàn
```

### 2. Redirect Components (Đã sửa trước đó)

#### `src/frontend/components/RootLayoutClient.tsx`
- ✅ Đã xóa check `/product-manager`

#### `src/frontend/components/EmployeeBreadcrumb.tsx`
- ✅ Tất cả position redirect về `/employee`

#### `src/frontend/components/RoleBasedRedirect.tsx`
- ✅ Tất cả EMPLOYEE redirect về `/employee`

### 3. Employee Layout

#### `src/frontend/app/employee/layout.tsx`
- ✅ Sidebar menu hiển thị tất cả chức năng
- ✅ Không có permission check cho menu (tất cả nhân viên đều thấy)
- ✅ Permission check chỉ ở nút action trong từng trang

## 🔐 Phân quyền

### Position Types
```typescript
type Position = 
  | 'SALE'           // Nhân viên bán hàng
  | 'CSKH'           // Chăm sóc khách hàng
  | 'PRODUCT_MANAGER' // Quản lý sản phẩm
  | 'WAREHOUSE'      // Nhân viên kho
  | 'ACCOUNTANT'     // Kế toán
  | 'SHIPPER'        // Tài xế giao hàng
```

### Permissions của PRODUCT_MANAGER
```typescript
PRODUCT_MANAGER: [
  'products.create',
  'products.edit',
  'products.delete',
  'products.publish',
  'categories.create',
  'categories.edit',
  'categories.delete',
  'warehouse.reports.view', // CHỈ XEM báo cáo kho
]
```

### Cách check permission
```typescript
import { hasPermission, type Position } from '@/lib/permissions'

// Trong component
const { employee } = useAuthStore()
const canCreate = hasPermission(employee?.position as Position, 'products.create')

// Conditional rendering
{canCreate && (
  <button>Tạo sản phẩm</button>
)}
```

## 🚀 Luồng hoạt động

### 1. Đăng nhập
```
User login → Check role
├─ ADMIN → /admin
├─ EMPLOYEE → /employee (tất cả positions)
└─ CUSTOMER → /
```

### 2. Navigation
```
/employee
├─ Dashboard (tất cả nhân viên)
├─ Sản phẩm
│  ├─ Danh sách (tất cả xem được)
│  ├─ Đăng bán (chỉ PRODUCT_MANAGER có nút)
│  └─ Danh mục (tất cả xem được)
├─ Kho hàng
│  ├─ Tổng quan (tất cả xem được)
│  ├─ Nhập kho (chỉ WAREHOUSE có nút tạo)
│  ├─ Xuất kho (chỉ WAREHOUSE có nút tạo)
│  └─ Tồn kho (tất cả xem được)
├─ Đơn hàng (tất cả xem được)
├─ Khách hàng (tất cả xem được)
├─ Nhà cung cấp (tất cả xem được)
├─ Kế toán (chỉ ACCOUNTANT có nút action)
└─ Giao hàng (chỉ SHIPPER có nút action)
```

### 3. Permission Check trong trang
```typescript
// Ví dụ: Trang Đăng bán sản phẩm
// /employee/products/publish/page.tsx

const { employee } = useAuthStore()
const canPublish = hasPermission(employee?.position as Position, 'products.publish')

// Hiển thị thông báo nếu không có quyền
{!canPublish && (
  <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
    <p className="text-yellow-800">
      Bạn chỉ có quyền xem danh sách sản phẩm kho, không thể đăng bán.
    </p>
  </div>
)}

// Ẩn form nếu không có quyền
{canPublish && (
  <form onSubmit={handlePublish}>
    {/* Form fields */}
  </form>
)}
```

## 📊 Ví dụ cụ thể

### Nhân viên PRODUCT_MANAGER
**Có thể làm:**
- ✅ Xem tất cả trang
- ✅ Tạo/sửa/xóa sản phẩm
- ✅ Đăng bán sản phẩm từ kho
- ✅ Tạo/sửa/xóa danh mục
- ✅ Xem báo cáo kho

**Không thể làm:**
- ❌ Tạo phiếu nhập/xuất kho
- ❌ Duyệt phiếu kho
- ❌ Thao tác kế toán
- ❌ Giao hàng

### Nhân viên WAREHOUSE
**Có thể làm:**
- ✅ Xem tất cả trang
- ✅ Tạo/duyệt phiếu nhập kho
- ✅ Tạo/duyệt phiếu xuất kho
- ✅ Quản lý tồn kho
- ✅ Xem báo cáo kho

**Không thể làm:**
- ❌ Tạo/sửa sản phẩm
- ❌ Đăng bán sản phẩm
- ❌ Thao tác kế toán
- ❌ Giao hàng

### Nhân viên SALE
**Có thể làm:**
- ✅ Xem tất cả trang
- ✅ Tạo/sửa đơn hàng
- ✅ Quản lý khách hàng

**Không thể làm:**
- ❌ Tạo/sửa sản phẩm
- ❌ Thao tác kho
- ❌ Thao tác kế toán
- ❌ Giao hàng

## 🧪 Cách test

### 1. Test redirect
```bash
# Đăng nhập với tài khoản PRODUCT_MANAGER
# Kiểm tra URL sau khi login
# Expected: /employee (không phải /product-manager)
```

### 2. Test menu
```bash
# Vào /employee
# Kiểm tra sidebar menu
# Expected: Hiển thị tất cả menu (Sản phẩm, Kho hàng, Đơn hàng, etc.)
```

### 3. Test permission
```bash
# Đăng nhập với PRODUCT_MANAGER
# Vào /employee/products/publish
# Expected: Thấy form đăng bán

# Đăng nhập với WAREHOUSE
# Vào /employee/products/publish
# Expected: Thấy thông báo "không có quyền", form bị ẩn
```

### 4. Test không còn route cũ
```bash
# Thử truy cập /product-manager
# Expected: 404 Not Found
```

## 🔍 Kiểm tra code

### Tìm reference còn sót
```bash
# Tìm trong code TypeScript/JavaScript
grep -r "product-manager" src/frontend/
grep -r "/product-manager" src/frontend/

# Expected: Không có kết quả (hoặc chỉ trong comments/docs)
```

### Kiểm tra interface props
```typescript
// HorizontalNav.tsx
interface HorizontalNavProps {
  role: 'WAREHOUSE' | 'ADMIN' | 'ACCOUNTANT' | 'SALES'
  // ✅ Không còn 'PRODUCT_MANAGER'
}

// EmployeeHeader.tsx
interface EmployeeHeaderProps {
  role: 'WAREHOUSE' | 'ADMIN'
  // ✅ Không còn 'PRODUCT_MANAGER'
}
```

## 📝 Lưu ý quan trọng

### 1. Position type vẫn tồn tại
- `PRODUCT_MANAGER` vẫn là một position hợp lệ
- Chỉ xóa route `/product-manager`, không xóa position type
- Permission mapping vẫn giữ nguyên

### 2. Admin categories page
- File `src/frontend/app/admin/categories/page.tsx` vẫn check `PRODUCT_MANAGER`
- Đây là trang admin riêng, không phải employee interface
- Để nguyên logic này

### 3. Employee register
- Form đăng ký nhân viên vẫn có option `PRODUCT_MANAGER`
- Đây là đúng vì position vẫn tồn tại

### 4. Permission system
- File `src/frontend/lib/permissions.ts` vẫn có `PRODUCT_MANAGER`
- Đây là đúng vì cần định nghĩa permissions

## 🎉 Kết quả

### Đã hoàn thành
- ✅ Xóa thư mục `/product-manager`
- ✅ Xóa case `PRODUCT_MANAGER` trong navigation components
- ✅ Cập nhật interface props
- ✅ Tất cả nhân viên redirect về `/employee`
- ✅ Không còn reference nào đến route `/product-manager`

### Giữ nguyên
- ✅ Position type `PRODUCT_MANAGER` trong `permissions.ts`
- ✅ Permission mapping cho `PRODUCT_MANAGER`
- ✅ Employee register form có option `PRODUCT_MANAGER`
- ✅ Admin pages có check `PRODUCT_MANAGER` role

## 📚 Tài liệu liên quan

- `EMPLOYEE-SYSTEM-COMPLETE.md` - Hệ thống nhân viên thống nhất
- `PERMISSION-SYSTEM-SUMMARY.md` - Tổng quan hệ thống phân quyền
- `PERMISSION-IMPLEMENTATION-GUIDE.md` - Hướng dẫn implement permission
- `PRODUCT-PUBLISH-COMPLETE.md` - Tính năng đăng bán sản phẩm

---
**Ngày hoàn thành**: 24/12/2025  
**Trạng thái**: ✅ Hoàn thành - Đã migrate sang unified employee interface
