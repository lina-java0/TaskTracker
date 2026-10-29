package com.example.tasktracker.unit_tests.security.exceptionHandler;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.security.ApiRequestMatcher;

import com.example.tasktracker.security.SecurityErrorResponseWriter;
import com.example.tasktracker.security.exceptionHandler.CustomAuthenticationEntryPoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationEntryPointTest {

    @Mock
    private SecurityErrorResponseWriter responseWriter;

    @Mock
    private ApiRequestMatcher apiRequestMatcher;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private CustomAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new CustomAuthenticationEntryPoint(
                responseWriter,
                apiRequestMatcher
        );
    }

    @Test
    void shouldWriteUnauthorizedResponseForApiRequest() throws Exception {
        when(apiRequestMatcher.matches(request)).thenReturn(true);

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Bad credentials")
        );

        verify(responseWriter).write(response, ErrorCode.UNAUTHORIZED);
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void shouldRedirectToLoginForUiRequest() throws Exception {
        when(apiRequestMatcher.matches(request)).thenReturn(false);
        when(request.getContextPath()).thenReturn("");

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Bad credentials")
        );

        verify(response).sendRedirect("/login");
        verifyNoInteractions(responseWriter);
    }

    @Test
    void shouldIncludeContextPathInLoginRedirect() throws Exception {
        when(apiRequestMatcher.matches(request)).thenReturn(false);
        when(request.getContextPath()).thenReturn("/tasktracker");

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException("Bad credentials")
        );

        verify(response).sendRedirect("/tasktracker/login");
        verifyNoInteractions(responseWriter);
    }
}