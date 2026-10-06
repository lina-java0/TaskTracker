package com.example.tasktracker.controller;

import com.example.tasktracker.dto.TaskRequest;
import com.example.tasktracker.dto.TaskUpdateRequest;
import com.example.tasktracker.entities.TaskEntity;
import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import com.example.tasktracker.enums.TaskStatus;
import com.example.tasktracker.service.TaskService;
import com.example.tasktracker.service.TaskStatusService;
import com.example.tasktracker.service.TaskUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private TaskUpdateService taskUpdateService;

    @MockBean
    private TaskStatusService taskStatusService;

    @Test
    void shouldGetTasks() throws Exception {
        TaskEntity task = createTask();

        Page<TaskEntity> page = new PageImpl<>(
                List.of(task),
                PageRequest.of(0, 20),
                1
        );

        when(taskService.getTasks(any(), eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test task"))
                .andExpect(jsonPath("$.content[0].status").value("TODO"))
                .andExpect(jsonPath("$.content[0].priority").value("MEDIUM"))
                .andExpect(jsonPath("$.content[0].category").value("WORK"));
    }

    @Test
    void shouldGetTasksWithPagination() throws Exception {
        Page<TaskEntity> page = new PageImpl<>(
                List.of(createTask()),
                PageRequest.of(1, 10),
                11
        );

        when(taskService.getTasks(any(), eq(1), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/tasks")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void shouldGetTaskById() throws Exception {
        TaskEntity task = createTask();

        when(taskService.getTaskById(1L))
                .thenReturn(task);

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test task"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.category").value("WORK"));
    }

    @Test
    void shouldCreateTask() throws Exception {
        TaskEntity task = createTask();

        when(taskService.createTask(any(TaskRequest.class)))
                .thenReturn(task);

        String request = """
                {
                    "title": "Test task",
                    "description": "Test description",
                    "priority": "MEDIUM",
                    "deadline": "2030-01-01",
                    "category": "WORK"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Task created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test task"));
    }

    @Test
    void shouldUpdateTask() throws Exception {
        TaskEntity task = createTask();

        when(taskUpdateService.updateTask(
                eq(1L),
                any(TaskUpdateRequest.class)
        )).thenReturn(task);

        String request = """
                {
                    "title": "Updated task",
                    "description": "Updated description",
                    "priority": "HIGH",
                    "deadline": "2030-01-01",
                    "category": "WORK"
                }
                """;

        mockMvc.perform(put("/tasks/1")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Task updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldChangeStatus() throws Exception {
        TaskEntity task = createTask();
        task.setStatus(TaskStatus.DONE);

        when(taskStatusService.changeStatus(1L, TaskStatus.DONE))
                .thenReturn(task);

        String request = """
                {
                    "status": "DONE"
                }
                """;

        mockMvc.perform(patch("/tasks/1/status")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Task status changed successfully"))
                .andExpect(jsonPath("$.data.status").value("DONE"));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Task deleted successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(taskService).deleteTask(1L);
    }

    @Test
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {
        String request = """
                {
                    "title": "",
                    "description": "Test description",
                    "priority": null,
                    "deadline": "2020-01-01",
                    "category": null
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(taskService, never()).createTask(any());
    }

    @Test
    void shouldReturnBadRequestWhenUpdateRequestIsInvalid() throws Exception {
        String request = """
                {
                    "title": "",
                    "description": "Test description",
                    "priority": null,
                    "deadline": "2020-01-01",
                    "category": null
                }
                """;

        mockMvc.perform(put("/tasks/1")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(taskUpdateService, never())
                .updateTask(anyLong(), any());
    }

    @Test
    void shouldReturnBadRequestWhenStatusIsNull() throws Exception {
        String request = """
                {
                    "status": null
                }
                """;

        mockMvc.perform(patch("/tasks/1/status")
                        .contentType(APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(taskStatusService, never())
                .changeStatus(anyLong(), any());
    }

    private TaskEntity createTask() {
        return TaskEntity.builder()
                .id(1L)
                .title("Test task")
                .description("Test description")
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .category(Category.WORK)
                .deadline(LocalDate.of(2030, 1, 1))
                .build();
    }
}