package com.example.tasktracker.security;

import com.example.tasktracker.security.exceptionHandler.CustomAccessDeniedHandler;
import com.example.tasktracker.security.exceptionHandler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers(
                                        "/login",
                                        "/register",
                                        "/css/**",
                                        "/js/**"
                                ).permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin(
                        form -> form
                                .loginPage("/login")
                                .defaultSuccessUrl("/ui/tasks", true)
                                .permitAll()
                )
                .logout(
                        logout -> logout
                                .logoutSuccessUrl("/login?logout")
                                .permitAll()
                )
                .exceptionHandling(
                        ex -> ex
                                .authenticationEntryPoint(authenticationEntryPoint)
                                .accessDeniedHandler(accessDeniedHandler)
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
