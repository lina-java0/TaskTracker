package com.example.tasktracker.dto.task.response;

import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import com.example.tasktracker.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse (
        Long id,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        LocalDate deadline,
        Category category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String ownerUsername
) {
}
