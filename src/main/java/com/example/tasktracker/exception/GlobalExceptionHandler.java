package com.example.tasktracker.exception;

import com.example.tasktracker.dto.error.ErrorResponse;
import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.*;
import com.example.tasktracker.exception.custom_exceptions.AuthenticatedUserNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex
    ) {
        ErrorCode errorCode = ex.getErrorCode();

        return buildResponse(
                errorCode,
                resolveMessage(errorCode, ex.getMessage())
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
            HandlerMethodValidationException ex
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        String message = ex.getAllErrors()
                .stream()
                .findFirst()
                .map(error -> resolveValidationMessage(error.getDefaultMessage()))
                .orElseGet(() -> resolveMessage(errorCode, null));

        return buildResponse(
                errorCode,
                message
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .map(this::resolveValidationMessage)
                .orElseGet(() -> resolveMessage(errorCode, null));

        return buildResponse(
                errorCode,
                message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException  ex
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        String message = ex.getConstraintViolations()
                .stream()
                .findFirst()
                .map(ConstraintViolation::getMessage)
                .map(this::resolveValidationMessage)
                .orElseGet(() -> resolveMessage(errorCode, null));

        return buildResponse(
                errorCode,
                message
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException  ex
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST_BODY;

        return buildResponse(
                errorCode,
                resolveMessage(errorCode, null)
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException  ex
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_PARAMETER;

        return buildResponse(
                errorCode,
                resolveMessage(errorCode, null)
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException  ex
    ) {
        ErrorCode errorCode = ErrorCode.MISSING_PARAMETER;

        return buildResponse(
                errorCode,
                resolveMessage(errorCode, null)
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException  ex
    ) {
        ErrorCode errorCode = ErrorCode.METHOD_NOT_ALLOWED;

        return buildResponse(
                errorCode,
                resolveMessage(errorCode, null)
        );
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTaskNotFoundException(
            TaskNotFoundException ex
    ) {
        ErrorCode errorCode = ErrorCode.TASK_NOT_FOUND;

        return buildResponse(
                errorCode,
                resolveMessage(errorCode, null)
        );
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenOperation(
            ForbiddenOperationException ex
    ) {
        ErrorCode errorCode = ErrorCode.FORBIDDEN;

        return buildResponse(
                errorCode,
                resolveMessage(errorCode, null)
        );
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUsernameAlreadyExists(
            UsernameAlreadyExistsException ex
    ) {
        ErrorCode errorCode = ErrorCode.USERNAME_ALREADY_EXISTS;

        return buildResponse(
                errorCode,
                resolveMessage(errorCode, null)
        );
    }

    @ExceptionHandler(AuthenticatedUserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticatedUserNotFound(
            AuthenticatedUserNotFoundException ex
    ) {
        ErrorCode errorCode = ErrorCode.AUTHENTICATED_USER_NOT_FOUND;

        return buildResponse(
                errorCode,
                resolveMessage(errorCode, null)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception ex
    ) {
        log.error("Unexpected exception occurred", ex);

        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        return buildResponse(
                errorCode,
                resolveMessage(errorCode, null)
        );
    }

    private String resolveMessage(
            ErrorCode errorCode,
            String fallbackMessage
    ) {
        if (fallbackMessage != null && !fallbackMessage.isBlank()) {
            return fallbackMessage;
        }

        return messageSource.getMessage(
                errorCode.getMessageKey(),
                null,
                errorCode.getMessageKey(),
                LocaleContextHolder.getLocale()
        );
    }

    private String resolveValidationMessage(
            String message
    ) {
        if (message == null || message.isBlank()) {
            return resolveMessage(ErrorCode.INVALID_REQUEST, null);
        }

        if (message.startsWith("{")
                && message.endsWith("}")
                && message.length() > 2) {

            String key = message.substring(1, message.length() - 1);

            return messageSource.getMessage(
                    key,
                    null,
                    message,
                    LocaleContextHolder.getLocale()
            );
        }

        return message;
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            ErrorCode errorCode,
            String message
    ) {
        ErrorResponse response = new ErrorResponse(
                errorCode.name(),
                message,
                errorCode.getHttpStatus(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }
}