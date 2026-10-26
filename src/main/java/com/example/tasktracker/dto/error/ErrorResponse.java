package com.example.tasktracker.dto.error;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

public record ErrorResponse (
        String code,
        String message,
        HttpStatus httpStatus,
        LocalDateTime timestamp
) {
}