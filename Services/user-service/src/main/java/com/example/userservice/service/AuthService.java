package com.example.userservice.service;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.UserDto;
import com.example.userservice.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        try {
            // Autenticar
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getContrasena()
                    )
            );

            // Buscar usuario
            User user = userService.findByEmail(request.getEmail());

            // Generar token con userId en los claims
            UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            String token = jwtService.generateToken(claims, userDetails);

            return AuthResponse.success(token, UserDto.fromEntity(user));
        } catch (Exception e) {
            return AuthResponse.error("Email o contraseña inválidos");
        }
    }
}
