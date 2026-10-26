package com.example.tasktracker.dto.task.request;

import com.example.tasktracker.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatusRequest {

    @NotNull(message = "{validation.task.status.not-null}")
    private TaskStatus status;
}
