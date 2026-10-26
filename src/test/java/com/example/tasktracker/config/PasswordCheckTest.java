package com.example.tasktracker.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordCheckTest {

    @Test
    void shouldMatchAdminPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String hash = "$2a$10$0Kn8tI5r8KIVh91tDtiteeJMgfe.OfLlQiOhHAk4jYpdpTazxMBda";
        String rawPassword = "ChangeMe!123";

        assertTrue(encoder.matches(rawPassword, hash));
    }
}