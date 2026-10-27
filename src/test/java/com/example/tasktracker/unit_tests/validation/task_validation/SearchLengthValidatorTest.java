package com.example.tasktracker.unit_tests.validation.task_validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import com.example.tasktracker.validation.task_validation.SearchLengthValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SearchLengthValidatorTest {

    private ValidationExceptionFactory exceptionFactory;
    private SearchLengthValidator validator;

    @BeforeEach
    void setUp() {
        exceptionFactory = mock(ValidationExceptionFactory.class);
        validator = new SearchLengthValidator(exceptionFactory);
    }

    private static Stream<Object[]> searchLengthCases() {
        return Stream.of(
                new Object[]{0, false},
                new Object[]{99, false},
                new Object[]{100, false},
                new Object[]{101, true}
        );
    }

    @ParameterizedTest
    @MethodSource("searchLengthCases")
    void shouldValidateSearchLength(
            int length,
            boolean shouldThrow
    ) {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .search("a".repeat(length))
                .build();

        if (shouldThrow) {
            ValidationException exception = new ValidationException(
                    ErrorCode.SEARCH_TOO_LONG,
                    "Search is too long"
            );

            when(exceptionFactory.create(ErrorCode.SEARCH_TOO_LONG, 100)).thenReturn(exception);

            ValidationException thrown = assertThrows(
                    ValidationException.class,
                    () -> validator.validate(request)
            );

            assertSame(exception, thrown);

            verify(exceptionFactory).create(ErrorCode.SEARCH_TOO_LONG, 100);

        } else {
            assertDoesNotThrow(() -> validator.validate(request));
            verifyNoInteractions(exceptionFactory);
        }
    }

    @Test
    void shouldNotThrowWhenSearchIsNull() {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .search(null)
                .build();

        assertDoesNotThrow(() -> validator.validate(request));
        verifyNoInteractions(exceptionFactory);
    }
}
