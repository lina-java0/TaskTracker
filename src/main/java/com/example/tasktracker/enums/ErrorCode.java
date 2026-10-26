package com.example.tasktracker.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INVALID_REQUEST(
            "error.invalid-request",
            HttpStatus.BAD_REQUEST
    ),
    INVALID_REQUEST_BODY(
            "error.request.body.invalid",
            HttpStatus.BAD_REQUEST
    ),
    INVALID_PARAMETER(
            "error.request.parameter.invalid",
            HttpStatus.BAD_REQUEST
    ),
    MISSING_PARAMETER(
            "error.request.parameter.missing",
            HttpStatus.BAD_REQUEST
    ),
    METHOD_NOT_ALLOWED(
            "error.request.method-not-allowed",
            HttpStatus.METHOD_NOT_ALLOWED
    ),

    INVALID_PAGE(
            "error.pagination.page.invalid",
            HttpStatus.BAD_REQUEST
    ),
    INVALID_PAGE_SIZE(
            "error.pagination.size.invalid",
            HttpStatus.BAD_REQUEST
    ),

    SEARCH_TOO_LONG(
            "error.filter.search.max",
            HttpStatus.BAD_REQUEST
    ),
    OWNER_USERNAME_TOO_LONG(
            "error.filter.owner-username.max",
            HttpStatus.BAD_REQUEST
    ),
    DEADLINE_FILTER_CONFLICT(
            "error.filter.deadline.conflict",
            HttpStatus.BAD_REQUEST
    ),
    WITHOUT_DEADLINE_CONFLICT(
            "error.filter.without-deadline.conflict",
            HttpStatus.BAD_REQUEST
    ),
    INVALID_DEADLINE_RANGE(
            "error.filter.deadline-range.invalid",
            HttpStatus.BAD_REQUEST
    ),

    UNAUTHORIZED(
            "error.security.unauthorized",
            HttpStatus.UNAUTHORIZED
    ),
    FORBIDDEN(
            "error.security.forbidden",
            HttpStatus.FORBIDDEN
    ),
    TASK_NOT_FOUND(
            "error.task-not-found",
            HttpStatus.NOT_FOUND
    ),

    USERNAME_ALREADY_EXISTS(
            "error.user.username.already-exists",
            HttpStatus.CONFLICT
    ),

    INTERNAL_ERROR(
            "error.internal-error",
            HttpStatus.INTERNAL_SERVER_ERROR
    );

    private final String messageKey;
    private final HttpStatus httpStatus;
}
