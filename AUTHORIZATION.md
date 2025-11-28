# 🔐 HỆ THỐNG PHÂN QUYỀN

## 📌 Cấu trúc Role

### Backend (Database)
```
User {
  role: Role (CUSTOMER, ADMIN, EMPLOYEE)
  employee: Employee {
    position: Position (WAREHOUSE, PRODUCT_MANAGER, SALE, CSKH, ACCOUNTANT)
  }
}
```

**Lưu ý quan trọng:**
- `EMPLOYEE` là role chung cho tất cả nhân viên
- `Position` xác định chức vụ cụ thể và quyền hạn
- Backend phân quyền dựa trên `Position`, không phải `Role.EMPLOYEE`

### Frontend (After Login)
```typescript
// Backend trả về
{
  role: "EMPLOYEE",
  position: "WAREHOUSE"  // hoặc "PRODUCT_MANAGER"
}

// Frontend convert
user.role = position  // "WAREHOUSE" hoặc "PRODUCT_MANAGER"
```

---

## 👥 CÁC VAI TRÒ (ROLES)

### 1. **CUSTOMER** - Khách hàng
**Backend:** `Role.CUSTOMER`  
**Frontend:** `role: "CUSTOMER"`

**Quyền hạn:**
- ✅ Xem sản phẩm, tìm kiếm
- ✅ Thêm vào giỏ hàng
- ✅ Mua hàng, thanh toán
- ✅ Xem lịch sử đơn hàng
- ✅ Quản lý thông tin cá nhân
- ❌ Không truy cập được trang admin

**Giao diện:**
- Trang chủ với danh sách sản phẩm
- Trang chi tiết sản phẩm
- Giỏ hàng
- Thanh toán
- Lịch sử đơn hàng
- Trang cá nhân

### 2. **WAREHOUSE** - Nhân viên kho
**Backend:** `Role.EMPLOYEE` + `Position.WAREHOUSE`  
**Frontend:** `role: "WAREHOUSE"`

**Quyền hạn:**
- ✅ Tạo phiếu nhập kho
- ✅ Hoàn tất nhập kho (nhập serial)
- ✅ Xuất kho
- ✅ Cập nhật tồn kho
- ✅ Xem báo cáo kho
- ✅ Quản lý thông tin cá nhân
- ❌ Không quản lý sản phẩm hiển thị
- ❌ Không quản lý danh mục

**Giao diện:**
- Dashboard kho hàng
- Quản lý tồn kho
- Tạo/xem phiếu nhập
- Tạo/xem phiếu xuất
- Báo cáo nhập xuất tồn

### 3. **PRODUCT_MANAGER** - Quản lý sản phẩm
**Backend:** `Role.EMPLOYEE` + `Position.PRODUCT_MANAGER`  
**Frontend:** `role: "PRODUCT_MANAGER"`

**Quyền hạn:**
- ✅ Đăng bán sản phẩm từ kho
- ✅ Chỉnh sửa thông tin sản phẩm hiển thị
- ✅ Quản lý danh mục
- ✅ Phân loại sản phẩm
- ✅ Quản lý thông tin cá nhân
- ❌ Không quản lý kho
- ❌ Không xóa sản phẩm

**Giao diện:**
- Dashboard sản phẩm
- Danh sách sản phẩm trong kho
- Đăng bán sản phẩm
- Chỉnh sửa sản phẩm
- Quản lý danh mục

### 4. **ADMIN** - Quản trị viên
**Backend:** `Role.ADMIN`  
**Frontend:** `role: "ADMIN"`

**Quyền hạn:**
- ✅ Tất cả quyền của WAREHOUSE
- ✅ Tất cả quyền của PRODUCT_MANAGER
- ✅ Xóa sản phẩm
- ✅ Duyệt nhân viên
- ✅ Xem tất cả báo cáo
- ✅ Quản lý người dùng

**Giao diện:**
- Dashboard tổng quan
- Tất cả chức năng của hệ thống

---

## 📋 BẢNG PHÂN QUYỀN CHI TIẾT

| Chức năng | CUSTOMER | WAREHOUSE | PRODUCT_MANAGER | ADMIN |
|-----------|:--------:|:---------:|:---------------:|:-----:|
| **Khách hàng** |
| Xem sản phẩm | ✅ | ✅ | ✅ | ✅ |
| Tìm kiếm sản phẩm | ✅ | ✅ | ✅ | ✅ |
| Thêm vào giỏ hàng | ✅ | ❌ | ❌ | ❌ |
| Mua hàng | ✅ | ❌ | ❌ | ❌ |
| Xem lịch sử mua | ✅ | ❌ | ❌ | ✅ |
| **Quản lý kho** |
| Tạo phiếu nhập kho | ❌ | ✅ | ❌ | ✅ |
| Hoàn tất nhập kho | ❌ | ✅ | ❌ | ✅ |
| Xuất kho | ❌ | ✅ | ❌ | ✅ |
| Cập nhật tồn kho | ❌ | ✅ | ❌ | ✅ |
| Xem tồn kho (read-only) | ❌ | ✅ | ✅ | ✅ |
| Xem báo cáo kho | ❌ | ✅ | ❌ | ✅ |
| **Quản lý sản phẩm** |
| Đăng bán sản phẩm | ❌ | ❌ | ✅ | ✅ |
| Chỉnh sửa thông tin SP | ❌ | ❌ | ✅ | ✅ |
| Quản lý danh mục | ❌ | ❌ | ✅ | ✅ |
| Xóa sản phẩm | ❌ | ❌ | ❌ | ✅ |
| **Quản trị** |
| Duyệt nhân viên | ❌ | ❌ | ❌ | ✅ |
| Quản lý người dùng | ❌ | ❌ | ❌ | ✅ |

---

## 🔗 API ENDPOINTS

### Public (Không cần đăng nhập)
```
GET  /api/auth/login
POST /api/auth/register
GET  /api/categories
GET  /api/categories/tree
GET  /api/categories/active
GET  /api/categories/{id}
GET  /api/products
GET  /api/products/{id}
GET  /api/products/{id}/with-specs
GET  /api/products/search-by-specs
GET  /api/products/filter-by-specs
```

### Customer Only
```
GET    /api/cart
POST   /api/cart/items
PUT    /api/cart/items/{id}
DELETE /api/cart/items/{id}
POST   /api/orders
GET    /api/orders
GET    /api/orders/{id}
GET    /api/customer/profile
PUT    /api/customer/profile
```

### Warehouse Only
```
POST   /api/inventory/create_pchaseOrder
POST   /api/inventory/suppliers
POST   /api/inventory/import
POST   /api/inventory/create (export)
GET    /api/inventory/purchase-orders
GET    /api/inventory/export-orders
GET    /api/inventory/purchase-orders/{id}
GET    /api/inventory/export-orders/{id}
PUT    /api/inventory/purchase-orders/{id}/cancel
PUT    /api/inventory/export-orders/{id}/cancel
```

### Warehouse + Product Manager
```
GET    /api/inventory/stock (PRODUCT_MANAGER: read-only)
```

### Product Manager Only
```
GET    /api/products/warehouse/list
POST   /api/products/warehouse/publish
PUT    /api/products/warehouse/publish/{id}
DELETE /api/products/warehouse/unpublish/{id}
POST   /api/categories
PUT    /api/categories/{id}
```

### Admin Only
```
DELETE /api/products/{id}
DELETE /api/categories/{id}
GET    /api/employee-registration/list
POST   /api/employee-registration/approve/{id}
```

---

## 🎨 CẤU TRÚC GIAO DIỆN

### Customer (`/`)
```
┌─────────────────────────────────────┐
│ Header: Logo | Tìm kiếm | Giỏ hàng │
├─────────────────────────────────────┤
│ Danh mục                            │
│ ┌─────────┬─────────┬─────────┐    │
│ │ SP 1    │ SP 2    │ SP 3    │    │
│ └─────────┴─────────┴─────────┘    │
└─────────────────────────────────────┘
```

### Warehouse (`/warehouse`)
```
┌─────────────────────────────────────┐
│ Sidebar: Kho hàng                   │
│ ├─ Tồn kho                          │
│ ├─ Nhập kho                         │
│ ├─ Xuất kho                         │
│ └─ Báo cáo                          │
├─────────────────────────────────────┤
│ Content: Dashboard kho              │
└─────────────────────────────────────┘
```

### Product Manager (`/product-manager`)
```
┌─────────────────────────────────────┐
│ Sidebar: Sản phẩm                   │
│ ├─ Đăng bán SP                      │
│ ├─ Quản lý SP                       │
│ └─ Danh mục                         │
├─────────────────────────────────────┤
│ Content: Dashboard sản phẩm         │
└─────────────────────────────────────┘
```

### Admin (`/admin`)
```
┌─────────────────────────────────────┐
│ Sidebar: Tất cả                     │
│ ├─ Dashboard                        │
│ ├─ Kho hàng                         │
│ ├─ Sản phẩm                         │
│ ├─ Đơn hàng                         │
│ ├─ Khách hàng                       │
│ └─ Nhân viên                        │
├─────────────────────────────────────┤
│ Content: Dashboard tổng quan        │
└─────────────────────────────────────┘
```

---

## 🚀 TRIỂN KHAI

### Backend (Spring Security)
Đã cấu hình trong `SecurityConfig.java`:
- Public endpoints không cần authentication
- Role-based access control với `@PreAuthorize`
- JWT authentication filter
- JWT claims chứa cả `role` và `position`

**Lưu ý:** Backend sử dụng `Role.EMPLOYEE` chung, phân quyền dựa trên `Position`

### Frontend (Next.js)

#### 1. Login Flow
```typescript
// Backend response
{
  role: "EMPLOYEE",
  position: "WAREHOUSE"  // hoặc "PRODUCT_MANAGER"
}

// Frontend xử lý
let actualRole = response.data.role
if (response.data.role === 'EMPLOYEE' && response.data.position) {
  actualRole = response.data.position  // "WAREHOUSE" hoặc "PRODUCT_MANAGER"
}

// Lưu vào store
setAuth({ ...user, role: actualRole }, token)
```

#### 2. Kiểm tra quyền trong component
```typescript
const { user } = useAuthStore()

// Redirect nếu không có quyền
if (user?.role !== 'WAREHOUSE' && user?.role !== 'ADMIN') {
  router.push('/')
  return
}

// Hiển thị UI theo role
{user?.role === 'ADMIN' && (
  <button>Xóa</button>
)}
```

#### 3. Routing
- `/` - Customer (public)
- `/warehouse/*` - WAREHOUSE only (Employee với Position.WAREHOUSE)
- `/product-manager/*` - PRODUCT_MANAGER only (Employee với Position.PRODUCT_MANAGER)
- `/admin/*` - ADMIN only

#### 4. Layout riêng
- Customer: Header + Footer đầy đủ
- Employee (WAREHOUSE, PRODUCT_MANAGER): EmployeeHeader đơn giản
- Admin: EmployeeHeader với đầy đủ quyền
