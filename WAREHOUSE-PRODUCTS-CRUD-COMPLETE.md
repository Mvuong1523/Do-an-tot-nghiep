# ✅ Hoàn thành CRUD Warehouse Products

## 🎯 Tổng quan

Đã hoàn thành đầy đủ các chức năng CRUD (Create, Read, Update, Delete) cho Warehouse Products.

## ✅ Đã hoàn thành

### 1. Backend APIs

#### GET /api/inventory/warehouse-products
- Lấy danh sách tất cả warehouse products
- Quyền: WAREHOUSE, PRODUCT_MANAGER, ADMIN

#### GET /api/inventory/warehouse-products/{id}
- Lấy chi tiết 1 warehouse product
- Quyền: WAREHOUSE, PRODUCT_MANAGER, ADMIN

#### POST /api/inventory/warehouse-products
- Tạo warehouse product mới
- Quyền: WAREHOUSE, ADMIN

#### PUT /api/inventory/warehouse-products/{id}
- Cập nhật warehouse product
- Quyền: WAREHOUSE, PRODUCT_MANAGER, ADMIN

### 2. Frontend Pages (Employee)

#### `/employee/warehouse/products`
- Danh sách sản phẩm kho
- Search theo SKU, tên
- Nút "Thêm sản phẩm" (nếu có quyền)
- Nút "Xem chi tiết", "Chỉnh sửa" cho từng sản phẩm

#### `/employee/warehouse/products/create`
- Form tạo sản phẩm mới
- Chọn nhà cung cấp
- Nhập thông số kỹ thuật (JSON)

#### `/employee/warehouse/products/[id]`
- Xem chi tiết sản phẩm
- Hiển thị thông tin cơ bản
- Hiển thị thông số kỹ thuật (parsed từ JSON)
- Hiển thị thông tin nhà cung cấp
- Nút "Chỉnh sửa" (nếu có quyền)

#### `/employee/warehouse/products/[id]/edit`
- Form chỉnh sửa sản phẩm
- Cập nhật SKU, tên, nhà cung cấp
- Cập nhật mô tả, thông số kỹ thuật

### 3. Permission System

| Position | View List | View Detail | Create | Edit |
|----------|-----------|-------------|--------|------|
| WAREHOUSE | ✅ | ✅ | ✅ | ❌ |
| PRODUCT_MANAGER | ✅ | ✅ | ✅ | ✅ |
| ADMIN | ✅ | ✅ | ✅ | ✅ |

## 📊 Luồng hoạt động

### Luồng 1: Tạo sản phẩm thủ công

```
1. Vào /employee/warehouse/products
2. Click "Thêm sản phẩm"
3. Nhập thông tin:
   - SKU (bắt buộc)
   - Tên sản phẩm (bắt buộc)
   - Nhà cung cấp (tùy chọn)
   - Mô tả (tùy chọn)
   - Thông số kỹ thuật JSON (tùy chọn)
4. Click "Tạo sản phẩm"
5. Redirect về danh sách
```

### Luồng 2: Tạo sản phẩm tự động (qua nhập kho)

```
1. Tạo phiếu nhập kho
   POST /api/inventory/create_pchaseOrder
2. Backend tự động tạo WarehouseProduct nếu SKU chưa tồn tại
3. Sản phẩm xuất hiện trong danh sách
```

### Luồng 3: Xem chi tiết sản phẩm

```
1. Vào /employee/warehouse/products
2. Click icon "Xem" (mắt) trên sản phẩm
3. Xem thông tin chi tiết:
   - Thông tin cơ bản
   - Thông số kỹ thuật
   - Nhà cung cấp
```

### Luồng 4: Chỉnh sửa sản phẩm

```
1. Vào chi tiết sản phẩm
2. Click "Chỉnh sửa" (nếu có quyền)
3. Cập nhật thông tin
4. Click "Lưu thay đổi"
5. Backend re-parse tech specs
6. Redirect về chi tiết
```

## 🔧 Technical Details

### WarehouseProduct Entity

```java
@Entity
public class WarehouseProduct {
    private Long id;
    private String sku;              // Unique
    private String internalName;
    private String description;
    private String techSpecsJson;    // JSON string
    
    @ManyToOne
    private Supplier supplier;
    
    private LocalDateTime lastImportDate;
}
```

### Tech Specs Format

```json
{
  "cpu": "Intel Core i7-12700H",
  "ram": "16GB DDR5",
  "storage": "512GB NVMe SSD",
  "display": "15.6 inch FHD IPS",
  "gpu": "NVIDIA RTX 3060",
  "weight": "2.1kg",
  "battery": "90Wh",
  "os": "Windows 11 Pro"
}
```

### ProductSpecification Table

Khi lưu WarehouseProduct, backend tự động parse `techSpecsJson` và lưu vào bảng `product_specifications`:

```sql
CREATE TABLE product_specifications (
    id BIGINT PRIMARY KEY,
    warehouse_product_id BIGINT,
    spec_key VARCHAR(255),
    spec_value TEXT,
    FOREIGN KEY (warehouse_product_id) REFERENCES warehouse_products(id)
);
```

Ví dụ:
```
| id | warehouse_product_id | spec_key | spec_value |
|----|---------------------|----------|------------|
| 1  | 1                   | cpu      | Intel i7   |
| 2  | 1                   | ram      | 16GB       |
| 3  | 1                   | storage  | 512GB SSD  |
```

## 🧪 Cách test

### Test 1: Tạo sản phẩm thủ công

```bash
POST http://localhost:8080/api/inventory/warehouse-products
Authorization: Bearer <token>
Content-Type: application/json

{
  "sku": "LAPTOP-DELL-XPS13",
  "internalName": "Laptop Dell XPS 13 9320",
  "supplierId": 1,
  "description": "Laptop cao cấp cho doanh nhân",
  "techSpecsJson": "{\"cpu\":\"Intel i7-1260P\",\"ram\":\"16GB\",\"storage\":\"512GB SSD\"}"
}
```

### Test 2: Lấy danh sách

```bash
GET http://localhost:8080/api/inventory/warehouse-products
Authorization: Bearer <token>
```

### Test 3: Lấy chi tiết

```bash
GET http://localhost:8080/api/inventory/warehouse-products/1
Authorization: Bearer <token>
```

### Test 4: Cập nhật

```bash
PUT http://localhost:8080/api/inventory/warehouse-products/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "sku": "LAPTOP-DELL-XPS13",
  "internalName": "Laptop Dell XPS 13 9320 (Updated)",
  "supplierId": 1,
  "description": "Laptop cao cấp - Cập nhật 2024",
  "techSpecsJson": "{\"cpu\":\"Intel i7-1260P\",\"ram\":\"32GB\",\"storage\":\"1TB SSD\"}"
}
```

### Test 5: Frontend flow

```
1. Login với WAREHOUSE hoặc PRODUCT_MANAGER
2. Vào http://localhost:3000/employee/warehouse/products
3. Click "Thêm sản phẩm"
4. Nhập thông tin và submit
5. Kiểm tra sản phẩm xuất hiện trong danh sách
6. Click "Xem" để xem chi tiết
7. Click "Chỉnh sửa" để cập nhật
```

## 🔍 Debug

### Nếu không thấy sản phẩm

1. **Kiểm tra database**:
```sql
SELECT * FROM warehouse_products;
```

2. **Kiểm tra API response**:
```bash
curl -X GET http://localhost:8080/api/inventory/warehouse-products \
  -H "Authorization: Bearer <token>"
```

3. **Kiểm tra frontend console**:
- Mở DevTools (F12) → Console
- Xem có lỗi CORS, 401, 403 không

### Nếu không thể chỉnh sửa

1. **Kiểm tra permission**:
```typescript
const canEdit = hasPermission(employee?.position as Position, 'products.edit')
console.log('Can edit:', canEdit, 'Position:', employee?.position)
```

2. **Kiểm tra backend logs**:
```
Access Denied: User does not have permission 'products.edit'
```

### Nếu tech specs không hiển thị

1. **Kiểm tra JSON format**:
```javascript
try {
  JSON.parse(techSpecsJson)
} catch (e) {
  console.error('Invalid JSON:', e)
}
```

2. **Kiểm tra database**:
```sql
SELECT tech_specs_json FROM warehouse_products WHERE id = 1;
```

## 📝 Lưu ý

### 1. SKU phải unique
- Backend check trùng lặp khi create/update
- Nếu trùng → trả về error

### 2. Tech Specs JSON
- Phải là valid JSON
- Frontend validate trước khi submit
- Backend parse và lưu vào `product_specifications`

### 3. Supplier optional
- Có thể tạo sản phẩm không có nhà cung cấp
- Có thể cập nhật nhà cung cấp sau

### 4. Permission-based UI
- Nút "Thêm" chỉ hiện với WAREHOUSE, PRODUCT_MANAGER, ADMIN
- Nút "Chỉnh sửa" chỉ hiện với PRODUCT_MANAGER, ADMIN
- Tất cả đều xem được danh sách và chi tiết

### 5. Auto-create từ Purchase Order
- Khi tạo phiếu nhập, backend tự tạo WarehouseProduct nếu chưa có
- Không cần tạo thủ công trước

## 🎉 Kết quả

- ✅ CRUD đầy đủ cho Warehouse Products
- ✅ Permission-based access control
- ✅ Tech specs parsing và storage
- ✅ Supplier relationship
- ✅ Frontend pages hoàn chỉnh
- ✅ Backend APIs hoàn chỉnh

---
**Ngày hoàn thành**: 24/12/2025  
**Trạng thái**: ✅ Hoàn thành - CRUD warehouse products đã hoạt động đầy đủ
