package com.ecommerce.ecommerce_backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final long MAX_SIZE = 5 * 1024 * 1024;

    @PostMapping("/review-image")
    public ResponseEntity<Map<String, String>> uploadReviewImage(@RequestParam("file") MultipartFile file) {
        return handleUpload(file, "review-images");
    }

    // ✅ NEW
    @PostMapping("/product-image")
    public ResponseEntity<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        return handleUpload(file, "product-images");
    }

    private ResponseEntity<Map<String, String>> handleUpload(MultipartFile file, String subDir) {
        Map<String, String> error = new HashMap<>();

        if (file.isEmpty()) {
            error.put("error", "No file selected!");
            return ResponseEntity.badRequest().body(error);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            error.put("error", "Only image files are allowed!");
            return ResponseEntity.badRequest().body(error);
        }
        if (file.getSize() > MAX_SIZE) {
            error.put("error", "Image must be under 5MB!");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            Path uploadPath = Paths.get("uploads/" + subDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String fileName = UUID.randomUUID() + extension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", baseUrl + "/uploads/" + subDir + "/" + fileName);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            error.put("error", "Failed to upload image. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}