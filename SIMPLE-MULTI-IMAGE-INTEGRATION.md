# 🎯 Tích hợp đơn giản: Nhiều ảnh sản phẩm

## ✅ Đã có sẵn:

1. ✅ `ProductImage` entity
2. ✅ `ProductImageRepository`
3. ✅ `ProductImageDTO`
4. ✅ `MultiImageUpload` component (Frontend)

## 🔧 Cần làm:

### Bước 1: Thêm field vào ProductServiceImpl

Mở file: `src/main/java/com/doan/WEB_TMDT/module/product/service/impl/ProductServiceImpl.java`

Thêm vào phần khai báo dependencies (sau các `private final` khác):

```java
private final com.doan.WEB_TMDT.module.product.repository.ProductImageRepository imageRepository;
```

### Bước 2: Thêm methods vào ProductService interface

Mở file: `src/main/java/com/doan/WEB_TMDT/module/product/service/ProductService.java`

Thêm vào cuối interface (trước dấu `}`):

```java
// Product Images
ApiResponse addProductImage(Long productId, String imageUrl, Boolean isPrimary);
ApiResponse getProductImages(Long productId);
ApiResponse setPrimaryImage(Long productId, Long imageId);
ApiResponse deleteProductImage(Long imageId);
ApiResponse reorderProductImages(Long productId, List<Long> imageIds);
```

### Bước 3: Implement methods trong ProductServiceImpl

Thêm vào cuối class ProductServiceImpl (trước dấu `}` cuối cùng):

```java
// === Product Images ===

@Override
@Transactional
public ApiResponse addProductImage(Long productId, String imageUrl, Boolean isPrimary) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

    if (isPrimary != null && isPrimary) {
        imageRepository.findByProductIdAndIsPrimaryTrue(productId)
                .ifPresent(img -> {
                    img.setIsPrimary(false);
                    imageRepository.save(img);
                });
    }

    long count = imageRepository.countByProductId(productId);
    
    com.doan.WEB_TMDT.module.product.entity.ProductImage image = 
        com.doan.WEB_TMDT.module.product.entity.ProductImage.builder()
            .product(product)
            .imageUrl(imageUrl)
            .displayOrder((int) count)
            .isPrimary(isPrimary != null ? isPrimary : count == 0)
            .build();

    com.doan.WEB_TMDT.module.product.entity.ProductImage saved = imageRepository.save(image);
    
    if (count == 0) {
        product.setImageUrl(imageUrl);
        productRepository.save(product);
    }

    return ApiResponse.success("Thêm ảnh thành công", toImageDTO(saved));
}

@Override
public ApiResponse getProductImages(Long productId) {
    List<com.doan.WEB_TMDT.module.product.dto.ProductImageDTO> images = 
        imageRepository.findByProductIdOrderByDisplayOrderAsc(productId)
            .stream()
            .map(this::toImageDTO)
            .collect(Collectors.toList());
    
    return ApiResponse.success("Lấy danh sách ảnh thành công", images);
}

@Override
@Transactional
public ApiResponse setPrimaryImage(Long productId, Long imageId) {
    List<com.doan.WEB_TMDT.module.product.entity.ProductImage> images = 
        imageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
    images.forEach(img -> img.setIsPrimary(false));
    imageRepository.saveAll(images);

    com.doan.WEB_TMDT.module.product.entity.ProductImage image = imageRepository.findById(imageId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
    
    image.setIsPrimary(true);
    com.doan.WEB_TMDT.module.product.entity.ProductImage saved = imageRepository.save(image);

    Product product = image.getProduct();
    product.setImageUrl(image.getImageUrl());
    productRepository.save(product);

    return ApiResponse.success("Đã đặt làm ảnh chính", toImageDTO(saved));
}

@Override
@Transactional
public ApiResponse deleteProductImage(Long imageId) {
    com.doan.WEB_TMDT.module.product.entity.ProductImage image = imageRepository.findById(imageId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
    
    Long productId = image.getProduct().getId();
    boolean wasPrimary = image.getIsPrimary();
    
    imageRepository.delete(image);
    
    if (wasPrimary) {
        List<com.doan.WEB_TMDT.module.product.entity.ProductImage> remaining = 
            imageRepository.findByProductIdOrderByDisplayOrderAsc(productId);
        if (!remaining.isEmpty()) {
            com.doan.WEB_TMDT.module.product.entity.ProductImage newPrimary = remaining.get(0);
            newPrimary.setIsPrimary(true);
            imageRepository.save(newPrimary);
            
            Product product = newPrimary.getProduct();
            product.setImageUrl(newPrimary.getImageUrl());
            productRepository.save(product);
        } else {
            Product product = productRepository.findById(productId).orElse(null);
            if (product != null) {
                product.setImageUrl(null);
                productRepository.save(product);
            }
        }
    }
    
    return ApiResponse.success("Xóa ảnh thành công", null);
}

@Override
@Transactional
public ApiResponse reorderProductImages(Long productId, List<Long> imageIds) {
    for (int i = 0; i < imageIds.size(); i++) {
        Long imageId = imageIds.get(i);
        final int order = i;
        imageRepository.findById(imageId).ifPresent(img -> {
            img.setDisplayOrder(order);
            imageRepository.save(img);
        });
    }
    
    return ApiResponse.success("Sắp xếp lại thành công", null);
}

private com.doan.WEB_TMDT.module.product.dto.ProductImageDTO toImageDTO(
        com.doan.WEB_TMDT.module.product.entity.ProductImage image) {
    return com.doan.WEB_TMDT.module.product.dto.ProductImageDTO.builder()
            .id(image.getId())
            .imageUrl(image.getImageUrl())
            .displayOrder(image.getDisplayOrder())
            .isPrimary(image.getIsPrimary())
            .altText(image.getAltText())
            .build();
}
```

### Bước 4: Thêm endpoints vào ProductController

Mở file: `src/main/java/com/doan/WEB_TMDT/module/product/controller/ProductController.java`

Thêm vào cuối class (trước dấu `}` cuối cùng):

```java
// === Product Images ===

@GetMapping("/{productId}/images")
public ApiResponse getProductImages(@PathVariable Long productId) {
    return productService.getProductImages(productId);
}

@PostMapping("/{productId}/images")
@PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCT_MANAGER')")
public ApiResponse addProductImage(
        @PathVariable Long productId,
        @RequestBody Map<String, Object> request
) {
    String imageUrl = (String) request.get("imageUrl");
    Boolean isPrimary = (Boolean) request.getOrDefault("isPrimary", false);
    return productService.addProductImage(productId, imageUrl, isPrimary);
}

@PutMapping("/{productId}/images/{imageId}/primary")
@PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCT_MANAGER')")
public ApiResponse setPrimaryImage(
        @PathVariable Long productId,
        @PathVariable Long imageId
) {
    return productService.setPrimaryImage(productId, imageId);
}

@DeleteMapping("/images/{imageId}")
@PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCT_MANAGER')")
public ApiResponse deleteProductImage(@PathVariable Long imageId) {
    return productService.deleteProductImage(imageId);
}

@PutMapping("/{productId}/images/reorder")
@PreAuthorize("hasAnyAuthority('ADMIN', 'PRODUCT_MANAGER')")
public ApiResponse reorderProductImages(
        @PathVariable Long productId,
        @RequestBody Map<String, List<Long>> request
) {
    List<Long> imageIds = request.get("imageIds");
    return productService.reorderProductImages(productId, imageIds);
}
```

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
# Start lại backend
```

### Bước 7: Sử dụng Frontend Component

Trong trang quản lý sản phẩm, thay thế `ImageUpload` bằng `MultiImageUpload`:

```tsx
import MultiImageUpload from '@/components/MultiImageUpload'

// Thay vì:
<ImageUpload value={imageUrl} onChange={setImageUrl} />

// Dùng:
<MultiImageUpload
  productId={productId}
  value={images}
  onChange={setImages}
  maxImages={10}
/>
```

---

## ✅ Xong!

Sau khi làm 7 bước trên, hệ thống sẽ hỗ trợ nhiều ảnh cho mỗi sản phẩm!

**Lý do tích hợp vào ProductService/Controller thay vì tạo riêng:**
- ✅ Đơn giản hơn
- ✅ Ít file hơn
- ✅ Dễ maintain
- ✅ Logic liên quan đến Product nên nằm trong ProductService

