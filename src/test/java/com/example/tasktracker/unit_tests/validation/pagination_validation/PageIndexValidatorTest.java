package com.example.tasktracker.unit_tests.validation.pagination_validation;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import com.example.tasktracker.validation.pagination_validation.PageIndexValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PageIndexValidatorTest {

    private ValidationExceptionFactory exceptionFactory;
    private PageIndexValidator validator;

    @BeforeEach
    void setUp() {
        exceptionFactory = mock(ValidationExceptionFactory.class);
        validator = new PageIndexValidator(exceptionFactory);
    }

    private static Stream<Object[]> pageIndexCases() {
        return Stream.of(
                new Object[]{0, false},
                new Object[]{1, false},
                new Object[]{10, false},
                new Object[]{Integer.MAX_VALUE, false},
                new Object[]{-1, true},
                new Object[]{Integer.MIN_VALUE, true}
        );
    }

    @ParameterizedTest
    @MethodSource("pageIndexCases")
    void shouldValidatePageIndex(
            int page,
            boolean shouldThrow
    ) {

        if (shouldThrow) {
            ValidationException exception = new ValidationException(
                    ErrorCode.INVALID_PAGE,
                    "Invalid page index"
            );

            when(exceptionFactory.create(ErrorCode.INVALID_PAGE)).thenReturn(exception);

            ValidationException thrown = assertThrows(
                    ValidationException.class,
                    () -> validator.validate(page, 10)
            );

            assertSame(exception, thrown);

            verify(exceptionFactory).create(ErrorCode.INVALID_PAGE);

        } else {
            assertDoesNotThrow(() -> validator.validate(page, 10));
            verifyNoInteractions(exceptionFactory);
        }
    }
}
