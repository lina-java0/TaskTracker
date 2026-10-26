package com.example.tasktracker.exception.custom_exceptions;

import com.example.tasktracker.enums.ErrorCode;
import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private final ErrorCode errorCode;

    public ValidationException(
            ErrorCode errorCode,
            String message
    ) {
        super(message);
        this.errorCode = errorCode;
    }
}
