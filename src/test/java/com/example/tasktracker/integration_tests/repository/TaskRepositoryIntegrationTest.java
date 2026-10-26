package com.example.tasktracker.integration_tests.repository;

import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.repository.task.TaskRepository;
import com.example.tasktracker.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(
                new UserEntity("me", "testuser", "password", "ROLE_USER")
        );
    }

    private UserEntity owner;

    @Test
    void shouldSaveAndFindTask() {
        TaskEntity task = TaskEntity.builder()
                .title("Test task")
                .description("Integration test")
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .deadline(LocalDate.now())
                .category(Category.WORK)
                .owner(owner)
                .build();

        TaskEntity savedTask = taskRepository.save(task);

        assertThat(savedTask.getId()).isNotNull();

        TaskEntity foundTask = taskRepository.findById(savedTask.getId()).orElseThrow();

        assertThat(foundTask.getTitle()).isEqualTo("Test task");
        assertThat(foundTask.getDescription()).isEqualTo("Integration test");
        assertThat(foundTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(foundTask.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(foundTask.getCategory()).isEqualTo(Category.WORK);
        assertThat(foundTask.getDeadline()).isEqualTo(LocalDate.now());
        assertThat(foundTask.getCreatedAt()).isNotNull();
        assertThat(foundTask.getUpdatedAt()).isNotNull();
    }
}
