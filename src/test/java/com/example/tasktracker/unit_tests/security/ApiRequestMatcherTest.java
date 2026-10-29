package com.example.tasktracker.unit_tests.security;

import com.example.tasktracker.security.ApiRequestMatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiRequestMatcherTest {

    private ApiRequestMatcher matcher;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        matcher = new ApiRequestMatcher();
        request = mock(HttpServletRequest.class);
    }

    @ParameterizedTest
    @CsvSource({
            "/tasks, true",
            "/tasks/1, true",
            "/tasks/1/history, true",
            "/register, true",
            "/register/anything, true",
            "/ui/tasks, false",
            "/tasks-other, false",
            "/login, false",
            "/css/style.css, false"
    })
    void shouldMatchApiRequests(
            String servletPath,
            boolean expected
    ) {
        when(request.getServletPath()).thenReturn(servletPath);

        assertEquals(expected, matcher.matches(request));
    }
}