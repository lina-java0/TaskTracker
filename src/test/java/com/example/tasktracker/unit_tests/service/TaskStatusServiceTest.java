package com.example.tasktracker.unit_tests.service;

import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.service.task.TaskService;
import com.example.tasktracker.service.task.TaskStatusService;
import com.example.tasktracker.service.task_history.TaskHistoryService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskStatusServiceTest {

    @Mock
    private TaskService taskService;

    @Mock
    private TaskHistoryService taskHistoryService;

    @InjectMocks
    private TaskStatusService taskStatusService;

    @ParameterizedTest
    @EnumSource(
            value = TaskStatus.class,
            names = {"IN_PROGRESS", "DONE"}
    )
        void shouldChangeStatusAndAddHistory(TaskStatus newStatus) {

            TaskEntity task = createTask(TaskStatus.TODO);

            when(taskService.getTaskById(1L)).thenReturn(task);

            TaskEntity result = taskStatusService.changeStatus(1L, newStatus);

            assertEquals(newStatus, result.getStatus());

            verify(taskHistoryService).addHistory(
                    1L,
                    TaskHistoryAction.STATUS_CHANGED,
                    "Status changed",
                    "TODO",
                    newStatus.name()
            );
        }

    @ParameterizedTest
    @EnumSource(TaskStatus.class)
    void shouldNotAddHistoryWhenStatusIsTheSame(TaskStatus status) {

        TaskEntity task = createTask(status);

        when(taskService.getTaskById(1L)).thenReturn(task);

        TaskEntity result = taskStatusService.changeStatus(1L, status);

        assertEquals(status, result.getStatus());

        verify(taskHistoryService, never()).addHistory(
                anyLong(),
                any(),
                anyString(),
                any(),
                any()
        );
    }

    private TaskEntity createTask(TaskStatus status) {
        return TaskEntity.builder()
                .id(1L)
                .status(status)
                .build();
    }
}
