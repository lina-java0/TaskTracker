package com.example.tasktracker.validation.pagination;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.validation.PaginationValidationRule;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class PageIndexValidator implements PaginationValidationRule {

    private final ValidationExceptionFactory validationExceptionFactory;

    @Override
    public void validate(int page, int size) {
        if (page < 0) {
            throw validationExceptionFactory.create(
                    ErrorCode.INVALID_PAGE);
        }
    }
}
