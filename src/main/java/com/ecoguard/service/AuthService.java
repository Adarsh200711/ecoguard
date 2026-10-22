package com.ecoguard.service;

import com.ecoguard.dto.LoginRequestDto;
import com.ecoguard.dto.LoginResponseDto;
import com.ecoguard.dto.RegisterRequestDto;
import com.ecoguard.model.User;
import com.ecoguard.repository.UserRepository;
import com.ecoguard.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponseDto login(LoginRequestDto dto) {
        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail().trim().toLowerCase());
        if (userOpt.isEmpty()) {
            return LoginResponseDto.failure("Invalid email or password.");
        }

        User user = userOpt.get();
        // Cryptographically secure password verification (PBKDF2 with salt)
        if (!SecurityUtils.verifyPassword(dto.getPassword(), user.getPassword())) {
            return LoginResponseDto.failure("Invalid email or password.");
        }

        String token = "ECO-" + UUID.randomUUID().toString().substring(0, 18);
        return LoginResponseDto.success("Authentication successful", user.getId(), user.getName(), user.getEmail(), user.getRole(), token);
    }

    public LoginResponseDto register(RegisterRequestDto dto) {
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            return LoginResponseDto.failure("An account with this email already exists.");
        }

        String role = dto.getRole() != null && !dto.getRole().isBlank() ? dto.getRole().toUpperCase() : "CITIZEN";
        if (!role.equals("ADMIN") && !role.equals("VOLUNTEER") && !role.equals("CITIZEN")) {
            role = "CITIZEN";
        }

        // Hash password before saving to database
        String hashedPassword = SecurityUtils.hashPassword(dto.getPassword());
        User user = new User(dto.getName().trim(), email, hashedPassword, role);
        User saved = userRepository.save(user);

        String token = "ECO-" + UUID.randomUUID().toString().substring(0, 18);
        return LoginResponseDto.success("User registered successfully", saved.getId(), saved.getName(), saved.getEmail(), saved.getRole(), token);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}
