package com.example.tasktracker.validation;

import com.example.tasktracker.dto.TaskFilterRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskFilterValidatorTest {

    private final TaskFilterValidator validator = new TaskFilterValidator();

    @Test
    void shouldNotThrowWhenWithoutDeadlineHasNoDeadlineFilters() {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .withoutDeadline(true)
                .build();

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void shouldThrowWhenWithoutDeadlineCombinedWithDeadline() {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .withoutDeadline(true)
                .deadline(LocalDate.now())
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request)
        );
    }

    @Test
    void shouldThrowWhenWithoutDeadlineCombinedWithDeadlineAfter() {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .withoutDeadline(true)
                .deadlineAfter(LocalDate.now())
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request)
        );
    }

    @Test
    void shouldThrowWhenWithoutDeadlineCombinedWithDeadlineBefore() {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .withoutDeadline(true)
                .deadlineBefore(LocalDate.now())
                .build();

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(request)
        );
    }

    @Test
    void shouldNotThrowWhenDeadlineFilterUsedWithoutWithoutDeadline() {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .deadline(LocalDate.now())
                .build();

        assertDoesNotThrow(() -> validator.validate(request));
    }
}