package com.example.tasktracker.integration_tests.controller;

import com.example.tasktracker.dto.TaskRequest;
import com.example.tasktracker.dto.TaskUpdateRequest;
import com.example.tasktracker.enums.Category;
import com.example.tasktracker.enums.Priority;
import com.example.tasktracker.repository.TaskHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskHistoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @BeforeEach
    void setUp() {
        taskHistoryRepository.deleteAll();
    }

    @Test
    void shouldGetTaskHistory() throws Exception {
        TaskRequest creationRequest = createTaskRequest();

        MvcResult creationResult = mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long id = extractId(creationResult);

        TaskUpdateRequest updateRequest = TaskUpdateRequest.builder()
                .title("Learn Kafka")
                .description("Study Kafka")
                .priority(Priority.MEDIUM)
                .category(Category.STUDY)
                .deadline(LocalDate.now().plusDays(5))
                .build();

        mockMvc.perform(put("/tasks/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/tasks/{id}", id))
                .andExpect(status().isOk());

        assertThat(taskHistoryRepository.findAll()).hasSize(6);

        mockMvc.perform(get("/tasks/{id}/history", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(6))

                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].action").value("CREATED"))
                .andExpect(jsonPath("$[0].description").value("Task created"))
                .andExpect(jsonPath("$[0].oldValue").doesNotExist())
                .andExpect(jsonPath("$[0].newValue").doesNotExist())

                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].action").value("TITLE_CHANGED"))
                .andExpect(jsonPath("$[1].description").value("Title changed"))
                .andExpect(jsonPath("$[1].oldValue").value("Learn Spring"))
                .andExpect(jsonPath("$[1].newValue").value("Learn Kafka"))

                .andExpect(jsonPath("$[2].id").isNumber())
                .andExpect(jsonPath("$[2].action").value("DESCRIPTION_CHANGED"))
                .andExpect(jsonPath("$[2].description").value("Description changed"))
                .andExpect(jsonPath("$[2].oldValue").value("Study integration tests"))
                .andExpect(jsonPath("$[2].newValue").value("Study Kafka"))

                .andExpect(jsonPath("$[3].id").isNumber())
                .andExpect(jsonPath("$[3].action").value("PRIORITY_CHANGED"))
                .andExpect(jsonPath("$[3].oldValue").value("HIGH"))
                .andExpect(jsonPath("$[3].newValue").value("MEDIUM"))

                .andExpect(jsonPath("$[4].id").isNumber())
                .andExpect(jsonPath("$[4].action").value("DEADLINE_CHANGED"))
                .andExpect(jsonPath("$[4].oldValue").value(LocalDate.now().plusDays(3).toString()))
                .andExpect(jsonPath("$[4].newValue").value(updateRequest.getDeadline().toString()))

                .andExpect(jsonPath("$[5].id").isNumber())
                .andExpect(jsonPath("$[5].action").value("DELETED"))
                .andExpect(jsonPath("$[5].oldValue").doesNotExist())
                .andExpect(jsonPath("$[5].newValue").doesNotExist());
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
                .deadline(LocalDate.now().plusDays(3))
                .build();
    }
}
