# 🔄 Hướng dẫn Restart Backend

## ✅ Đã sửa

**File:** `src/main/java/com/doan/WEB_TMDT/config/SecurityConfig.java`

### Vấn đề 1: Category endpoints
**Vấn đề:** SecurityConfig đang conflict với `@PreAuthorize` trong CategoryController
- SecurityConfig yêu cầu `PRODUCT_MANAGER` hoặc `ADMIN` cho `/api/categories`
- Nhưng rule này chỉ áp dụng cho GET, không áp dụng cho POST/PUT/DELETE
- POST/PUT/DELETE bị rule `.anyRequest().authenticated()` bắt trước
- Sau đó `@PreAuthorize` trong Controller mới check, nhưng đã bị từ chối ở SecurityConfig

**Giải pháp:** Xóa rule `/api/categories` khỏi SecurityConfig, để `@PreAuthorize` trong Controller xử lý

### Vấn đề 2: Inventory stock endpoint
**Vấn đề:** PRODUCT_MANAGER không thể xem tồn kho
- SecurityConfig chặn toàn bộ `/api/inventory/**` chỉ cho WAREHOUSE và ADMIN
- Nhưng theo AUTHORIZATION.md, PRODUCT_MANAGER được phép xem tồn kho (read-only)
- InventoryController đã có `@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'PRODUCT_MANAGER', 'ADMIN')")` cho endpoint `/stock`

**Giải pháp:** Thêm rule riêng cho `/api/inventory/stock` cho phép PRODUCT_MANAGER truy cập

## 🚀 Cách Restart Backend

### Option 1: Nếu đang chạy trong IDE (IntelliJ/Eclipse)
1. Dừng ứng dụng (Stop button)
2. Chạy lại `WebTMDTApplication.java`

### Option 2: Nếu đang chạy bằng Maven
```bash
# Dừng process hiện tại (Ctrl+C)
# Sau đó chạy lại:
mvn spring-boot:run
```

### Option 3: Nếu đang chạy file JAR
```bash
# Dừng process hiện tại (Ctrl+C)
# Build lại:
mvn clean package -DskipTests
# Chạy lại:
java -jar target/WEB_TMDT-0.0.1-SNAPSHOT.jar
```

## ✅ Kiểm tra sau khi restart

1. Xem backend logs, phải thấy:
   ```
   Started WebTMDTApplication in X.XXX seconds
   ```

2. Test API trực tiếp:
   ```bash
   # Login
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"productmanager@example.com","password":"your_password"}'
   
   # Copy token từ response
   
   # Test create category
   curl -X POST http://localhost:8080/api/categories \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -d '{"name":"Test Category","description":"Test","active":true}'
   ```

3. Nếu thành công, response sẽ là:
   ```json
   {
     "success": true,
     "message": "Tạo danh mục thành công",
     "data": { ... }
   }
   ```

## 🔍 Debug nếu vẫn lỗi

Kiểm tra backend logs khi tạo category:
- Phải thấy: `✅ User role: EMPLOYEE`
- Phải thấy: `✅ User position: PRODUCT_MANAGER`

Nếu không thấy, có nghĩa là JWT token không chứa position hoặc JwtAuthenticationFilter không thêm vào authorities.

## 📝 Tóm tắt thay đổi

### Thay đổi 1: Category endpoints
**Trước:**
```java
.requestMatchers("/api/categories").hasAnyAuthority("PRODUCT_MANAGER", "ADMIN")
```
→ Chỉ áp dụng cho GET, POST/PUT/DELETE bị từ chối

**Sau:**
```java
// Xóa rule này, để @PreAuthorize trong Controller xử lý
```
→ POST/PUT/DELETE được check bởi `@PreAuthorize` trong CategoryController

**CategoryController vẫn giữ nguyên:**
```java
@PostMapping
@PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
public ApiResponse create(@Valid @RequestBody CreateCategoryRequest request) { ... }
```

### Thay đổi 2: Inventory stock endpoint
**Trước:**
```java
.requestMatchers("/api/inventory/**").hasAnyAuthority("WAREHOUSE", "ADMIN")
```
→ PRODUCT_MANAGER không thể xem tồn kho

**Sau:**
```java
.requestMatchers("/api/inventory/stock").hasAnyAuthority("WAREHOUSE", "PRODUCT_MANAGER", "ADMIN")
.requestMatchers("/api/inventory/**").hasAnyAuthority("WAREHOUSE", "ADMIN")
```
→ PRODUCT_MANAGER có thể xem tồn kho (read-only), các endpoint khác vẫn chỉ cho WAREHOUSE và ADMIN
