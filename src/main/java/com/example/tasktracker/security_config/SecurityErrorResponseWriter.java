package com.example.tasktracker.security_config;

import com.example.tasktracker.dto.error.ErrorResponse;
import com.example.tasktracker.enums.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public void write(
            HttpServletResponse response,
            ErrorCode errorCode
    ) throws IOException {

        HttpStatus status = errorCode.getHttpStatus();

        String message = messageSource.getMessage(
                errorCode.getMessageKey(),
                null,
                errorCode.getMessageKey(),
                LocaleContextHolder.getLocale()
        );

        ErrorResponse errorResponse = new ErrorResponse(
                errorCode.name(),
                message,
                status,
                LocalDateTime.now()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getOutputStream(),
                errorResponse
        );
    }
}
