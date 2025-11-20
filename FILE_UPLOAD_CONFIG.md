# 📁 Cấu hình Upload File

## 1. Thêm vào application.properties

```properties
# File Upload Configuration
file.upload-dir=uploads/products
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## 2. Cấu hình SecurityConfig

Thêm vào SecurityConfig.java:

```java
.requestMatchers("/api/files/**").permitAll() // Public để xem ảnh
```

## 3. Cách sử dụng

### Upload ảnh (Frontend):

```typescript
// Trong component upload
const handleFileUpload = async (file: File) => {
  const formData = new FormData()
  formData.append('file', file)
  
  try {
    const response = await fetch('http://localhost:8080/api/files/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: formData
    })
    
    const data = await response.json()
    if (data.success) {
      // data.data = "/api/files/abc-123.jpg"
      setImageUrl(data.data)
    }
  } catch (error) {
    console.error('Upload error:', error)
  }
}
```

### Hiển thị ảnh:

```tsx
<img src={`http://localhost:8080${product.imageUrl}`} alt={product.name} />
```

## 4. Cấu trúc thư mục

```
project/
  uploads/
    products/
      abc-123-456.jpg
      def-789-012.png
```

## 5. Option đơn giản hơn: Dùng public folder của Frontend

### Cấu trúc:
```
src/frontend/
  public/
    images/
      products/
        macbook-pro.jpg
        iphone-15.jpg
```

### Cách dùng:
```typescript
// Lưu trong database
imageUrl: "/images/products/macbook-pro.jpg"

// Hiển thị
<img src={product.imageUrl} alt={product.name} />
```

### Ưu điểm:
- ✅ Đơn giản, không cần API
- ✅ Next.js tự động serve từ public
- ✅ Không cần authentication

### Nhược điểm:
- ❌ Phải deploy lại khi thêm ảnh mới
- ❌ Không có upload UI
- ❌ Khó quản lý nhiều ảnh

## 6. Khuyến nghị

**Cho development/demo:** Dùng public folder (Option 1)
**Cho production:** Dùng backend upload (Option 2) hoặc cloud storage (S3, Cloudinary)

## 7. Cloud Storage (Nâng cao)

### Cloudinary (Free tier: 25GB)
```typescript
import { v2 as cloudinary } from 'cloudinary'

cloudinary.config({
  cloud_name: 'your_cloud_name',
  api_key: 'your_api_key',
  api_secret: 'your_api_secret'
})

const uploadToCloudinary = async (file: File) => {
  const result = await cloudinary.uploader.upload(file.path)
  return result.secure_url // https://res.cloudinary.com/...
}
```

### AWS S3
```java
@Service
public class S3Service {
    private final AmazonS3 s3Client;
    
    public String uploadFile(MultipartFile file) {
        String key = "products/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        s3Client.putObject(bucketName, key, file.getInputStream(), null);
        return s3Client.getUrl(bucketName, key).toString();
    }
}
```
