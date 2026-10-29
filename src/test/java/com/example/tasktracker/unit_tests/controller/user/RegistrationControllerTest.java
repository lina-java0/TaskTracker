
package com.example.tasktracker.unit_tests.controller.user;

import com.example.tasktracker.controller.user.RegistrationController;
import com.example.tasktracker.dto.user.request.UserRegistrationRequest;
import com.example.tasktracker.service.user.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc(addFilters = false)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRegistrationService userRegistrationService;

    @Test
    void shouldRegisterUser() throws Exception {
        String request = """
                {
                    "name": "Test User",
                    "username": "testuser",
                    "password": "StrongPass1!"
                }
                """;

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());

        verify(userRegistrationService).register(any(UserRegistrationRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        String request = """
                {
                    "name": "",
                    "username": "",
                    "password": ""
                }
                """;

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());

        verify(userRegistrationService, never()).register(any(UserRegistrationRequest.class));
    }
}
