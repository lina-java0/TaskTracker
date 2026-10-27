package com.example.tasktracker.unit_tests.validation.pagination_validation;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import com.example.tasktracker.validation.pagination_validation.PageSizeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PageSizeValidatorTest {

    private ValidationExceptionFactory exceptionFactory;
    private PageSizeValidator validator;

    @BeforeEach
    void setUp() {
        exceptionFactory = mock(ValidationExceptionFactory.class);
        validator = new PageSizeValidator(exceptionFactory);
    }

    private static Stream<Object[]> pageSizeCases() {
        return Stream.of(
                new Object[]{1, false},
                new Object[]{100, false},
                new Object[]{0, true},
                new Object[]{101, true},
                new Object[]{Integer.MAX_VALUE, true},
                new Object[]{Integer.MIN_VALUE, true}
        );
    }

    @ParameterizedTest
    @MethodSource("pageSizeCases")
    void shouldValidatePageSize(
            int size,
            boolean shouldThrow
    ) {

        if (shouldThrow) {
            ValidationException exception = new ValidationException(
                    ErrorCode.INVALID_PAGE_SIZE,
                    "Invalid page size"
            );

            when(exceptionFactory.create(ErrorCode.INVALID_PAGE_SIZE)).thenReturn(exception);

            ValidationException thrown = assertThrows(
                    ValidationException.class,
                    () -> validator.validate(0, size)
            );

            assertSame(exception, thrown);

            verify(exceptionFactory).create(ErrorCode.INVALID_PAGE_SIZE);

        } else {
            assertDoesNotThrow(() -> validator.validate(0, size));
            verifyNoInteractions(exceptionFactory);
        }
    }
}
