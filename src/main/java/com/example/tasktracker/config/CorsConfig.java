package com.example.tasktracker.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Разрешаем все эндпоинты
                        .allowedOrigins("http://localhost:3000") // Адрес фронтенда
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH") // Разрешенные HTTP-методы
                        .allowedHeaders("*"); // Разрешаем все заголовки
            }
        };
    }
}