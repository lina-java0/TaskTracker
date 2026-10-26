package com.example.tasktracker.integration_tests.repository;

import com.example.tasktracker.entities.task_history.TaskHistoryEntity;
import com.example.tasktracker.enums.TaskHistoryAction;
import com.example.tasktracker.repository.task_history.TaskHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class TaskHistoryRepositoryIntegrationTest {

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @BeforeEach
    void setUp() {
        taskHistoryRepository.deleteAll();
    }

    @Test
    void shouldFindTaskHistoryOrderedByCreatedAt() {

        Long taskId = 1L;
        LocalDateTime now = LocalDateTime.of(2026, 10, 14, 12, 0);

        TaskHistoryEntity older = TaskHistoryEntity.builder()
                .taskId(taskId)
                .action(TaskHistoryAction.CREATED)
                .description("Task created")
                .createdAt(now.minusMinutes(10))
                .build();

        TaskHistoryEntity newer = TaskHistoryEntity.builder()
                .taskId(taskId)
                .action(TaskHistoryAction.TITLE_CHANGED)
                .description("Title changed")
                .createdAt(now)
                .build();

        taskHistoryRepository.save(older);
        taskHistoryRepository.save(newer);

        List<TaskHistoryEntity> history =
                taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(taskId);

        assertThat(history)
                .hasSize(2)
                .extracting(TaskHistoryEntity::getAction)
                .containsExactly(
                        TaskHistoryAction.CREATED,
                        TaskHistoryAction.TITLE_CHANGED
                );

        assertThat(history)
                .extracting(TaskHistoryEntity::getDescription)
                .containsExactly("Task created", "Title changed");
    }
}
