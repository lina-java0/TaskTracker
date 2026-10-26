package com.example.tasktracker.validation.validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.validation.TaskFilterValidationRule;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class DeadlineRangeValidator implements TaskFilterValidationRule  {

    private final ValidationExceptionFactory validationExceptionFactory;

    @Override
    public void validate(TaskFilterRequest request) {

        if (request.getDeadlineBefore() != null
                && request.getDeadlineAfter() != null
                && request.getDeadlineAfter().isAfter(request.getDeadlineBefore())) {

            throw validationExceptionFactory.create(
                    ErrorCode.INVALID_DEADLINE_RANGE
            );
        }
    }
}
