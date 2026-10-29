package com.example.tasktracker.security.exceptionHandler;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.security.ApiRequestMatcher;
import com.example.tasktracker.security.SecurityErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import org.springframework.security.core.AuthenticationException;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorResponseWriter responseWriter;
    private final ApiRequestMatcher apiRequestMatcher;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException
    ) throws IOException {

        if (apiRequestMatcher.matches(request)) {
            responseWriter.write(
                    response,
                    ErrorCode.UNAUTHORIZED
            );
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }
}
