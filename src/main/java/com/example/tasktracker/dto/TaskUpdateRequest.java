package com.example.tasktracker.dto;

import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TaskUpdateRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title must be at most 100 characters")
    private String title;

    @Size(max = 600, message = "Description must be at most 600 characters")
    private String description;

    @NotNull(message = "Priority cannot be null")
    private Priority priority;

    @FutureOrPresent(message = "Deadline cannot be in the past")
    private LocalDate deadline;

    @NotNull(message = "Category cannot be null")
    private Category category;
}
