package com.example.tasktracker.exception.custom_exceptions;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException() {
        super();
    }

    public UsernameAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
