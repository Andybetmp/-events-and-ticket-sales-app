package com.example.userservice.dto;

import com.example.userservice.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private Long id;
    private String email;
    private String nombre;
    private String apellido;
    private String telefono;
    private String rol;
    private LocalDateTime fechaCreacion;
    
    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nombre(user.getNombre())
                .apellido(user.getApellido())
                .telefono(user.getTelefono())
                .rol(user.getRol().name())
                .fechaCreacion(user.getFechaCreacion())
                .build();
    }
}
