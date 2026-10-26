package com.example.tasktracker.validation;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ValidationExceptionFactory {

    private final MessageSource messageSource;

    public ValidationException create(
            ErrorCode errorCode,
            Object... arguments
    ) {
        String message = messageSource.getMessage(
                errorCode.getMessageKey(),
                arguments,
                errorCode.getMessageKey(),
                LocaleContextHolder.getLocale()
        );

        return new ValidationException(errorCode, message);
    }
}
