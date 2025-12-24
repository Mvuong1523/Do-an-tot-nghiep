# Employee Warehouse Fix - Giải quyết vấn đề không hiển thị dữ liệu và quyền

## Vấn đề
1. **Trang tổng quan kho của nhân viên** (`/employee/warehouse`) hiển thị "Chưa có sản phẩm nào" mặc dù có dữ liệu trong kho
2. **Nhân viên kho không thể sử dụng các chức năng kho** mặc dù có position WAREHOUSE

## Nguyên nhân
Cấu trúc dữ liệu employee không đúng trong authStore:

### Backend trả về (LoginResponse):
```json
{
  "token": "...",
  "userId": 123,
  "email": "user@example.com",
  "fullName": "Nguyen Van A",
  "phone": "0123456789",
  "role": "EMPLOYEE",
  "position": "WAREHOUSE",  // ← Position ở level root
  "status": "ACTIVE"
}
```

### Frontend authStore mong đợi:
```typescript
{
  user: {
    id: 123,
    email: "user@example.com",
    role: "EMPLOYEE",
    employee: {              // ← Employee object lồng nhau
      fullName: "Nguyen Van A",
      position: "WAREHOUSE"  // ← Position trong employee object
    }
  },
  employee: {                // ← Employee được extract ra
    fullName: "Nguyen Van A",
    position: "WAREHOUSE"
  }
}
```

### Vấn đề trong code cũ:
```typescript
// ❌ SAI - Không có employee object
setAuth({
  id: response.data.userId,
  email: response.data.email,
  role: response.data.role,
  position: response.data.position,  // ← Chỉ có position ở root
  status: response.data.status,
}, response.data.token)
```

Khi authStore xử lý:
```typescript
employee: user.employee || null  // ← user.employee = undefined → employee = null
```

Khi component check permission:
```typescript
const { employee } = useAuthStore()  // ← employee = null
hasPermission(employee?.position as Position, 'warehouse.import.create')  // ← null → false
```

## Giải pháp

### 1. Sửa Login Page (`src/frontend/app/login/page.tsx`)
```typescript
// ✅ ĐÚNG - Thêm employee object
setAuth({
  id: response.data.userId,
  email: response.data.email,
  fullName: response.data.fullName,
  phone: response.data.phone,
  address: response.data.address,
  role: response.data.role,
  position: response.data.position,  // ← Giữ ở root cho tương thích
  status: response.data.status,
  // ✅ Thêm employee object nếu là EMPLOYEE
  employee: response.data.role === 'EMPLOYEE' ? {
    fullName: response.data.fullName,
    phone: response.data.phone,
    address: response.data.address,
    position: response.data.position,  // ← Position trong employee
    firstLogin: false
  } : undefined
}, response.data.token)
```

### 2. Sửa Register Page (`src/frontend/app/register/page.tsx`)
Tương tự như login page.

### 3. Thêm Debug Info (`src/frontend/app/employee/warehouse/page.tsx`)
```typescript
// Debug trong console
console.log('🔍 DEBUG - Employee data:', employee)
console.log('🔍 DEBUG - Employee position:', employee?.position)
console.log('🔍 DEBUG - Can import:', canImport)
console.log('🔍 DEBUG - Can export:', canExport)

// Debug trong UI (development mode)
{process.env.NODE_ENV === 'development' && (
  <div className="mb-4 p-4 bg-gray-100 border border-gray-300 rounded-lg">
    <h3 className="font-bold mb-2">🔍 Debug Info:</h3>
    <pre className="text-xs overflow-auto">
      {JSON.stringify({
        employee: employee,
        position: employee?.position,
        canImport,
        canExport,
        inventoryCount: inventory.length
      }, null, 2)}
    </pre>
  </div>
)}
```

## Cách hoạt động của Permission System

### Backend (Spring Security)
```java
// JWT Token chứa position claim
claims.put("position", user.getEmployee().getPosition().name());
claims.put(user.getEmployee().getPosition().name(), true);

// Spring Security Authority
authorities.add(new SimpleGrantedAuthority(position.toString()));

// API endpoint yêu cầu authority
@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'PRODUCT_MANAGER', 'ADMIN')")
public ApiResponse getStocks() { ... }
```

### Frontend (Permission Check)
```typescript
// lib/permissions.ts
export const POSITION_PERMISSIONS: Record<Position, Permission[]> = {
  WAREHOUSE: [
    'warehouse.import.create',
    'warehouse.import.approve',
    'warehouse.export.create',
    'warehouse.export.approve',
    'suppliers.create',
    'suppliers.edit',
  ],
  // ...
}

export function hasPermission(position: Position | null, permission: Permission): boolean {
  if (!position) return false
  return POSITION_PERMISSIONS[position]?.includes(permission) || false
}
```

### Component Usage
```typescript
const { employee } = useAuthStore()
const canImport = hasPermission(employee?.position as Position, 'warehouse.import.create')

{canImport && (
  <Link href="/employee/warehouse/import/create">
    <button>Nhập hàng</button>
  </Link>
)}
```

## Kiểm tra sau khi fix

### 1. Đăng nhập lại
- Đăng xuất khỏi tài khoản hiện tại
- Đăng nhập lại với tài khoản nhân viên kho (position: WAREHOUSE)

### 2. Kiểm tra Debug Info
- Mở trang `/employee/warehouse`
- Xem debug box (chỉ hiện trong development mode)
- Kiểm tra:
  - `employee` object có dữ liệu
  - `position` = "WAREHOUSE"
  - `canImport` = true
  - `canExport` = true
  - `inventoryCount` > 0 (nếu có dữ liệu)

### 3. Kiểm tra Console
```
🔍 DEBUG - Employee data: { fullName: "...", position: "WAREHOUSE", ... }
🔍 DEBUG - Employee position: WAREHOUSE
🔍 DEBUG - Can import: true
🔍 DEBUG - Can export: true
🔍 DEBUG - API Response: { success: true, data: [...] }
🔍 DEBUG - Mapped inventory: [...]
```

### 4. Kiểm tra chức năng
- Nút "Nhập hàng" và "Xuất hàng" hiển thị
- Bảng tồn kho hiển thị dữ liệu
- Có thể truy cập `/employee/warehouse/import/create`
- Có thể truy cập `/employee/warehouse/export/create`

## Files đã sửa
1. `src/frontend/app/login/page.tsx` - Thêm employee object vào setAuth
2. `src/frontend/app/register/page.tsx` - Thêm employee object vào setAuth
3. `src/frontend/app/employee/warehouse/page.tsx` - Thêm debug logging và UI

## Lưu ý
- Debug info chỉ hiển thị trong development mode (`NODE_ENV === 'development'`)
- Sau khi xác nhận fix hoạt động, có thể xóa debug code
- Cần đăng nhập lại để token mới có đầy đủ thông tin employee
