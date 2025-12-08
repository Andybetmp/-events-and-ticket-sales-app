package com.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud para actualizar información del usuario")
public class UpdateUserRequest {

    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Schema(description = "Nombre del usuario", example = "Juan")
    private String nombre;

    @Size(min = 2, max = 50, message = "El apellido debe tener entre 2 y 50 caracteres")
    @Schema(description = "Apellido del usuario", example = "Pérez")
    private String apellido;

    @Pattern(regexp = "^[0-9]{9,15}$", message = "El teléfono debe contener entre 9 y 15 dígitos")
    @Schema(description = "Número de teléfono", example = "987654321")
    private String telefono;

    @Schema(description = "Contraseña actual (requerida para cambiar contraseña)", example = "Password123")
    private String contrasenaActual;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Schema(description = "Nueva contraseña (opcional)", example = "NuevaPassword123")
    private String contrasena;
}
