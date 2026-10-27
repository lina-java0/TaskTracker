package com.example.tasktracker.unit_tests.validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.validation.TaskFilterValidationRule;
import com.example.tasktracker.validation.TaskFilterValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TaskFilterValidatorTest {

    private TaskFilterValidationRule firstRule;
    private TaskFilterValidationRule secondRule;
    private TaskFilterValidator validator;

    @BeforeEach
    void setUp() {
        firstRule = mock(TaskFilterValidationRule.class);
        secondRule = mock(TaskFilterValidationRule.class);

        validator = new TaskFilterValidator(
                List.of(firstRule, secondRule)
        );
    }

    @Test
    void shouldPassSameRequestToAllRules() {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .ownerUsername("testuser")
                .build();

        validator.validate(request);

        verify(firstRule).validate(same(request));
        verify(secondRule).validate(same(request));
    }

    @Test
    void shouldStopValidationWhenFirstRuleThrowsException() {
        TaskFilterRequest request = TaskFilterRequest.builder().build();
        ValidationException exception = new ValidationException(
                ErrorCode.INVALID_REQUEST,
                "Invalid request"
        );

        doThrow(exception)
                .when(firstRule)
                .validate(request);

        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> validator.validate(request)
        );

        assertSame(exception, thrown);

        verify(firstRule).validate(request);
        verifyNoInteractions(secondRule);
    }

    @Test
    void shouldStopValidationWhenSecondRuleThrowsException() {
        TaskFilterRequest request = TaskFilterRequest.builder().build();
        ValidationException exception = new ValidationException(
                ErrorCode.INVALID_REQUEST,
                "Invalid request"
        );

        doThrow(exception)
                .when(secondRule)
                .validate(request);

        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> validator.validate(request)
        );

        assertSame(exception, thrown);

        verify(firstRule).validate(request);
        verify(secondRule).validate(request);
    }
}