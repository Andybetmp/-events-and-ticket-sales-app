package com.example.userservice.service;

import com.example.userservice.dto.AuthResponse;
import com.example.userservice.dto.LoginRequest;
import com.example.userservice.dto.RegisterRequest;
import com.example.userservice.dto.UpdateUserRequest;
import com.example.userservice.dto.UserDto;
import com.example.userservice.model.User;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, 
                      @Lazy PasswordEncoder passwordEncoder,
                      JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getContrasena(),
                new ArrayList<>()
        );
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        try {
            // Validar que el email no exista
            if (userRepository.existsByEmail(request.getEmail())) {
                return AuthResponse.error("El email ya está registrado");
            }

            // Crear usuario
            User user = User.builder()
                    .email(request.getEmail())
                    .contrasena(passwordEncoder.encode(request.getContrasena()))
                    .nombre(request.getNombre())
                    .apellido(request.getApellido())
                    .telefono(request.getTelefono())
                    .rol(User.Rol.USUARIO)
                    .activo(true)
                    .build();

            user = userRepository.save(user);

            // Generar token con userId en los claims
            UserDetails userDetails = loadUserByUsername(user.getEmail());
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            String token = jwtService.generateToken(claims, userDetails);

            return AuthResponse.success(token, UserDto.fromEntity(user));
        } catch (Exception e) {
            return AuthResponse.error("Registro fallido: " + e.getMessage());
        }
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    public UserDto getUserByEmail(String email) {
        User user = findByEmail(email);
        return UserDto.fromEntity(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return UserDto.fromEntity(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));

        // Actualizar solo los campos que no son nulos
        if (request.getNombre() != null && !request.getNombre().isEmpty()) {
            user.setNombre(request.getNombre());
        }
        if (request.getApellido() != null && !request.getApellido().isEmpty()) {
            user.setApellido(request.getApellido());
        }
        if (request.getTelefono() != null && !request.getTelefono().isEmpty()) {
            user.setTelefono(request.getTelefono());
        }
        if (request.getContrasena() != null && !request.getContrasena().isEmpty()) {
            user.setContrasena(passwordEncoder.encode(request.getContrasena()));
        }

        user = userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        //Desactivar el usuario
        user.setActivo(false);
        userRepository.save(user);
    }
}
