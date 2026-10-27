package com.example.tasktracker.unit_tests.mapper.task;

import com.example.tasktracker.dto.task.response.TaskResponse;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import com.example.tasktracker.enums.TaskStatus;
import com.example.tasktracker.mapper.task.TaskMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskMapperTest {

    private final TaskMapper taskMapper = new TaskMapper();

    @Test
    void shouldMapTaskEntityToResponse() {
        UserEntity owner = UserEntity.builder()
                .id(10L)
                .username("testuser")
                .build();

        LocalDateTime createdAt =
                LocalDateTime.of(2028, 10, 20, 12, 0);

        LocalDateTime updatedAt =
                LocalDateTime.of(2028, 10, 21, 15, 30);

        TaskEntity task = TaskEntity.builder()
                .id(1L)
                .title("Learn Mockito")
                .description("Write unit tests")
                .status(TaskStatus.IN_PROGRESS)
                .priority(Priority.HIGH)
                .category(Category.STUDY)
                .deadline(LocalDate.of(2028, 11, 1))
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .owner(owner)
                .build();

        TaskResponse response = taskMapper.toResponse(task);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Learn Mockito", response.title());
        assertEquals("Write unit tests", response.description());
        assertEquals(TaskStatus.IN_PROGRESS, response.status());
        assertEquals(Priority.HIGH, response.priority());
        assertEquals(LocalDate.of(2028, 11, 1), response.deadline());
        assertEquals(Category.STUDY, response.category());
        assertEquals(createdAt, response.createdAt());
        assertEquals(updatedAt, response.updatedAt());
        assertEquals("testuser", response.ownerUsername());
    }

    @Test
    void shouldMapTaskWithoutDeadline() {
        UserEntity owner = UserEntity.builder()
                .id(10L)
                .username("testuser")
                .build();

        TaskEntity task = TaskEntity.builder()
                .id(2L)
                .title("Task without deadline")
                .status(TaskStatus.TODO)
                .priority(Priority.LOW)
                .category(Category.PERSONAL)
                .deadline(null)
                .owner(owner)
                .build();

        TaskResponse response = taskMapper.toResponse(task);

        assertNotNull(response);
        assertEquals(2L, response.id());
        assertNull(response.deadline());
    }
}