package com.example.tasktracker.unit_tests.service.task;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.dto.task.request.TaskRequest;
import com.example.tasktracker.dto.task.response.TaskResponse;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.exception.custom_exceptions.TaskNotFoundException;
import com.example.tasktracker.exception.custom_exceptions.ValidationException;
import com.example.tasktracker.mapper.task.TaskMapper;
import com.example.tasktracker.repository.task.TaskRepository;
import com.example.tasktracker.service.user.CurrentUserService;
import com.example.tasktracker.service.task.TaskService;
import com.example.tasktracker.service.task.TaskSortService;
import com.example.tasktracker.service.task_history.TaskHistoryService;
import com.example.tasktracker.validation.PaginationValidator;
import com.example.tasktracker.validation.TaskFilterValidator;
import com.example.tasktracker.validation.task_validation.TaskAccessValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskSortService taskSortService;

    @Mock
    private TaskHistoryService taskHistoryService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TaskFilterValidator taskFilterValidator;

    @Mock
    private PaginationValidator paginationValidator;

    @Mock
    private TaskAccessValidator taskAccessValidator;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldCreateTaskAndAddHistory() {

        UserEntity owner = createAndMockOwner();

        TaskRequest request = TaskRequest.builder()
                .title("Learn Mockito")
                .description("Write unit tests")
                .priority(Priority.HIGH)
                .deadline(LocalDate.of(2026, 10, 30))
                .category(Category.STUDY)
                .build();

        TaskEntity savedTask = TaskEntity.builder()
                .id(1L)
                .title("Learn Mockito")
                .description("Write unit tests")
                .priority(Priority.HIGH)
                .deadline(LocalDate.of(2026, 10, 30))
                .category(Category.STUDY)
                .status(TaskStatus.TODO)
                .owner(owner)
                .build();

        when(taskRepository.save(any(TaskEntity.class))).thenReturn(savedTask);

        TaskEntity result = taskService.createTask(request);

        ArgumentCaptor<TaskEntity> captor = ArgumentCaptor.forClass(TaskEntity.class);
        verify(taskRepository).save(captor.capture());
        TaskEntity createdTask = captor.getValue();

        assertSame(savedTask, result);

        assertEquals("Learn Mockito", createdTask.getTitle());
        assertEquals("Write unit tests", createdTask.getDescription());
        assertEquals(Priority.HIGH, createdTask.getPriority());
        assertEquals(LocalDate.of(2026, 10, 30), createdTask.getDeadline());
        assertEquals(Category.STUDY, createdTask.getCategory());
        assertEquals(TaskStatus.TODO, createdTask.getStatus());
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

        doThrow(new ValidationException(
                ErrorCode.INVALID_PAGE,
                "Invalid page"))
                .when(paginationValidator).validate(page, size);

        assertThrows(
                ValidationException.class,
                () -> taskService.getTasks(
                        TaskFilterRequest.builder().build(),
                        page,
                        size
                )
        );

        verifyNoInteractions(taskAccessValidator);
        verifyNoInteractions(taskFilterValidator);
        verifyNoInteractions(taskRepository);
        verifyNoInteractions(taskSortService);
    }

    @Test
    void shouldGetTasksWithFiltersAndSorting() {

        createAndMockOwner();

        TaskFilterRequest filterRequest = TaskFilterRequest.builder()
                .category(Category.WORK)
                .sortType(TaskSortType.DEADLINE_ASC)
                .build();

        Sort sort = Sort.by(Sort.Direction.ASC, "deadline");
        Page<TaskEntity> expectedPage = new PageImpl<>(List.of());

        when(currentUserService.isAdmin()).thenReturn(false);
        when(taskSortService.getSort(TaskSortType.DEADLINE_ASC)).thenReturn(sort);

        when(taskRepository.findAll(
                ArgumentMatchers.<Specification<TaskEntity>>any(),
                eq(PageRequest.of(0, 10, sort))
        )).thenReturn(expectedPage);

        Page<TaskEntity> result = taskService.getTasks(filterRequest, 0, 10);

        assertSame(expectedPage, result);

        verify(currentUserService).isAdmin();
        verify(paginationValidator).validate(0, 10);
        verify(taskAccessValidator).validateAdminFilters(filterRequest, false);
        verify(taskFilterValidator).validate(filterRequest);
        verify(currentUserService).getCurrentUser();
        verify(taskSortService).getSort(TaskSortType.DEADLINE_ASC);

        verify(taskRepository).findAll(
                ArgumentMatchers.<Specification<TaskEntity>>any(),
                eq(PageRequest.of(0, 10, sort))
        );
    }

    @Test
    void shouldStopProcessingWhenAdminFilterValidationFails() {
        TaskFilterRequest filter = TaskFilterRequest.builder()
                .ownerUsername("anotheruser")
                .build();

        when(currentUserService.isAdmin()).thenReturn(false);

        doThrow(new ValidationException(
                ErrorCode.FORBIDDEN,
                "Forbidden"
        )).when(taskAccessValidator)
                .validateAdminFilters(filter, false);

        assertThrows(
                ValidationException.class,
                () -> taskService.getTasks(filter, 0, 20)
        );

        verify(taskAccessValidator)
                .validateAdminFilters(filter, false);

        verify(paginationValidator).validate(0, 20);
        verify(taskAccessValidator).validateAdminFilters(filter, false);

        verifyNoInteractions(
                taskRepository,
                taskSortService,
                taskFilterValidator
        );
    }

    static Stream<TaskResponseCase> taskResponseCases() {
        UserEntity owner = UserEntity.builder()
                .id(1L)
                .username("testuser")
                .build();

        TaskEntity firstTask = TaskEntity.builder()
                .id(1L)
                .title("First task")
                .owner(owner)
                .build();

        TaskEntity secondTask = TaskEntity.builder()
                .id(2L)
                .title("Second task")
                .owner(owner)
                .build();

        TaskResponse firstResponse = createTaskResponse(1L, "First task");
        TaskResponse secondResponse = createTaskResponse(2L, "Second task");

        return Stream.of(
                new TaskResponseCase(
                        List.of(firstTask),
                        List.of(firstResponse)
                ),
                new TaskResponseCase(
                        List.of(firstTask, secondTask),
                        List.of(firstResponse, secondResponse)
                ),
                new TaskResponseCase(
                        List.of(),
                        List.of()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("taskResponseCases")
    void shouldReturnTaskResponse(TaskResponseCase testCase) {
        List<TaskEntity> tasks = testCase.tasks();
        List<TaskResponse> expectedResponses = testCase.expectedResponses();

        when(currentUserService.isAdmin()).thenReturn(false);
        when(taskSortService.getSort(null)).thenReturn(
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        when(taskRepository.findAll(
                ArgumentMatchers.<Specification<TaskEntity>>any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(tasks));

        for (int i = 0; i < tasks.size(); i++) {
            when(taskMapper.toResponse(tasks.get(i)))
                    .thenReturn(expectedResponses.get(i));
        }

        Page<TaskResponse> result = taskService.getTaskResponse(null, 0, 20);

        assertEquals(expectedResponses.size(), result.getTotalElements());
        assertEquals(expectedResponses, result.getContent());

        for (TaskEntity task : tasks) {
            verify(taskMapper).toResponse(task);
        }

        verifyNoMoreInteractions(taskMapper);
    }

    @Test
    void shouldReturnTaskByIdForRegularUser() {

        UserEntity owner = createAndMockOwner();

        TaskEntity task = TaskEntity.builder()
                .id(1L)
                .title("User task")
                .owner(owner)
                .build();

        when(currentUserService.isAdmin()).thenReturn(false);
        when(taskRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(task));

        TaskEntity result = taskService.getTaskById(1L);

        assertSame(task, result);

        verify(taskRepository).findByIdAndOwner(1L, owner);
        verify(taskRepository, never()).findById(1L);
    }

    @Test
    void shouldReturnTaskByIdForAdmin() {
        TaskEntity task = TaskEntity.builder()
                .id(1L)
                .title("Admin task")
                .build();

        when(currentUserService.isAdmin()).thenReturn(true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskEntity result = taskService.getTaskById(1L);

        assertSame(task, result);

        verify(taskRepository).findById(1L);
        verify(taskRepository, never()).findByIdAndOwner(anyLong(), any(UserEntity.class));
        verify(currentUserService, never()).getCurrentUser();
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFoundForRegularUser() {

        UserEntity owner = createAndMockOwner();

        when(currentUserService.isAdmin()).thenReturn(false);
        when(taskRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTaskById(1L)
        );

        verify(taskRepository).findByIdAndOwner(1L, owner);
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFoundForAdmin() {

        when(currentUserService.isAdmin()).thenReturn(true);
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.getTaskById(1L)
        );

        verify(taskRepository).findById(1L);
    }

    @Test
    void shouldDeleteTaskAndAddHistory() {

        UserEntity owner = createAndMockOwner();

        TaskEntity task = TaskEntity.builder()
                .id(1L)
                .owner(owner)
                .build();

        when(currentUserService.isAdmin()).thenReturn(false);
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

    @Test
    void shouldNotDeleteTaskOwnedByAnotherUser() {
        Long taskId = 1L;
        UserEntity owner = createAndMockOwner();

        when(currentUserService.isAdmin()).thenReturn(false);
        when(taskRepository.findByIdAndOwner(taskId, owner))
                .thenReturn(Optional.empty());

        assertThrows(
                TaskNotFoundException.class,
                () -> taskService.deleteTask(taskId)
        );

        verify(taskRepository, never()).delete(any(TaskEntity.class));
        verify(taskHistoryService, never()).addHistory(
                anyLong(),
                any(TaskHistoryAction.class),
                anyString(),
                nullable(String.class),
                nullable(String.class)
        );
    }

    private UserEntity createAndMockOwner() {
        UserEntity owner = UserEntity.builder()
                .id(1L)
                .username("testuser")
                .build();

        when(currentUserService.getCurrentUser()).thenReturn(owner);

        return owner;
    }

    private static TaskResponse createTaskResponse(Long id, String title) {
        return new TaskResponse(
                id,
                title,
                null,
                TaskStatus.TODO,
                Priority.LOW,
                null,
                Category.STUDY,
                null,
                null,
                "testuser"
        );
    }

    private record TaskResponseCase(
            List<TaskEntity> tasks,
            List<TaskResponse> expectedResponses
    ) {
    }
}