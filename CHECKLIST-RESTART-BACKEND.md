# ✅ Checklist: Restart Backend và Kiểm Tra Fix

## 📋 Trước Khi Restart

### 1. Xác Nhận Files Đã Được Sửa
- [x] `src/main/java/com/doan/WEB_TMDT/controller/DashboardController.java`
- [x] `src/main/java/com/doan/WEB_TMDT/config/SecurityConfig.java`
- [x] `src/main/java/com/doan/WEB_TMDT/security/JwtAuthenticationFilter.java`

### 2. Kiểm Tra Thay Đổi
```bash
# Xem thay đổi trong DashboardController
# Phải thấy: hasAnyAuthority('ADMIN', 'EMPLOYEE', 'ACCOUNTANT', ...)

# Xem thay đổi trong SecurityConfig
# Phải thấy: .requestMatchers("/api/dashboard/**").hasAnyAuthority("ADMIN", "EMPLOYEE", "ACCOUNTANT", ...)

# Xem thay đổi trong JwtAuthenticationFilter
# Phải thấy: authorities.add(new SimpleGrantedAuthority("EMPLOYEE"))
#           authorities.add(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))
```

## 🔄 Restart Backend

### Option 1: Restart từ IDE (IntelliJ/Eclipse)
1. Stop application hiện tại (Ctrl+F2 hoặc Stop button)
2. Run lại application (Shift+F10 hoặc Run button)
3. Đợi backend khởi động hoàn tất

### Option 2: Restart từ Command Line
```bash
# Stop backend hiện tại (Ctrl+C)

# Restart với Maven
./mvnw spring-boot:run

# Hoặc với Maven Wrapper trên Windows
mvnw.cmd spring-boot:run
```

### Option 3: Restart với JAR file
```bash
# Build lại project
./mvnw clean package

# Run JAR file
java -jar target/WEB_TMDT-0.0.1-SNAPSHOT.jar
```

## ✅ Kiểm Tra Sau Khi Restart

### 1. Kiểm Tra Backend Logs
Tìm các dòng log sau khi login với ACCOUNTANT user:

```
✅ Added authority: ACCOUNTANT
✅ Added authority: EMPLOYEE
✅ Added authority: ROLE_EMPLOYEE
✅ User position: ACCOUNTANT
🔑 Final authorities: [ACCOUNTANT, EMPLOYEE, ROLE_EMPLOYEE]
```

### 2. Test API với HTTP Client
Mở file `test-employee-dashboard-fix.http` và chạy:

```http
### 1. Login as ACCOUNTANT
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "ketoan@gmail.com",
  "password": "123456"
}

### 2. Get Dashboard Stats (Phải trả về 200 OK)
GET http://localhost:8080/api/dashboard/stats
Authorization: Bearer {{auth_token}}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "totalOrders": 123,
    "totalRevenue": 1000000,
    "totalProducts": 50,
    "totalCustomers": 100,
    "pendingOrders": 5,
    "lowStockProducts": 3,
    "overdueOrders": 2,
    "overduePayables": 1
  }
}
```

### 3. Test Frontend
1. Mở browser: http://localhost:3000
2. Login với: ketoan@gmail.com / 123456
3. Navigate to: http://localhost:3000/employee
4. Kiểm tra:
   - [ ] Dashboard hiển thị đầy đủ stats
   - [ ] Không có lỗi 403 trong console
   - [ ] NotificationBell không có lỗi
   - [ ] Recent orders hiển thị đúng

### 4. Kiểm Tra Browser Console
Mở Developer Tools (F12) và kiểm tra console logs:

**Expected Logs:**
```javascript
🔑 Token exists: true
👤 User: {id: 4, email: 'ketoan@gmail.com', fullName: 'Lê Bá Quang', ...}
👔 Employee: {fullName: 'Lê Bá Quang', position: 'ACCOUNTANT', ...}
🔄 Loading dashboard data...
📊 Stats response: {status: 200, data: {...}}
📦 Orders response: {status: 200, data: [...]}
✅ Dashboard data loaded successfully
```

**NO MORE ERRORS:**
```javascript
// ❌ Không còn thấy lỗi này nữa:
// GET http://localhost:8080/api/dashboard/stats 403 (Forbidden)
// Error: {status: 403, error: 'Forbidden', ...}
```

## 🧪 Test Cases

### Test Case 1: ACCOUNTANT User
- [x] Login thành công
- [ ] Dashboard stats load được (200 OK)
- [ ] Recent orders load được (200 OK)
- [ ] NotificationBell không có lỗi
- [ ] UI hiển thị đầy đủ dữ liệu

### Test Case 2: SALE User (nếu có)
- [ ] Login thành công
- [ ] Dashboard stats load được (200 OK)
- [ ] Recent orders load được (200 OK)

### Test Case 3: WAREHOUSE User (nếu có)
- [ ] Login thành công
- [ ] Dashboard stats load được (200 OK)
- [ ] Recent orders load được (200 OK)

### Test Case 4: ADMIN User
- [ ] Login thành công
- [ ] Dashboard stats load được (200 OK)
- [ ] Recent orders load được (200 OK)
- [ ] Vẫn có quyền truy cập tất cả endpoints

## 🐛 Troubleshooting

### Nếu Vẫn Bị 403 Error

#### 1. Kiểm Tra Backend Logs
```bash
# Tìm dòng log khi request đến /api/dashboard/stats
# Phải thấy authorities được thêm đúng
```

#### 2. Kiểm Tra JWT Token
```bash
# Copy token từ localStorage
# Paste vào https://jwt.io
# Verify claims có:
# - role: "EMPLOYEE"
# - position: "ACCOUNTANT"
```

#### 3. Clear Cache và Logout/Login Lại
```javascript
// Trong browser console
localStorage.clear()
// Reload page và login lại
```

#### 4. Kiểm Tra Database
```sql
SELECT 
    u.id,
    u.email,
    u.role,
    e.position
FROM users u
LEFT JOIN employees e ON e.user_id = u.id
WHERE u.email = 'ketoan@gmail.com';

-- Expected:
-- role: EMPLOYEE
-- position: ACCOUNTANT
```

#### 5. Rebuild Project
```bash
# Clean và rebuild
./mvnw clean package

# Restart lại
./mvnw spring-boot:run
```

### Nếu Frontend Không Load Dữ Liệu

#### 1. Clear Frontend Cache
```bash
cd src/frontend
rm -rf .next
npm run dev
```

#### 2. Kiểm Tra API Base URL
```typescript
// Trong src/frontend/lib/api.ts
// Phải là: http://localhost:8080
```

#### 3. Kiểm Tra CORS
```bash
# Backend logs phải thấy:
# CORS configuration loaded
# Allowed origins: *
```

## 📊 Success Criteria

### Backend
- [x] Compile thành công (no errors)
- [ ] Start thành công (port 8080)
- [ ] JWT filter logs hiển thị đúng authorities
- [ ] API endpoints trả về 200 OK

### Frontend
- [ ] Login thành công
- [ ] Dashboard page load được
- [ ] Stats hiển thị đúng
- [ ] No 403 errors trong console
- [ ] NotificationBell hoạt động

### Database
- [ ] User data đúng (role: EMPLOYEE)
- [ ] Employee data đúng (position: ACCOUNTANT)
- [ ] Relationships đúng (user_id mapping)

## 🎯 Final Verification

Sau khi tất cả test cases pass:

1. [ ] ACCOUNTANT user có thể truy cập employee dashboard
2. [ ] Dashboard hiển thị đầy đủ stats
3. [ ] Recent orders hiển thị đúng
4. [ ] NotificationBell không có lỗi
5. [ ] Tất cả employee positions đều có quyền truy cập
6. [ ] ADMIN vẫn hoạt động bình thường
7. [ ] Không có breaking changes

---

**✅ Nếu tất cả checklist items đều pass, fix đã thành công!**

**🚀 Có thể deploy lên production.**
