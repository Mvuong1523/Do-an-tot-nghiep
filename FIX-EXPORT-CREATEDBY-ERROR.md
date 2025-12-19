# Fix Lỗi 400 "createdBy must not be blank"

## Vấn đề
Khi gọi API `POST /api/inventory/export-for-sale`, nhận lỗi:
```json
{
  "success": false,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "createdBy": "must not be blank"
  }
}
```

## Nguyên nhân
1. DTO `SaleExportRequest` có validation `@NotBlank` cho trường `createdBy`
2. Frontend không gửi `createdBy` (đúng, vì nên lấy từ user đang đăng nhập)
3. Controller có code set `createdBy` từ Authentication NHƯNG validation `@Valid` chạy TRƯỚC khi vào method
4. → Validation fail trước khi có cơ hội set `createdBy`

## Giải pháp

### 1. Bỏ validation `@NotBlank` cho `createdBy` ✅
File: `src/main/java/com/doan/WEB_TMDT/module/inventory/dto/SaleExportRequest.java`

**Trước:**
```java
@Data
public class SaleExportRequest {
    @NotBlank  // ❌ Validation này gây lỗi
    private String createdBy;
    
    @NotNull
    private Long orderId;
    // ...
}
```

**Sau:**
```java
@Data
public class SaleExportRequest {
    // createdBy sẽ được set tự động từ Authentication trong controller
    // Không cần @NotBlank vì frontend không gửi, backend tự set
    private String createdBy;  // ✅ Bỏ @NotBlank
    
    @NotNull
    private Long orderId;
    // ...
}
```

### 2. Bỏ `@Valid` và validate thủ công ✅
File: `src/main/java/com/doan/WEB_TMDT/module/inventory/controller/InventoryController.java`

**Trước:**
```java
@PostMapping("/export-for-sale")
@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
public ApiResponse exportForSale(@Valid @RequestBody SaleExportRequest req, Authentication auth) {
    // @Valid chạy TRƯỚC khi vào đây → Lỗi validation
    String actor = auth != null ? auth.getName() : "system";
    req.setCreatedBy(actor);
    return inventoryService.exportForSale(req);
}
```

**Sau:**
```java
@PostMapping("/export-for-sale")
@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
public ApiResponse exportForSale(@RequestBody SaleExportRequest req, Authentication auth) {
    // Set createdBy từ user đang đăng nhập
    String actor = auth != null ? auth.getName() : "system";
    req.setCreatedBy(actor);
    
    // Validate manually sau khi set createdBy
    if (req.getOrderId() == null) {
        return ApiResponse.error("Order ID không được để trống");
    }
    if (req.getReason() == null || req.getReason().isBlank()) {
        return ApiResponse.error("Lý do xuất kho không được để trống");
    }
    if (req.getItems() == null || req.getItems().isEmpty()) {
        return ApiResponse.error("Danh sách sản phẩm không được để trống");
    }
    
    return inventoryService.exportForSale(req);
}
```

## Request Body (Frontend)

Frontend KHÔNG cần gửi `createdBy`:

```json
{
  "orderId": 1,
  "reason": "Xuất kho bán hàng - Giao cho khách",
  "note": "Kiểm tra kỹ trước khi giao",
  "items": [
    {
      "productSku": "IP15-128-BLK",
      "serialNumbers": ["SN001", "SN002"]
    }
  ]
}
```

Backend sẽ tự động:
1. Lấy username từ JWT token (Authentication)
2. Set vào `req.setCreatedBy(username)`
3. Lưu vào database

## Luồng xử lý

```
1. Frontend gửi request (không có createdBy)
   ↓
2. Spring Security verify JWT token
   ↓
3. Controller nhận request
   ↓
4. Set createdBy = auth.getName() (từ token)
   ↓
5. Validate các trường khác (orderId, reason, items)
   ↓
6. Gọi service để xử lý
   ↓
7. Service lưu vào DB với createdBy đã được set
```

## Lợi ích

1. **Bảo mật**: User không thể fake `createdBy`, luôn lấy từ token
2. **Đơn giản**: Frontend không cần quan tâm đến `createdBy`
3. **Chính xác**: Đảm bảo `createdBy` là user thực sự đang đăng nhập

## Testing

### 1. Đăng nhập
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "warehouse@example.com",
  "password": "password123"
}
```

### 2. Lấy token từ response
```
Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 3. Gọi API xuất kho
```http
POST http://localhost:8080/api/inventory/export-for-sale
Authorization: Bearer <TOKEN>
Content-Type: application/json

{
  "orderId": 1,
  "reason": "Xuất kho bán hàng",
  "note": "",
  "items": [
    {
      "productSku": "IP15-128-BLK",
      "serialNumbers": ["SN001"]
    }
  ]
}
```

### 4. Kết quả mong đợi
```json
{
  "success": true,
  "message": "Xuất kho thành công",
  "data": {
    "exportOrderId": 1,
    "ghnOrderCode": "GHN123456",
    "createdBy": "warehouse@example.com"  // ✅ Tự động set
  }
}
```

## Files đã thay đổi

1. ✅ `src/main/java/com/doan/WEB_TMDT/module/inventory/dto/SaleExportRequest.java`
   - Bỏ `@NotBlank` cho `createdBy`

2. ✅ `src/main/java/com/doan/WEB_TMDT/module/inventory/controller/InventoryController.java`
   - Bỏ `@Valid` annotation
   - Thêm manual validation
   - Set `createdBy` trước khi validate

3. ✅ `test-warehouse-export.http`
   - Thêm comment giải thích

## Lưu ý

- Tương tự cho API `/export-for-warranty` nếu có cùng vấn đề
- Các API khác cũng nên áp dụng pattern này cho các trường như `createdBy`, `updatedBy`
- Không nên để frontend gửi các trường audit (createdBy, createdAt, updatedBy, updatedAt)

## Troubleshooting

### Vẫn bị lỗi 400?
- Kiểm tra token có hợp lệ không
- Kiểm tra các trường khác (orderId, reason, items)
- Xem log backend để biết validation nào fail

### createdBy vẫn null?
- Kiểm tra Authentication có null không
- Kiểm tra JWT filter có chạy không
- Xem log: `auth.getName()` trả về gì

### Muốn test với user khác?
- Đăng nhập với user khác để lấy token mới
- Mỗi token tương ứng với 1 user
- createdBy sẽ tự động thay đổi theo user trong token
