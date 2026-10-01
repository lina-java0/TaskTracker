package com.example.tasktracker.dto;

import com.example.tasktracker.enums.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class TaskFilterRequest {
    private Priority priority;

    private Category category;

    private TaskStatus status;

    private LocalDate deadline;

    private LocalDate deadlineAfter;

    private LocalDate deadlineBefore;

    private boolean withoutDeadline;

    private TaskSortType sortType;
}
