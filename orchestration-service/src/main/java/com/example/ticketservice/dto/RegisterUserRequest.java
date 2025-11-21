package com.example.ticketservice.dto;

import lombok.Data;

@Data
public class RegisterUserRequest {
    private String nombre;
    private String email;
    private String password;
}
