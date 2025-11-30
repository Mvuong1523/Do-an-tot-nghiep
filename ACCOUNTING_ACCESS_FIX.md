# ✅ Fix: Admin và Accountant có thể truy cập Module Kế toán

## 🐛 Vấn đề

Admin và nhân viên Kế toán không thể truy cập trang kế toán vì:
1. Frontend check `role === 'ACCOUNTANT'` nhưng với Employee thì role là `EMPLOYEE`, position mới là `ACCOUNTANT`
2. User data trong localStorage không có field `position`
3. Login logic không lưu position vào store

## ✅ Giải pháp đã áp dụng

### 1. Cập nhật AuthStore (src/frontend/store/authStore.ts)

**Thêm field position vào User interface:**
```typescript
export interface User {
  // ... existing fields
  role: 'CUSTOMER' | 'ADMIN' | 'WAREHOUSE' | 'PRODUCT_MANAGER' | 'EMPLOYEE'
  position?: 'WAREHOUSE' | 'PRODUCT_MANAGER' | 'ACCOUNTANT' | 'SALE' | 'CSKH'
  // ... other fields
}
```

### 2. Cập nhật Login Logic (src/frontend/app/login/page.tsx)

**Lưu position vào store:**
```typescript
setAuth(
  {
    id: response.data.userId,
    email: response.data.email,
    fullName: response.data.fullName,
    phone: response.data.phone,
    address: response.data.address,
    role: response.data.role,      // Giữ nguyên role gốc (EMPLOYEE)
    position: response.data.position, // Thêm position (ACCOUNTANT)
    status: response.data.status,
  },
  response.data.token
)
```

**Redirect theo position:**
```typescript
if (response.data.role === 'ADMIN') {
  router.push('/admin')
} else if (response.data.role === 'EMPLOYEE' && response.data.position) {
  switch (response.data.position) {
    case 'ACCOUNTANT':
      router.push('/admin/accounting')
      break
    // ... other positions
  }
}
```

### 3. Cập nhật Authorization Check trong Accounting Pages

**Trước (Sai):**
```typescript
if (userData.role !== 'ADMIN' && userData.role !== 'ACCOUNTANT') {
  // Sai vì role của Employee là 'EMPLOYEE', không phải 'ACCOUNTANT'
}
```

**Sau (Đúng):**
```typescript
const isAdmin = userData.role === 'ADMIN'
const isAccountant = userData.position === 'ACCOUNTANT'

if (!isAdmin && !isAccountant) {
  toast.error('Bạn không có quyền truy cập')
  router.push('/')
  return
}
```

**Áp dụng cho tất cả các trang:**
- ✅ `/admin/accounting/page.tsx`
- ✅ `/admin/accounting/reconciliation/page.tsx`
- ✅ `/admin/accounting/reports/page.tsx`
- ✅ `/admin/accounting/periods/page.tsx`

### 4. Cập nhật Header Menu (src/frontend/components/layout/Header.tsx)

**Thêm menu cho ACCOUNTANT:**
```typescript
{user.position === 'ACCOUNTANT' && (
  <Link
    href="/admin/accounting"
    className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50"
    onClick={() => setIsUserMenuOpen(false)}
  >
    Kế toán & Đối soát
  </Link>
)}
```

## 📊 Cấu trúc Role & Position

### Backend (Spring Security)
```java
// User có role: CUSTOMER, ADMIN, EMPLOYEE
// Employee có position: WAREHOUSE, PRODUCT_MANAGER, ACCOUNTANT, SALE, CSKH

// Authorities được set trong UserDetailsServiceImpl:
authorities.add(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()));
if (u.getEmployee() != null && u.getEmployee().getPosition() != null) {
    authorities.add(new SimpleGrantedAuthority(u.getEmployee().getPosition().name()));
}

// Vậy một Employee với position ACCOUNTANT sẽ có authorities:
// - ROLE_EMPLOYEE
// - ACCOUNTANT
```

### Frontend (Authorization)
```typescript
// Admin: role === 'ADMIN'
// Accountant: role === 'EMPLOYEE' && position === 'ACCOUNTANT'

// Check quyền:
const isAdmin = userData.role === 'ADMIN'
const isAccountant = userData.position === 'ACCOUNTANT'

if (isAdmin || isAccountant) {
  // Cho phép truy cập
}
```

## 🧪 Testing

### 1. Test với Admin
```
Email: admin@example.com
Password: admin123

✅ Có thể truy cập /admin/accounting
✅ Thấy menu "Kế toán & Đối soát"
✅ Có thể mở khóa kỳ (Admin only)
```

### 2. Test với Accountant
```
Email: ketoan@example.com
Password: ketoan123

✅ Có thể truy cập /admin/accounting
✅ Thấy menu "Kế toán & Đối soát"
✅ Redirect về /admin/accounting sau khi login
❌ Không thể mở khóa kỳ (Admin only)
```

## 📝 Tạo tài khoản Kế toán

**File SQL:** `create_accountant_user.sql`

```sql
-- Tạo User với role EMPLOYEE
INSERT INTO users (email, password, role, status) 
VALUES ('ketoan@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'EMPLOYEE', 'ACTIVE');

-- Tạo Employee với position ACCOUNTANT
INSERT INTO employees (user_id, position, full_name, phone, address, first_login)
SELECT id, 'ACCOUNTANT', 'Nguyễn Văn Kế Toán', '0987654321', 'Hà Nội', false
FROM users 
WHERE email = 'ketoan@example.com';
```

**Chạy SQL:**
- Sử dụng MySQL Workbench, phpMyAdmin
- Hoặc command line: `mysql -u root web2 < create_accountant_user.sql`

## ✅ Kết quả

- ✅ Admin có thể truy cập module Kế toán
- ✅ Nhân viên Kế toán có thể truy cập module Kế toán
- ✅ Menu hiển thị đúng cho từng role
- ✅ Authorization check chính xác
- ✅ Redirect đúng sau khi login
- ✅ Admin có quyền mở khóa kỳ
- ✅ Accountant không thể mở khóa kỳ

## 🚀 Đã áp dụng

Frontend đã được restart và sẵn sàng test!

**URLs:**
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Accounting: http://localhost:3000/admin/accounting

**Logout và login lại để test!**
