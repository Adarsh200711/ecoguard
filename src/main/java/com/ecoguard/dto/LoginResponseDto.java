package com.ecoguard.dto;

public class LoginResponseDto {

    private boolean success;
    private String message;
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String token;

    public LoginResponseDto() {
    }

    public LoginResponseDto(boolean success, String message, Long userId, String name, String email, String role, String token) {
        this.success = success;
        this.message = message;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
    }

    public static LoginResponseDto failure(String message) {
        return new LoginResponseDto(false, message, null, null, null, null, null);
    }

    public static LoginResponseDto success(String message, Long userId, String name, String email, String role, String token) {
        return new LoginResponseDto(true, message, userId, name, email, role, token);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
