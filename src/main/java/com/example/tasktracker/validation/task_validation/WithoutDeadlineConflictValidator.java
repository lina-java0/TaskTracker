package com.example.tasktracker.validation.task_validation;

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
public class WithoutDeadlineConflictValidator implements TaskFilterValidationRule {

    private final ValidationExceptionFactory validationExceptionFactory;

    @Override
    public void validate(TaskFilterRequest request) {

        boolean hasExactDeadline = request.getDeadline() != null;

        boolean hasDeadlineRange =
                request.getDeadlineBefore() != null
                        || request.getDeadlineAfter() != null;

        if (Boolean.TRUE.equals(request.getWithoutDeadline())
                && (hasExactDeadline || hasDeadlineRange)) {
            throw validationExceptionFactory.create(
                    ErrorCode.WITHOUT_DEADLINE_CONFLICT
            );
        }
    }
}
