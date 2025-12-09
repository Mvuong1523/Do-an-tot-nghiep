package com.doan.WEB_TMDT.module.file.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.doan.WEB_TMDT.module.file.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new RuntimeException("File không được để trống");
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("File phải là ảnh (PNG, JPG, GIF)");
            }

            // Validate file size (max 10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                throw new RuntimeException("Kích thước ảnh không được vượt quá 10MB");
            }

            // Upload to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), 
                ObjectUtils.asMap(
                    "folder", "products",  // Lưu vào folder products
                    "resource_type", "image"
                )
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("Uploaded image to Cloudinary: {}", imageUrl);
            
            return imageUrl;

        } catch (IOException e) {
            log.error("Error uploading to Cloudinary", e);
            throw new RuntimeException("Lỗi khi upload ảnh: " + e.getMessage());
        }
    }

    @Override
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted image from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Error deleting from Cloudinary", e);
            throw new RuntimeException("Lỗi khi xóa ảnh: " + e.getMessage());
        }
    }

    /**
     * Extract public_id from Cloudinary URL
     * Example: https://res.cloudinary.com/demo/image/upload/v1234567890/products/abc123.jpg
     * Returns: products/abc123
     */
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("cloudinary.com")) {
            return null;
        }
        
        try {
            // Split by /upload/ and take the part after it
            String[] parts = imageUrl.split("/upload/");
            if (parts.length < 2) return null;
            
            // Remove version (v1234567890) and extension
            String pathWithVersion = parts[1];
            String path = pathWithVersion.replaceFirst("v\\d+/", "");
            
            // Remove extension
            int lastDot = path.lastIndexOf('.');
            if (lastDot > 0) {
                path = path.substring(0, lastDot);
            }
            
            return path;
        } catch (Exception e) {
            log.error("Error extracting public_id from URL: {}", imageUrl, e);
            return null;
        }
    }
}
