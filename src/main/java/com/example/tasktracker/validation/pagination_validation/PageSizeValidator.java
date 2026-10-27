package com.example.tasktracker.validation.pagination_validation;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.validation.PaginationValidationRule;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class PageSizeValidator implements PaginationValidationRule {

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;

    private final ValidationExceptionFactory validationExceptionFactory;

    @Override
    public void validate(int page, int size) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw validationExceptionFactory.create(
                    ErrorCode.INVALID_PAGE_SIZE);
        }
    }
}
