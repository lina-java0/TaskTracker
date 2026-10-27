package com.example.tasktracker.unit_tests.validation.task_validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import com.example.tasktracker.validation.task_validation.DeadlineFilterValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

public class DeadlineFilterValidatorTest {

    private ValidationExceptionFactory exceptionFactory;
    private DeadlineFilterValidator validator;

    @BeforeEach
    void setUp() {
        exceptionFactory = mock(ValidationExceptionFactory.class);
        validator = new DeadlineFilterValidator(exceptionFactory);
    }

    private static Stream<Object[]> deadlineFilterCases() {
        return Stream.of(
                // Нет точной даты — допустимый диапазон
                new Object[] {
                        null,
                        LocalDate.of(2028, 10, 5),
                        LocalDate.of(2028, 10, 20),
                        false
                },

                // Только точная дата
                new Object[] {
                        LocalDate.of(2028, 10, 10),
                        null,
                        null,
                        false
                },

                // Одинаковые границы диапазона допустимы
                new Object[]{
                        null,
                        LocalDate.of(2028, 10, 10),
                        LocalDate.of(2028, 10, 10),
                        false
                },

                // Только нижняя граница
                new Object[]{
                        null,
                        LocalDate.of(2028, 10, 5),
                        null,
                        false
                },

                // Только верхняя граница
                new Object[]{
                        null,
                        null,
                        LocalDate.of(2028, 10, 20),
                        false
                },

                // Точная дата вместе с диапазоном
                new Object[] {
                        LocalDate.of(2028, 10, 10),
                        LocalDate.of(2028, 10, 5),
                        LocalDate.of(2028, 10, 20),
                        true
                },

                // Точная дата вместе с нижней границей
                new Object[] {
                        LocalDate.of(2028, 10, 10),
                        null,
                        LocalDate.of(2028, 10, 20),
                        true
                },

                // Точная дата вместе с верхней границей
                new Object[] {
                        LocalDate.of(2028, 10, 10),
                        LocalDate.of(2028, 10, 5),
                        null,
                        true
                }
        );
    }

    @ParameterizedTest
    @MethodSource("deadlineFilterCases")
    void shouldValidateDeadlineRange(
            LocalDate deadline,
            LocalDate deadlineAfter,
            LocalDate deadlineBefore,
            boolean shouldThrow
    ) {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .deadline(deadline)
                .deadlineAfter(deadlineAfter)
                .deadlineBefore(deadlineBefore)
                .build();

        if (shouldThrow) {
            ValidationException exception = new ValidationException(
                    ErrorCode.DEADLINE_FILTER_CONFLICT,
                    "Deadline filter conflict"
            );

            when(exceptionFactory.create(ErrorCode.DEADLINE_FILTER_CONFLICT)).thenReturn(exception);

            ValidationException thrown = assertThrows(
                    ValidationException.class,
                    () -> validator.validate(request)
            );

            assertSame(exception, thrown);

            verify(exceptionFactory).create(ErrorCode.DEADLINE_FILTER_CONFLICT);

        } else {
            assertDoesNotThrow(() -> validator.validate(request));
            verifyNoInteractions(exceptionFactory);
        }
    }
}
