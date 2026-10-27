package com.example.tasktracker.unit_tests.validation.task_validation;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.enums.TaskSortType;
import com.example.tasktracker.exception.custom_exceptions.ForbiddenOperationException;

import com.example.tasktracker.validation.task_validation.TaskAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TaskAccessValidatorTest {

    private TaskAccessValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TaskAccessValidator();
    }

    private static Stream<Object[]> accessCases() {
        return Stream.of(
                // ADMIN может использовать фильтр владельца
                new Object[]{
                        true,
                        "testuser",
                        null,
                        false
                },

                // ADMIN может сортировать по владельцу ASC
                new Object[]{
                        true,
                        null,
                        TaskSortType.OWNER_USERNAME_ASC,
                        false
                },

                // ADMIN может сортировать по владельцу DESC
                new Object[]{
                        true,
                        null,
                        TaskSortType.OWNER_USERNAME_DESC,
                        false
                },

                // USER без фильтра владельца
                new Object[]{
                        false,
                        null,
                        null,
                        false
                },

                // USER с пустым фильтром владельца
                new Object[]{
                        false,
                        "",
                        null,
                        false
                },

                // USER с фильтром из пробелов
                new Object[]{
                        false,
                        "   ",
                        null,
                        false
                },

                // USER с фильтром владельца
                new Object[]{
                        false,
                        "testuser",
                        null,
                        true
                },

                // USER сортирует по владельцу ASC
                new Object[]{
                        false,
                        null,
                        TaskSortType.OWNER_USERNAME_ASC,
                        true
                },

                // USER сортирует по владельцу DESC
                new Object[]{
                        false,
                        null,
                        TaskSortType.OWNER_USERNAME_DESC,
                        true
                },

                // USER сортирует по другому полю
                new Object[]{
                        false,
                        null,
                        TaskSortType.CREATED_AT_DESC,
                        false
                }
        );
    }

    @ParameterizedTest
    @MethodSource("accessCases")
    void shouldValidateAdminFilters(
            boolean isAdmin,
            String ownerUsername,
            TaskSortType sortType,
            boolean shouldThrow
    ) {
        TaskFilterRequest request = TaskFilterRequest.builder()
                .ownerUsername(ownerUsername)
                .sortType(sortType)
                .build();

        if (shouldThrow) {
            assertThrows(
                    ForbiddenOperationException.class,
                    () -> validator.validateAdminFilters(request, isAdmin)
            );
        } else {
            assertDoesNotThrow(
                    () -> validator.validateAdminFilters(request, isAdmin)
            );
        }
    }
}