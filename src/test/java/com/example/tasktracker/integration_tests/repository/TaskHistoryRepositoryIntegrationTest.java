package com.example.tasktracker.integration_tests.repository;

import com.example.tasktracker.entities.TaskHistoryEntity;
import com.example.tasktracker.enums.TaskHistoryAction;
import com.example.tasktracker.repository.TaskHistoryRepository;
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

        TaskHistoryEntity older = TaskHistoryEntity.builder()
                .taskId(taskId)
                .action(TaskHistoryAction.CREATED)
                .description("Task created")
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();

        TaskHistoryEntity newer = TaskHistoryEntity.builder()
                .taskId(taskId)
                .action(TaskHistoryAction.TITLE_CHANGED)
                .description("Title changed")
                .createdAt(LocalDateTime.now())
                .build();

        taskHistoryRepository.save(older);
        taskHistoryRepository.save(newer);

        List<TaskHistoryEntity> history =
                taskHistoryRepository.findByTaskIdOrderByCreatedAtAsc(taskId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getAction()).isEqualTo(TaskHistoryAction.CREATED);
        assertThat(history.get(0).getDescription()).isEqualTo("Task created");

        assertThat(history.get(1).getAction()).isEqualTo(TaskHistoryAction.TITLE_CHANGED);
        assertThat(history.get(1).getDescription()).isEqualTo("Title changed");
    }
}
