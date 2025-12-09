package com.doan.WEB_TMDT.module.file.controller;

import com.doan.WEB_TMDT.common.dto.ApiResponse;
import com.doan.WEB_TMDT.module.file.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @Value("${file.upload-dir:uploads/products}")
    private String uploadDir;

    // Upload ảnh lên Cloudinary (PRODUCT_MANAGER, ADMIN)
    @PostMapping("/upload")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = cloudinaryService.uploadImage(file);
            return ApiResponse.success("Upload thành công", imageUrl);
        } catch (Exception e) {
            return ApiResponse.error("Lỗi khi upload file: " + e.getMessage());
        }
    }

    // Upload local (backup method)
    @PostMapping("/upload-local")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse uploadFileLocal(@RequestParam("file") MultipartFile file) {
        try {
            // Tạo thư mục nếu chưa có
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String filename = UUID.randomUUID().toString() + extension;

            // Lưu file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về URL
            String fileUrl = "/api/files/" + filename;
            return ApiResponse.success("Upload thành công", fileUrl);

        } catch (IOException e) {
            return ApiResponse.error("Lỗi khi upload file: " + e.getMessage());
        }
    }

    // Lấy ảnh (Public)
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Xác định content type
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Xóa ảnh (PRODUCT_MANAGER, ADMIN)
    @DeleteMapping("/{filename:.+}")
    @PreAuthorize("hasAnyAuthority('PRODUCT_MANAGER', 'ADMIN')")
    public ApiResponse deleteFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            return ApiResponse.success("Xóa file thành công");
        } catch (IOException e) {
            return ApiResponse.error("Lỗi khi xóa file: " + e.getMessage());
        }
    }
}
