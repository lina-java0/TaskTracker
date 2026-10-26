package com.example.tasktracker.unit_tests.service;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.dto.task.request.TaskRequest;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.exception.custom_exceptions.TaskNotFoundException;
import com.example.tasktracker.repository.task.TaskRepository;
import com.example.tasktracker.service.user.CurrentUserService;
import com.example.tasktracker.service.task.TaskService;
import com.example.tasktracker.service.task.TaskSortService;
import com.example.tasktracker.service.task_history.TaskHistoryService;
import com.example.tasktracker.validation.TaskFilterValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskSortService taskSortService;

    @Mock
    private TaskFilterValidator taskFilterValidator;

    @Mock
    private TaskHistoryService taskHistoryService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldCreateTaskAndAddHistory() {

        UserEntity owner = createAndMockOwner();

        TaskRequest request = TaskRequest.builder()
                .title("Learn Mockito")
                .description("Write unit tests")
                .priority(Priority.HIGH)
                .deadline(LocalDate.of(2026, 10, 10))
                .category(Category.STUDY)
                .build();

        TaskEntity savedTask = TaskEntity.builder()
                .id(1L)
                .title("Learn Mockito")
                .description("Write unit tests")
                .priority(Priority.HIGH)
                .deadline(LocalDate.of(2026, 10, 10))
                .category(Category.STUDY)
                .status(TaskStatus.TODO)
                .owner(owner)
                .build();

        when(taskRepository.save(any(TaskEntity.class))).thenReturn(savedTask);

        TaskEntity result = taskService.createTask(request);

        ArgumentCaptor<TaskEntity> captor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(taskRepository).save(captor.capture());

        TaskEntity createdTask = captor.getValue();

        assertEquals("Learn Mockito", createdTask.getTitle());
        assertEquals("Write unit tests", createdTask.getDescription());
        assertEquals(Priority.HIGH, createdTask.getPriority());
        assertEquals(
                LocalDate.of(2026, 10, 10),
                createdTask.getDeadline()
        );
        assertEquals(Category.STUDY, createdTask.getCategory());
        assertEquals(TaskStatus.TODO, createdTask.getStatus());

        assertEquals(savedTask, result);
        assertSame(owner, createdTask.getOwner());

        verify(currentUserService).getCurrentUser();

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.CREATED,
                "Task created",
                null,
                null
        );
    }

    @ParameterizedTest
    @CsvSource({
            "-1, 10",
            "0, 0",
            "0, 101",
            "1, 101"
    })
    void shouldRejectInvalidPagination(int page, int size) {

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getTasks(
                        TaskFilterRequest.builder().build(),
                        page,
                        size
                )
        );

        verifyNoInteractions(taskFilterValidator);
        verifyNoInteractions(taskRepository);
        verifyNoInteractions(taskSortService);
    }

    @Test
    void shouldGetTasksWithFiltersAndSorting() {

        UserEntity owner = createAndMockOwner();

        TaskFilterRequest filterRequest = TaskFilterRequest.builder()
                .category(Category.WORK)
                .sortType(TaskSortType.DEADLINE_ASC)
                .build();

        Sort sort = Sort.by(
                Sort.Direction.ASC,
                "deadline"
        );

        Page<TaskEntity> expectedPage = new PageImpl<>(List.of());

        when(taskSortService.getSort(TaskSortType.DEADLINE_ASC)).thenReturn(sort);

        when(taskRepository.findAll(
                ArgumentMatchers.<Specification<TaskEntity>>any(),
                eq(PageRequest.of(0, 10, sort))
        )).thenReturn(expectedPage);

        Page<TaskEntity> result =
                taskService.getTasks(
                        filterRequest,
                        0,
                        10
                );

        assertEquals(expectedPage, result);

        verify(taskFilterValidator).validate(filterRequest);

        verify(taskSortService).getSort(TaskSortType.DEADLINE_ASC);

        verify(taskRepository)
                .findAll(
                        ArgumentMatchers.<Specification<TaskEntity>>any(),
                        eq(PageRequest.of(0, 10, sort))
                );
    }

    @Test
    void shouldReturnTaskById() {

        TaskEntity task = TaskEntity.builder()
                .id(1L)
                .build();

        UserEntity owner = createAndMockOwner();

        when(taskRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(task));

        TaskEntity result = taskService.getTaskById(1L);

        assertEquals(task, result);

        verify(taskRepository).findByIdAndOwner(1L, owner);
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {

        UserEntity owner = createAndMockOwner();

        when(taskRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTaskById(1L)
        );

        verify(taskRepository).findByIdAndOwner(1L, owner);
    }

    @Test
    void shouldDeleteTaskAndAddHistory() {

        TaskEntity task = TaskEntity.builder()
                .id(1L)
                .build();

        UserEntity owner = createAndMockOwner();

        when(taskRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskHistoryService).addHistory(
                1L,
                TaskHistoryAction.DELETED,
                "Task deleted",
                null,
                null
        );

        verify(taskRepository).delete(task);
    }

    private UserEntity createAndMockOwner() {
        UserEntity owner = UserEntity.builder()
                .id(1L)
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(owner);

        return owner;
    }
}