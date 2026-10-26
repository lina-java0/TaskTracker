package com.example.tasktracker.dto.user.response;


public record UserResponse (
        Long id,
        String username,
        String role
) {
}
