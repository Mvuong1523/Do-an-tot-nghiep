# 🔒 Tích hợp Product Images với Validation đầy đủ

## ✅ Validation đã thêm:

### 1. Thêm ảnh (addProductImage):
- ✅ Kiểm tra sản phẩm tồn tại
- ✅ **Giới hạn tối đa 9 ảnh/sản phẩm**
- ✅ Kiểm tra URL không rỗng
- ✅ Kiểm tra URL hợp lệ (http/https)

### 2. Lấy danh sách ảnh (getProductImages):
- ✅ Kiểm tra sản phẩm tồn tại

### 3. Đặt ảnh chính (setPrimaryImage):
- ✅ Kiểm tra sản phẩm tồn tại
- ✅ Kiểm tra ảnh tồn tại
- ✅ Kiểm tra ảnh thuộc về sản phẩm

### 4. Xóa ảnh (deleteProductImage):
- ✅ Kiểm tra ảnh tồn tại
- ✅ Tự động set ảnh mới làm primary nếu xóa ảnh chính

### 5. Sắp xếp lại (reorderProductImages):
- ✅ Kiểm tra sản phẩm tồn tại
- ✅ Kiểm tra danh sách không rỗng
- ✅ Kiểm tra số lượng ảnh khớp
- ✅ Kiểm tra tất cả ảnh thuộc về sản phẩm

### 6. Cập nhật ảnh (updateProductImage) - MỚI:
- ✅ Kiểm tra ảnh tồn tại
- ✅ Kiểm tra dữ liệu hợp lệ
- ✅ Kiểm tra URL hợp lệ (nếu cập nhật URL)
- ✅ Tự động cập nhật imageUrl cũ nếu là ảnh chính

---

## 📝 Hướng dẫn tích hợp

### Bước 1: Thêm field vào ProductServiceImpl

Mở: `src/main/java/com/doan/WEB_TMDT/module/product/service/impl/ProductServiceImpl.java`

Thêm vào phần khai báo dependencies:

```java
private final ProductImageRepository imageRepository;
```

### Bước 2: Thêm methods vào ProductService

Mở: `src/main/java/com/doan/WEB_TMDT/module/product/service/ProductService.java`

Copy toàn bộ nội dung từ file: **`ProductServiceInterface.java`**

### Bước 3: Implement methods trong ProductServiceImpl

Mở: `src/main/java/com/doan/WEB_TMDT/module/product/service/impl/ProductServiceImpl.java`

Copy toàn bộ nội dung từ file: **`ProductImageMethods.java`**

Paste vào cuối class (trước dấu `}` cuối cùng)

### Bước 4: Thêm endpoints vào ProductController

Mở: `src/main/java/com/doan/WEB_TMDT/module/product/controller/ProductController.java`

Copy toàn bộ nội dung từ file: **`ProductControllerImageEndpoints.java`**

Paste vào cuối class (trước dấu `}` cuối cùng)

### Bước 5: Chạy Migration SQL

```sql
CREATE TABLE product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    alt_text VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);
CREATE INDEX idx_product_images_display_order ON product_images(display_order);
```

### Bước 6: Restart Backend

```bash
# Stop backend
# Start lại
```

---

## 🎯 API Endpoints

### 1. Lấy danh sách ảnh
```http
GET /api/products/{productId}/images

Response:
{
  "success": true,
  "message": "Lấy danh sách ảnh thành công",
  "data": [
    {
      "id": 1,
      "imageUrl": "https://...",
      "displayOrder": 0,
      "isPrimary": true,
      "altText": null
    }
  ]
}
```

### 2. Thêm ảnh mới
```http
POST /api/products/{productId}/images
Authorization: Bearer {token}

Body:
{
  "imageUrl": "https://res.cloudinary.com/.../image.jpg",
  "isPrimary": false
}

Response (Success):
{
  "success": true,
  "message": "Thêm ảnh thành công",
  "data": {...}
}

Response (Error - Vượt quá 9 ảnh):
{
  "success": false,
  "message": "Sản phẩm chỉ được tối đa 9 ảnh"
}
```

### 3. Đặt ảnh chính
```http
PUT /api/products/{productId}/images/{imageId}/primary
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "Đã đặt làm ảnh chính",
  "data": {...}
}
```

### 4. Xóa ảnh
```http
DELETE /api/products/images/{imageId}
Authorization: Bearer {token}

Response:
{
  "success": true,
  "message": "Xóa ảnh thành công"
}
```

### 5. Sắp xếp lại
```http
PUT /api/products/{productId}/images/reorder
Authorization: Bearer {token}

Body:
{
  "imageIds": [3, 1, 2]
}

Response:
{
  "success": true,
  "message": "Sắp xếp lại thành công"
}
```

### 6. Cập nhật ảnh
```http
PUT /api/products/images/{imageId}
Authorization: Bearer {token}

Body:
{
  "imageUrl": "https://new-url.jpg",
  "altText": "Mô tả ảnh"
}

Response:
{
  "success": true,
  "message": "Cập nhật ảnh thành công",
  "data": {...}
}
```

---

## 🔒 Validation Rules

### Constant:
```java
private static final int MAX_IMAGES_PER_PRODUCT = 9;
```

### Rules:
1. **Tối đa 9 ảnh/sản phẩm** - Chặn cứng ở backend
2. **URL phải hợp lệ** - Bắt đầu bằng http:// hoặc https://
3. **Ảnh phải thuộc về sản phẩm** - Không thể set primary/xóa ảnh của sản phẩm khác
4. **Tự động quản lý ảnh chính** - Luôn có 1 ảnh primary
5. **Backward compatibility** - Tự động cập nhật field `imageUrl` cũ

---

## 🎨 Frontend Integration

Sử dụng component `MultiImageUpload`:

```tsx
import MultiImageUpload from '@/components/MultiImageUpload'

function ProductForm() {
  const [images, setImages] = useState([])

  return (
    <div>
      <label>Hình ảnh sản phẩm (Tối đa 9 ảnh)</label>
      <MultiImageUpload
        productId={productId}
        value={images}
        onChange={setImages}
        maxImages={9}  // Khớp với backend
      />
    </div>
  )
}
```

---

## ✅ Checklist

- [ ] Thêm field `imageRepository` vào ProductServiceImpl
- [ ] Thêm methods vào ProductService interface
- [ ] Implement methods trong ProductServiceImpl
- [ ] Thêm endpoints vào ProductController
- [ ] Chạy migration SQL
- [ ] Restart backend
- [ ] Test API với Postman
- [ ] Tích hợp frontend component
- [ ] Test upload 9 ảnh
- [ ] Test upload ảnh thứ 10 (phải bị chặn)
- [ ] Test set primary
- [ ] Test delete
- [ ] Test reorder
- [ ] Test update

---

## 🎉 Kết quả

Sau khi hoàn thành:
- ✅ Mỗi sản phẩm tối đa 9 ảnh (chặn cứng backend + frontend)
- ✅ Validation đầy đủ
- ✅ API RESTful chuẩn
- ✅ Tự động quản lý ảnh chính
- ✅ Backward compatible
- ✅ Bảo mật (chỉ ADMIN/PRODUCT_MANAGER)

---

**Files cần dùng:**
1. `ProductServiceInterface.java` - Copy vào ProductService
2. `ProductImageMethods.java` - Copy vào ProductServiceImpl
3. `ProductControllerImageEndpoints.java` - Copy vào ProductController

**Chúc bạn tích hợp thành công!** 🚀
