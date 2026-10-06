package com.example.tasktracker.controller;

import com.example.tasktracker.dto.TaskHistoryResponse;
import com.example.tasktracker.enums.TaskHistoryAction;
import com.example.tasktracker.service.TaskHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskHistoryController.class)
class TaskHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskHistoryService taskHistoryService;

    @Test
    void shouldGetTaskHistory() throws Exception {
        TaskHistoryResponse history = new TaskHistoryResponse(
                1L,
                1L,
                TaskHistoryAction.CREATED,
                "Task created",
                null,
                "Test task",
                LocalDateTime.of(2026, 10, 6, 12, 0)
        );

        when(taskHistoryService.getHistory(1L))
                .thenReturn(List.of(history));

        mockMvc.perform(get("/tasks/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].taskId").value(1))
                .andExpect(jsonPath("$[0].action").value("CREATED"))
                .andExpect(jsonPath("$[0].description").value("Task created"))
                .andExpect(jsonPath("$[0].oldValue").doesNotExist())
                .andExpect(jsonPath("$[0].newValue").value("Test task"));
    }

    @Test
    void shouldReturnEmptyHistory() throws Exception {
        when(taskHistoryService.getHistory(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/tasks/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}