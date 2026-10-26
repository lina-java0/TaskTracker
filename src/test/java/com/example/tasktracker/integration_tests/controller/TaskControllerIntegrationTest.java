package com.example.tasktracker.integration_tests.controller;

import com.example.tasktracker.dto.task.request.TaskRequest;
import com.example.tasktracker.dto.task.request.TaskStatusRequest;
import com.example.tasktracker.dto.task.request.TaskUpdateRequest;
import com.example.tasktracker.entities.task.TaskEntity;
import com.example.tasktracker.enums.*;
import com.example.tasktracker.repository.task.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
    }

    @Test
    void shouldGetTasks() throws Exception {
        TaskRequest firstTask = createTaskRequest();

        TaskRequest secondTask = TaskRequest.builder()
                .title("Learn Hibernate")
                .description("Study ORM")
                .priority(Priority.MEDIUM)
                .category(Category.STUDY)
                .deadline(LocalDate.now().plusDays(3))
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstTask)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondTask)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/tasks")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[*].title")
                        .value(org.hamcrest.Matchers.containsInAnyOrder(
                                "Learn Spring", "Learn Hibernate"
                        )));

        assertThat(taskRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldGetById() throws Exception {
        TaskRequest request = createTaskRequest();

        MvcResult creationResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = extractId(creationResult);

        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(id))
                .andExpect(jsonPath("$.title")
                        .value(request.getTitle()))
                .andExpect(jsonPath("$.description")
                        .value(request.getDescription()))
                .andExpect(jsonPath("$.priority")
                        .value(request.getPriority().name()))
                .andExpect(jsonPath("$.category")
                        .value(request.getCategory().name()));

        assertThat(taskRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldCreateTask() throws Exception {
        TaskRequest request = createTaskRequest();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Task created successfully"))
                .andExpect(jsonPath("$.data.title")
                        .value("Learn Spring"))
                .andExpect(jsonPath("$.data.priority")
                        .value("HIGH"));

        assertThat(taskRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldUpdateTask() throws Exception {
        TaskRequest creationRequest = createTaskRequest();

        MvcResult creationResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = extractId(creationResult);

        TaskUpdateRequest updateRequest = TaskUpdateRequest.builder()
                .title("Learn Spring")
                .description("Study integration tests")
                .priority(Priority.HIGH)
                .category(Category.STUDY)
                .deadline(LocalDate.now().plusDays(2))
                .build();

        mockMvc.perform(put("/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Task updated successfully"))
                .andExpect(jsonPath("$.data.id")
                        .value(id))
                .andExpect(jsonPath("$.data.deadline")
                        .value(updateRequest.getDeadline().toString()));

        assertThat(taskRepository.findAll()).hasSize(1);

        TaskEntity updatedTask = taskRepository.findById(id).orElseThrow();

        assertThat(updatedTask.getDeadline()).isEqualTo(updateRequest.getDeadline());
        assertThat(updatedTask.getTitle()).isEqualTo(updateRequest.getTitle());
    }

    @Test
    void shouldChangeStatus() throws Exception {
        TaskRequest creationRequest = createTaskRequest();

        MvcResult creationResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = extractId(creationResult);

        TaskStatusRequest statusRequest = TaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();

        mockMvc.perform(patch("/tasks/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Task status changed successfully"))
                .andExpect(jsonPath("$.data.id")
                        .value(id))
                .andExpect(jsonPath("$.data.status")
                        .value(statusRequest.getStatus().name()));

        assertThat(taskRepository.findAll()).hasSize(1);

        TaskEntity changeStatusTask = taskRepository.findById(id).orElseThrow();

        assertThat(changeStatusTask.getStatus()).isEqualTo(statusRequest.getStatus());
    }

    @Test
    void shouldDeleteTask() throws Exception {
        TaskRequest creationRequest = createTaskRequest();

        MvcResult creationResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = extractId(creationResult);

        mockMvc.perform(delete("/tasks/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Task deleted successfully"))
                .andExpect(jsonPath("$.data")
                        .doesNotExist());

        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        mockMvc.perform(get("/tasks/{id}", 999999L))
                .andExpect(status().isNotFound());

        assertThat(taskRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsMissing() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .description("Study integration tests")
                .priority(Priority.HIGH)
                .category(Category.STUDY)
                .deadline(LocalDate.now().plusDays(5))
                .build();

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(taskRepository.findAll()).isEmpty();
    }

    private Long extractId(MvcResult result) throws Exception {
        return ((Number) JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.id"
        )).longValue();
    }

    private TaskRequest createTaskRequest() {
        return TaskRequest.builder()
                .title("Learn Spring")
                .description("Study integration tests")
                .priority(Priority.HIGH)
                .category(Category.STUDY)
                .deadline(LocalDate.now().plusDays(5))
                .build();
    }
}
