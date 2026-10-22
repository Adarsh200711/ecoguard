package com.ecoguard.controller;

import com.ecoguard.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(
                new ApiResponseDto<>(false, "Validation failed: Please verify required fields", errors),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return new ResponseEntity<>(
                ApiResponseDto.error("Photo exceeds the maximum allowed upload size (10 MB)."),
                HttpStatus.PAYLOAD_TOO_LARGE
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return new ResponseEntity<>(
                ApiResponseDto.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleGenericException(Exception ex) {
        String msg = ex.getMessage();
        // Detect database connectivity issues
        if (msg != null && (msg.contains("CommunicationsException") || msg.contains("Connection refused") || msg.contains("CannotCreateTransactionException"))) {
            return new ResponseEntity<>(
                    ApiResponseDto.error("Database connection unavailable. Please verify MySQL service is running and configured correctly."),
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }

        return new ResponseEntity<>(
                ApiResponseDto.error("An error occurred: " + (msg != null ? msg : "Internal server error")),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
