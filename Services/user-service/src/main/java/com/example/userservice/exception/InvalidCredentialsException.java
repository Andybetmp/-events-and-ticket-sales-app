package com.example.userservice.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenciales inválidas");
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
