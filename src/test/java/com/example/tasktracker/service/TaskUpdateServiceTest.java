package com.example.tasktracker.service;

import com.example.tasktracker.dto.TaskUpdateRequest;
import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.enums.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskUpdateServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private TaskHistoryService taskHistoryService;

    @InjectMocks
    private TaskUpdateService taskUpdateService;


    @ParameterizedTest
    @EnumSource(Priority.class)
    void shouldUpdatePriorityAndAddHistory(Priority newPriority) {

        TaskEntity task = createTask();

        TaskUpdateRequest request = createRequestBuilder()
                .priority(newPriority)
                .build();

        when(taskService.getTaskById(1L))
                .thenReturn(task);

        taskUpdateService.updateTask(1L, request);

        assertEquals(newPriority, task.getPriority());

        if (newPriority != Priority.LOW) {
            verify(taskHistoryService).addHistory(
                    1L,
                    TaskHistoryAction.PRIORITY_CHANGED,
                    "Priority changed",
                    "LOW",
                    newPriority.name()
            );
        }
    }

    @ParameterizedTest
    @EnumSource(Category.class)
    void shouldUpdateCategoryAndAddHistory(Category newCategory) {

        TaskEntity task = createTask();

        TaskUpdateRequest request = createRequestBuilder()
                .category(newCategory)
                .build();

        when(taskService.getTaskById(1L))
                .thenReturn(task);

        taskUpdateService.updateTask(1L, request);

        assertEquals(newCategory, task.getCategory());

        if (newCategory != Category.WORK) {
            verify(taskHistoryService).addHistory(
                    1L,
                    TaskHistoryAction.CATEGORY_CHANGED,
                    "Category changed",
                    "WORK",
                    newCategory.name()
            );
        }
    }

    @Test
    void shouldUpdateDeadlineAndAddHistory() {
        TaskEntity task = createTask();

        LocalDate newDeadline = LocalDate.of(2026, 12, 1);

        TaskUpdateRequest request = createRequestBuilder()
                .deadline(newDeadline)
                .build();

        when(taskService.getTaskById(1L))
                .thenReturn(task);

        taskUpdateService.updateTask(1L, request);

        assertEquals(newDeadline, task.getDeadline());

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.DEADLINE_CHANGED,
                "Deadline changed",
                "2026-10-10",
                "2026-12-01"
        );
    }

    @Test
    void shouldAddUpdatedHistoryWhenTitleChanges() {

        TaskEntity task = createTask();

        TaskUpdateRequest request = createRequestBuilder()
                .title("New title")
                .build();

        when(taskService.getTaskById(1L))
                .thenReturn(task);

        taskUpdateService.updateTask(1L, request);

        assertEquals("New title", task.getTitle());

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.UPDATED,
                "Task updated",
                null,
                null
        );
    }

    @Test
    void shouldAddUpdatedHistoryWhenDescriptionChanges() {

        TaskEntity task = createTask();

        TaskUpdateRequest request = createRequestBuilder()
                .description("New description")
                .build();

        when(taskService.getTaskById(1L))
                .thenReturn(task);

        taskUpdateService.updateTask(1L, request);

        assertEquals("New description", task.getDescription());

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.UPDATED,
                "Task updated",
                null,
                null
        );
    }

    @Test
    void shouldNotAddHistoryWhenNothingChanged() {

        TaskEntity task = createTask();

        TaskUpdateRequest request = createRequestBuilder().build();

        when(taskService.getTaskById(1L))
                .thenReturn(task);

        taskUpdateService.updateTask(1L, request);

        verifyNoInteractions(taskHistoryService);
    }


    private TaskEntity createTask() {
        return TaskEntity.builder()
                .id(1L)
                .title("Old title")
                .description("Old description")
                .priority(Priority.LOW)
                .category(Category.WORK)
                .deadline(LocalDate.of(2026, 10, 10))
                .build();
    }


    private TaskUpdateRequest.TaskUpdateRequestBuilder createRequestBuilder() {
        return TaskUpdateRequest.builder()
                .title("Old title")
                .description("Old description")
                .priority(Priority.LOW)
                .category(Category.WORK)
                .deadline(LocalDate.of(2026, 10, 10));
    }
}