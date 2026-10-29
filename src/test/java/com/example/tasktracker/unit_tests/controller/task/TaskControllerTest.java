package com.example.tasktracker.unit_tests.controller.task;

import com.example.tasktracker.controller.task.TaskController;
import com.example.tasktracker.dto.task.request.TaskRequest;
import com.example.tasktracker.dto.task.request.TaskUpdateRequest;
import com.example.tasktracker.dto.task.response.TaskResponse;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import com.example.tasktracker.enums.TaskStatus;
import com.example.tasktracker.mapper.task.TaskMapper;
import com.example.tasktracker.service.task.TaskService;
import com.example.tasktracker.service.task.TaskStatusService;
import com.example.tasktracker.service.task.TaskUpdateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private TaskUpdateService taskUpdateService;

    @MockBean
    private TaskStatusService taskStatusService;

    @MockBean
    private TaskMapper taskMapper;

    private final UserEntity testUser =
            new UserEntity("me", "testuser", "password", "ROLE_USER");

    @Test
    void shouldGetTasks() throws Exception {

        Page<TaskResponse> page = new PageImpl<>(
                List.of(createResponse(TaskStatus.TODO)),
                PageRequest.of(0, 20),
                1
        );

        when(taskService.getTaskResponse(any(), eq(0), eq(20)))
                .thenReturn(page);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Test task"))
                .andExpect(jsonPath("$.content[0].status").value("TODO"))
                .andExpect(jsonPath("$.content[0].priority").value("MEDIUM"))
                .andExpect(jsonPath("$.content[0].category").value("WORK"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(taskService).getTaskResponse(any(), eq(0), eq(20));
    }

    @Test
    void shouldGetTasksWithPagination() throws Exception {

        Page<TaskResponse> page = new PageImpl<>(
                List.of(createResponse(TaskStatus.TODO)),
                PageRequest.of(1, 10),
                11
        );

        when(taskService.getTaskResponse(any(), eq(1), eq(10)))
                .thenReturn(page);

        mockMvc.perform(get("/tasks")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(10));

        verify(taskService).getTaskResponse(any(), eq(1), eq(10));
    }

    @Test
    void shouldGetTaskById() throws Exception {
        TaskEntity task = createTask();

        when(taskService.getTaskById(1L)).thenReturn(task);

        when(taskMapper.toResponse(task)).thenReturn(createResponse(TaskStatus.TODO));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test task"))
                .andExpect(jsonPath("$.description").value("Test description"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.category").value("WORK"))
                .andExpect(jsonPath("$.ownerUsername").value("testuser"));

        verify(taskService).getTaskById(1L);
        verify(taskMapper).toResponse(task);
    }

    @Test
    void shouldCreateTask() throws Exception {
        TaskEntity task = createTask();

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(task);

        when(taskMapper.toResponse(task)).thenReturn(createResponse(TaskStatus.TODO));

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

        org.mockito.ArgumentCaptor<TaskRequest> captor =
                org.mockito.ArgumentCaptor.forClass(TaskRequest.class);

        verify(taskService).createTask(captor.capture());

        TaskRequest result = captor.getValue();

        assertEquals("Test task", result.getTitle());
        assertEquals("Test description", result.getDescription());
        assertEquals(Priority.MEDIUM, result.getPriority());
        assertEquals(LocalDate.of(2030, 1, 1), result.getDeadline());
        assertEquals(Category.WORK, result.getCategory());

        verify(taskMapper).toResponse(task);
    }

    @Test
    void shouldUpdateTask() throws Exception {
        TaskEntity task = createTask();

        when(taskUpdateService.updateTask(eq(1L), any(TaskUpdateRequest.class)))
                .thenReturn(task);

        when(taskMapper.toResponse(task))
                .thenReturn(createResponse(TaskStatus.TODO));

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

        org.mockito.ArgumentCaptor<TaskUpdateRequest> captor =
                org.mockito.ArgumentCaptor.forClass(TaskUpdateRequest.class);

        verify(taskUpdateService).updateTask(eq(1L), captor.capture());

        TaskUpdateRequest result = captor.getValue();

        assertEquals("Updated task", result.getTitle());
        assertEquals("Updated description", result.getDescription());
        assertEquals(Priority.HIGH, result.getPriority());
        assertEquals(LocalDate.of(2030, 1, 1), result.getDeadline());
        assertEquals(Category.WORK, result.getCategory());

        verify(taskMapper).toResponse(task);
    }

    @Test
    void shouldChangeStatus() throws Exception {
        TaskEntity task = createTask();
        task.setStatus(TaskStatus.DONE);

        when(taskStatusService.changeStatus(1L, TaskStatus.DONE))
                .thenReturn(task);

        when(taskMapper.toResponse(task))
                .thenReturn(createResponse(TaskStatus.DONE));

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

        verify(taskStatusService).changeStatus(1L, TaskStatus.DONE);
        verify(taskMapper).toResponse(task);
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

        verify(taskUpdateService, never()).updateTask(anyLong(), any());
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

        verify(taskStatusService, never()).changeStatus(anyLong(), any());
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
                .owner(testUser)
                .build();
    }

    private TaskResponse createResponse(TaskStatus status) {
        return new TaskResponse(
                1L,
                "Test task",
                "Test description",
                status,
                Priority.MEDIUM,
                LocalDate.of(2030, 1, 1),
                Category.WORK,
                LocalDateTime.of(2026, 10, 1, 10, 0),
                LocalDateTime.of(2026, 10, 1, 10, 0),
                "testuser"
        );
    }
}