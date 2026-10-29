package com.example.tasktracker.unit_tests.exception;

import com.example.tasktracker.dto.error.ErrorResponse;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.GlobalExceptionHandler;
import com.example.tasktracker.exception.custom_exceptions.AuthenticatedUserNotFoundException;
import com.example.tasktracker.exception.custom_exceptions.ForbiddenOperationException;
import com.example.tasktracker.exception.custom_exceptions.TaskNotFoundException;
import com.example.tasktracker.exception.custom_exceptions.UsernameAlreadyExistsException;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler(messageSource);
    }

    @Test
    void shouldReturnErrorCodeAndMessageFromValidationException() {
        ErrorCode errorCode = ErrorCode.SEARCH_TOO_LONG;
        String message = "Search must be at most 100 characters.";

        ValidationException exception =
                new ValidationException(errorCode, message);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationException(exception);

        assertErrorResponse(response, errorCode, message);

        verifyNoInteractions(messageSource);
    }

    @Test
    void shouldResolveMessageFromMessageSourceWhenValidationMessageIsBlank() {
        ErrorCode errorCode = ErrorCode.SEARCH_TOO_LONG;
        String message = "Search must be at most {0} characters.";

        ValidationException exception =
                new ValidationException(errorCode, " ");

        mockMessage(errorCode, message);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationException(exception);

        assertErrorResponse(response, errorCode, message);
    }

    @Test
    void shouldReturnFirstErrorForHandlerMethodValidationException() {
        HandlerMethodValidationException exception =
                mock(HandlerMethodValidationException.class);

        ObjectError firstError = new ObjectError(
                "request",
                "First validation error"
        );

        ObjectError secondError = new ObjectError(
                "request",
                "Second validation error"
        );

        doReturn(List.of(firstError, secondError)).when(exception).getAllErrors();

        ResponseEntity<ErrorResponse> response =
                handler.handleHandlerMethodValidation(exception);

        assertErrorResponse(
                response,
                ErrorCode.INVALID_REQUEST,
                "First validation error"
        );

        verifyNoInteractions(messageSource);
    }

    @Test
    void shouldResolveMessageKeyForHandlerMethodValidationException() {
        HandlerMethodValidationException exception =
                mock(HandlerMethodValidationException.class);

        ObjectError error = new ObjectError(
                "request",
                "{validation.task.title.not-blank}"
        );

        doReturn(List.of(error)).when(exception).getAllErrors();

        when(messageSource.getMessage(
                eq("validation.task.title.not-blank"),
                isNull(),
                eq("{validation.task.title.not-blank}"),
                any(Locale.class)
        )).thenReturn("Title must not be blank.");

        ResponseEntity<ErrorResponse> response =
                handler.handleHandlerMethodValidation(exception);

        assertErrorResponse(
                response,
                ErrorCode.INVALID_REQUEST,
                "Title must not be blank."
        );
    }

    @Test
    void shouldReturnFallbackWhenHandlerMethodValidationErrorsAreEmpty() {
        HandlerMethodValidationException exception =
                mock(HandlerMethodValidationException.class);

        when(exception.getAllErrors()).thenReturn(List.of());

        mockMessage(ErrorCode.INVALID_REQUEST, "Invalid request.");

        ResponseEntity<ErrorResponse> response =
                handler.handleHandlerMethodValidation(exception);

        assertErrorResponse(
                response,
                ErrorCode.INVALID_REQUEST,
                "Invalid request."
        );
    }

    @Test
    void shouldReturnFirstFieldErrorForMethodArgumentNotValidException() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(new FieldError(
                "request",
                "title",
                "First validation error"
        ));

        bindingResult.addError(new FieldError(
                "request",
                "description",
                "Second validation error"
        ));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValid(exception);

        assertErrorResponse(
                response,
                ErrorCode.INVALID_REQUEST,
                "First validation error"
        );

        verifyNoInteractions(messageSource);
    }

    @Test
    void shouldResolveMessageKeyForMethodArgumentNotValidException() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(new FieldError(
                "request",
                "title",
                "{validation.task.title.not-blank}"
        ));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        when(messageSource.getMessage(
                eq("validation.task.title.not-blank"),
                isNull(),
                eq("{validation.task.title.not-blank}"),
                any(Locale.class)
        )).thenReturn("Title must not be blank.");

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValid(exception);

        assertErrorResponse(
                response,
                ErrorCode.INVALID_REQUEST,
                "Title must not be blank."
        );
    }

    @Test
    void shouldReturnFallbackWhenMethodArgumentNotValidHasNoFieldErrors() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");

        bindingResult.addError(new ObjectError(
                "request",
                "Object-level error"
        ));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        mockMessage(ErrorCode.INVALID_REQUEST, "Invalid request.");

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodArgumentNotValid(exception);

        assertErrorResponse(
                response,
                ErrorCode.INVALID_REQUEST,
                "Invalid request."
        );
    }

    @Test
    void shouldReturnViolationMessageForConstraintViolationException() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation =
                mock(ConstraintViolation.class);

        when(violation.getMessage()).thenReturn("Invalid field value.");

        ConstraintViolationException exception =
                new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response =
                handler.handleConstraintViolation(exception);

        assertErrorResponse(
                response,
                ErrorCode.INVALID_REQUEST,
                "Invalid field value."
        );

        verifyNoInteractions(messageSource);
    }

    @Test
    void shouldResolveMessageKeyForConstraintViolationException() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation =
                mock(ConstraintViolation.class);

        when(violation.getMessage())
                .thenReturn("{validation.task.title.not-blank}");

        ConstraintViolationException exception =
                new ConstraintViolationException(Set.of(violation));

        when(messageSource.getMessage(
                eq("validation.task.title.not-blank"),
                isNull(),
                eq("{validation.task.title.not-blank}"),
                any(Locale.class)
        )).thenReturn("Title must not be blank.");

        ResponseEntity<ErrorResponse> response =
                handler.handleConstraintViolation(exception);

        assertErrorResponse(
                response,
                ErrorCode.INVALID_REQUEST,
                "Title must not be blank."
        );
    }

    @Test
    void shouldReturnFallbackWhenConstraintViolationsAreEmpty() {
        ConstraintViolationException exception =
                new ConstraintViolationException(Set.of());

        mockMessage(ErrorCode.INVALID_REQUEST, "Invalid request.");

        ResponseEntity<ErrorResponse> response =
                handler.handleConstraintViolation(exception);

        assertErrorResponse(
                response,
                ErrorCode.INVALID_REQUEST,
                "Invalid request."
        );
    }

    @Test
    void shouldReturnInvalidRequestBodyForUnreadableMessageException() {
        mockMessage(
                ErrorCode.INVALID_REQUEST_BODY,
                "Invalid request body."
        );

        ResponseEntity<ErrorResponse> response =
                handler.handleUnreadableMessage(
                        new HttpMessageNotReadableException("Malformed JSON")
                );

        assertErrorResponse(
                response,
                ErrorCode.INVALID_REQUEST_BODY,
                "Invalid request body."
        );
    }

    @Test
    void shouldReturnInvalidParameterForTypeMismatchException() {
        mockMessage(
                ErrorCode.INVALID_PARAMETER,
                "Invalid parameter."
        );

        MethodArgumentTypeMismatchException exception =
                new MethodArgumentTypeMismatchException(
                        "invalid",
                        Long.class,
                        "id",
                        null,
                        new IllegalArgumentException()
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleTypeMismatch(exception);

        assertErrorResponse(
                response,
                ErrorCode.INVALID_PARAMETER,
                "Invalid parameter."
        );
    }

    @Test
    void shouldReturnMissingParameterForMissingServletRequestParameterException() {
        mockMessage(
                ErrorCode.MISSING_PARAMETER,
                "Required parameter is missing."
        );

        MissingServletRequestParameterException exception =
                new MissingServletRequestParameterException(
                        "title",
                        "String"
                );

        ResponseEntity<ErrorResponse> response =
                handler.handleMissingParameter(exception);

        assertErrorResponse(
                response,
                ErrorCode.MISSING_PARAMETER,
                "Required parameter is missing."
        );
    }

    @Test
    void shouldReturnMethodNotAllowedForUnsupportedHttpMethodException() {
        mockMessage(
                ErrorCode.METHOD_NOT_ALLOWED,
                "HTTP method is not supported."
        );

        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("PATCH");

        ResponseEntity<ErrorResponse> response =
                handler.handleMethodNotSupported(exception);

        assertErrorResponse(
                response,
                ErrorCode.METHOD_NOT_ALLOWED,
                "HTTP method is not supported."
        );
    }

    @Test
    void shouldReturnNotFoundForTaskNotFoundException() {
        mockMessage(
                ErrorCode.TASK_NOT_FOUND,
                "Task with such id not found."
        );

        ResponseEntity<ErrorResponse> response =
                handler.handleTaskNotFoundException(
                        new TaskNotFoundException()
                );

        assertErrorResponse(
                response,
                ErrorCode.TASK_NOT_FOUND,
                "Task with such id not found."
        );
    }

    @Test
    void shouldReturnForbiddenForForbiddenOperationException() {
        mockMessage(
                ErrorCode.FORBIDDEN,
                "You do not have permission to perform this action."
        );

        ResponseEntity<ErrorResponse> response =
                handler.handleForbiddenOperation(
                        new ForbiddenOperationException()
                );

        assertErrorResponse(
                response,
                ErrorCode.FORBIDDEN,
                "You do not have permission to perform this action."
        );
    }

    @Test
    void shouldReturnConflictForUsernameAlreadyExistsException() {
        mockMessage(
                ErrorCode.USERNAME_ALREADY_EXISTS,
                "This username is already taken."
        );

        ResponseEntity<ErrorResponse> response =
                handler.handleUsernameAlreadyExists(
                        new UsernameAlreadyExistsException()
                );

        assertErrorResponse(
                response,
                ErrorCode.USERNAME_ALREADY_EXISTS,
                "This username is already taken."
        );
    }

    @Test
    void shouldReturnExpectedErrorForAuthenticatedUserNotFoundException() {
        mockMessage(
                ErrorCode.AUTHENTICATED_USER_NOT_FOUND,
                "Authenticated user was not found."
        );

        ResponseEntity<ErrorResponse> response =
                handler.handleAuthenticatedUserNotFound(
                        new AuthenticatedUserNotFoundException()
                );

        assertErrorResponse(
                response,
                ErrorCode.AUTHENTICATED_USER_NOT_FOUND,
                "Authenticated user was not found."
        );
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedException() {
        mockMessage(
                ErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred. Please try again later."
        );

        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpectedException(
                        new RuntimeException("Sensitive internal details")
                );

        assertErrorResponse(
                response,
                ErrorCode.INTERNAL_ERROR,
                "An unexpected error occurred. Please try again later."
        );

        assertFalse(
                Objects.requireNonNull(response.getBody()).message()
                        .contains("Sensitive internal details")
        );
    }

    private void mockMessage(ErrorCode errorCode, String message) {
        when(messageSource.getMessage(
                eq(errorCode.getMessageKey()),
                isNull(),
                eq(errorCode.getMessageKey()),
                any(Locale.class)
        )).thenReturn(message);
    }

    private void assertErrorResponse(
            ResponseEntity<ErrorResponse> response,
            ErrorCode expectedCode,
            String expectedMessage
    ) {
        assertEquals(expectedCode.getHttpStatus(), response.getStatusCode());
        assertNotNull(response.getBody());

        ErrorResponse body = response.getBody();

        assertEquals(expectedCode.name(), body.code());
        assertEquals(expectedMessage, body.message());
        assertEquals(expectedCode.getHttpStatus(), body.httpStatus());
        assertNotNull(body.timestamp());
    }
}