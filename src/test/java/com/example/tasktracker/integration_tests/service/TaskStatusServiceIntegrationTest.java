package com.example.tasktracker.integration_tests.service;

import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.entities.TaskHistoryEntity;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.repository.TaskHistoryRepository;
import com.example.tasktracker.repository.TaskRepository;
import com.example.tasktracker.service.TaskStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TaskStatusServiceIntegrationTest {

    @Autowired
    private TaskStatusService taskStatusService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @BeforeEach
    void setUp() {
        taskHistoryRepository.deleteAll();
        taskRepository.deleteAll();
    }

    static Stream<Arguments> statusTransitions() {
        return Stream.of(
                Arguments.of(TaskStatus.TODO, TaskStatus.IN_PROGRESS),
                Arguments.of(TaskStatus.IN_PROGRESS, TaskStatus.DONE),
                Arguments.of(TaskStatus.DONE, TaskStatus.TODO)
        );
    }

    @ParameterizedTest
    @MethodSource("statusTransitions")
    void shouldChangeStatusAndSaveHistory(TaskStatus oldStatus, TaskStatus newStatus) {

        TaskEntity task = taskRepository.save(TaskEntity.builder()
                .title("Task")
                .description("Description")
                .status(oldStatus)
                .priority(Priority.MEDIUM)
                .category(Category.WORK)
                .deadline(LocalDate.now().plusDays(1))
                .build());

        taskStatusService.changeStatus(task.getId(), newStatus);

        TaskEntity updatedTask = taskRepository.findById(task.getId()).orElseThrow();

        assertThat(updatedTask.getStatus()).isEqualTo(newStatus);

        List<TaskHistoryEntity> history = taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(task.getId());

        assertThat(history).hasSize(1);
        assertThat(history.getFirst().getAction()).isEqualTo(TaskHistoryAction.STATUS_CHANGED);
        assertThat(history.getFirst().getOldValue()).isEqualTo(oldStatus.name());
        assertThat(history.getFirst().getNewValue()).isEqualTo(newStatus.name());
    }

    @Test
    void shouldNotSaveHistoryWhenStatusIsNotChanged() {
        TaskEntity task = taskRepository.save(TaskEntity.builder()
                .title("Task")
                .description("Description")
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .category(Category.WORK)
                .deadline(LocalDate.now().plusDays(1))
                .build());

        taskStatusService.changeStatus(task.getId(), TaskStatus.TODO);

        TaskEntity updatedTask = taskRepository.findById(task.getId()).orElseThrow();

        assertThat(updatedTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(task.getId())).isEmpty();
    }
}