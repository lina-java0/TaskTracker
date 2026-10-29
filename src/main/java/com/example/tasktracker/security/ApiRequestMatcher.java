package com.example.tasktracker.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

@Component
public class ApiRequestMatcher implements RequestMatcher {

    @Override
    public boolean matches(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.equals("/tasks")
                || path.startsWith("/tasks/")
                || path.equals("/register")
                || path.startsWith("/register/");
    }
}
