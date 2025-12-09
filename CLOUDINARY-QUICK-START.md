# Cloudinary - Quick Start

## ⚡ Setup nhanh trong 5 phút

### 1. Đăng ký Cloudinary (2 phút)
- Vào: https://cloudinary.com/users/register/free
- Đăng ký tài khoản miễn phí
- Xác nhận email

### 2. Lấy thông tin API (1 phút)
- Đăng nhập: https://cloudinary.com/console
- Copy 3 thông tin:
  - **Cloud Name**: `dxxxxxx`
  - **API Key**: `123456789012345`
  - **API Secret**: `abcdefghijklmnopqrstuvwxyz`

### 3. Cấu hình Backend (1 phút)
Mở `src/main/resources/application.properties`:

```properties
# Thay YOUR_XXX bằng thông tin từ bước 2
cloudinary.cloud-name=YOUR_CLOUD_NAME
cloudinary.api-key=YOUR_API_KEY
cloudinary.api-secret=YOUR_API_SECRET
cloudinary.upload-preset=products_preset
```

### 4. Tạo Upload Preset (1 phút)
- Vào **Settings** → **Upload** → **Add upload preset**
- **Preset name**: `products_preset`
- **Signing Mode**: `Unsigned`
- **Folder**: `products`
- Click **Save**

### 5. Restart Backend
```bash
# Stop backend và start lại
```

## ✅ Xong! Giờ có thể upload ảnh

### Test API:
```http
POST http://localhost:8080/api/files/upload
Authorization: Bearer YOUR_TOKEN
Body: file (chọn ảnh)
```

### Test Frontend:
1. Đăng nhập Admin
2. Vào trang tạo sản phẩm
3. Upload ảnh
4. URL sẽ có dạng: `https://res.cloudinary.com/...`

## 📝 Files đã tạo

### Backend:
- ✅ `CloudinaryConfig.java` - Config bean
- ✅ `CloudinaryService.java` - Interface
- ✅ `CloudinaryServiceImpl.java` - Upload/delete logic
- ✅ `FileUploadController.java` - API endpoint (đã cập nhật)

### Frontend:
- ✅ `ImageUpload.tsx` - Component upload (đã có sẵn)
- ✅ `cloudinary.ts` - Helper functions (đã cập nhật)

### Docs:
- ✅ `CLOUDINARY-SETUP-GUIDE.md` - Hướng dẫn chi tiết
- ✅ `CLOUDINARY-QUICK-START.md` - Setup nhanh (file này)

## 🎯 API Endpoint

```
POST /api/files/upload
- Upload ảnh lên Cloudinary
- Chỉ ADMIN và PRODUCT_MANAGER
- Max 10MB
- Returns: Cloudinary URL
```

## 💡 Lưu ý

- ⚠️ **Không commit API Secret** vào Git
- ✅ Free tier: 25GB storage, 25GB bandwidth/tháng
- ✅ Ảnh tự động backup trên cloud
- ✅ CDN delivery toàn cầu
