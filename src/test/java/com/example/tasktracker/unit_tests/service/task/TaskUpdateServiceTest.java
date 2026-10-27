package com.example.tasktracker.unit_tests.service.task;

import com.example.tasktracker.dto.task.request.TaskUpdateRequest;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import com.example.tasktracker.enums.TaskHistoryAction;
import com.example.tasktracker.service.task.TaskService;
import com.example.tasktracker.service.task.TaskUpdateService;
import com.example.tasktracker.service.task_history.TaskHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskUpdateServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private TaskHistoryService taskHistoryService;

    @InjectMocks
    private TaskUpdateService taskUpdateService;

    @Test
    void shouldUpdateTitleAndAddHistory() {
        TaskEntity task = createTask();
        TaskUpdateRequest request = createRequestBuilder()
                .title("New title")
                .build();

        mockAndUpdateTask(task, request);

        assertEquals("New title", task.getTitle());

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.TITLE_CHANGED,
                "Title changed",
                "Old title",
                "New title"
        );
    }

    @Test
    void shouldUpdateDescriptionAndAddHistory() {
        TaskEntity task = createTask();
        TaskUpdateRequest request = createRequestBuilder()
                .description("New description")
                .build();

        mockAndUpdateTask(task, request);

        assertEquals("New description", task.getDescription());

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.DESCRIPTION_CHANGED,
                "Description changed",
                "Old description",
                "New description"
        );
    }

    @ParameterizedTest
    @EnumSource(Priority.class)
    void shouldUpdatePriorityAndAddHistory(Priority newPriority) {
        TaskEntity task = createTask();
        TaskUpdateRequest request = createRequestBuilder()
                .priority(newPriority)
                .build();

        mockAndUpdateTask(task, request);

        assertEquals(newPriority, task.getPriority());

        if (newPriority != Priority.LOW) {
            verify(taskHistoryService).addHistory(
                    1L,
                    TaskHistoryAction.PRIORITY_CHANGED,
                    "Priority changed",
                    "LOW",
                    newPriority.name()
            );
        } else {
            verifyNoInteractions(taskHistoryService);
        }
    }

    @ParameterizedTest
    @EnumSource(Category.class)
    void shouldUpdateCategoryAndAddHistory(Category newCategory) {
        TaskEntity task = createTask();
        TaskUpdateRequest request = createRequestBuilder()
                .category(newCategory)
                .build();

        mockAndUpdateTask(task, request);

        assertEquals(newCategory, task.getCategory());

        if (newCategory != Category.WORK) {
            verify(taskHistoryService).addHistory(
                    1L,
                    TaskHistoryAction.CATEGORY_CHANGED,
                    "Category changed",
                    "WORK",
                    newCategory.name()
            );
        } else {
            verifyNoInteractions(taskHistoryService);
        }
    }

    @ParameterizedTest
    @MethodSource("deadlineCases")
    void shouldUpdateDeadlineAndAddHistory(LocalDate newDeadline) {
        TaskEntity task = createTask();
        TaskUpdateRequest request = createRequestBuilder()
                .deadline(newDeadline)
                .build();

        mockAndUpdateTask(task, request);

        assertEquals(newDeadline, task.getDeadline());

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.DEADLINE_CHANGED,
                "Deadline changed",
                "2026-10-10",
                newDeadline == null ? null : newDeadline.toString()
        );
    }

    private static Stream<LocalDate> deadlineCases() {
        return Stream.of(
                LocalDate.of(2026, 12, 1),
                null
        );
    }

    @Test
    void shouldAddHistoryForEveryChangedField() {
        TaskEntity task = createTask();

        TaskUpdateRequest request = createRequestBuilder()
                .title("New title")
                .description("New description")
                .priority(Priority.HIGH)
                .category(Category.STUDY)
                .deadline(LocalDate.of(2028, 11, 1))
                .build();

        when(taskService.getTaskById(1L)).thenReturn(task);

        TaskEntity result = taskUpdateService.updateTask(1L, request);

        assertSame(task, result);
        assertEquals("New title", result.getTitle());
        assertEquals("New description", result.getDescription());
        assertEquals(Priority.HIGH, result.getPriority());
        assertEquals(Category.STUDY, result.getCategory());
        assertEquals(LocalDate.of(2028, 11, 1), result.getDeadline());

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.TITLE_CHANGED,
                "Title changed",
                "Old title",
                "New title"
        );

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.DESCRIPTION_CHANGED,
                "Description changed",
                "Old description",
                "New description"
        );

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.PRIORITY_CHANGED,
                "Priority changed",
                "LOW",
                "HIGH"
        );

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.CATEGORY_CHANGED,
                "Category changed",
                "WORK",
                "STUDY"
        );

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.DEADLINE_CHANGED,
                "Deadline changed",
                "2026-10-10",
                "2028-11-01"
        );

        verifyNoMoreInteractions(taskHistoryService);
    }

    @Test
    void shouldNotAddHistoryWhenNothingChanged() {
        TaskEntity task = createTask();
        TaskUpdateRequest request = createRequestBuilder().build();

        mockAndUpdateTask(task, request);

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

    private void mockAndUpdateTask(TaskEntity task, TaskUpdateRequest request) {
        when(taskService.getTaskById(1L)).thenReturn(task);
        taskUpdateService.updateTask(1L, request);
    }
}