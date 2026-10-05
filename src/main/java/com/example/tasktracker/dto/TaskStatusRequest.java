package com.example.tasktracker.dto;

import com.example.tasktracker.enums.TaskStatus;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusRequest {

    @NotNull(message = "Status cannot be null")
    private TaskStatus status;
}
