package com.example.tasktracker.dto.task.response;


public record ApiResponse<T>(
        String message,
        T data
) {
}
