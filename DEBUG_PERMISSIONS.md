# 🔍 DEBUG PHÂN QUYỀN

## Checklist kiểm tra khi gặp lỗi "Không có quyền truy cập"

### 1. Kiểm tra JWT Token

#### Frontend - Console log
```typescript
// Trong component
console.log('User from store:', user)
console.log('Token:', localStorage.getItem('auth_token'))
```

#### Decode JWT Token
Vào https://jwt.io và paste token để xem claims:
```json
{
  "role": "EMPLOYEE",
  "position": "WAREHOUSE",  // ← Phải có cái này!
  "sub": "email@example.com",
  "iat": 1234567890,
  "exp": 1234567890
}
```

**Nếu không có `position`:** Token cũ, cần đăng nhập lại!

---

### 2. Kiểm tra Backend Logs

Khi gọi API, backend sẽ log:
```
✅ User role: EMPLOYEE
✅ User position: WAREHOUSE
```

**Nếu không thấy log `position`:** 
- Token không có position
- Hoặc JwtAuthenticationFilter không extract đúng

---

### 3. Kiểm tra API Endpoint

#### Backend - InventoryController.java
```java
@GetMapping("/api/inventory/stock")
@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'PRODUCT_MANAGER', 'ADMIN')")
public ApiResponse getStocks() {
    return inventoryService.getStocks();
}
```

**Authorities được check:**
- `WAREHOUSE` ← Từ JWT claim `position`
- `PRODUCT_MANAGER` ← Từ JWT claim `position`
- `ADMIN` ← Từ JWT claim `role`

---

### 4. Kiểm tra Frontend Role

#### AuthStore
```typescript
// store/authStore.ts
export interface User {
  role: 'CUSTOMER' | 'ADMIN' | 'WAREHOUSE' | 'PRODUCT_MANAGER'
  // ← Phải là WAREHOUSE, không phải EMPLOYEE!
}
```

#### Login Flow
```typescript
// login/page.tsx
let actualRole = response.data.role
if (response.data.role === 'EMPLOYEE' && response.data.position) {
  actualRole = response.data.position  // "WAREHOUSE"
}
```

---

### 5. Kiểm tra AuthProvider

#### Restore từ localStorage
```typescript
// components/AuthProvider.tsx
const userData = await authApi.getCurrentUser()
let actualRole = userData.role
if (userData.role === 'EMPLOYEE' && userData.position) {
  actualRole = userData.position  // Convert EMPLOYEE → WAREHOUSE
}
```

---

## 🚨 Các lỗi thường gặp

### Lỗi 1: "403 Forbidden" khi gọi API

**Nguyên nhân:**
- JWT không có `position` trong claims
- Backend không extract `position` thành authority

**Giải pháp:**
1. Đăng xuất
2. Đăng nhập lại (để tạo token mới có `position`)
3. Kiểm tra backend logs xem có `✅ User position: WAREHOUSE` không

---

### Lỗi 2: Frontend check `user.role === 'EMPLOYEE'`

**Nguyên nhân:**
- Code check sai role

**Giải pháp:**
```typescript
// ❌ SAI
if (user?.role === 'EMPLOYEE') { ... }

// ✅ ĐÚNG
if (user?.role === 'WAREHOUSE') { ... }
```

---

### Lỗi 3: Refresh trang bị mất quyền

**Nguyên nhân:**
- AuthProvider không restore đúng
- API `/auth/me` không trả về `position`

**Giải pháp:**
1. Check API `/auth/me` có trả về `position` không
2. Check AuthProvider có convert role đúng không

---

## ✅ Test Cases

### Test 1: Login với WAREHOUSE
```
1. Login với email nhân viên kho
2. Check console: user.role phải là "WAREHOUSE"
3. Check localStorage: token phải có position: "WAREHOUSE"
4. Gọi API /api/inventory/stock → 200 OK
```

### Test 2: Refresh trang
```
1. Đang ở /warehouse
2. F5 refresh
3. Không bị redirect về /
4. Vẫn thấy sidebar warehouse
5. API vẫn gọi được
```

### Test 3: Logout và login lại
```
1. Logout
2. Login lại
3. Token mới phải có position
4. Tất cả API phải hoạt động
```

---

## 🔧 Quick Fix

### Nếu gặp lỗi ngay lập tức:

1. **Xóa token cũ:**
```javascript
localStorage.removeItem('auth_token')
```

2. **Đăng nhập lại**

3. **Check token mới:**
```javascript
const token = localStorage.getItem('auth_token')
const payload = JSON.parse(atob(token.split('.')[1]))
console.log(payload)
// Phải có: { role: "EMPLOYEE", position: "WAREHOUSE" }
```

4. **Check user trong store:**
```javascript
console.log(useAuthStore.getState().user)
// Phải có: { role: "WAREHOUSE" }
```

---

## 📋 Checklist đầy đủ

- [ ] Backend: UserServiceImpl trả về `position` trong LoginResponse
- [ ] Backend: JwtService tạo token với claims `role` và `position`
- [ ] Backend: JwtAuthenticationFilter extract cả `role` và `position` thành authorities
- [ ] Backend: Controller dùng `@PreAuthorize("hasAnyAuthority('WAREHOUSE', ...)")`
- [ ] Frontend: Login convert `EMPLOYEE` + `position` → `role`
- [ ] Frontend: AuthProvider restore đúng role
- [ ] Frontend: Component check `user.role === 'WAREHOUSE'`
- [ ] Frontend: API `/auth/me` trả về đầy đủ thông tin

---

## 🎯 Kết luận

**Nguyên tắc vàng:**
1. Backend JWT phải có cả `role` và `position`
2. Backend authorities phải include `position`
3. Frontend phải convert `EMPLOYEE` + `position` → `role`
4. Đăng nhập lại khi thay đổi logic phân quyền
