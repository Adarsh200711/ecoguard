package com.ecoguard.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get("uploads/photos").toAbsolutePath().normalize();
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");

    public FileStorageService() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create directory for photo uploads: " + uploadDir, e);
        }
    }

    public String storePhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }

        // Validate extension
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file type: Only JPG, PNG, and WebP images are allowed.");
        }

        // Generate safe unique filename
        String cleanName = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
        String uniqueFilename = UUID.randomUUID().toString() + "_" + cleanName;

        try {
            Path targetLocation = this.uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            // Return public URL path
            return "/uploads/photos/" + uniqueFilename;
        } catch (IOException e) {
            throw new RuntimeException("Could not store photo file " + uniqueFilename + ". Please try again.", e);
        }
    }
}
