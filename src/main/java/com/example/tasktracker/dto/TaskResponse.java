package com.example.tasktracker.dto;

import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import com.example.tasktracker.enums.TaskStatus;
import lombok.*;

import java.time.*;

@Getter
@Setter
@Builder
public class TaskResponse {
    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private Priority priority;

    private LocalDate deadline;

    private Category category;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
