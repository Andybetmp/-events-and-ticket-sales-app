package com.example.userservice.controller;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.ForgotPasswordRequest;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.ResetPasswordRequest;
import com.example.userservice.dto.UpdateUserRequest;
import com.example.userservice.dto.UserDto;
import com.example.userservice.service.AuthService;
import com.example.userservice.service.PasswordResetService;
import com.example.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Gestión de Usuarios", description = "Endpoints de autenticación y gestión de usuarios")
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verificar si el servicio está funcionando")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of("exitoso", true, "servicio", "user-service", "estado", "ACTIVO"));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario", description = "Crear una nueva cuenta de usuario")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        if (!response.isExitoso()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autenticar usuario y obtener token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        if (!response.isExitoso()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión", 
            description = "Cerrar sesión del usuario (invalida el token del lado del cliente)",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, Object>> logout(Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "unknown";
        return ResponseEntity.ok(Map.of(
                "exitoso", true,
                "mensaje", "Sesión cerrada correctamente",
                "usuario", email,
                "nota", "El token debe ser eliminado del cliente"
        ));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Obtener usuario actual", 
            description = "Obtener perfil del usuario autenticado",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserDto> getCurrentUser(Authentication authentication) {
        UserDto user = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos los usuarios",
            description = "Obtener lista de todos los usuarios registrados (requiere autenticación)",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Obtener información de un usuario específico por su ID (requiere autenticación)",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Obtener usuario por email",
            description = "Obtener información de un usuario específico por su email"
    )
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar usuario",
            description = "Actualizar información de un usuario (requiere autenticación)",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDto updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar usuario",
            description = "Eliminar (desactivar) un usuario del sistema (requiere autenticación)",
            security = @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of(
                "exitoso", true,
                "mensaje", "Usuario eliminado correctamente",
                "id", id
        ));
    }

    @PostMapping("/forgot-password")
    @Operation(
            summary = "Solicitar restablecimiento de contraseña",
            description = "Envía un email con un link para restablecer la contraseña"
    )
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok(Map.of(
                    "exitoso", true,
                    "mensaje", "Se ha enviado un correo electrónico con instrucciones para restablecer tu contraseña"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "exitoso", false,
                    "mensaje", e.getMessage()
            ));
        }
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Restablecer contraseña",
            description = "Restablecer la contraseña usando el token recibido por email"
    )
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of(
                    "exitoso", true,
                    "mensaje", "Tu contraseña ha sido restablecida exitosamente"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "exitoso", false,
                    "mensaje", e.getMessage()
            ));
        }
    }

    @GetMapping("/validate-reset-token/{token}")
    @Operation(
            summary = "Validar token de restablecimiento",
            description = "Verificar si un token de restablecimiento es válido"
    )
    public ResponseEntity<Map<String, Object>> validateResetToken(@PathVariable String token) {
        try {
            passwordResetService.validateToken(token);
            return ResponseEntity.ok(Map.of(
                    "exitoso", true,
                    "mensaje", "Token válido"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "exitoso", false,
                    "mensaje", e.getMessage()
            ));
        }
    }
}
