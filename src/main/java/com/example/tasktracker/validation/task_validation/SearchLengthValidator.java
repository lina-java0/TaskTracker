package com.example.tasktracker.validation.task_validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.validation.TaskFilterValidationRule;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
@RequiredArgsConstructor
public class SearchLengthValidator implements TaskFilterValidationRule {

    private static final int MAX_LENGTH = 100;

    private final ValidationExceptionFactory validationExceptionFactory;

    @Override
    public void validate(TaskFilterRequest request) {

        String search = request.getSearch();

        if (search != null && search.strip().length() > MAX_LENGTH) {

            throw validationExceptionFactory.create(
                    ErrorCode.SEARCH_TOO_LONG,
                    MAX_LENGTH
            );
        }
    }
}
