# Hướng dẫn tích hợp Cloudinary để upload ảnh sản phẩm

## 🎯 Tổng quan

Hệ thống đã được tích hợp Cloudinary để upload và quản lý ảnh sản phẩm trên cloud thay vì lưu local.

## ✅ Đã hoàn thành

### Backend
- ✅ Thêm dependency `cloudinary-http44` vào `pom.xml`
- ✅ Tạo `CloudinaryConfig` - Bean configuration
- ✅ Tạo `CloudinaryService` - Service upload/delete ảnh
- ✅ Cập nhật `FileUploadController`:
  - `/api/files/upload` - Upload lên Cloudinary (mặc định)
  - `/api/files/upload-local` - Upload local (backup)
- ✅ Thêm config vào `application.properties`

### Frontend
- ✅ Component `ImageUpload` đã sẵn sàng
- ✅ Helper `uploadToCloudinary` gọi API backend
- ✅ Tạo file `.env.local.example` mẫu

## 📋 Cách setup Cloudinary

### Bước 1: Đăng ký tài khoản Cloudinary

1. Truy cập: https://cloudinary.com/users/register/free
2. Đăng ký tài khoản miễn phí (Free tier: 25GB storage, 25GB bandwidth/tháng)
3. Xác nhận email

### Bước 2: Lấy thông tin API

1. Đăng nhập vào Cloudinary Dashboard: https://cloudinary.com/console
2. Ở trang Dashboard, bạn sẽ thấy:
   - **Cloud Name**: `dxxxxxx` (ví dụ: `demo`)
   - **API Key**: `123456789012345`
   - **API Secret**: `abcdefghijklmnopqrstuvwxyz`

### Bước 3: Tạo Upload Preset (Unsigned)

1. Vào **Settings** → **Upload**
2. Scroll xuống phần **Upload presets**
3. Click **Add upload preset**
4. Cấu hình:
   - **Preset name**: `products_preset` (hoặc tên bạn muốn)
   - **Signing Mode**: Chọn **Unsigned** (để frontend có thể upload trực tiếp)
   - **Folder**: `products` (tự động lưu vào folder này)
   - **Access mode**: `public`
5. Click **Save**

### Bước 4: Cấu hình Backend

Mở file `src/main/resources/application.properties` và cập nhật:

```properties
# --- Cloudinary Configuration ---
cloudinary.cloud-name=YOUR_CLOUD_NAME
cloudinary.api-key=YOUR_API_KEY
cloudinary.api-secret=YOUR_API_SECRET
cloudinary.upload-preset=products_preset
```

**Thay thế:**
- `YOUR_CLOUD_NAME` → Cloud name từ dashboard
- `YOUR_API_KEY` → API Key từ dashboard
- `YOUR_API_SECRET` → API Secret từ dashboard

### Bước 5: Cấu hình Frontend (Optional)

Tạo file `src/frontend/.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME=YOUR_CLOUD_NAME
NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET=products_preset
```

**Lưu ý:** Frontend hiện tại upload qua Backend API nên không cần config này. Chỉ cần nếu muốn upload trực tiếp từ frontend.

### Bước 6: Restart Backend

```bash
# Stop backend nếu đang chạy
# Restart lại để load config mới
```

## 🧪 Test Upload

### Test qua API (Postman/Thunder Client)

```http
POST http://localhost:8080/api/files/upload
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: multipart/form-data

Body:
- file: [chọn file ảnh]
```

**Response thành công:**
```json
{
  "success": true,
  "message": "Upload thành công",
  "data": "https://res.cloudinary.com/YOUR_CLOUD_NAME/image/upload/v1234567890/products/abc123.jpg"
}
```

### Test qua Frontend

1. Đăng nhập với quyền ADMIN hoặc PRODUCT_MANAGER
2. Vào trang tạo sản phẩm
3. Upload ảnh
4. Kiểm tra URL ảnh có dạng `https://res.cloudinary.com/...`

## 📁 Cấu trúc Files

### Backend
```
src/main/java/com/doan/WEB_TMDT/
├── config/
│   └── CloudinaryConfig.java          # Bean config
├── module/file/
│   ├── controller/
│   │   └── FileUploadController.java  # API upload
│   └── service/
│       ├── CloudinaryService.java     # Interface
│       └── impl/
│           └── CloudinaryServiceImpl.java  # Implementation
```

### Frontend
```
src/frontend/
├── components/
│   └── ImageUpload.tsx               # Component upload ảnh
├── lib/
│   └── cloudinary.ts                 # Helper functions
└── .env.local.example                # Config mẫu
```

## 🔧 API Endpoints

### Upload ảnh lên Cloudinary
```
POST /api/files/upload
Authorization: Bearer {token}
Content-Type: multipart/form-data
Body: file (image file)

Response:
{
  "success": true,
  "message": "Upload thành công",
  "data": "https://res.cloudinary.com/..."
}
```

### Upload ảnh local (backup)
```
POST /api/files/upload-local
Authorization: Bearer {token}
Content-Type: multipart/form-data
Body: file (image file)

Response:
{
  "success": true,
  "message": "Upload thành công",
  "data": "/api/files/abc-123.jpg"
}
```

### Lấy ảnh local
```
GET /api/files/{filename}
Response: Image file
```

### Xóa ảnh local
```
DELETE /api/files/{filename}
Authorization: Bearer {token}
```

## 🔐 Bảo mật

- ✅ Chỉ ADMIN và PRODUCT_MANAGER mới upload được
- ✅ API Secret không expose ra frontend
- ✅ Upload qua backend để kiểm soát
- ✅ Validate file type (chỉ ảnh)
- ✅ Validate file size (max 10MB)

## 💡 Tính năng

### CloudinaryService

```java
// Upload ảnh
String imageUrl = cloudinaryService.uploadImage(file);
// Returns: https://res.cloudinary.com/.../products/abc123.jpg

// Xóa ảnh
cloudinaryService.deleteImage(publicId);
// publicId: products/abc123

// Extract public_id từ URL
String publicId = cloudinaryService.extractPublicId(imageUrl);
```

### Frontend Component

```tsx
import ImageUpload from '@/components/ImageUpload'

<ImageUpload
  value={imageUrl}
  onChange={(url) => setImageUrl(url)}
  disabled={false}
/>
```

## 📊 Cloudinary Free Tier

- ✅ 25 GB storage
- ✅ 25 GB bandwidth/tháng
- ✅ 25,000 transformations/tháng
- ✅ Unlimited images
- ✅ Auto backup
- ✅ CDN delivery

## 🐛 Troubleshooting

### Lỗi: "Invalid cloud_name"
- ✅ Kiểm tra `cloudinary.cloud-name` trong `application.properties`
- ✅ Đảm bảo không có khoảng trắng thừa

### Lỗi: "Invalid API key"
- ✅ Kiểm tra `cloudinary.api-key` và `cloudinary.api-secret`
- ✅ Copy chính xác từ Cloudinary dashboard

### Lỗi: "Upload failed"
- ✅ Kiểm tra file có phải ảnh không (PNG, JPG, GIF)
- ✅ Kiểm tra kích thước file < 10MB
- ✅ Kiểm tra token JWT còn hạn không

### Ảnh không hiển thị
- ✅ Kiểm tra URL có dạng `https://res.cloudinary.com/...`
- ✅ Kiểm tra Access mode = `public` trong Upload preset
- ✅ Thử mở URL trực tiếp trên browser

## 🚀 Nâng cao

### Upload nhiều ảnh cùng lúc

```java
List<String> imageUrls = new ArrayList<>();
for (MultipartFile file : files) {
    String url = cloudinaryService.uploadImage(file);
    imageUrls.add(url);
}
```

### Tối ưu ảnh (transformation)

Cloudinary tự động tối ưu ảnh. Có thể thêm params vào URL:

```
Original: https://res.cloudinary.com/.../products/abc123.jpg
Resize:   https://res.cloudinary.com/.../w_500,h_500/products/abc123.jpg
Quality:  https://res.cloudinary.com/.../q_auto/products/abc123.jpg
```

### Xóa ảnh cũ khi cập nhật sản phẩm

```java
// Lấy public_id từ URL cũ
String oldPublicId = cloudinaryService.extractPublicId(oldImageUrl);

// Upload ảnh mới
String newImageUrl = cloudinaryService.uploadImage(newFile);

// Xóa ảnh cũ
if (oldPublicId != null) {
    cloudinaryService.deleteImage(oldPublicId);
}
```

## 📚 Tài liệu tham khảo

- Cloudinary Docs: https://cloudinary.com/documentation
- Java SDK: https://cloudinary.com/documentation/java_integration
- Upload API: https://cloudinary.com/documentation/image_upload_api_reference
