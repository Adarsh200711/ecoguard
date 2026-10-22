package com.ecoguard.controller;

import com.ecoguard.dto.ApiResponseDto;
import com.ecoguard.dto.LoginRequestDto;
import com.ecoguard.dto.LoginResponseDto;
import com.ecoguard.dto.RegisterRequestDto;
import com.ecoguard.model.User;
import com.ecoguard.repository.UserRepository;
import com.ecoguard.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        LoginResponseDto response = authService.login(dto);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        LoginResponseDto response = authService.register(dto);
        if (response.isSuccess()) {
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponseDto<List<User>>> listUsers() {
        List<User> users = userRepository.findAll();
        // Hide passwords in output for security
        users.forEach(u -> u.setPassword("********"));
        return ResponseEntity.ok(ApiResponseDto.success("Users retrieved", users));
    }
}
