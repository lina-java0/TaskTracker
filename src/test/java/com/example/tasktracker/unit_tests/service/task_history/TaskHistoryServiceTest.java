package com.example.tasktracker.unit_tests.service.task_history;

import com.example.tasktracker.dto.task_history.TaskHistoryResponse;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.entities.task_history.TaskHistoryEntity;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.enums.TaskHistoryAction;
import com.example.tasktracker.exception.custom_exceptions.TaskNotFoundException;
import com.example.tasktracker.repository.task.TaskRepository;
import com.example.tasktracker.repository.task_history.TaskHistoryRepository;
import com.example.tasktracker.service.task_history.TaskHistoryService;
import com.example.tasktracker.service.user.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskHistoryServiceTest {

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CurrentUserService currentUserService;

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

        verifyNoMoreInteractions(taskHistoryRepository);
    }

    @Test
    void shouldNotChangeHistoryWhenGettingIt() {

        TaskHistoryEntity history = createTaskHistory();
        UserEntity owner = createOwner();
        TaskEntity task = createTask(owner);

        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(taskRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(task));
        when(taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(history));

        TaskHistoryResponse expected = new TaskHistoryResponse(
                10L,
                1L,
                TaskHistoryAction.STATUS_CHANGED,
                "Status changed",
                "TODO",
                "DONE",
                LocalDateTime.of(2026, 10, 6, 15, 0)
        );

        List<TaskHistoryResponse> result = taskHistoryService.getHistory(1L);

        assertEquals(List.of(expected), result);

        assertEquals(1L, history.getTaskId());
        assertEquals(TaskHistoryAction.STATUS_CHANGED, history.getAction());
        assertEquals("Status changed", history.getDescription());
        assertEquals("TODO", history.getOldValue());
        assertEquals("DONE", history.getNewValue());

        verify(currentUserService).isAdmin();
        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(1L, owner);
        verify(taskHistoryRepository).findByTaskIdOrderByCreatedAtAsc(1L);
        verify(taskHistoryRepository, never()).save(any());
        verifyNoMoreInteractions(taskHistoryRepository);
    }

    @Test
    void shouldReturnMultipleHistoryEntriesInOrder() {

        TaskHistoryEntity createdHistory = createTaskHistory();
        TaskHistoryEntity updatedHistory = updateTaskHistory();
        UserEntity owner = createOwner();
        TaskEntity task = createTask(owner);

        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(taskRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(task));
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

        verify(currentUserService).isAdmin();
        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(1L, owner);
        verify(taskHistoryRepository).findByTaskIdOrderByCreatedAtAsc(1L);
        verify(taskHistoryRepository, never()).save(any());
        verifyNoMoreInteractions(taskHistoryRepository);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnHistoryForAdminAndRegularUser(boolean admin) {

        UserEntity owner = createOwner();
        TaskEntity task = createTask(owner);
        TaskHistoryEntity history = createTaskHistory();

        when(currentUserService.isAdmin()).thenReturn(admin);

        if (admin) {
            when(taskRepository.existsById(1L)).thenReturn(true);
        } else {
            when(currentUserService.getCurrentUser()).thenReturn(owner);
            when(taskRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(task));
        }

        when(taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(1L))
                .thenReturn(List.of(history));

        List<TaskHistoryResponse> result = taskHistoryService.getHistory(1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).id());
        assertEquals(1L, result.get(0).taskId());

        if (admin) {
            verify(taskRepository).existsById(1L);
            verify(taskRepository, never()).findByIdAndOwner(anyLong(), any());
            verify(currentUserService, never()).getCurrentUser();
        } else {
            verify(currentUserService).getCurrentUser();
            verify(taskRepository).findByIdAndOwner(1L, owner);
            verify(taskRepository, never()).existsById(anyLong());
        }

        verify(taskHistoryRepository).findByTaskIdOrderByCreatedAtAsc(1L);
        verify(taskHistoryRepository, never()).save(any());
        verifyNoMoreInteractions(taskHistoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenRegularUserTriesToGetAnotherUsersHistory() {

        UserEntity owner = createOwner();

        when(currentUserService.isAdmin()).thenReturn(false);
        when(currentUserService.getCurrentUser()).thenReturn(owner);
        when(taskRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskHistoryService.getHistory(1L)
        );

        verify(currentUserService).isAdmin();
        verify(currentUserService).getCurrentUser();
        verify(taskRepository).findByIdAndOwner(1L, owner);
        verify(taskHistoryRepository, never()).findByTaskIdOrderByCreatedAtAsc(anyLong());
        verify(taskHistoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenAdminTriesToGetHistoryOfNonexistentTask() {

        when(currentUserService.isAdmin()).thenReturn(true);
        when(taskRepository.existsById(1L)).thenReturn(false);

        assertThrows(
                TaskNotFoundException.class,
                () -> taskHistoryService.getHistory(1L)
        );

        verify(currentUserService).isAdmin();
        verify(taskRepository).existsById(1L);
        verify(currentUserService, never()).getCurrentUser();
        verify(taskRepository, never()).findByIdAndOwner(anyLong(), any());
        verify(taskHistoryRepository, never()).findByTaskIdOrderByCreatedAtAsc(anyLong());
        verify(taskHistoryRepository, never()).save(any());
    }

    private UserEntity createOwner() {
        return UserEntity.builder()
                .id(1L)
                .username("testuser")
                .build();
    }

    private TaskEntity createTask(UserEntity owner) {
        return TaskEntity.builder()
                .id(1L)
                .owner(owner)
                .build();
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
