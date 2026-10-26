package com.example.tasktracker.integration_tests.service;

import com.example.tasktracker.dto.task_history.TaskHistoryResponse;
import com.example.tasktracker.dto.task.request.TaskUpdateRequest;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.entities.task_history.TaskHistoryEntity;
import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import com.example.tasktracker.enums.TaskHistoryAction;
import com.example.tasktracker.enums.TaskStatus;
import com.example.tasktracker.repository.task_history.TaskHistoryRepository;
import com.example.tasktracker.repository.task.TaskRepository;
import com.example.tasktracker.service.task_history.TaskHistoryService;
import com.example.tasktracker.service.task.TaskService;
import com.example.tasktracker.service.task.TaskUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
@ActiveProfiles("test")
class TaskHistoryServiceIntegrationTest {

    @Autowired
    private TaskHistoryService taskHistoryService;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskUpdateService taskUpdateService;

    @BeforeEach
    void setUp() {
        taskHistoryRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void shouldSaveHistory() {

        Long taskId = 1L;

        taskHistoryService.addHistory(
                taskId,
                TaskHistoryAction.TITLE_CHANGED,
                "Title changed",
                "Old title",
                "New title"
        );

        List<TaskHistoryEntity> history = taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(taskId);

        assertThat(history).hasSize(1);

        TaskHistoryEntity savedHistory = history.getFirst();

        assertThat(savedHistory.getTaskId()).isEqualTo(taskId);
        assertThat(savedHistory.getAction()).isEqualTo(TaskHistoryAction.TITLE_CHANGED);
        assertThat(savedHistory.getDescription()).isEqualTo("Title changed");
        assertThat(savedHistory.getOldValue()).isEqualTo("Old title");
        assertThat(savedHistory.getNewValue()).isEqualTo("New title");
        assertThat(savedHistory.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldGetHistoryInCreatedAtOrder() {

        Long taskId = 1L;

        taskHistoryRepository.save(
                TaskHistoryEntity.builder()
                        .taskId(taskId)
                        .action(TaskHistoryAction.CREATED)
                        .description("Task created")
                        .createdAt(LocalDateTime.of(2026, 10, 8, 10, 0))
                        .build()
        );

        taskHistoryRepository.save(
                TaskHistoryEntity.builder()
                        .taskId(taskId)
                        .action(TaskHistoryAction.TITLE_CHANGED)
                        .description("Task updated")
                        .createdAt(LocalDateTime.of(2026, 10, 8, 11, 0))
                        .build()
        );

        List<TaskHistoryResponse> history = taskHistoryService.getHistory(taskId);

        assertThat(history).hasSize(2);

        assertThat(history)
                .extracting("action")
                .containsExactly(
                        TaskHistoryAction.CREATED,
                        TaskHistoryAction.TITLE_CHANGED
                );
    }

    @Test
    void shouldReturnEmptyHistoryWhenTaskHasNoHistory() {

        List<TaskHistoryResponse> history = taskHistoryService.getHistory(1L);

        assertThat(history).isEmpty();
    }

    @Test
    void shouldUpdateTaskAndCreateHistory() {

        TaskEntity task = taskRepository.save(
                TaskEntity.builder()
                        .title("Old title")
                        .description("Old description")
                        .priority(Priority.LOW)
                        .category(Category.WORK)
                        .status(TaskStatus.TODO)
                        .deadline(LocalDate.of(2026, 10, 10))
                        .build()
        );

        TaskUpdateRequest request = TaskUpdateRequest.builder()
                .title("New title")
                .description("New description")
                .priority(Priority.HIGH)
                .category(Category.STUDY)
                .deadline(LocalDate.of(2026, 11, 1))
                .build();

        taskUpdateService.updateTask(task.getId(), request);

        TaskEntity updatedTask = taskRepository.findById(task.getId()).orElseThrow();

        assertThat(updatedTask.getTitle()).isEqualTo("New title");
        assertThat(updatedTask.getDescription()).isEqualTo("New description");
        assertThat(updatedTask.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(updatedTask.getCategory()).isEqualTo(Category.STUDY);
        assertThat(updatedTask.getDeadline()).isEqualTo(LocalDate.of(2026, 11, 1));

        List<TaskHistoryEntity> history = taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(task.getId());

        assertThat(history).hasSize(5);

        assertThat(history)
                .extracting(TaskHistoryEntity::getAction)
                .containsExactly(
                        TaskHistoryAction.TITLE_CHANGED,
                        TaskHistoryAction.DESCRIPTION_CHANGED,
                        TaskHistoryAction.PRIORITY_CHANGED,
                        TaskHistoryAction.CATEGORY_CHANGED,
                        TaskHistoryAction.DEADLINE_CHANGED
                );
    }

    @Test
    void shouldKeepHistoryWhenTaskIsDeleted() {

        TaskEntity task = taskRepository.save(
                TaskEntity.builder()
                        .title("Task")
                        .description("Description")
                        .priority(Priority.MEDIUM)
                        .category(Category.WORK)
                        .status(TaskStatus.TODO)
                        .build()
        );

        taskHistoryService.addHistory(
                task.getId(),
                TaskHistoryAction.CREATED,
                "Task created",
                null,
                null
        );

        taskHistoryService.addHistory(
                task.getId(),
                TaskHistoryAction.TITLE_CHANGED,
                "Task updated",
                "Old title",
                "New title"
        );

        taskService.deleteTask(task.getId());

        assertThat(taskRepository.findById(task.getId())).isEmpty();

        List<TaskHistoryEntity> history = taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(task.getId());

        assertThat(history).hasSize(3);

        assertThat(history)
                .extracting(TaskHistoryEntity::getAction)
                .containsExactly(
                        TaskHistoryAction.CREATED,
                        TaskHistoryAction.TITLE_CHANGED,
                        TaskHistoryAction.DELETED
                );
    }
}
