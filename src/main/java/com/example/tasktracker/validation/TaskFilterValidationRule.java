package com.example.tasktracker.validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;

public interface TaskFilterValidationRule {

    void validate(TaskFilterRequest request);
}
