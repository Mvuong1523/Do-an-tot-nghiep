# 🎭 HỆ THỐNG ROLE - GIẢI THÍCH CHI TIẾT

## 📊 Kiến trúc Role

### Backend (Java/Spring Boot)

```java
// Entity: User.java
public class User {
    @Enumerated(EnumType.STRING)
    private Role role;  // CUSTOMER, ADMIN, EMPLOYEE
    
    @OneToOne
    private Employee employee;  // Chỉ có khi role = EMPLOYEE
}

// Entity: Employee.java
public class Employee {
    @Enumerated(EnumType.STRING)
    private Position position;  // WAREHOUSE, PRODUCT_MANAGER, SALE, CSKH, ACCOUNTANT
    
    private String fullName;
    private boolean firstLogin;
}

// Enum: Role.java
public enum Role {
    CUSTOMER,   // Khách hàng
    ADMIN,      // Quản trị viên
    EMPLOYEE    // Nhân viên (chung)
}

// Enum: Position.java
public enum Position {
    WAREHOUSE,        // Nhân viên kho
    PRODUCT_MANAGER,  // Quản lý sản phẩm
    SALE,            // Nhân viên bán hàng
    CSKH,            // Chăm sóc khách hàng
    ACCOUNTANT       // Kế toán
}
```

### Frontend (TypeScript/React)

```typescript
// store/authStore.ts
export interface User {
  id?: string
  email: string
  fullName?: string
  role: 'CUSTOMER' | 'ADMIN' | 'WAREHOUSE' | 'PRODUCT_MANAGER'
  // Lưu ý: Frontend không dùng 'EMPLOYEE', mà dùng position cụ thể
  status?: string
}
```

---

## 🔄 Flow xử lý Role

### 1. Đăng nhập (Login)

#### Backend (UserServiceImpl.java)
```java
@Override
public ApiResponse login(LoginRequest request) {
    User user = userRepository.findByEmail(request.getEmail());
    
    // Lấy thông tin
    String fullName = null;
    String position = null;
    
    if (user.getEmployee() != null) {
        fullName = user.getEmployee().getFullName();
        position = user.getEmployee().getPosition().name(); // "WAREHOUSE"
    }
    
    // Tạo JWT với claims
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", user.getRole().name());  // "EMPLOYEE"
    if (position != null) {
        claims.put("position", position);  // "WAREHOUSE"
    }
    
    String token = jwtService.generateToken(user.getEmail(), claims);
    
    // Response
    LoginResponse response = new LoginResponse(
        token,
        user.getId(),
        user.getEmail(),
        fullName,
        user.getRole().name(),  // "EMPLOYEE"
        position,               // "WAREHOUSE"
        user.getStatus().name()
    );
    
    return ApiResponse.success("Đăng nhập thành công!", response);
}
```

#### Frontend (login/page.tsx)
```typescript
const response = await authApi.login({ email, password })

// Backend trả về:
// {
//   role: "EMPLOYEE",
//   position: "WAREHOUSE"
// }

// Convert position thành role
let actualRole = response.data.role
if (response.data.role === 'EMPLOYEE' && response.data.position) {
  actualRole = response.data.position  // "WAREHOUSE"
}

// Lưu vào store
setAuth(
  {
    id: response.data.userId,
    email: response.data.email,
    fullName: response.data.fullName,
    role: actualRole,  // "WAREHOUSE" (không phải "EMPLOYEE")
    status: response.data.status,
  },
  response.data.token
)

// Redirect theo role
switch (actualRole) {
  case 'WAREHOUSE':
    router.push('/warehouse')
    break
  case 'PRODUCT_MANAGER':
    router.push('/product-manager')
    break
  // ...
}
```

---

## 🔐 Phân quyền

### Backend - Spring Security

#### Cách 1: Dùng Position trong JWT claims
```java
@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
@GetMapping("/api/inventory/stock")
public ApiResponse getStocks() {
    return inventoryService.getStocks();
}
```

**Giải thích:**
- JWT claims chứa `position: "WAREHOUSE"`
- Spring Security đọc claims và check authority
- `hasAnyAuthority('WAREHOUSE')` sẽ match với position

#### Cách 2: Check trong code
```java
public ApiResponse someMethod(Authentication auth) {
    String email = auth.getName();
    User user = userRepository.findByEmail(email);
    
    if (user.getRole() == Role.EMPLOYEE) {
        Position position = user.getEmployee().getPosition();
        if (position == Position.WAREHOUSE) {
            // Cho phép
        }
    }
}
```

### Frontend - React/Next.js

```typescript
// Trong component
const { user } = useAuthStore()

// Check quyền
if (user?.role !== 'WAREHOUSE' && user?.role !== 'ADMIN') {
  toast.error('Chỉ nhân viên kho mới có quyền truy cập')
  router.push('/')
  return
}

// Hiển thị UI theo role
{user?.role === 'ADMIN' && (
  <button>Xóa sản phẩm</button>
)}

{(user?.role === 'WAREHOUSE' || user?.role === 'ADMIN') && (
  <button>Nhập kho</button>
)}
```

---

## 📋 Mapping Role

| Backend Role | Backend Position | Frontend Role | Dashboard URL |
|-------------|------------------|---------------|---------------|
| CUSTOMER | - | CUSTOMER | `/` |
| ADMIN | - | ADMIN | `/admin` |
| EMPLOYEE | WAREHOUSE | WAREHOUSE | `/warehouse` |
| EMPLOYEE | PRODUCT_MANAGER | PRODUCT_MANAGER | `/product-manager` |
| EMPLOYEE | SALE | SALE | `/sale` (TODO) |
| EMPLOYEE | CSKH | CSKH | `/cskh` (TODO) |
| EMPLOYEE | ACCOUNTANT | ACCOUNTANT | `/accountant` (TODO) |

---

## ✅ Ưu điểm của cách này

1. **Backend linh hoạt:**
   - Dễ thêm position mới (SALE, CSKH, ACCOUNTANT)
   - Không cần thêm Role mới
   - Tất cả nhân viên đều là `EMPLOYEE`

2. **Frontend đơn giản:**
   - Không cần phân biệt `EMPLOYEE` + `Position`
   - Chỉ cần check `user.role`
   - Code dễ đọc, dễ maintain

3. **Bảo mật:**
   - JWT chứa cả `role` và `position`
   - Backend verify dựa trên JWT claims
   - Frontend chỉ là UI, không ảnh hưởng bảo mật

---

## 🚨 Lưu ý quan trọng

1. **Không bao giờ check `role === 'EMPLOYEE'` ở frontend**
   - Frontend đã convert sang position cụ thể
   - Luôn check: `role === 'WAREHOUSE'` hoặc `role === 'PRODUCT_MANAGER'`

2. **Backend phải set JWT claims đúng:**
   ```java
   claims.put("role", user.getRole().name());      // "EMPLOYEE"
   claims.put("position", position);                // "WAREHOUSE"
   ```

3. **Frontend phải convert đúng:**
   ```typescript
   if (response.data.role === 'EMPLOYEE' && response.data.position) {
     actualRole = response.data.position
   }
   ```

4. **AuthProvider phải restore đúng:**
   ```typescript
   // Khi load lại trang
   const userData = await authApi.getCurrentUser()
   let actualRole = userData.role
   if (userData.role === 'EMPLOYEE' && userData.position) {
     actualRole = userData.position
   }
   ```

---

## 🔧 Troubleshooting

### Vấn đề: "Không có quyền truy cập"

**Nguyên nhân:**
- Frontend check `user.role === 'EMPLOYEE'` thay vì `'WAREHOUSE'`
- Backend không set JWT claims đúng
- AuthProvider không convert role đúng

**Giải pháp:**
1. Check console log: `console.log('User role:', user?.role)`
2. Verify JWT claims: Decode token xem có `position` không
3. Check code convert trong login và AuthProvider

### Vấn đề: Refresh trang bị mất role

**Nguyên nhân:**
- AuthProvider không restore state từ localStorage
- API `/auth/me` không trả về `position`

**Giải pháp:**
1. Implement `GET /api/auth/me` trả về đầy đủ thông tin
2. AuthProvider gọi API này khi mount
3. Convert role đúng cách

---

## 📚 Tài liệu tham khảo

- `AUTHORIZATION.md` - Chi tiết quyền hạn từng role
- `FRONTEND_PERMISSIONS.md` - Quyền truy cập các trang frontend
- `ROLE_PAGES.md` - Cấu trúc trang theo role
