package com.example.tasktracker.unit_tests.validation.task_validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import com.example.tasktracker.validation.task_validation.OwnerUsernameLengthValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OwnerUsernameLengthValidatorTest {

    private ValidationExceptionFactory exceptionFactory;
    private OwnerUsernameLengthValidator validator;

    @BeforeEach
    void setUp() {
        exceptionFactory = mock(ValidationExceptionFactory.class);
        validator = new OwnerUsernameLengthValidator(exceptionFactory);
    }

    private static Stream<Object[]> ownerUsernameLengthCases() {
        return Stream.of(
                new Object[]{0, false},
                new Object[]{49, false},
                new Object[]{50, false},
                new Object[]{51, true}
        );
    }

    @ParameterizedTest
    @MethodSource("ownerUsernameLengthCases")
    void shouldValidateOwnerUsernameLength(
            int length,
            boolean shouldThrow
    ) {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .ownerUsername("a".repeat(length))
                .build();

        if (shouldThrow) {
            ValidationException exception = new ValidationException(
                    ErrorCode.OWNER_USERNAME_TOO_LONG,
                    "Owner username is too long"
            );

            when(exceptionFactory.create(ErrorCode.OWNER_USERNAME_TOO_LONG, 50)).thenReturn(exception);

            ValidationException thrown = assertThrows(
                    ValidationException.class,
                    () -> validator.validate(request)
            );

            assertSame(exception, thrown);

            verify(exceptionFactory).create(ErrorCode.OWNER_USERNAME_TOO_LONG, 50);

        } else {
            assertDoesNotThrow(() -> validator.validate(request));
            verifyNoInteractions(exceptionFactory);
        }
    }

    @Test
    void shouldNotThrowWhenOwnerUsernameIsNull() {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .ownerUsername(null)
                .build();

        assertDoesNotThrow(() -> validator.validate(request));
        verifyNoInteractions(exceptionFactory);
    }
}
