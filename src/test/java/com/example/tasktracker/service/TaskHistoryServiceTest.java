package com.example.tasktracker.service;

import com.example.tasktracker.entities.TaskHistoryEntity;
import com.example.tasktracker.enums.TaskHistoryAction;
import com.example.tasktracker.repository.TaskHistoryRepository;
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
    void shouldGetHistoryAndConvertToResponse() {

        LocalDateTime createdAt = LocalDateTime.of(2026, 10, 6, 15, 0);

        TaskHistoryEntity history = TaskHistoryEntity.builder()
                .id(10L)
                .taskId(1L)
                .action(TaskHistoryAction.STATUS_CHANGED)
                .description("Status changed")
                .oldValue("TODO")
                .newValue("DONE")
                .createdAt(createdAt)
                .build();

        when(taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(history));

        var result = taskHistoryService.getHistory(1L);

        assertEquals(1, result.size());

        var response = result.getFirst();

        assertEquals(10L, response.id());
        assertEquals(1L, response.taskId());
        assertEquals(
                TaskHistoryAction.STATUS_CHANGED,
                response.action()
        );
        assertEquals("Status changed", response.description());
        assertEquals("TODO", response.oldValue());
        assertEquals("DONE", response.newValue());
        assertEquals(createdAt, response.createdAt());
    }
}
