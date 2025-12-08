package com.example.ticketservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterUserRequest {
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;
    
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 50, message = "La contraseña debe tener entre 6 y 50 caracteres")
    private String password;
}
