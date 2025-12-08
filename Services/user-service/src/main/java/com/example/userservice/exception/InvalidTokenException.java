package com.example.userservice.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException() {
        super("Token de restablecimiento inválido o expirado");
    }
}
