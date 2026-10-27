package com.example.tasktracker.unit_tests.validation.task_validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import com.example.tasktracker.validation.task_validation.WithoutDeadlineConflictValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

public class WithoutDeadlineConflictValidatorTest {

    private ValidationExceptionFactory exceptionFactory;
    private WithoutDeadlineConflictValidator validator;

    @BeforeEach
    void setUp() {
        exceptionFactory = mock(ValidationExceptionFactory.class);
        validator = new WithoutDeadlineConflictValidator(exceptionFactory);
    }

    private static Stream<Object[]> withoutDeadlineConflictCases() {
        return Stream.of(
                // withoutDeadline = false, диапазон задан
                new Object[]{
                        false,
                        null,
                        LocalDate.of(2028, 10, 5),
                        LocalDate.of(2028, 10, 20),
                        false
                },

                // withoutDeadline = false, точная дата задана
                new Object[]{
                        false,
                        LocalDate.of(2028, 10, 10),
                        null,
                        null,
                        false
                },

                // withoutDeadline = true, даты не заданы
                new Object[]{
                        true,
                        null,
                        null,
                        null,
                        false
                },

                // withoutDeadline = true, задана точная дата
                new Object[]{
                        true,
                        LocalDate.of(2028, 10, 10),
                        null,
                        null,
                        true
                },

                // withoutDeadline = true, задан диапазон
                new Object[]{
                        true,
                        null,
                        LocalDate.of(2028, 10, 5),
                        LocalDate.of(2028, 10, 20),
                        true
                },

                // withoutDeadline = true, задана нижняя граница
                new Object[]{
                        true,
                        null,
                        LocalDate.of(2028, 10, 5),
                        null,
                        true
                },

                // withoutDeadline = true, задана верхняя граница
                new Object[]{
                        true,
                        null,
                        null,
                        LocalDate.of(2028, 10, 20),
                        true
                },

                // withoutDeadline = null, даты не заданы
                new Object[]{
                        null,
                        null,
                        null,
                        null,
                        false
                },

                // withoutDeadline = null, задан диапазон
                new Object[]{
                        null,
                        null,
                        LocalDate.of(2028, 10, 5),
                        LocalDate.of(2028, 10, 20),
                        false
                }
        );
    }

    @ParameterizedTest
    @MethodSource("withoutDeadlineConflictCases")
    void shouldValidateWithoutDeadlineConflict(
            Boolean withoutDeadline,
            LocalDate deadline,
            LocalDate deadlineAfter,
            LocalDate deadlineBefore,
            boolean shouldThrow
    ) {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .withoutDeadline(withoutDeadline)
                .deadline(deadline)
                .deadlineAfter(deadlineAfter)
                .deadlineBefore(deadlineBefore)
                .build();

        if (shouldThrow) {
            ValidationException exception = new ValidationException(
                    ErrorCode.WITHOUT_DEADLINE_CONFLICT,
                    "Without deadline conflict"
            );

            when(exceptionFactory.create(ErrorCode.WITHOUT_DEADLINE_CONFLICT)).thenReturn(exception);

            ValidationException thrown = assertThrows(
                    ValidationException.class,
                    () -> validator.validate(request)
            );

            assertSame(exception, thrown);

            verify(exceptionFactory).create(ErrorCode.WITHOUT_DEADLINE_CONFLICT);

        } else {
            assertDoesNotThrow(() -> validator.validate(request));
            verifyNoInteractions(exceptionFactory);
        }
    }
}
