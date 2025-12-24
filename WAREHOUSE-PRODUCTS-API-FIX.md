# ✅ Fix API Warehouse Products

## 🐛 Vấn đề

Sau khi tạo phiếu nhập kho, sản phẩm không hiển thị trong trang "Sản phẩm kho" (`/employee/warehouse/products`).

### Nguyên nhân
Frontend gọi API `GET /api/inventory/warehouse-products` nhưng backend **không có endpoint này**.

## ✅ Giải pháp

### 1. Thêm endpoint GET warehouse-products

**File**: `src/main/java/com/doan/WEB_TMDT/module/inventory/controller/InventoryController.java`

```java
@GetMapping("/warehouse-products")
@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'PRODUCT_MANAGER', 'ADMIN')")
public ApiResponse getWarehouseProducts() {
    var products = warehouseProductRepository.findAll();
    return ApiResponse.success("Danh sách sản phẩm kho", products);
}
```

**Quyền truy cập:**
- WAREHOUSE - Có thể xem và tạo
- PRODUCT_MANAGER - Chỉ xem (để đăng bán)
- ADMIN - Full quyền

### 2. Thêm endpoint POST warehouse-products

**File**: `src/main/java/com/doan/WEB_TMDT/module/inventory/controller/InventoryController.java`

```java
@PostMapping("/warehouse-products")
@PreAuthorize("hasAnyAuthority('WAREHOUSE', 'ADMIN')")
public ApiResponse createWarehouseProduct(@Valid @RequestBody CreateWarehouseProductRequest req) {
    return inventoryService.createWarehouseProduct(req);
}
```

**Quyền truy cập:**
- WAREHOUSE - Có thể tạo
- ADMIN - Full quyền

### 3. Tạo DTO CreateWarehouseProductRequest

**File**: `src/main/java/com/doan/WEB_TMDT/module/inventory/dto/CreateWarehouseProductRequest.java`

```java
@Data
public class CreateWarehouseProductRequest {
    @NotBlank(message = "SKU không được để trống")
    private String sku;
    
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String internalName;
    
    private Long supplierId;
    private String description;
    private String techSpecsJson;
}
```

### 4. Thêm method vào Service

**Interface**: `src/main/java/com/doan/WEB_TMDT/module/inventory/service/InventoryService.java`

```java
ApiResponse createWarehouseProduct(CreateWarehouseProductRequest req);
```

**Implementation**: `src/main/java/com/doan/WEB_TMDT/module/inventory/service/impl/InventoryServiceImpl.java`

```java
@Override
public ApiResponse createWarehouseProduct(CreateWarehouseProductRequest req) {
    // Check if SKU already exists
    Optional<WarehouseProduct> existing = warehouseProductRepository.findBySku(req.getSku());
    if (existing.isPresent()) {
        return ApiResponse.error("SKU đã tồn tại: " + req.getSku());
    }
    
    // Get supplier if provided
    Supplier supplier = null;
    if (req.getSupplierId() != null) {
        supplier = supplierRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhà cung cấp #" + req.getSupplierId()));
    }
    
    // Create warehouse product
    WarehouseProduct wp = WarehouseProduct.builder()
            .sku(req.getSku())
            .internalName(req.getInternalName())
            .supplier(supplier)
            .description(req.getDescription())
            .techSpecsJson(req.getTechSpecsJson() != null ? req.getTechSpecsJson() : "{}")
            .lastImportDate(LocalDateTime.now())
            .build();
    
    WarehouseProduct saved = warehouseProductRepository.save(wp);
    
    // Parse and save specifications
    productSpecificationService.parseAndSaveSpecs(saved);
    
    return ApiResponse.success("Tạo sản phẩm kho thành công", saved);
}
```

## 📊 Luồng hoạt động

### Luồng nhập kho hiện tại

```
1. Tạo phiếu nhập (CREATED)
   POST /api/inventory/create_pchaseOrder
   ↓
   - Tạo/lấy Supplier
   - Tạo PurchaseOrder (status = CREATED)
   - Tạo WarehouseProduct (nếu chưa có)
   - Tạo PurchaseOrderItem
   ↓
2. Hoàn thiện phiếu nhập (COMPLETED)
   POST /api/inventory/import
   ↓
   - Nhập serial numbers
   - Tạo ProductDetail (serial)
   - Cập nhật InventoryStock
   - Tạo SupplierPayable (công nợ)
   - Update status = COMPLETED
```

### Khi nào sản phẩm xuất hiện trong danh sách?

**Ngay sau bước 1** - Tạo phiếu nhập:
- ✅ `WarehouseProduct` đã được tạo
- ✅ Có thể xem trong `/employee/warehouse/products`
- ❌ Chưa có serial (chưa nhập kho thực tế)
- ❌ Chưa có tồn kho

**Sau bước 2** - Hoàn thiện phiếu nhập:
- ✅ `ProductDetail` đã có (serial numbers)
- ✅ `InventoryStock` đã cập nhật
- ✅ Có thể đăng bán lên trang khách hàng

## 🧪 Cách test

### Test 1: Xem danh sách warehouse products

```bash
# Request
GET http://localhost:8080/api/inventory/warehouse-products
Authorization: Bearer <token>

# Expected Response
{
  "success": true,
  "message": "Danh sách sản phẩm kho",
  "data": [
    {
      "id": 1,
      "sku": "LAPTOP-001",
      "internalName": "Laptop Dell XPS 13",
      "supplier": {
        "id": 1,
        "name": "Công ty ABC",
        "taxCode": "0123456789"
      },
      "description": "Laptop cao cấp",
      "techSpecsJson": "{\"cpu\":\"Intel i7\",\"ram\":\"16GB\"}",
      "lastImportDate": "2024-12-24T10:30:00"
    }
  ]
}
```

### Test 2: Tạo warehouse product thủ công

```bash
# Request
POST http://localhost:8080/api/inventory/warehouse-products
Authorization: Bearer <token>
Content-Type: application/json

{
  "sku": "MOUSE-001",
  "internalName": "Chuột Logitech MX Master 3",
  "supplierId": 1,
  "description": "Chuột không dây cao cấp",
  "techSpecsJson": "{\"connection\":\"Bluetooth\",\"battery\":\"70 days\"}"
}

# Expected Response
{
  "success": true,
  "message": "Tạo sản phẩm kho thành công",
  "data": {
    "id": 2,
    "sku": "MOUSE-001",
    "internalName": "Chuột Logitech MX Master 3",
    ...
  }
}
```

### Test 3: Luồng nhập kho đầy đủ

```bash
# Bước 1: Tạo phiếu nhập
POST http://localhost:8080/api/inventory/create_pchaseOrder
{
  "poCode": "PO20241224_001",
  "createdBy": "admin",
  "supplier": {
    "name": "Công ty XYZ",
    "taxCode": "9876543210",
    "phone": "0901234567",
    "email": "xyz@example.com"
  },
  "items": [
    {
      "sku": "KEYBOARD-001",
      "internalName": "Bàn phím cơ Keychron K2",
      "quantity": 10,
      "unitCost": 2000000,
      "warrantyMonths": 12,
      "techSpecsJson": "{\"switch\":\"Gateron Brown\",\"layout\":\"75%\"}"
    }
  ]
}

# Bước 2: Kiểm tra warehouse products
GET http://localhost:8080/api/inventory/warehouse-products
# Expected: Thấy KEYBOARD-001 trong danh sách

# Bước 3: Hoàn thiện phiếu nhập (nhập serial)
POST http://localhost:8080/api/inventory/import
{
  "poId": 1,
  "items": [
    {
      "sku": "KEYBOARD-001",
      "serials": [
        "KB001-SN001",
        "KB001-SN002",
        ...
      ]
    }
  ]
}

# Bước 4: Kiểm tra tồn kho
GET http://localhost:8080/api/inventory/stock
# Expected: Thấy KEYBOARD-001 với quantity = 10
```

## 🔍 Debug

### Nếu không thấy sản phẩm sau khi nhập kho

1. **Kiểm tra database**:
```sql
-- Xem warehouse_products
SELECT * FROM warehouse_products;

-- Xem purchase_orders
SELECT * FROM purchase_orders;

-- Xem purchase_order_items
SELECT * FROM purchase_order_items;
```

2. **Kiểm tra backend logs**:
```
🆕 Tạo WarehouseProduct mới cho SKU: KEYBOARD-001
```

3. **Kiểm tra frontend console**:
```javascript
// Mở DevTools (F12) → Console
// Xem request/response
```

4. **Kiểm tra API response**:
```bash
curl -X GET http://localhost:8080/api/inventory/warehouse-products \
  -H "Authorization: Bearer <token>"
```

## 📝 Lưu ý

### 1. WarehouseProduct vs Product

- **WarehouseProduct** - Sản phẩm trong kho (chưa đăng bán)
  - Bảng: `warehouse_products`
  - Có: SKU, internalName, supplier, techSpecs
  - Không có: giá bán, category, published

- **Product** - Sản phẩm đã đăng bán (khách hàng thấy)
  - Bảng: `products`
  - Có: name, price, category, published
  - Liên kết: `warehouse_product_id` (1-1)

### 2. Quyền truy cập

| Endpoint | WAREHOUSE | PRODUCT_MANAGER | ADMIN |
|----------|-----------|-----------------|-------|
| GET /warehouse-products | ✅ | ✅ (xem) | ✅ |
| POST /warehouse-products | ✅ | ❌ | ✅ |
| POST /create_pchaseOrder | ✅ | ❌ | ✅ |
| POST /import | ✅ | ❌ | ✅ |

### 3. Khi nào tạo WarehouseProduct?

- **Tự động**: Khi tạo phiếu nhập (`createPurchaseOrder`)
- **Thủ công**: Qua endpoint `POST /warehouse-products`

### 4. TechSpecs JSON format

```json
{
  "cpu": "Intel Core i7-12700H",
  "ram": "16GB DDR5",
  "storage": "512GB NVMe SSD",
  "display": "15.6 inch FHD IPS",
  "gpu": "NVIDIA RTX 3060",
  "weight": "2.1kg"
}
```

## 🎉 Kết quả

- ✅ Endpoint `GET /api/inventory/warehouse-products` hoạt động
- ✅ Endpoint `POST /api/inventory/warehouse-products` hoạt động
- ✅ Sản phẩm hiển thị ngay sau khi tạo phiếu nhập
- ✅ Frontend có thể lấy danh sách warehouse products
- ✅ PRODUCT_MANAGER có thể xem để đăng bán

---
**Ngày sửa**: 24/12/2025  
**Trạng thái**: ✅ Hoàn thành - API warehouse products đã hoạt động
