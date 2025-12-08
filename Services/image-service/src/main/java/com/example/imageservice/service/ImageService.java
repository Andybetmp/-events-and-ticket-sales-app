package com.example.imageservice.service;

import com.example.imageservice.dto.ImageUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
@Slf4j
public class ImageService {

    @Value("${upload.dir}")
    private String uploadDir;

    @Value("${upload.base-url}")
    private String baseUrl;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public ImageUploadResponse uploadImage(MultipartFile file) throws IOException {
        // Validar archivo
        if (file.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío");
        }

        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo excede el tamaño máximo permitido (10MB)");
        }

        // Validar tipo de archivo
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Nombre de archivo inválido");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Permitidos: " + ALLOWED_EXTENSIONS);
        }

        // Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Directorio de uploads creado: {}", uploadPath);
        }

        // Generar nombre único
        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;
        Path filePath = uploadPath.resolve(uniqueFileName);

        // Guardar archivo
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("Imagen guardada: {}", uniqueFileName);

        // Construir URL pública
        String fileUrl = baseUrl + "/uploads/" + uniqueFileName;

        return ImageUploadResponse.builder()
                .fileName(uniqueFileName)
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    public void deleteImage(String fileName) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("Imagen eliminada: {}", fileName);
        } else {
            throw new IllegalArgumentException("Archivo no encontrado: " + fileName);
        }
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
