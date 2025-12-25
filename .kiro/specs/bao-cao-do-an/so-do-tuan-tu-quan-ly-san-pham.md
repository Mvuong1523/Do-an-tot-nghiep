# Sơ Đồ Tuần Tự - Quản Lý Sản Phẩm và Đăng Bán Từ Kho

## Tổng Quan

Tài liệu này mô tả các sơ đồ tuần tự (sequence diagrams) cho luồng nghiệp vụ quản lý sản phẩm, đặc biệt là quy trình đăng bán sản phẩm từ kho lên trang bán hàng.

---

## 1. Sơ Đồ Tổng Quan - Luồng Đăng Bán Sản Phẩm Từ Kho

### Mô tả
Luồng này cho phép Product Manager/Admin lấy sản phẩm từ kho (WarehouseProduct) và đăng bán lên trang web (Product) để khách hàng có thể mua.

### Sơ Đồ Tuần Tự

```mermaid
sequenceDiagram
    participant PM as Product Manager
    participant UI as Admin UI
    participant API as ProductController
    participant Service as ProductService
    participant WRepo as WarehouseProductRepo
    participant PRepo as ProductRepo
    participant CRepo as CategoryRepo
    participant IRepo as InventoryStockRepo
    participant DB as Database
    
    Note over PM,DB: 1. XEM DANH SÁCH SẢN PHẨM TRONG KHO
    
    PM->>UI: Vào trang "Quản lý sản phẩm"
    UI->>API: GET /api/products/warehouse/list
    API->>Service: getWarehouseProductsForPublish()
    Service->>WRepo: findAll()
    WRepo->>DB: SELECT * FROM warehouse_products
    DB-->>WRepo: List<WarehouseProduct>
    WRepo-->>Service: warehouseProducts
    
    loop Cho mỗi WarehouseProduct
        Service->>PRepo: Tìm Product đã liên kết
        PRepo-->>Service: Optional<Product>
        Service->>IRepo: findByWarehouseProduct_Id(wpId)
        IRepo->>DB: SELECT * FROM inventory_stock WHERE warehouse_product_id = ?
        DB-->>IRepo: InventoryStock
        IRepo-->>Service: stockQuantity, sellableQuantity
        Service->>Service: Build WarehouseProductListResponse
    end
    
    Service-->>API: List<WarehouseProductListResponse>
    API-->>UI: ApiResponse với danh sách
    UI-->>PM: Hiển thị bảng sản phẩm kho
    
    Note over PM,DB: 2. ĐĂNG BÁN SẢN PHẨM MỚI
    
    PM->>UI: Click "Đăng bán" trên sản phẩm chưa publish
    UI->>PM: Hiển thị form đăng bán
    PM->>UI: Nhập thông tin:<br/>- Tên sản phẩm<br/>- Giá bán<br/>- Mô tả<br/>- Danh mục
    PM->>UI: Click "Xác nhận đăng bán"
    
    UI->>API: POST /api/products/warehouse/publish<br/>{warehouseProductId, name, price, description, categoryId}
    API->>Service: createProductFromWarehouse(request)
    
    Service->>WRepo: findById(warehouseProductId)
    WRepo->>DB: SELECT * FROM warehouse_products WHERE id = ?
    DB-->>WRepo: WarehouseProduct
    WRepo-->>Service: warehouseProduct
    
    Service->>Service: Kiểm tra đã đăng bán chưa
    alt Đã đăng bán
        Service-->>API: Error "Sản phẩm đã được đăng bán"
        API-->>UI: ApiResponse.error()
        UI-->>PM: Hiển thị thông báo lỗi
    else Chưa đăng bán
        Service->>CRepo: findById(categoryId)
        CRepo->>DB: SELECT * FROM categories WHERE id = ?
        DB-->>CRepo: Category
        CRepo-->>Service: category
        
        Service->>IRepo: findByWarehouseProduct_Id(wpId)
        IRepo->>DB: SELECT * FROM inventory_stock WHERE warehouse_product_id = ?
        DB-->>IRepo: InventoryStock
        IRepo-->>Service: sellableQuantity
        
        Service->>Service: Tạo Product mới:<br/>- name = request.name<br/>- sku = wp.sku<br/>- price = request.price<br/>- stockQuantity = sellable<br/>- warehouseProduct = wp<br/>- category = category
        
        Service->>PRepo: save(product)
        PRepo->>DB: INSERT INTO products (...)
        DB-->>PRepo: Product (với ID mới)
        PRepo-->>Service: savedProduct
        
        Service-->>API: ApiResponse.success(product)
        API-->>UI: Sản phẩm đã tạo
        UI-->>PM: "Đăng bán thành công!"<br/>Refresh danh sách
    end
```

---

## 2. Sơ Đồ Chi Tiết - Cập Nhật Sản Phẩm Đã Đăng Bán

### Mô tả
Sau khi đăng bán, Product Manager có thể cập nhật thông tin sản phẩm (tên, giá, mô tả, danh mục).

### Sơ Đồ Tuần Tự

```mermaid
sequenceDiagram
    participant PM as Product Manager
    participant UI as Admin UI
    participant API as ProductController
    participant Service as ProductService
    participant PRepo as ProductRepo
    participant CRepo as CategoryRepo
    participant IRepo as InventoryStockRepo
    participant DB as Database
    
    PM->>UI: Click "Chỉnh sửa" trên sản phẩm đã publish
    UI->>API: GET /api/products/{productId}
    API->>Service: getById(productId)
    Service->>PRepo: findById(productId)
    PRepo->>DB: SELECT * FROM products WHERE id = ?
    DB-->>PRepo: Product
    PRepo-->>Service: product
    Service-->>API: Optional<Product>
    API-->>UI: Product details
    UI-->>PM: Hiển thị form với dữ liệu hiện tại
    
    PM->>UI: Chỉnh sửa thông tin
    PM->>UI: Click "Lưu thay đổi"
    
    UI->>API: PUT /api/products/warehouse/publish/{productId}<br/>{name, price, description, categoryId}
    API->>Service: updatePublishedProduct(productId, request)
    
    Service->>PRepo: findById(productId)
    PRepo->>DB: SELECT * FROM products WHERE id = ?
    DB-->>PRepo: Product
    PRepo-->>Service: product
    
    alt Thay đổi danh mục
        Service->>CRepo: findById(categoryId)
        CRepo->>DB: SELECT * FROM categories WHERE id = ?
        DB-->>CRepo: Category
        CRepo-->>Service: category
        Service->>Service: product.setCategory(category)
    end
    
    Service->>Service: Cập nhật các field:<br/>- name<br/>- price<br/>- description
    
    Service->>Service: Lấy warehouseProduct từ product
    Service->>IRepo: findByWarehouseProduct_Id(wpId)
    IRepo->>DB: SELECT * FROM inventory_stock WHERE warehouse_product_id = ?
    DB-->>IRepo: InventoryStock
    IRepo-->>Service: sellableQuantity
    Service->>Service: product.setStockQuantity(sellable)
    
    Service->>PRepo: save(product)
    PRepo->>DB: UPDATE products SET ... WHERE id = ?
    DB-->>PRepo: Updated Product
    PRepo-->>Service: updatedProduct
    
    Service-->>API: ApiResponse.success(product)
    API-->>UI: Sản phẩm đã cập nhật
    UI-->>PM: "Cập nhật thành công!"
```


---

## 3. Sơ Đồ - Gỡ Sản Phẩm Khỏi Trang Bán (Unpublish)

### Mô tả
Product Manager có thể gỡ sản phẩm khỏi trang bán (xóa Product nhưng giữ nguyên WarehouseProduct).

### Sơ Đồ Tuần Tự

```mermaid
sequenceDiagram
    participant PM as Product Manager
    participant UI as Admin UI
    participant API as ProductController
    participant Service as ProductService
    participant PRepo as ProductRepo
    participant DB as Database
    
    PM->>UI: Click "Gỡ khỏi trang bán"
    UI->>PM: Hiển thị xác nhận:<br/>"Bạn có chắc muốn gỡ sản phẩm này?"
    PM->>UI: Click "Xác nhận"
    
    UI->>API: DELETE /api/products/warehouse/unpublish/{productId}
    API->>Service: unpublishProduct(productId)
    
    Service->>PRepo: findById(productId)
    PRepo->>DB: SELECT * FROM products WHERE id = ?
    DB-->>PRepo: Product
    PRepo-->>Service: product
    
    Service->>PRepo: delete(product)
    PRepo->>DB: DELETE FROM products WHERE id = ?
    Note over DB: Xóa Product<br/>WarehouseProduct vẫn tồn tại
    DB-->>PRepo: Success
    PRepo-->>Service: void
    
    Service-->>API: ApiResponse.success("Gỡ thành công")
    API-->>UI: Success response
    UI-->>PM: "Đã gỡ sản phẩm khỏi trang bán"<br/>Refresh danh sách
```

---

## 4. Sơ Đồ - Quản Lý Hình Ảnh Sản Phẩm

### Mô tả
Product Manager thêm, xóa, sắp xếp hình ảnh cho sản phẩm đã đăng bán.

### Sơ Đồ Tuần Tự

```mermaid
sequenceDiagram
    participant PM as Product Manager
    participant UI as Admin UI
    participant API as ProductController
    participant Service as ProductService
    participant PRepo as ProductRepo
    participant ImgRepo as ProductImageRepo
    participant Cloud as Cloudinary
    participant DB as Database
    
    Note over PM,DB: 1. THÊM HÌNH ẢNH MỚI
    
    PM->>UI: Click "Thêm ảnh"
    UI->>PM: Hiển thị dialog upload
    PM->>UI: Chọn file ảnh
    UI->>Cloud: Upload ảnh
    Cloud-->>UI: imageUrl
    
    UI->>API: POST /api/products/{productId}/images<br/>{imageUrl, isPrimary}
    API->>Service: addProductImage(productId, imageUrl, isPrimary)
    
    Service->>PRepo: findById(productId)
    PRepo->>DB: SELECT * FROM products WHERE id = ?
    DB-->>PRepo: Product
    PRepo-->>Service: product
    
    alt isPrimary = true
        Service->>ImgRepo: findByProductIdAndIsPrimaryTrue(productId)
        ImgRepo->>DB: SELECT * FROM product_images<br/>WHERE product_id = ? AND is_primary = true
        DB-->>ImgRepo: Optional<ProductImage>
        ImgRepo-->>Service: currentPrimaryImage
        
        Service->>Service: currentPrimaryImage.setIsPrimary(false)
        Service->>ImgRepo: save(currentPrimaryImage)
        ImgRepo->>DB: UPDATE product_images SET is_primary = false
    end
    
    Service->>ImgRepo: countByProductId(productId)
    ImgRepo->>DB: SELECT COUNT(*) FROM product_images WHERE product_id = ?
    DB-->>ImgRepo: count
    ImgRepo-->>Service: displayOrder = count
    
    Service->>Service: Tạo ProductImage mới:<br/>- imageUrl<br/>- displayOrder<br/>- isPrimary
    
    Service->>ImgRepo: save(productImage)
    ImgRepo->>DB: INSERT INTO product_images (...)
    DB-->>ImgRepo: ProductImage (với ID)
    ImgRepo-->>Service: savedImage
    
    Service-->>API: ApiResponse.success(imageDTO)
    API-->>UI: Ảnh đã thêm
    UI-->>PM: Hiển thị ảnh mới trong gallery
    
    Note over PM,DB: 2. ĐẶT ẢNH CHÍNH
    
    PM->>UI: Click "Đặt làm ảnh chính" trên một ảnh
    UI->>API: PUT /api/products/{productId}/images/{imageId}/primary
    API->>Service: setPrimaryImage(productId, imageId)
    
    Service->>ImgRepo: findByProductIdOrderByDisplayOrderAsc(productId)
    ImgRepo->>DB: SELECT * FROM product_images<br/>WHERE product_id = ? ORDER BY display_order
    DB-->>ImgRepo: List<ProductImage>
    ImgRepo-->>Service: allImages
    
    Service->>Service: Loop: Set tất cả isPrimary = false
    Service->>ImgRepo: saveAll(allImages)
    ImgRepo->>DB: UPDATE product_images SET is_primary = false
    
    Service->>ImgRepo: findById(imageId)
    ImgRepo->>DB: SELECT * FROM product_images WHERE id = ?
    DB-->>ImgRepo: ProductImage
    ImgRepo-->>Service: selectedImage
    
    Service->>Service: selectedImage.setIsPrimary(true)
    Service->>ImgRepo: save(selectedImage)
    ImgRepo->>DB: UPDATE product_images SET is_primary = true WHERE id = ?
    DB-->>ImgRepo: Updated image
    ImgRepo-->>Service: primaryImage
    
    Service-->>API: ApiResponse.success(imageDTO)
    API-->>UI: Success
    UI-->>PM: Cập nhật UI, ảnh chính có badge "Chính"
    
    Note over PM,DB: 3. XÓA HÌNH ẢNH
    
    PM->>UI: Click "Xóa" trên một ảnh
    UI->>PM: Xác nhận "Bạn có chắc muốn xóa?"
    PM->>UI: Click "Xác nhận"
    
    UI->>API: DELETE /api/products/images/{imageId}
    API->>Service: deleteProductImage(imageId)
    
    Service->>ImgRepo: findById(imageId)
    ImgRepo->>DB: SELECT * FROM product_images WHERE id = ?
    DB-->>ImgRepo: ProductImage
    ImgRepo-->>Service: image (productId, wasPrimary)
    
    Service->>ImgRepo: delete(image)
    ImgRepo->>DB: DELETE FROM product_images WHERE id = ?
    DB-->>ImgRepo: Success
    
    alt wasPrimary = true
        Service->>ImgRepo: findByProductIdOrderByDisplayOrderAsc(productId)
        ImgRepo->>DB: SELECT * FROM product_images<br/>WHERE product_id = ? ORDER BY display_order LIMIT 1
        DB-->>ImgRepo: List<ProductImage>
        ImgRepo-->>Service: remainingImages
        
        alt Còn ảnh khác
            Service->>Service: firstImage.setIsPrimary(true)
            Service->>ImgRepo: save(firstImage)
            ImgRepo->>DB: UPDATE product_images SET is_primary = true
        end
    end
    
    Service-->>API: ApiResponse.success("Xóa thành công")
    API-->>UI: Success
    UI-->>PM: Xóa ảnh khỏi gallery
    
    Note over PM,DB: 4. SẮP XẾP LẠI THỨ TỰ ẢNH
    
    PM->>UI: Kéo thả ảnh để sắp xếp
    UI->>UI: Cập nhật thứ tự trong UI
    PM->>UI: Click "Lưu thứ tự"
    
    UI->>API: PUT /api/products/{productId}/images/reorder<br/>{imageIds: [3, 1, 2, 4]}
    API->>Service: reorderProductImages(productId, imageIds)
    
    loop Cho mỗi imageId
        Service->>ImgRepo: findById(imageId)
        ImgRepo->>DB: SELECT * FROM product_images WHERE id = ?
        DB-->>ImgRepo: ProductImage
        ImgRepo-->>Service: image
        
        Service->>Service: image.setDisplayOrder(index)
        Service->>ImgRepo: save(image)
        ImgRepo->>DB: UPDATE product_images SET display_order = ? WHERE id = ?
    end
    
    Service-->>API: ApiResponse.success("Sắp xếp thành công")
    API-->>UI: Success
    UI-->>PM: "Đã lưu thứ tự ảnh"
```

---

## 5. Sơ Đồ - Khách Hàng Xem Sản Phẩm

### Mô tả
Khách hàng xem danh sách và chi tiết sản phẩm đã được đăng bán.

### Sơ Đồ Tuần Tự

```mermaid
sequenceDiagram
    participant C as Customer
    participant Web as Website
    participant API as ProductController
    participant Service as ProductService
    participant PRepo as ProductRepo
    participant ImgRepo as ProductImageRepo
    participant DB as Database
    
    Note over C,DB: 1. XEM DANH SÁCH SẢN PHẨM
    
    C->>Web: Truy cập trang sản phẩm
    Web->>API: GET /api/products
    API->>Service: getAll()
    
    Service->>PRepo: findAll()
    PRepo->>DB: SELECT p.*, c.* FROM products p<br/>LEFT JOIN categories c ON p.category_id = c.id
    DB-->>PRepo: List<Product> (với Category)
    PRepo-->>Service: products
    
    Service->>Service: Filter: active = true
    
    loop Cho mỗi Product
        Service->>ImgRepo: findByProductIdOrderByDisplayOrderAsc(productId)
        ImgRepo->>DB: SELECT * FROM product_images<br/>WHERE product_id = ? ORDER BY display_order
        DB-->>ImgRepo: List<ProductImage>
        ImgRepo-->>Service: images
        
        Service->>Service: Build ProductWithSpecsDTO:<br/>- id, name, sku, price<br/>- categoryName<br/>- images<br/>- specifications (từ techSpecsJson)
    end
    
    Service-->>API: List<ProductWithSpecsDTO>
    API-->>Web: ApiResponse.success(products)
    Web-->>C: Hiển thị grid sản phẩm với:<br/>- Ảnh chính<br/>- Tên<br/>- Giá<br/>- Danh mục
    
    Note over C,DB: 2. XEM CHI TIẾT SẢN PHẨM
    
    C->>Web: Click vào một sản phẩm
    Web->>API: GET /api/products/{id}
    API->>Service: getById(id)
    
    Service->>PRepo: findById(id)
    PRepo->>DB: SELECT p.*, c.* FROM products p<br/>LEFT JOIN categories c ON p.category_id = c.id<br/>WHERE p.id = ?
    DB-->>PRepo: Optional<Product>
    PRepo-->>Service: product
    
    Service->>ImgRepo: findByProductIdOrderByDisplayOrderAsc(id)
    ImgRepo->>DB: SELECT * FROM product_images<br/>WHERE product_id = ? ORDER BY display_order
    DB-->>ImgRepo: List<ProductImage>
    ImgRepo-->>Service: images
    
    Service->>Service: Parse techSpecsJson thành Map<String, String>
    Service->>Service: Build ProductWithSpecsDTO đầy đủ
    
    Service-->>API: Optional<ProductWithSpecsDTO>
    API-->>Web: ApiResponse.success(productDTO)
    Web-->>C: Hiển thị trang chi tiết:<br/>- Gallery ảnh (có thể zoom, slide)<br/>- Tên, giá, mô tả<br/>- Thông số kỹ thuật<br/>- Số lượng còn lại<br/>- Nút "Thêm vào giỏ"
```

---

## 6. Sơ Đồ - Bật/Tắt Trạng Thái Bán (Toggle Active)

### Mô tả
Product Manager có thể tạm ngừng bán hoặc mở lại bán sản phẩm mà không cần xóa.

### Sơ Đồ Tuần Tự

```mermaid
sequenceDiagram
    participant PM as Product Manager
    participant UI as Admin UI
    participant API as ProductController
    participant Service as ProductService
    participant PRepo as ProductRepo
    participant DB as Database
    
    PM->>UI: Click toggle "Đang bán / Ngừng bán"
    UI->>API: PUT /api/products/{id}/toggle-active
    API->>Service: getById(id)
    
    Service->>PRepo: findById(id)
    PRepo->>DB: SELECT * FROM products WHERE id = ?
    DB-->>PRepo: Product
    PRepo-->>Service: product
    
    Service->>Service: currentActive = product.getActive()<br/>(null → true)
    Service->>Service: product.setActive(!currentActive)
    
    Service->>PRepo: save(product)
    PRepo->>DB: UPDATE products SET active = ? WHERE id = ?
    DB-->>PRepo: Updated Product
    PRepo-->>Service: updatedProduct
    
    Service-->>API: updatedProduct
    API-->>UI: ApiResponse.success("Đã chuyển sang trạng thái X")
    UI-->>PM: Cập nhật UI:<br/>- Badge "Đang bán" / "Ngừng bán"<br/>- Toggle button state
    
    Note over C,Web: Khách hàng không thấy sản phẩm active=false
```

---

## 7. Tổng Hợp Các API Endpoints

### Bảng API cho Product Manager/Admin

| Endpoint | Method | Mô tả | Request Body | Response |
|----------|--------|-------|--------------|----------|
| `/api/products/warehouse/list` | GET | Lấy danh sách sản phẩm kho | - | List<WarehouseProductListResponse> |
| `/api/products/warehouse/publish` | POST | Đăng bán sản phẩm từ kho | CreateProductFromWarehouseRequest | Product |
| `/api/products/warehouse/publish/{id}` | PUT | Cập nhật sản phẩm đã đăng | CreateProductFromWarehouseRequest | Product |
| `/api/products/warehouse/unpublish/{id}` | DELETE | Gỡ sản phẩm khỏi trang bán | - | Success message |
| `/api/products/{id}/toggle-active` | PUT | Bật/tắt trạng thái bán | - | Product |
| `/api/products/{id}/images` | POST | Thêm ảnh sản phẩm | {imageUrl, isPrimary} | ProductImageDTO |
| `/api/products/{id}/images/{imgId}/primary` | PUT | Đặt ảnh chính | - | ProductImageDTO |
| `/api/products/images/{imgId}` | DELETE | Xóa ảnh | - | Success message |
| `/api/products/{id}/images/reorder` | PUT | Sắp xếp lại ảnh | {imageIds: []} | Success message |

### Bảng API cho Khách Hàng

| Endpoint | Method | Mô tả | Query Params | Response |
|----------|--------|-------|--------------|----------|
| `/api/products` | GET | Danh sách sản phẩm (active=true) | - | List<ProductWithSpecsDTO> |
| `/api/products/{id}` | GET | Chi tiết sản phẩm | - | ProductWithSpecsDTO |
| `/api/products/search-by-specs` | GET | Tìm theo thông số | keyword | List<Product> |
| `/api/products/filter-by-specs` | GET | Lọc theo thông số | key, value | List<Product> |

---

## 8. Luồng Dữ Liệu và Quan Hệ

### Mối Quan Hệ Giữa Các Entity

```mermaid
erDiagram
    WAREHOUSE_PRODUCT ||--o| PRODUCT : "publishes to"
    WAREHOUSE_PRODUCT ||--|| INVENTORY_STOCK : "has"
    PRODUCT }o--|| CATEGORY : "belongs to"
    PRODUCT ||--o{ PRODUCT_IMAGE : "has"
    
    WAREHOUSE_PRODUCT {
        Long id PK
        String sku UK
        String internalName
        String description
        String techSpecsJson
        Date lastImportDate
    }
    
    INVENTORY_STOCK {
        Long id PK
        Long warehouseProductId FK
        Long onHand
        Long reserved
        Long damaged
        Long sellable "computed"
    }
    
    PRODUCT {
        Long id PK
        String sku
        String name
        Double price
        String description
        Long stockQuantity
        Long categoryId FK
        Long warehouseProductId FK
        Boolean active
        String techSpecsJson
    }
    
    CATEGORY {
        Long id PK
        String name
        Long parentId FK
        Boolean active
    }
    
    PRODUCT_IMAGE {
        Long id PK
        Long productId FK
        String imageUrl
        Integer displayOrder
        Boolean isPrimary
        String altText
    }
```

### Luồng Dữ Liệu Khi Đăng Bán

```
1. WarehouseProduct (Kho)
   ↓ (Product Manager chọn)
2. Nhập thông tin: name, price, description, category
   ↓
3. Lấy sellableQuantity từ InventoryStock
   ↓
4. Tạo Product mới
   ↓ (liên kết)
5. Product.warehouseProductId → WarehouseProduct.id
   ↓
6. Khách hàng thấy Product trên website
```

### Đồng Bộ Số Lượng Tồn Kho

```
InventoryStock.sellable (Source of Truth)
   ↓ (sync khi update)
Product.stockQuantity (Display cho khách hàng)
```

**Khi nào sync:**
- Khi đăng bán sản phẩm mới
- Khi cập nhật sản phẩm đã đăng
- Khi nhập/xuất kho (tự động qua event)

---

## 9. Kết Luận

### Điểm Mạnh Của Thiết Kế

1. **Tách biệt rõ ràng**: WarehouseProduct (nội bộ) vs Product (public)
2. **Linh hoạt**: Có thể publish/unpublish mà không mất dữ liệu kho
3. **Quản lý ảnh độc lập**: Dễ dàng thêm/xóa/sắp xếp ảnh
4. **Đồng bộ tồn kho**: Luôn lấy từ InventoryStock (single source of truth)
5. **Soft delete**: Dùng active flag thay vì xóa cứng

### Các Bước Trong Quy Trình Đăng Bán

1. **Nhập hàng vào kho** → Tạo WarehouseProduct + InventoryStock
2. **Product Manager xem danh sách kho** → API trả về sản phẩm chưa publish
3. **Chọn sản phẩm và đăng bán** → Tạo Product liên kết với WarehouseProduct
4. **Thêm hình ảnh** → Upload Cloudinary, lưu ProductImage
5. **Khách hàng xem và mua** → Chỉ thấy Product có active=true
6. **Cập nhật/Gỡ bán** → Update hoặc delete Product (WarehouseProduct vẫn tồn tại)
