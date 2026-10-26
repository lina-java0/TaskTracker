package com.example.tasktracker.unit_tests.service;

import com.example.tasktracker.dto.task_history.TaskHistoryResponse;
import com.example.tasktracker.entities.task_history.TaskHistoryEntity;
import com.example.tasktracker.enums.TaskHistoryAction;
import com.example.tasktracker.repository.task_history.TaskHistoryRepository;
import com.example.tasktracker.service.task_history.TaskHistoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskHistoryServiceTest {

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @InjectMocks
    private TaskHistoryService taskHistoryService;

    @Test
    void shouldCreateAndSaveHistory() {

        taskHistoryService.addHistory(
                1L,
                TaskHistoryAction.STATUS_CHANGED,
                "Status changed",
                "TODO",
                "DONE"
        );

        ArgumentCaptor<TaskHistoryEntity> captor =
                ArgumentCaptor.forClass(TaskHistoryEntity.class);

        verify(taskHistoryRepository).save(captor.capture());

        TaskHistoryEntity history = captor.getValue();

        assertEquals(1L, history.getTaskId());
        assertEquals(
                TaskHistoryAction.STATUS_CHANGED,
                history.getAction()
        );
        assertEquals("Status changed", history.getDescription());
        assertEquals("TODO", history.getOldValue());
        assertEquals("DONE", history.getNewValue());
        assertNotNull(history.getCreatedAt());
    }

    @Test
    void shouldNotChangeHistoryWhenGettingIt() {

        TaskHistoryEntity history = createTaskHistory();

        when(taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(history));

        taskHistoryService.getHistory(1L);

        verify(taskHistoryRepository).findByTaskIdOrderByCreatedAtAsc(1L);
        verify(taskHistoryRepository, never()).save(any());
        verifyNoMoreInteractions(taskHistoryRepository);

        assertEquals("TODO", history.getOldValue());
        assertEquals("DONE", history.getNewValue());
        assertEquals("Status changed", history.getDescription());
    }

    @Test
    void shouldReturnMultipleHistoryEntriesInOrder() {

        TaskHistoryEntity createdHistory = createTaskHistory();
        TaskHistoryEntity updatedHistory = updateTaskHistory();

        when(taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(createdHistory, updatedHistory));

        List<TaskHistoryResponse> result = taskHistoryService.getHistory(1L);

        assertEquals(2, result.size());

        assertEquals(
                new TaskHistoryResponse(
                        10L,
                        1L,
                        TaskHistoryAction.STATUS_CHANGED,
                        "Status changed",
                        "TODO",
                        "DONE",
                        LocalDateTime.of(2026, 10, 6, 15, 0)
                ),
                result.get(0)
        );

        assertEquals(
                new TaskHistoryResponse(
                        11L,
                        1L,
                        TaskHistoryAction.PRIORITY_CHANGED,
                        "Priority changed",
                        "LOW",
                        "MEDIUM",
                        LocalDateTime.of(2026, 10, 6, 16, 0)
                ),
                result.get(1)
        );

        assertEquals(result.get(0).taskId(), result.get(1).taskId());
        assertNotEquals(result.get(0).id(), result.get(1).id());
    }

    private TaskHistoryEntity createTaskHistory() {

        return TaskHistoryEntity.builder()
                .id(10L)
                .taskId(1L)
                .action(TaskHistoryAction.STATUS_CHANGED)
                .description("Status changed")
                .oldValue("TODO")
                .newValue("DONE")
                .createdAt(LocalDateTime.of(2026, 10, 6, 15, 0))
                .build();
    }

    private TaskHistoryEntity updateTaskHistory() {

        return TaskHistoryEntity.builder()
                .id(11L)
                .taskId(1L)
                .action(TaskHistoryAction.PRIORITY_CHANGED)
                .description("Priority changed")
                .oldValue("LOW")
                .newValue("MEDIUM")
                .createdAt(LocalDateTime.of(2026, 10, 6, 16, 0))
                .build();
    }
}
