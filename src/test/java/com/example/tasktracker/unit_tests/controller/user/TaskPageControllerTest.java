
package com.example.tasktracker.unit_tests.controller.user;

import com.example.tasktracker.controller.user.TaskPageController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(TaskPageController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnTasksPage() throws Exception {
        mockMvc.perform(get("/ui/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks"));
    }
}
