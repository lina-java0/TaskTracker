package com.example.tasktracker.integration_tests.service;

import com.example.tasktracker.dto.TaskUpdateRequest;
import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.entities.TaskHistoryEntity;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.repository.TaskHistoryRepository;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.service.TaskUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskUpdateServiceIntegrationTest {

    @Autowired
    private TaskUpdateService taskUpdateService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @BeforeEach
    void setUp() {
        taskHistoryRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void shouldUpdateAllFieldsAndSaveHistory() {
        LocalDate oldDeadline = LocalDate.now().plusDays(1);
        LocalDate newDeadline = LocalDate.now().plusDays(10);

        TaskEntity task = taskRepository.save(TaskEntity.builder()
                .title("Old title")
                .description("Old description")
                .status(TaskStatus.TODO)
                .priority(Priority.LOW)
                .category(Category.WORK)
                .deadline(oldDeadline)
                .build());

        TaskUpdateRequest request = TaskUpdateRequest.builder()
                .title("New title")
                .description("New description")
                .priority(Priority.HIGH)
                .category(Category.PERSONAL)
                .deadline(newDeadline)
                .build();

        taskUpdateService.updateTask(task.getId(), request);

        TaskEntity updatedTask = taskRepository.findById(task.getId())
                .orElseThrow();

        assertThat(updatedTask.getTitle()).isEqualTo("New title");
        assertThat(updatedTask.getDescription()).isEqualTo("New description");
        assertThat(updatedTask.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(updatedTask.getCategory()).isEqualTo(Category.PERSONAL);
        assertThat(updatedTask.getDeadline()).isEqualTo(newDeadline);

        List<TaskHistoryEntity> history =
                taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(task.getId());

        assertThat(history)
                .extracting(TaskHistoryEntity::getAction)
                .containsExactly(
                        TaskHistoryAction.TITLE_CHANGED,
                        TaskHistoryAction.DESCRIPTION_CHANGED,
                        TaskHistoryAction.PRIORITY_CHANGED,
                        TaskHistoryAction.CATEGORY_CHANGED,
                        TaskHistoryAction.DEADLINE_CHANGED
                );

        assertThat(history)
                .extracting(TaskHistoryEntity::getOldValue)
                .containsExactly(
                        "Old title",
                        "Old description",
                        "LOW",
                        "WORK",
                        oldDeadline.toString()
                );

        assertThat(history)
                .extracting(TaskHistoryEntity::getNewValue)
                .containsExactly(
                        "New title",
                        "New description",
                        "HIGH",
                        "PERSONAL",
                        newDeadline.toString()
                );
    }

    @Test
    void shouldNotSaveHistoryWhenNothingChanged() {
        LocalDate deadline = LocalDate.now().plusDays(1);

        TaskEntity task = taskRepository.save(TaskEntity.builder()
                .title("Task")
                .description("Description")
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .category(Category.WORK)
                .deadline(deadline)
                .build());

        TaskUpdateRequest request = TaskUpdateRequest.builder()
                .title("Task")
                .description("Description")
                .priority(Priority.MEDIUM)
                .category(Category.WORK)
                .deadline(deadline)
                .build();

        taskUpdateService.updateTask(task.getId(), request);

        assertThat(taskHistoryRepository
                .findByTaskIdOrderByCreatedAtAsc(task.getId()))
                .isEmpty();
    }
}