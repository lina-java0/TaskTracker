package com.example.tasktracker.security.exceptionHandler;

import com.example.tasktracker.enums.ErrorCode;
import com.example.tasktracker.security.ApiRequestMatcher;
import com.example.tasktracker.security.SecurityErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final SecurityErrorResponseWriter responseWriter;
    private final ApiRequestMatcher apiRequestMatcher;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException
    ) throws IOException {

        if (apiRequestMatcher.matches(request)) {
            responseWriter.write(
                    response,
                    ErrorCode.FORBIDDEN
            );
            return;
        }

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
