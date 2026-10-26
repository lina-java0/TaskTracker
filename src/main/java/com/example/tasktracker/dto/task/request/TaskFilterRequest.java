package com.example.tasktracker.dto.task.request;

import com.example.tasktracker.enums.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskFilterRequest {

    private String search;

    private Priority priority;

    private Category category;

    private TaskStatus status;

    private LocalDate deadline;

    private LocalDate deadlineAfter;

    private LocalDate deadlineBefore;

    private Boolean withoutDeadline;

    private TaskSortType sortType;

    private String ownerUsername;
}
