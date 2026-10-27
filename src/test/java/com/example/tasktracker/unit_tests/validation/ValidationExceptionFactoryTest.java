package com.example.tasktracker.unit_tests.validation;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.validation.ValidationExceptionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class ValidationExceptionFactoryTest {

    private MessageSource messageSource;
    private ValidationExceptionFactory factory;

    @BeforeEach
    void setUp() {
        messageSource = mock(MessageSource.class);
        factory = new ValidationExceptionFactory(messageSource);

        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void shouldCreateExceptionWithErrorCodeAndMessage() {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        String messageKey = errorCode.getMessageKey();

        when(messageSource.getMessage(
                eq(messageKey),
                any(Object[].class),
                eq(messageKey),
                eq(Locale.ENGLISH)
        )).thenReturn("Invalid request");

        ValidationException exception = factory.create(errorCode);

        assertEquals(errorCode, exception.getErrorCode());
        assertEquals("Invalid request", exception.getMessage());

        verify(messageSource).getMessage(
                eq(messageKey),
                any(Object[].class),
                eq(messageKey),
                eq(Locale.ENGLISH)
        );
    }

    @Test
    void shouldPassArgumentsToMessageSource() {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        String messageKey = errorCode.getMessageKey();
        Object[] arguments = {"title", 100};

        when(messageSource.getMessage(
                eq(messageKey),
                same(arguments),
                eq(messageKey),
                eq(Locale.ENGLISH)
        )).thenReturn("Validation failed");

        ValidationException exception = factory.create(errorCode, arguments);

        assertEquals("Validation failed", exception.getMessage());

        verify(messageSource).getMessage(
                eq(messageKey),
                same(arguments),
                eq(messageKey),
                eq(Locale.ENGLISH)
        );
    }

    @Test
    void shouldUseMessageKeyAsDefaultMessage() {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        String messageKey = errorCode.getMessageKey();

        when(messageSource.getMessage(
                eq(messageKey),
                any(Object[].class),
                eq(messageKey),
                eq(Locale.ENGLISH)
        )).thenReturn(messageKey);

        ValidationException exception = factory.create(errorCode);

        assertEquals(messageKey, exception.getMessage());
    }

    @Test
    void shouldUseLocaleFromLocaleContextHolder() {
        Locale locale = Locale.forLanguageTag("ru");
        LocaleContextHolder.setLocale(locale);

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        String messageKey = errorCode.getMessageKey();

        when(messageSource.getMessage(
                eq(messageKey),
                any(Object[].class),
                eq(messageKey),
                eq(locale)
        )).thenReturn("Некорректный запрос");

        ValidationException exception = factory.create(errorCode);

        assertEquals("Некорректный запрос", exception.getMessage());

        verify(messageSource).getMessage(
                eq(messageKey),
                any(Object[].class),
                eq(messageKey),
                eq(locale)
        );
    }
}