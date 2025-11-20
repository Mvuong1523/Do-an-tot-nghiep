# 🔄 Migration: Thêm tech_specs_json vào bảng products

## 📋 Tổng quan

Thay vì phải join với `warehouse_products` mỗi lần lấy thông số kỹ thuật, giờ **copy thông số trực tiếp vào bảng `products`** khi đăng bán.

## ✅ Thay đổi

### 1. Database Schema
**File:** `V7__add_tech_specs_to_products.sql`

```sql
-- Thêm cột mới
ALTER TABLE products ADD COLUMN tech_specs_json TEXT;

-- Copy dữ liệu từ warehouse_products (cho sản phẩm đã đăng bán)
UPDATE products p
INNER JOIN warehouse_products wp ON p.warehouse_product_id = wp.id
SET p.tech_specs_json = wp.tech_specs_json
WHERE wp.tech_specs_json IS NOT NULL;
```

### 2. Entity
**File:** `Product.java`

```java
@Column(name = "tech_specs_json", columnDefinition = "TEXT")
private String techSpecsJson;
```

### 3. Service Logic
**File:** `ProductServiceImpl.java`

**Khi đăng bán sản phẩm:**
```java
Product product = Product.builder()
    // ... các field khác
    .techSpecsJson(warehouseProduct.getTechSpecsJson()) // ✅ Copy thông số
    .build();
```

**Khi lấy thông số:**
```java
// Trước: Lấy từ product.getWarehouseProduct().getSpecifications()
// Sau: Lấy từ product.getTechSpecsJson()

if (product.getTechSpecsJson() != null) {
    ObjectMapper mapper = new ObjectMapper();
    Map<String, String> specs = mapper.readValue(product.getTechSpecsJson(), Map.class);
    dto.setSpecifications(specs);
}
```

### 4. API Response
**File:** `ProductController.java`

```java
@GetMapping
public ApiResponse getAll() {
    List<Product> products = productService.getAll();
    List<ProductWithSpecsDTO> productsWithSpecs = products.stream()
            .map(productService::toProductWithSpecs)
            .collect(Collectors.toList());
    return ApiResponse.success("Danh sách sản phẩm", productsWithSpecs);
}
```

## 🎯 Lợi ích

### Trước (join với warehouse_products):
```sql
SELECT p.*, wp.tech_specs_json 
FROM products p
LEFT JOIN warehouse_products wp ON p.warehouse_product_id = wp.id
WHERE p.id = 1;
```
- ❌ Phải join mỗi lần query
- ❌ Chậm hơn
- ❌ Phụ thuộc vào warehouse_products

### Sau (lưu trực tiếp):
```sql
SELECT p.* 
FROM products p
WHERE p.id = 1;
```
- ✅ Không cần join
- ✅ Nhanh hơn
- ✅ Độc lập, dễ cache
- ✅ Có thể chỉnh sửa thông số riêng cho sản phẩm bán (nếu cần)

## 📊 Dữ liệu

### Cấu trúc JSON trong tech_specs_json:
```json
{
  "Màn hình": "6.7 inch AMOLED",
  "Chip": "Apple A17 Pro",
  "RAM": "8GB",
  "Bộ nhớ": "256GB",
  "Camera sau": "48MP + 12MP + 12MP",
  "Camera trước": "12MP",
  "Pin": "4422 mAh",
  "Hệ điều hành": "iOS 17"
}
```

## 🚀 Cách chạy migration

### Option 1: Flyway (tự động)
```bash
# Restart backend, Flyway sẽ tự động chạy migration
mvn spring-boot:run
```

### Option 2: Manual (nếu không dùng Flyway)
```sql
-- Chạy trực tiếp trong MySQL
source src/main/resources/db/migration/V7__add_tech_specs_to_products.sql
```

## ✅ Checklist

- [x] Thêm field `techSpecsJson` vào Product entity
- [x] Tạo migration SQL
- [x] Cập nhật logic copy thông số khi đăng bán
- [x] Cập nhật method `toProductWithSpecs` để parse JSON
- [x] Cập nhật API response
- [ ] Restart backend để chạy migration
- [ ] Test đăng bán sản phẩm mới
- [ ] Verify thông số hiển thị đúng

## 🔍 Testing

### 1. Kiểm tra migration
```sql
-- Xem cột mới
DESCRIBE products;

-- Kiểm tra dữ liệu
SELECT id, name, tech_specs_json FROM products LIMIT 5;
```

### 2. Test đăng bán sản phẩm
```bash
# POST /api/products/warehouse/publish
{
  "warehouseProductId": 1,
  "name": "iPhone 15 Pro Max",
  "price": 29990000,
  "categoryId": 1
}

# Response phải có techSpecsJson
{
  "success": true,
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro Max",
    "techSpecsJson": "{\"Màn hình\":\"6.7 inch\", ...}"
  }
}
```

### 3. Test API lấy sản phẩm
```bash
# GET /api/products
# Response phải có specifications
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "iPhone 15 Pro Max",
      "specifications": {
        "Màn hình": "6.7 inch AMOLED",
        "Chip": "Apple A17 Pro",
        ...
      }
    }
  ]
}
```

## 📝 Notes

- Thông số được **copy 1 lần** khi đăng bán
- Nếu cần update thông số sau khi đăng bán, có thể:
  - Option 1: Unpublish rồi publish lại
  - Option 2: Thêm API update riêng cho techSpecsJson
- Vẫn giữ link với `warehouse_product_id` để trace nguồn gốc
