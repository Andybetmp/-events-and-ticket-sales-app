package com.example.userservice.service;

import com.example.userservice.exception.InvalidTokenException;
import com.example.userservice.exception.UserNotFoundException;
import com.example.userservice.model.PasswordResetToken;
import com.example.userservice.model.User;
import com.example.userservice.repository.PasswordResetTokenRepository;
import com.example.userservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {
    
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    
    @Value("${services.notification-service.url:http://localhost:8085}")
    private String notificationServiceUrl;
    
    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;
    
    @Transactional
    public void requestPasswordReset(String email) {
        // Por seguridad, no revelamos si el email existe o no (prevenir enumeración de usuarios)
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("Solicitud de reset para email no registrado: {}", email);
            return; // Responder exitosamente pero no hacer nada
        }
        
        // Eliminar tokens anteriores del usuario
        tokenRepository.deleteByUserId(user.getId());
        
        // Generar nuevo token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);
        
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(expiryDate);
        resetToken.setUsed(false);
        
        tokenRepository.save(resetToken);
        
        // Enviar email con el link de restablecimiento
        sendResetEmail(user.getEmail(), token, user.getNombre());
    }
    
    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token de restablecimiento inválido"));
        
        if (!resetToken.isValid()) {
            throw new InvalidTokenException("El token ha expirado o ya fue utilizado");
        }
        
        // Actualizar contraseña
        User user = resetToken.getUser();
        user.setContrasena(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Marcar token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
    
    public void validateToken(String token) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token de restablecimiento inválido"));
        
        if (!resetToken.isValid()) {
            throw new InvalidTokenException("El token ha expirado o ya fue utilizado");
        }
    }
    
    private void sendResetEmail(String email, String token, String nombre) {
        try {
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            
            Map<String, Object> emailRequest = new HashMap<>();
            emailRequest.put("tipo", "PASSWORD_RESET");
            emailRequest.put("destinatario", email);
            
            Map<String, String> datos = new HashMap<>();
            datos.put("nombre", nombre);
            datos.put("resetLink", resetLink);
            emailRequest.put("datos", datos);
            
            restTemplate.postForEntity(
                notificationServiceUrl + "/api/notifications/send",
                emailRequest,
                String.class
            );
            log.info("Email de restablecimiento enviado a: {}", email);
        } catch (Exception e) {
            log.error("Error al enviar email de restablecimiento a {}: {}", email, e.getMessage());
            // No lanzamos excepción para que el token se genere aunque falle el email
        }
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenRepository.deleteByExpiryDateBefore(now);
        log.info("Limpieza de tokens expirados ejecutada");
    }
}
