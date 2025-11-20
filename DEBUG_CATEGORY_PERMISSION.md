# 🔍 Hướng dẫn Debug vấn đề phân quyền Category

## ✅ Đã sửa

### 1. **Token Storage**
- Trước: authStore lưu vào `auth_token`, nhưng component đọc từ `token`
- Sau: Lưu vào cả 2 key để tương thích

### 2. **API Calls**
- Trước: Dùng `fetch` trực tiếp, không tự động thêm token
- Sau: Dùng `categoryApi` từ `lib/api.ts`, tự động thêm token vào header

### 3. **Debug Logs**
- Thêm console.log để kiểm tra user role và token

## 🧪 Cách kiểm tra

### Bước 1: Kiểm tra đăng nhập
1. Mở Developer Tools (F12)
2. Vào tab Console
3. Đăng nhập với tài khoản Product Manager
4. Kiểm tra log:
   ```
   ✅ User found: email@example.com, Role: EMPLOYEE
   ✅ Password matched
   ✅ Account active
   🔑 Generating JWT token...
   ✅ Token generated: eyJhbGciOiJIUzI1Ni...
   ✅ Login successful! Position: PRODUCT_MANAGER
   ```

### Bước 2: Kiểm tra token trong localStorage
Trong Console, chạy:
```javascript
localStorage.getItem('auth_token')
localStorage.getItem('token')
```
Cả 2 phải trả về cùng 1 token.

### Bước 3: Kiểm tra user role
Trong Console, chạy:
```javascript
JSON.parse(localStorage.getItem('auth-storage'))
```
Kiểm tra `state.user.role` phải là `"PRODUCT_MANAGER"` (không phải `"EMPLOYEE"`).

### Bước 4: Kiểm tra trang categories
1. Vào `/product-manager/categories`
2. Kiểm tra Console log:
   ```
   🔍 Auth Check: { isAuthenticated: true, user: { role: 'PRODUCT_MANAGER', ... } }
   👤 User role: PRODUCT_MANAGER
   ```

### Bước 5: Thử tạo category
1. Click "Thêm danh mục"
2. Nhập thông tin
3. Click "Tạo mới"
4. Kiểm tra Console log:
   ```
   📤 Submitting category: { name: "Test", ... }
   📥 Response: { success: true, ... }
   ```

## ❌ Nếu vẫn lỗi

### Lỗi 403 Forbidden
**Nguyên nhân:** Backend không nhận được Position trong authorities

**Kiểm tra:**
1. Xem log backend khi login:
   ```
   ✅ User role: EMPLOYEE
   ✅ User position: PRODUCT_MANAGER
   ```

2. Xem log backend khi tạo category:
   ```
   ✅ User role: EMPLOYEE
   ✅ User position: PRODUCT_MANAGER
   ```

**Giải pháp:** Nếu không thấy log position, kiểm tra:
- JWT token có chứa claim `position` không?
- JwtAuthenticationFilter có thêm position vào authorities không?

### Lỗi 401 Unauthorized
**Nguyên nhân:** Token không hợp lệ hoặc hết hạn

**Giải pháp:**
1. Đăng xuất và đăng nhập lại
2. Kiểm tra token expiration trong JWT

### User role vẫn là EMPLOYEE
**Nguyên nhân:** Login page không convert role đúng

**Kiểm tra file:** `src/frontend/app/login/page.tsx`
```typescript
// Phải có đoạn này:
let actualRole = response.data.role
if (response.data.role === 'EMPLOYEE' && response.data.position) {
  actualRole = response.data.position
}
```

## 🔧 Test API trực tiếp

Dùng file `test-category-permission.http`:

1. Login để lấy token:
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "productmanager@example.com",
  "password": "your_password"
}
```

2. Copy token từ response

3. Test tạo category:
```http
POST http://localhost:8080/api/categories
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN_HERE

{
  "name": "Test Category",
  "description": "Test",
  "active": true
}
```

Nếu API trực tiếp hoạt động nhưng frontend không, vấn đề nằm ở frontend.
Nếu API cũng lỗi 403, vấn đề nằm ở backend (JWT hoặc Spring Security).

## 📝 Checklist

- [ ] Token được lưu vào cả `auth_token` và `token`
- [ ] User role là `PRODUCT_MANAGER` (không phải `EMPLOYEE`)
- [ ] Console log hiển thị đúng role
- [ ] API client tự động thêm token vào header
- [ ] Backend log hiển thị position trong authorities
- [ ] Test API trực tiếp thành công

## 🎯 Kết luận

Vấn đề chính là:
1. **Token storage inconsistency** - Đã sửa
2. **Không dùng API client** - Đã sửa
3. **Thiếu debug logs** - Đã thêm

Sau khi sửa, hãy:
1. Đăng xuất
2. Đăng nhập lại với tài khoản Product Manager
3. Thử tạo category
4. Kiểm tra Console logs

Nếu vẫn lỗi, gửi cho tôi:
- Screenshot Console logs
- Screenshot Network tab (request headers)
- Backend logs khi tạo category
