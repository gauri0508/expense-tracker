package com.expensetracker.service;

import com.expensetracker.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.max-size}")
    private long maxFileSize;

    @Value("${app.file.allowed-types}")
    private String allowedTypes;

    private Path fileStorageLocation;
    private List<String> allowedMimeTypes;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedMimeTypes = Arrays.asList(allowedTypes.split(","));

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }

    public String storeFile(MultipartFile file, String userId, String expenseId) {
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + fileExtension;

        // Create user-specific directory
        Path userDir = this.fileStorageLocation.resolve(userId);
        try {
            Files.createDirectories(userDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create user directory", e);
        }

        Path targetLocation = userDir.resolve(fileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + fileName, e);
        }

        log.info("File stored: {} for user: {} expense: {}", fileName, userId, expenseId);
        return "/uploads/receipts/" + userId + "/" + fileName;
    }

    public void deleteFile(String fileUrl, String userId) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = this.fileStorageLocation.resolve(userId).resolve(fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted: {} for user: {}", fileName, userId);
            }
        } catch (IOException e) {
            log.error("Could not delete file: {}", e.getMessage());
        }
    }

    public byte[] loadFile(String userId, String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(userId).resolve(fileName);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not read file " + fileName, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BadRequestException("File size exceeds maximum allowed size (5MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedMimeTypes.contains(contentType)) {
            throw new BadRequestException("File type not allowed. Allowed types: JPEG, PNG, PDF");
        }

        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (fileName.contains("..")) {
            throw new BadRequestException("Invalid file name");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
