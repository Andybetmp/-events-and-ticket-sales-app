package com.example.userservice.service;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.UserDto;
import com.example.userservice.exception.InvalidCredentialsException;
import com.example.userservice.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Servicio de Autenticación - Gestiona login y generación de tokens JWT.
 * 
 * RESPONSABILIDADES:
 * 1. Validar credenciales del usuario (email + contraseña)
 * 2. Generar token JWT con claims personalizados (userId)
 * 3. Retornar información del usuario autenticado
 * 
 * FLUJO DE LOGIN:
 * Cliente → Login request → Validar credenciales → Generar JWT → Respuesta
 * 
 * SEGURIDAD:
 * - Usa Spring Security AuthenticationManager para validar
 * - Contraseñas hasheadas con BCrypt
 * - Token incluye userId en claims para identificación posterior
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        try {
            // PASO 1: Autenticar credenciales con Spring Security
            // Spring Security validará la contraseña hasheada automáticamente
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getContrasena()
                    )
            );

            // PASO 2: Buscar información completa del usuario
            User user = userService.findByEmail(request.getEmail());

            // PASO 3: Generar token JWT con userId y rol en los claims
            // Los claims son datos adicionales embebidos en el token
            UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId()); // Claim personalizado para identificar usuario
            claims.put("nombre", user.getNombre());
            claims.put("apellido", user.getApellido());
            claims.put("rol", user.getRol().name()); // Claim para autorización basada en roles
            String token = jwtService.generateToken(claims, userDetails);

            // PASO 4: Retornar token + datos del usuario
            return AuthResponse.success(token, UserDto.fromEntity(user));
        } catch (Exception e) {
            // Cualquier error en autenticación lanza excepción personalizada por seguridad
            throw new InvalidCredentialsException();
        }
    }
}
