package com.example.ticketservice.exception;

public class ReservaExpiredException extends RuntimeException {
    public ReservaExpiredException(String message) {
        super(message);
    }
}
