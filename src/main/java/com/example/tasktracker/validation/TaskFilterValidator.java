package com.example.tasktracker.validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TaskFilterValidator {

    private final List<TaskFilterValidationRule> validationRules;

    public void validate(TaskFilterRequest request) {

        for (TaskFilterValidationRule rule : validationRules) {
            rule.validate(request);
        }
    }
}
