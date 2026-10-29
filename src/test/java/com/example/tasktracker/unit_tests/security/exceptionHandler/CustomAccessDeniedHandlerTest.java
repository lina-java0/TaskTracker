package com.example.tasktracker.unit_tests.security.exceptionHandler;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.security.ApiRequestMatcher;

import com.example.tasktracker.security.SecurityErrorResponseWriter;
import com.example.tasktracker.security.exceptionHandler.CustomAccessDeniedHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAccessDeniedHandlerTest {

    @Mock
    private SecurityErrorResponseWriter responseWriter;

    @Mock
    private ApiRequestMatcher apiRequestMatcher;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private CustomAccessDeniedHandler accessDeniedHandler;

    @BeforeEach
    void setUp() {
        accessDeniedHandler = new CustomAccessDeniedHandler(
                responseWriter,
                apiRequestMatcher
        );
    }

    @Test
    void shouldWriteForbiddenResponseForApiRequest() throws Exception {
        when(apiRequestMatcher.matches(request)).thenReturn(true);

        accessDeniedHandler.handle(
                request,
                response,
                new AccessDeniedException("Access denied")
        );

        verify(responseWriter).write(response, ErrorCode.FORBIDDEN);
        verify(response, never()).sendError(anyInt());
    }

    @Test
    void shouldSendForbiddenErrorForUiRequest() throws Exception {
        when(apiRequestMatcher.matches(request)).thenReturn(false);

        accessDeniedHandler.handle(
                request,
                response,
                new AccessDeniedException("Access denied")
        );

        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN);
        verifyNoInteractions(responseWriter);
    }
}