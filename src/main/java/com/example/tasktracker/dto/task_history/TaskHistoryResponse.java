package com.example.tasktracker.dto.task_history;

import com.example.tasktracker.enums.TaskHistoryAction;

import java.time.LocalDateTime;

public record TaskHistoryResponse(
        Long id,
        Long taskId,
        TaskHistoryAction action,
        String description,
        String oldValue,
        String newValue,
        LocalDateTime createdAt
) {
}