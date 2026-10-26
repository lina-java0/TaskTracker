package com.example.tasktracker.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaginationValidator {

    private final List<PaginationValidationRule> validationRules;

    public void validate(int page, int size) {

        for (PaginationValidationRule rule : validationRules) {
            rule.validate(page, size);
        }
    }
}
