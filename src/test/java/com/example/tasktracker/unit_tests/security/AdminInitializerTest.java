package com.example.tasktracker.unit_tests.security;

import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.repository.user.UserRepository;
import com.example.tasktracker.security.AdminInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationArguments args;

    private AdminInitializer adminInitializer;

    @BeforeEach
    void setUp() {
        adminInitializer = new AdminInitializer(
                userRepository,
                passwordEncoder,
                "admin",
                "Password123!"
        );
    }

    @Test
    void shouldCreateAdminWhenItDoesNotExist() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");

        adminInitializer.run(args);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        verify(passwordEncoder).encode("Password123!");

        UserEntity savedAdmin = captor.getValue();

        assertEquals("Administrator", savedAdmin.getName());
        assertEquals("admin", savedAdmin.getUsername());
        assertEquals("encodedPassword", savedAdmin.getPassword());
        assertEquals("ROLE_ADMIN", savedAdmin.getRole());
    }

    @Test
    void shouldNotCreateAdminWhenItAlreadyExists() throws Exception {
        UserEntity existingAdmin = mock(UserEntity.class);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(existingAdmin));

        adminInitializer.run(args);

        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode(anyString());
    }
}