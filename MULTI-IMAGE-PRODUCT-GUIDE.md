# 📸 Hướng dẫn: Nhiều ảnh cho mỗi sản phẩm

## ✅ Đã hoàn thành

Hệ thống giờ hỗ trợ **nhiều ảnh cho mỗi sản phẩm** với đầy đủ tính năng quản lý.

---

## 🎯 Tính năng

### Backend (Java Spring Boot):

✅ **ProductImage Entity** - Bảng lưu ảnh sản phẩm
- `id`: ID ảnh
- `product_id`: Liên kết với sản phẩm
- `imageUrl`: URL ảnh trên Cloudinary
- `displayOrder`: Thứ tự hiển thị (0, 1, 2...)
- `isPrimary`: Ảnh chính (thumbnail)
- `altText`: Mô tả ảnh (SEO)

✅ **API Endpoints:**
```
GET    /api/products/{productId}/images          - Lấy danh sách ảnh
POST   /api/products/{productId}/images          - Thêm ảnh mới
PUT    /api/products/{productId}/images/{id}/primary - Đặt ảnh chính
DELETE /api/products/{productId}/images/{id}     - Xóa ảnh
PUT    /api/products/{productId}/images/reorder  - Sắp xếp lại
PUT    /api/products/{productId}/images/{id}     - Cập nhật thông tin
```

✅ **Tự động xử lý:**
- Ảnh đầu tiên tự động là ảnh chính
- Xóa ảnh chính → Ảnh tiếp theo trở thành ảnh chính
- Backward compatibility với field `imageUrl` cũ

### Frontend (Next.js + React):

✅ **MultiImageUpload Component**
- Upload nhiều ảnh cùng lúc
- Drag & drop
- Preview grid
- Set ảnh chính (⭐)
- Sắp xếp thứ tự (↑↓)
- Xóa ảnh
- Validate (type, size, max images)

---

## 🚀 Cách sử dụng

### 1. Backend - Thêm ảnh cho sản phẩm

```java
// Thêm ảnh mới
POST /api/products/1/images
{
  "imageUrl": "https://res.cloudinary.com/...",
  "isPrimary": false
}

// Response
{
  "success": true,
  "message": "Thêm ảnh thành công",
  "data": {
    "id": 1,
    "imageUrl": "https://...",
    "displayOrder": 0,
    "isPrimary": true
  }
}
```

### 2. Frontend - Sử dụng Component

```tsx
import MultiImageUpload from '@/components/MultiImageUpload'

function ProductForm() {
  const [images, setImages] = useState([])

  return (
    <div>
      <label>Hình ảnh sản phẩm</label>
      <MultiImageUpload
        productId={productId}
        value={images}
        onChange={setImages}
        maxImages={10}
      />
    </div>
  )
}
```

---

## 📊 Database Schema

### Bảng `product_images`:

```sql
CREATE TABLE product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    alt_text VARCHAR(255),
    created_at TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

CREATE INDEX idx_product_images_product_id ON product_images(product_id);
CREATE INDEX idx_product_images_display_order ON product_images(display_order);
```

### Migration (nếu cần):

```sql
-- Tạo bảng mới
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

-- Migrate dữ liệu cũ (nếu có)
INSERT INTO product_images (product_id, image_url, display_order, is_primary)
SELECT id, image_url, 0, TRUE
FROM products
WHERE image_url IS NOT NULL AND image_url != '';

-- Giữ lại cột imageUrl cũ để backward compatibility
-- ALTER TABLE products DROP COLUMN image_url; -- Không xóa
```

---

## 🎨 Giao diện Component

### Upload Area:
```
┌─────────────────────────────────────┐
│         📷                          │
│   Click để upload hoặc kéo thả      │
│   PNG, JPG, GIF (MAX. 5MB)         │
│   Tối đa 10 ảnh                     │
└─────────────────────────────────────┘
```

### Images Grid:
```
┌──────────┬──────────┬──────────┬──────────┐
│ ⭐ Ảnh chính │          │          │          │
│  [Image]  │ [Image]  │ [Image]  │ [Image]  │
│  #1       │  #2      │  #3      │  #4      │
│ [⭐↑↓❌]  │ [⭐↑↓❌] │ [⭐↑↓❌] │ [⭐↑↓❌] │
└──────────┴──────────┴──────────┴──────────┘

📸 4/10 ảnh
💡 Ảnh đầu tiên sẽ hiển thị làm thumbnail
```

### Actions (Hover):
- **⭐ Set Primary** - Đặt làm ảnh chính
- **↑ Move Up** - Di chuyển lên
- **↓ Move Down** - Di chuyển xuống
- **❌ Delete** - Xóa ảnh

---

## 💡 Tính năng chi tiết

### 1. Ảnh chính (Primary Image)

**Quy tắc:**
- Mỗi sản phẩm có 1 ảnh chính duy nhất
- Ảnh chính hiển thị làm thumbnail
- Ảnh đầu tiên tự động là ảnh chính
- Click ⭐ để đổi ảnh chính

**Ví dụ:**
```
Ảnh 1: ⭐ Ảnh chính (thumbnail)
Ảnh 2: Ảnh phụ
Ảnh 3: Ảnh phụ
```

### 2. Thứ tự hiển thị (Display Order)

**Quy tắc:**
- Ảnh được sắp xếp theo `displayOrder` (0, 1, 2...)
- Click ↑↓ để thay đổi thứ tự
- Tự động cập nhật khi thêm/xóa

**Ví dụ:**
```
Before:  [A] [B] [C] [D]
         0   1   2   3

Click ↓ on B:
After:   [A] [C] [B] [D]
         0   1   2   3
```

### 3. Upload nhiều ảnh

**Cách 1: Click chọn**
1. Click vào upload area
2. Chọn nhiều file (Ctrl/Cmd + Click)
3. Tự động upload tất cả

**Cách 2: Drag & Drop**
1. Kéo nhiều file vào upload area
2. Thả chuột
3. Tự động upload

**Validate:**
- ✅ Chỉ file ảnh (PNG, JPG, GIF, WebP)
- ✅ Max 5MB/ảnh
- ✅ Tối đa 10 ảnh/sản phẩm
- ❌ File không hợp lệ sẽ bị bỏ qua

### 4. Xóa ảnh

**Quy tắc:**
- Click ❌ để xóa
- Nếu xóa ảnh chính → Ảnh tiếp theo trở thành ảnh chính
- Tự động cập nhật thứ tự

**Ví dụ:**
```
Before:  [A⭐] [B] [C]
Delete A:
After:   [B⭐] [C]
```

---

## 🔧 Tích hợp vào trang quản lý

### Trang Publish Product:

```tsx
// src/frontend/app/product-manager/products/publish/page.tsx

import MultiImageUpload from '@/components/MultiImageUpload'

// Thay thế ImageUpload cũ
<MultiImageUpload
  value={publishForm.images || []}
  onChange={(images) => setPublishForm({...publishForm, images})}
  maxImages={10}
/>
```

### Trang Edit Product:

```tsx
// src/frontend/app/product-manager/products/page.tsx

import MultiImageUpload from '@/components/MultiImageUpload'

// Thay thế ImageUpload cũ
<MultiImageUpload
  productId={editForm.id}
  value={editForm.images || []}
  onChange={(images) => setEditForm({...editForm, images})}
  maxImages={10}
/>
```

---

## 📝 API Examples

### 1. Lấy danh sách ảnh

```bash
GET /api/products/1/images

Response:
{
  "success": true,
  "data": [
    {
      "id": 1,
      "imageUrl": "https://res.cloudinary.com/.../img1.jpg",
      "displayOrder": 0,
      "isPrimary": true
    },
    {
      "id": 2,
      "imageUrl": "https://res.cloudinary.com/.../img2.jpg",
      "displayOrder": 1,
      "isPrimary": false
    }
  ]
}
```

### 2. Thêm ảnh mới

```bash
POST /api/products/1/images
Content-Type: application/json

{
  "imageUrl": "https://res.cloudinary.com/.../new-img.jpg",
  "isPrimary": false
}

Response:
{
  "success": true,
  "message": "Thêm ảnh thành công",
  "data": {
    "id": 3,
    "imageUrl": "https://...",
    "displayOrder": 2,
    "isPrimary": false
  }
}
```

### 3. Đặt ảnh chính

```bash
PUT /api/products/1/images/2/primary

Response:
{
  "success": true,
  "message": "Đã đặt làm ảnh chính",
  "data": {
    "id": 2,
    "isPrimary": true
  }
}
```

### 4. Xóa ảnh

```bash
DELETE /api/products/1/images/3

Response:
{
  "success": true,
  "message": "Xóa ảnh thành công"
}
```

### 5. Sắp xếp lại

```bash
PUT /api/products/1/images/reorder
Content-Type: application/json

{
  "imageIds": [3, 1, 2]  // Thứ tự mới
}

Response:
{
  "success": true,
  "message": "Sắp xếp lại thành công"
}
```

---

## 🎯 Use Cases

### Use Case 1: Thêm sản phẩm mới với nhiều ảnh

1. Vào trang Publish Product
2. Upload 5 ảnh cùng lúc
3. Ảnh đầu tiên tự động là ảnh chính
4. Sắp xếp lại nếu cần
5. Lưu sản phẩm

### Use Case 2: Thay đổi ảnh chính

1. Vào trang Edit Product
2. Xem danh sách ảnh hiện tại
3. Click ⭐ trên ảnh muốn làm ảnh chính
4. Lưu thay đổi

### Use Case 3: Thêm ảnh cho sản phẩm đã có

1. Vào trang Edit Product
2. Upload thêm ảnh mới
3. Sắp xếp thứ tự
4. Lưu thay đổi

---

## ✅ Checklist triển khai

### Backend:
- [x] Tạo ProductImage entity
- [x] Tạo ProductImageRepository
- [x] Tạo ProductImageService
- [x] Tạo ProductImageController
- [x] Cập nhật Product entity
- [ ] Chạy migration database
- [ ] Test API endpoints

### Frontend:
- [x] Tạo MultiImageUpload component
- [ ] Tích hợp vào Publish Product page
- [ ] Tích hợp vào Edit Product page
- [ ] Tích hợp vào Admin Create Product page
- [ ] Test upload nhiều ảnh
- [ ] Test set primary
- [ ] Test reorder
- [ ] Test delete

---

## 🚀 Tiếp theo

1. **Chạy migration database** để tạo bảng `product_images`
2. **Restart backend** để load các class mới
3. **Tích hợp component** vào các trang quản lý sản phẩm
4. **Test đầy đủ** các tính năng

---

**Hệ thống đã sẵn sàng hỗ trợ nhiều ảnh!** 🎉

Ngày hoàn thành: 2025-12-08
