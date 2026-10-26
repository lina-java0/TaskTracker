package com.example.tasktracker.integration_tests.service;

import com.example.tasktracker.dto.task.request.TaskFilterRequest;
import com.example.tasktracker.dto.task.request.TaskRequest;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.entities.task_history.TaskHistoryEntity;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.exception.custom_exceptions.TaskNotFoundException;
import com.example.tasktracker.repository.task_history.TaskHistoryRepository;
import com.example.tasktracker.repository.task.TaskRepository;
import com.example.tasktracker.repository.user.UserRepository;
import com.example.tasktracker.service.task.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TaskServiceIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        taskHistoryRepository.deleteAll();
        taskRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(
                new UserEntity("me", "testuser", "password", "ROLE_USER")
        );
    }

    private UserEntity owner;

    @Test
    void shouldCreateTaskAndSaveCreationHistory() {

        TaskRequest request = TaskRequest.builder()
                .title("Test task")
                .description("Integration test")
                .priority(Priority.MEDIUM)
                .deadline(LocalDate.now())
                .category(Category.WORK)
                .build();

        TaskEntity createdTask = taskService.createTask(request);

        assertThat(createdTask.getId()).isNotNull();
        assertThat(createdTask.getTitle()).isEqualTo("Test task");
        assertThat(createdTask.getDescription()).isEqualTo("Integration test");
        assertThat(createdTask.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(createdTask.getDeadline()).isEqualTo(LocalDate.now());
        assertThat(createdTask.getCategory()).isEqualTo(Category.WORK);
        assertThat(createdTask.getStatus()).isEqualTo(TaskStatus.TODO);

        List<TaskHistoryEntity> history = taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(createdTask.getId());

        assertThat(history).hasSize(1);

        TaskHistoryEntity historyEntry = history.getFirst();

        assertThat(historyEntry.getTaskId()).isEqualTo(createdTask.getId());
        assertThat(historyEntry.getAction()).isEqualTo(TaskHistoryAction.CREATED);
        assertThat(historyEntry.getDescription()).isEqualTo("Task created");
        assertThat(historyEntry.getOldValue()).isNull();
        assertThat(historyEntry.getNewValue()).isNull();
        assertThat(historyEntry.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindTasksWithFilters() {

        TaskEntity workTask = TaskEntity.builder()
                .title("Prepare report")
                .description("Work task")
                .status(TaskStatus.TODO)
                .priority(Priority.HIGH)
                .category(Category.WORK)
                .deadline(LocalDate.now().plusDays(2))
                .owner(owner)
                .build();

        TaskEntity personalTask = TaskEntity.builder()
                .title("Buy groceries")
                .description("Personal task")
                .status(TaskStatus.TODO)
                .priority(Priority.LOW)
                .category(Category.PERSONAL)
                .deadline(LocalDate.now().plusDays(5))
                .owner(owner)
                .build();

        taskRepository.saveAll(List.of(workTask, personalTask));

        TaskFilterRequest filterRequest = TaskFilterRequest.builder()
                .category(Category.WORK)
                .priority(Priority.HIGH)
                .build();

        var result = taskService.getTasks(filterRequest, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent())
                .extracting(TaskEntity::getTitle)
                .containsExactly("Prepare report");
    }

    @Test
    void shouldFindTaskById() {

        TaskEntity task = TaskEntity.builder()
                .title("Find me")
                .description("Test description")
                .status(TaskStatus.TODO)
                .priority(Priority.HIGH)
                .category(Category.WORK)
                .deadline(LocalDate.now())
                .owner(owner)
                .build();

        TaskEntity savedTask = taskRepository.save(task);

        TaskEntity foundTask = taskService.getTaskById(savedTask.getId());

        assertThat(foundTask.getId()).isEqualTo(savedTask.getId());
        assertThat(foundTask.getTitle()).isEqualTo("Find me");
        assertThat(foundTask.getDescription()).isEqualTo("Test description");
        assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(foundTask.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(foundTask.getCategory()).isEqualTo(Category.WORK);
        assertThat(foundTask.getDeadline()).isEqualTo(LocalDate.now());
    }

    @Test
    void shouldThrowExceptionWhenTaskNotFound() {

        Long nonExistingId = 999L;

        assertThatThrownBy(() -> taskService.getTaskById(nonExistingId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found with id: " + nonExistingId);
    }

    @Test
    void shouldDeleteTaskAndSaveDeletionHistory() {

        TaskEntity task = TaskEntity.builder()
                .title("Delete me")
                .description("Task to delete")
                .status(TaskStatus.TODO)
                .priority(Priority.LOW)
                .category(Category.PERSONAL)
                .deadline(LocalDate.now())
                .owner(owner)
                .build();

        TaskEntity savedTask = taskRepository.save(task);

        taskService.deleteTask(savedTask.getId());

        assertThat(taskRepository.findById(savedTask.getId())).isEmpty();

        List<TaskHistoryEntity> history = taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(savedTask.getId());

        assertThat(history).hasSize(1);

        TaskHistoryEntity historyEntry = history.getFirst();

        assertThat(historyEntry.getTaskId()).isEqualTo(savedTask.getId());
        assertThat(historyEntry.getAction()).isEqualTo(TaskHistoryAction.DELETED);
        assertThat(historyEntry.getDescription()).isEqualTo("Task deleted");
        assertThat(historyEntry.getOldValue()).isNull();
        assertThat(historyEntry.getNewValue()).isNull();
        assertThat(historyEntry.getCreatedAt()).isNotNull();
    }
}
