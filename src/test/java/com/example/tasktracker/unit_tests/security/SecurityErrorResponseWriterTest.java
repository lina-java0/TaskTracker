package com.example.tasktracker.unit_tests.security;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.security.SecurityErrorResponseWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityErrorResponseWriterTest {

    @Mock
    private MessageSource messageSource;

    private ObjectMapper objectMapper;
    private SecurityErrorResponseWriter responseWriter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        responseWriter = new SecurityErrorResponseWriter(
                objectMapper,
                messageSource
        );
    }

    @Test
    void shouldWriteUnauthorizedErrorResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(messageSource.getMessage(
                eq("error.security.unauthorized"),
                isNull(),
                eq("error.security.unauthorized"),
                any(Locale.class)
        )).thenReturn("Authentication required");

        responseWriter.write(response, ErrorCode.UNAUTHORIZED);

        assertEquals(
                HttpServletResponse.SC_UNAUTHORIZED,
                response.getStatus()
        );
        assertTrue(Objects.requireNonNull(response.getContentType()).startsWith("application/json"));
        assertEquals("UTF-8", response.getCharacterEncoding());

        JsonNode json = objectMapper.readTree(response.getContentAsString());

        assertEquals("UNAUTHORIZED", json.get("code").asText());
        assertEquals("Authentication required", json.get("message").asText());
        assertEquals("UNAUTHORIZED", json.get("httpStatus").asText());
        assertTrue(json.hasNonNull("timestamp"));

        verify(messageSource).getMessage(
                eq("error.security.unauthorized"),
                isNull(),
                eq("error.security.unauthorized"),
                any(Locale.class)
        );
    }
}