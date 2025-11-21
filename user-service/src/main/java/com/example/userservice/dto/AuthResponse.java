package com.example.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private boolean exitoso;
    private String token;
    private UserDto usuario;
    private String error;
    
    public static AuthResponse success(String token, UserDto usuario) {
        return AuthResponse.builder()
                .exitoso(true)
                .token(token)
                .usuario(usuario)
                .build();
    }
    
    public static AuthResponse error(String error) {
        return AuthResponse.builder()
                .exitoso(false)
                .error(error)
                .build();
    }
}
