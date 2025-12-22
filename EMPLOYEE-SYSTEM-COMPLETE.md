# ✅ Hệ thống nhân viên thống nhất - Hoàn thành

## 📋 Tổng quan
Đã tạo hệ thống nhân viên thống nhất với:
- **1 giao diện chung** cho tất cả nhân viên tại `/employee`
- **Tất cả nhân viên có thể XEM** tất cả trang
- **Chỉ thực hiện chức năng** theo quyền hạn position

## 🎯 Yêu cầu đã hoàn thành

### 1. Giao diện thống nhất
- ✅ Tất cả nhân viên vào cùng 1 trang `/employee`
- ✅ Sidebar menu hiển thị tất cả chức năng
- ✅ Dashboard chung với thống kê tổng quan

### 2. Phân quyền theo Position
Các vị trí nhân viên:
- **SALE** - Nhân viên bán hàng
- **CSKH** - Chăm sóc khách hàng
- **PRODUCT_MANAGER** - Quản lý sản phẩm
- **WAREHOUSE** - Nhân viên kho
- **ACCOUNTANT** - Kế toán
- **SHIPPER** - Tài xế giao hàng

### 3. Quyền truy cập
- ✅ **Tất cả nhân viên** có thể XEM tất cả trang
- ✅ **Nút chức năng** (Thêm, Sửa, Xóa) chỉ hiện với nhân viên có quyền
- ✅ **Thông báo quyền hạn** hiển thị khi không có quyền thực hiện
- ✅ **Không block truy cập** - chỉ ẩn nút action

## 🔧 Các thay đổi đã thực hiện

### 1. Frontend

#### A. Hệ thống Permission (`src/frontend/lib/permissions.ts`)
```typescript
// Định nghĩa Position types
export type Position = 'SALE' | 'CSKH' | 'PRODUCT_MANAGER' | 'WAREHOUSE' | 'ACCOUNTANT' | 'SHIPPER'

// Định nghĩa Permissions
export type Permission = 
  | 'products.create'
  | 'products.edit'
  | 'warehouse.import.create'
  | 'warehouse.export.create'
  // ... và nhiều permissions khác

// Mapping permissions cho từng position
export const POSITION_PERMISSIONS: Record<Position, Permission[]>

// Helper functions
hasPermission(position, permission)
hasAnyPermission(position, permissions)
hasAllPermissions(position, permissions)
```

#### B. Layout nhân viên (`src/frontend/app/employee/layout.tsx`)
- Sidebar menu với tất cả chức năng
- Header với thông tin user
- Navigation theo module

#### C. Dashboard nhân viên (`src/frontend/app/employee/page.tsx`)
- Thống kê tổng quan: đơn hàng, doanh thu, sản phẩm
- Danh sách đơn hàng gần đây
- Hiển thị position của nhân viên

#### D. 25 trang chức năng đã migrate
Tất cả trang đều có:
1. **Permission check**: `const canCreate = hasPermission(employee?.position, 'xxx.create')`
2. **Conditional rendering**: `{canCreate && <button>Tạo mới</button>}`
3. **Permission notice**: Box thông báo quyền hạn
4. **No blocking**: Không redirect, chỉ ẩn nút

**Danh sách trang:**
- Warehouse: import, export, inventory, reports, suppliers, orders (11 trang)
- Products: products, publish, categories, inventory (4 trang)
- Sales: orders, export (2 trang)
- Accounting: reconciliation, payables, statements, bank-accounts (4 trang)
- Shipping: shipping list (1 trang)
- Customers: customers list (1 trang)
- Suppliers: suppliers list (1 trang)
- Inventory: stock list (1 trang)

#### E. Login redirect (`src/frontend/app/login/page.tsx`)
```typescript
// Redirect logic
if (role === 'ADMIN') {
  router.push('/admin')
} else if (role === 'EMPLOYEE') {
  router.push('/employee')  // ✅ Tất cả nhân viên vào đây
} else {
  router.push('/')  // Customer
}
```

### 2. Backend

#### A. DashboardController (`src/main/java/.../controller/DashboardController.java`)
```java
@PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
public ResponseEntity<DashboardStatsDTO> getDashboardStats()
```
- Sửa từ `hasAnyRole('ADMIN', 'EMPLOYEE')` → `hasAnyRole('ROLE_ADMIN', 'ROLE_EMPLOYEE')`
- Spring Security yêu cầu prefix `ROLE_`

#### B. SecurityConfig (`src/main/java/.../config/SecurityConfig.java`)
```java
.requestMatchers("/api/dashboard/**").hasAnyAuthority("ADMIN", "EMPLOYEE")
```
- Cho phép ADMIN và EMPLOYEE truy cập dashboard API

#### C. DashboardService (`src/main/java/.../service/impl/DashboardServiceImpl.java`)
- Tính toán thống kê: orders, revenue, products, customers
- Lấy danh sách đơn hàng gần đây
- Profit tạm thời set = 0 (cần implement tracking serial numbers)

### 3. Bug fixes

#### A. API URL duplicate (`/api/api/dashboard/stats`)
**Vấn đề**: Frontend gọi `/api/api/dashboard/stats` thay vì `/api/dashboard/stats`

**Nguyên nhân**: 
- `api.ts` có `baseURL = 'http://localhost:8080/api'`
- Các trang lại thêm `/api` vào đầu URL

**Sửa**:
```typescript
// ❌ SAI
await api.get('/api/dashboard/stats')

// ✅ ĐÚNG
await api.get('/dashboard/stats')
```

**Files đã sửa**:
- `src/frontend/app/admin/page.tsx`
- `src/frontend/app/employee/page.tsx`

#### B. Permission check sai (`warehouse.export` không tồn tại)
**Vấn đề**: Trang export check permission `warehouse.export` nhưng permission thực tế là `warehouse.export.create`

**Sửa**:
```typescript
// ❌ SAI
const canExport = hasPermission(position, 'warehouse.export')

// ✅ ĐÚNG
const canExport = hasPermission(position, 'warehouse.export.create')
```

**File đã sửa**: `src/frontend/app/employee/export/page.tsx`

## 📊 Ví dụ cụ thể

### Nhân viên SALE vào trang Xuất kho
- ✅ **Có thể XEM**: Danh sách đơn hàng cần xuất, chi tiết đơn
- ✅ **Thấy thông báo**: "Bạn chỉ có quyền xem danh sách đơn hàng, không thể thực hiện xuất kho"
- ❌ **Không thấy**: Nút "Xuất kho" bị ẩn
- ✅ **Không bị redirect**: Vẫn ở trang, không bị đá về trang chủ

### Nhân viên PRODUCT_MANAGER vào trang Nhập kho
- ✅ **Có thể XEM**: Danh sách phiếu nhập, chi tiết phiếu
- ✅ **Thấy thông báo**: "Bạn có thể xem danh sách và chi tiết phiếu nhập kho, nhưng không thể tạo hoặc chỉnh sửa"
- ❌ **Không thấy**: Nút "Tạo phiếu nhập", nút "Duyệt phiếu"
- ✅ **Không bị redirect**: Vẫn ở trang

### Nhân viên WAREHOUSE vào trang Sản phẩm
- ✅ **Có thể XEM**: Danh sách sản phẩm, chi tiết
- ✅ **Thấy thông báo**: "Bạn chỉ có quyền xem"
- ❌ **Không thấy**: Nút "Thêm sản phẩm", "Sửa", "Xóa"
- ✅ **Không bị redirect**: Vẫn ở trang

## 🧪 Cách test

### 1. Đăng nhập với nhân viên
```
Email: [email nhân viên]
Password: [password]
```

### 2. Kiểm tra redirect
- Sau khi đăng nhập, phải tự động vào `/employee`
- Dashboard hiển thị thống kê

### 3. Kiểm tra menu
- Sidebar hiển thị tất cả menu
- Click vào menu bất kỳ → vào được trang

### 4. Kiểm tra quyền hạn
- Vào trang không thuộc quyền của mình
- Phải thấy thông báo quyền hạn
- Nút chức năng bị ẩn
- Vẫn xem được dữ liệu

### 5. Kiểm tra dashboard
- Mở DevTools (F12) → Console
- Không có lỗi 400, 403, 500
- Thống kê hiển thị đúng

## 🔐 Bảng phân quyền

| Position | Products | Categories | Warehouse | Orders | Customers | Accounting | Shipping |
|----------|----------|------------|-----------|--------|-----------|------------|----------|
| SALE | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| CSKH | ❌ | ❌ | ❌ | ✅ | ✅ | ❌ | ❌ |
| PRODUCT_MANAGER | ✅ | ✅ | 👁️ | ❌ | ❌ | ❌ | ❌ |
| WAREHOUSE | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |
| ACCOUNTANT | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ |
| SHIPPER | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

**Chú thích:**
- ✅ = Có quyền thực hiện (create, edit, delete)
- 👁️ = Chỉ xem (view only)
- ❌ = Không có quyền (nhưng vẫn vào xem được)

## 📝 Lưu ý quan trọng

### 1. Tất cả nhân viên đều vào `/employee`
- Không còn `/warehouse`, `/product-manager`, `/sales` riêng
- Tất cả dùng chung 1 giao diện

### 2. Permission check ở 2 tầng
- **Frontend**: Ẩn/hiện nút, hiển thị thông báo
- **Backend**: Kiểm tra quyền khi gọi API (security layer)

### 3. Không block truy cập
- Nhân viên có thể vào tất cả trang để XEM
- Chỉ không thể THỰC HIỆN chức năng nếu không có quyền

### 4. Backend cần chạy
- Backend phải chạy trên port 8080
- SecurityConfig đã được cấu hình đúng
- DashboardController có `@PreAuthorize` đúng

### 5. Frontend cần refresh
- Sau khi sửa code, cần hard refresh (Ctrl + Shift + R)
- Hoặc restart Next.js dev server

## 🚀 Triển khai

### Backend
```bash
./mvnw spring-boot:run
```

### Frontend
```bash
cd src/frontend
npm run dev
```

### Test
1. Đăng nhập với tài khoản nhân viên
2. Kiểm tra redirect vào `/employee`
3. Click vào các menu khác nhau
4. Kiểm tra nút chức năng có ẩn/hiện đúng không

## 📂 Files quan trọng

### Frontend
- `src/frontend/lib/permissions.ts` - Hệ thống phân quyền
- `src/frontend/app/employee/layout.tsx` - Layout chung
- `src/frontend/app/employee/page.tsx` - Dashboard
- `src/frontend/app/login/page.tsx` - Login redirect
- `src/frontend/store/authStore.ts` - Auth state management

### Backend
- `src/main/java/.../controller/DashboardController.java`
- `src/main/java/.../service/impl/DashboardServiceImpl.java`
- `src/main/java/.../config/SecurityConfig.java`

---
**Ngày hoàn thành**: 22/12/2025  
**Trạng thái**: ✅ Hoàn thành - Backend đang khởi động, cần test
