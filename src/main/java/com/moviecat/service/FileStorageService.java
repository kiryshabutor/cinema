package com.moviecat.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadDir;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".webp", ".gif");

    private boolean isValidExtension(String extension) {
        return extension != null && ALLOWED_EXTENSIONS.contains(extension.toLowerCase());
    }

    @Autowired
    public FileStorageService() {
        this(Paths.get("uploads"));
    }

    FileStorageService(Path uploadDir) {
        this.uploadDir = Objects.requireNonNull(uploadDir, "uploadDir");
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Failed to store empty file.");
            }

            String extension = extractExtension(file.getOriginalFilename());
            String normalizedExtension = normalizeExtension(extension);
            if (!isValidExtension(normalizedExtension)) {
                throw new IllegalArgumentException("Invalid file extension. Allowed extensions: " + ALLOWED_EXTENSIONS);
            }
            try (InputStream inputStream = file.getInputStream()) {
                return storeFile(inputStream, normalizedExtension);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file.", e);
        }
    }

    public String storeFile(InputStream inputStream, String extension) {
        Objects.requireNonNull(inputStream, "inputStream");
        String normalizedExtension = normalizeExtension(extension);
        if (!isValidExtension(normalizedExtension)) {
            throw new IllegalArgumentException("Invalid file extension. Allowed extensions: " + ALLOWED_EXTENSIONS);
        }

        String filename = UUID.randomUUID() + normalizedExtension;
        Path destinationFile = this.uploadDir.resolve(filename).normalize().toAbsolutePath();
        try {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to store file.", e);
        }
        return filename;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    private String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        return extension.trim().toLowerCase();
    }
}
