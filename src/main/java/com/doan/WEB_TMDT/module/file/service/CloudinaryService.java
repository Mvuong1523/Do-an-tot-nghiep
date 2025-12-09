package com.doan.WEB_TMDT.module.file.service;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {
    String uploadImage(MultipartFile file);
    void deleteImage(String publicId);
}
