package com.example.imageservice.controller;

import com.example.imageservice.dto.ImageUploadResponse;
import com.example.imageservice.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Imágenes", description = "API para gestión de imágenes")
public class ImageController {

    private final ImageService imageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Subir imagen", description = "Sube una imagen al servidor y retorna la URL pública")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            log.info("📤 Recibiendo imagen: {} ({} bytes)", file.getOriginalFilename(), file.getSize());
            ImageUploadResponse response = imageService.uploadImage(file);
            log.info("✅ Imagen subida exitosamente: {}", response.getFileUrl());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Error al subir imagen", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al subir la imagen: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{fileName}")
    @Operation(summary = "Eliminar imagen", description = "Elimina una imagen del servidor")
    public ResponseEntity<?> deleteImage(@PathVariable String fileName) {
        try {
            log.info("🗑️ Eliminando imagen: {}", fileName);
            imageService.deleteImage(fileName);
            return ResponseEntity.ok(Map.of("message", "Imagen eliminada exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Error al eliminar imagen", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al eliminar la imagen"));
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica el estado del servicio")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "image-service"
        ));
    }
}
