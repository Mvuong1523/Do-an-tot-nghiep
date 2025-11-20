# 🔄 Hướng dẫn chuyển từ Local sang Cloudinary

## 📋 Hiện tại (Local)

### Cấu trúc:
```
src/frontend/public/images/products/
  ├── macbook-pro.jpg
  ├── iphone-15.jpg
```

### Database:
```
imageUrl: "/images/products/macbook-pro.jpg"
```

### Hiển thị:
```tsx
<img src={product.imageUrl} alt={product.name} />
```

---

## 🚀 Sau này (Cloudinary)

### 1. Đăng ký Cloudinary
- Vào: https://cloudinary.com/
- Đăng ký free (25GB storage, 25GB bandwidth/tháng)
- Lấy: `cloud_name`, `api_key`, `api_secret`

### 2. Tạo Upload Preset
- Vào Settings > Upload
- Tạo unsigned upload preset
- Copy preset name

### 3. Cấu hình Environment Variables

**Frontend (.env.local):**
```env
NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME=your_cloud_name
NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET=your_upload_preset
```

**Backend (application.properties):**
```properties
cloudinary.cloud-name=your_cloud_name
cloudinary.api-key=your_api_key
cloudinary.api-secret=your_api_secret
```

### 4. Cập nhật lib/cloudinary.ts

```typescript
export const uploadToCloudinary = async (file: File): Promise<string> => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('upload_preset', process.env.NEXT_PUBLIC_CLOUDINARY_UPLOAD_PRESET!)
  
  const response = await fetch(
    `https://api.cloudinary.com/v1_1/${process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME}/image/upload`,
    {
      method: 'POST',
      body: formData
    }
  )
  
  const data = await response.json()
  return data.secure_url
}
```

### 5. Cập nhật ImageUpload component

Uncomment dòng này trong `components/ImageUpload.tsx`:
```typescript
// Thay đổi từ:
// const url = await uploadToCloudinary(file)

// Thành:
const url = await uploadToCloudinary(file)
setPreview(url)
onChange(url)
```

### 6. Migration Script (Chuyển ảnh cũ)

```typescript
// scripts/migrate-to-cloudinary.ts
import { uploadToCloudinary } from '@/lib/cloudinary'
import fs from 'fs'
import path from 'path'

async function migrateImages() {
  const productsDir = 'public/images/products'
  const files = fs.readdirSync(productsDir)
  
  for (const file of files) {
    const filePath = path.join(productsDir, file)
    const fileBuffer = fs.readFileSync(filePath)
    const fileBlob = new Blob([fileBuffer])
    const fileObj = new File([fileBlob], file)
    
    try {
      const cloudinaryUrl = await uploadToCloudinary(fileObj)
      console.log(`Migrated ${file} -> ${cloudinaryUrl}`)
      
      // TODO: Update database
      // await updateProductImageUrl(file, cloudinaryUrl)
    } catch (error) {
      console.error(`Failed to migrate ${file}:`, error)
    }
  }
}

migrateImages()
```

### 7. Database Migration

```sql
-- Backup trước
CREATE TABLE products_backup AS SELECT * FROM products;

-- Update URLs (nếu cần)
UPDATE products 
SET image_url = REPLACE(image_url, '/images/products/', 'https://res.cloudinary.com/your_cloud_name/image/upload/')
WHERE image_url LIKE '/images/products/%';
```

### 8. Cleanup

Sau khi migrate xong:
```bash
# Xóa ảnh local (backup trước!)
rm -rf src/frontend/public/images/products/*
```

---

## ✅ Checklist Migration

- [ ] Đăng ký Cloudinary account
- [ ] Tạo upload preset
- [ ] Cấu hình environment variables
- [ ] Test upload 1 ảnh mới
- [ ] Chạy migration script cho ảnh cũ
- [ ] Verify tất cả ảnh hiển thị đúng
- [ ] Update database URLs
- [ ] Xóa ảnh local (sau khi backup)
- [ ] Update documentation

---

## 🎯 Lợi ích Cloudinary

✅ **Tự động optimize** - Resize, compress, format conversion
✅ **CDN global** - Load nhanh từ mọi nơi
✅ **Transformations** - Crop, resize, effects on-the-fly
✅ **Backup tự động** - Không lo mất ảnh
✅ **Free tier** - 25GB storage, 25GB bandwidth/tháng

## 📝 Example URLs

**Local:**
```
/images/products/macbook-pro.jpg
```

**Cloudinary:**
```
https://res.cloudinary.com/demo/image/upload/v1234567890/products/macbook-pro.jpg
```

**Cloudinary với transformations:**
```
https://res.cloudinary.com/demo/image/upload/w_500,h_500,c_fill/products/macbook-pro.jpg
```
- `w_500,h_500` - Resize 500x500
- `c_fill` - Crop to fill
- `q_auto` - Auto quality
- `f_auto` - Auto format (WebP nếu browser hỗ trợ)
