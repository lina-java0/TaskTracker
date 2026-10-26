package com.example.tasktracker.dto.task.request;

import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "{validation.task.title.not-blank}")
    @Size(
            max = 100,
            message = "{validation.task.title.max}")
    private String title;

    @Size(
            max = 600,
            message = "{validation.task.description.max}")
    private String description;

    @NotNull(message = "{validation.task.priority.not-null}")
    private Priority priority;

    @FutureOrPresent(message = "{validation.task.deadline.not-in-the-past}")
    private LocalDate deadline;

    @NotNull(message = "{validation.task.category.not-null}")
    private Category category;
}
