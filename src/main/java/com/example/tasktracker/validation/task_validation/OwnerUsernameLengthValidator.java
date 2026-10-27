package com.example.tasktracker.validation.task_validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.validation.TaskFilterValidationRule;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
@RequiredArgsConstructor
public class OwnerUsernameLengthValidator implements TaskFilterValidationRule {

    private static final int MAX_LENGTH = 50;

    private final ValidationExceptionFactory validationExceptionFactory;

    @Override
    public void validate(TaskFilterRequest request) {

        String username = request.getOwnerUsername();

        if (username != null && username.strip().length() > MAX_LENGTH) {

            throw validationExceptionFactory.create(
                    ErrorCode.OWNER_USERNAME_TOO_LONG,
                    MAX_LENGTH
            );
        }
    }
}
