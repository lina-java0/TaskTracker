package com.example.tasktracker.unit_tests.validation;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.validation.PaginationValidationRule;
import com.example.tasktracker.validation.PaginationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaginationValidatorTest {

    private PaginationValidationRule firstRule;
    private PaginationValidationRule secondRule;
    private PaginationValidator validator;

    @BeforeEach
    void setUp() {
        firstRule = mock(PaginationValidationRule.class);
        secondRule = mock(PaginationValidationRule.class);

        validator = new PaginationValidator(
                List.of(firstRule, secondRule)
        );
    }

    @Test
    void shouldPassPaginationValuesToAllRules() {
        validator.validate(3, 50);

        verify(firstRule).validate(3, 50);
        verify(secondRule).validate(3, 50);
    }

    @Test
    void shouldStopValidationWhenFirstRuleThrowsException() {
        ValidationException exception = new ValidationException(
                ErrorCode.INVALID_PAGE_SIZE,
                "Invalid page size"
        );

        doThrow(exception)
                .when(firstRule)
                .validate(0, 20);

        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> validator.validate(0, 20)
        );

        assertSame(exception, thrown);

        verify(firstRule).validate(0, 20);
        verifyNoInteractions(secondRule);
    }

    @Test
    void shouldStopValidationWhenSecondRuleThrowsException() {
        ValidationException exception = new ValidationException(
                ErrorCode.INVALID_PAGE_SIZE,
                "Invalid page size"
        );

        doThrow(exception)
                .when(secondRule)
                .validate(0, 20);

        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> validator.validate(0, 20)
        );

        assertSame(exception, thrown);

        verify(firstRule).validate(0, 20);
        verify(secondRule).validate(0, 20);
    }
}