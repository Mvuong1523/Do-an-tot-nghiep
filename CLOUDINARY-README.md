# 📸 Tích hợp Cloudinary - Upload ảnh sản phẩm

## ✅ Đã hoàn thành

Hệ thống đã được tích hợp **Cloudinary** để upload và quản lý ảnh sản phẩm trên cloud.

### Backend
- ✅ Thêm Cloudinary dependency
- ✅ Tạo CloudinaryConfig, CloudinaryService
- ✅ API `/api/files/upload` - Upload lên Cloudinary
- ✅ API `/api/files/upload-local` - Upload local (backup)
- ✅ Validate file type, size (max 10MB)
- ✅ Chỉ ADMIN và PRODUCT_MANAGER có quyền upload

### Frontend
- ✅ Component `ImageUpload` sẵn sàng
- ✅ Helper `uploadToCloudinary` gọi backend API
- ✅ Auto preview ảnh sau khi upload

## 🚀 Cách sử dụng

### Bước 1: Setup Cloudinary (5 phút)

Xem hướng dẫn chi tiết trong:
- **Quick Start**: `CLOUDINARY-QUICK-START.md` (setup nhanh)
- **Full Guide**: `CLOUDINARY-SETUP-GUIDE.md` (hướng dẫn đầy đủ)

Tóm tắt:
1. Đăng ký tài khoản Cloudinary (free)
2. Lấy Cloud Name, API Key, API Secret
3. Cập nhật `application.properties`
4. Restart backend

### Bước 2: Upload ảnh

**Qua API:**
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@image.jpg"
```

**Qua Frontend:**
```tsx
import ImageUpload from '@/components/ImageUpload'

<ImageUpload
  value={imageUrl}
  onChange={(url) => setImageUrl(url)}
/>
```

## 📁 Files mới

```
Backend:
├── config/CloudinaryConfig.java
├── module/file/
│   ├── service/CloudinaryService.java
│   └── service/impl/CloudinaryServiceImpl.java
└── module/file/controller/FileUploadController.java (updated)

Frontend:
├── components/ImageUpload.tsx (updated)
├── lib/cloudinary.ts (updated)
└── .env.local.example (new)

Docs:
├── CLOUDINARY-README.md (this file)
├── CLOUDINARY-QUICK-START.md
└── CLOUDINARY-SETUP-GUIDE.md
```

## 🔧 Config cần thiết

File: `src/main/resources/application.properties`

```properties
cloudinary.cloud-name=YOUR_CLOUD_NAME
cloudinary.api-key=YOUR_API_KEY
cloudinary.api-secret=YOUR_API_SECRET
cloudinary.upload-preset=products_preset
```

⚠️ **Lưu ý:** Không commit API Secret vào Git!

## 💡 Tính năng

- ✅ Upload ảnh lên Cloudinary cloud
- ✅ Auto resize, optimize ảnh
- ✅ CDN delivery toàn cầu
- ✅ Backup tự động
- ✅ Validate file type (PNG, JPG, GIF)
- ✅ Validate file size (max 10MB)
- ✅ Lưu vào folder `products`
- ✅ Trả về HTTPS URL

## 📊 Cloudinary Free Tier

- 25 GB storage
- 25 GB bandwidth/tháng
- 25,000 transformations/tháng
- Unlimited images

## 🐛 Troubleshooting

**Lỗi: "Invalid cloud_name"**
→ Kiểm tra config trong `application.properties`

**Lỗi: "Upload failed"**
→ Kiểm tra file có phải ảnh không, size < 10MB

**Ảnh không hiển thị**
→ Kiểm tra URL có dạng `https://res.cloudinary.com/...`

## 📚 Docs

- Quick Start: `CLOUDINARY-QUICK-START.md`
- Full Guide: `CLOUDINARY-SETUP-GUIDE.md`
- Cloudinary Docs: https://cloudinary.com/documentation
