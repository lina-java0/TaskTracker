package com.example.tasktracker.validation.validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.validation.TaskFilterValidationRule;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class DeadlineFilterValidator implements TaskFilterValidationRule {

    private final ValidationExceptionFactory validationExceptionFactory;

    @Override
    public void validate(TaskFilterRequest request) {

        boolean hasExactDeadline = request.getDeadline() != null;

        boolean hasDeadlineRange =
                request.getDeadlineBefore() != null
                || request.getDeadlineAfter() != null;

        if (hasExactDeadline && hasDeadlineRange) {
            throw validationExceptionFactory.create(
                    ErrorCode.DEADLINE_FILTER_CONFLICT
            );
        }

        if (Boolean.TRUE.equals(request.getWithoutDeadline())
                && (hasExactDeadline || hasDeadlineRange)) {
            throw validationExceptionFactory.create(
                    ErrorCode.WITHOUT_DEADLINE_CONFLICT
            );
        }
    }
}
