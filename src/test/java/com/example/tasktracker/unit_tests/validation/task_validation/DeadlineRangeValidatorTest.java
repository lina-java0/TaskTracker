package com.example.tasktracker.unit_tests.validation.task_validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import com.example.tasktracker.validation.task_validation.DeadlineRangeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DeadlineRangeValidatorTest {

    private ValidationExceptionFactory exceptionFactory;
    private DeadlineRangeValidator validator;

    @BeforeEach
    void setUp() {
        exceptionFactory = mock(ValidationExceptionFactory.class);
        validator = new DeadlineRangeValidator(exceptionFactory);
    }

    private static Stream<Object[]> deadlineRangeCases() {
        return Stream.of(
                new Object[]{
                        LocalDate.of(2028, 10, 10),
                        LocalDate.of(2028, 10, 20),
                        false
                },
                new Object[]{
                        LocalDate.of(2028, 10, 10),
                        LocalDate.of(2028, 10, 10),
                        false
                },
                new Object[]{
                        LocalDate.of(2028, 10, 20),
                        LocalDate.of(2028, 10, 10),
                        true
                },
                new Object[]{
                        LocalDate.of(2028, 10, 10),
                        null,
                        false
                },
                new Object[]{
                        null,
                        LocalDate.of(2028, 10, 20),
                        false
                },
                new Object[]{
                        null,
                        null,
                        false
                }
        );
    }

    @ParameterizedTest
    @MethodSource("deadlineRangeCases")
    void shouldValidateDeadlineRange(
            LocalDate deadlineAfter,
            LocalDate deadlineBefore,
            boolean shouldThrow
    ) {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .deadlineAfter(deadlineAfter)
                .deadlineBefore(deadlineBefore)
                .build();

        if (shouldThrow) {
            ValidationException exception = new ValidationException(
                    ErrorCode.INVALID_DEADLINE_RANGE,
                    "Invalid deadline range"
            );

            when(exceptionFactory.create(ErrorCode.INVALID_DEADLINE_RANGE)).thenReturn(exception);

            ValidationException thrown = assertThrows(
                    ValidationException.class,
                    () -> validator.validate(request)
            );

            assertSame(exception, thrown);

            verify(exceptionFactory).create(ErrorCode.INVALID_DEADLINE_RANGE);

        } else {
            assertDoesNotThrow(() -> validator.validate(request));
            verifyNoInteractions(exceptionFactory);
        }
    }
}
