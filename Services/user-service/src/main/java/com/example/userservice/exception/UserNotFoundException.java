package com.example.userservice.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long id) {
        super("Usuario no encontrado con ID: " + id);
    }
    
    public UserNotFoundException(String field, String value) {
        super("Usuario no encontrado con " + field + ": " + value);
    }
}
