
package com.example.tasktracker.unit_tests.service.user;

import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.repository.user.UserRepository;
import com.example.tasktracker.service.user.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnUserDetails() {
        UserEntity user = new UserEntity(
                "Test User",
                "testuser",
                "encodedPassword",
                "ROLE_USER"
        );

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("testuser");

        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_USER")));

        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void shouldReturnAdminAuthority() {
        UserEntity admin = new UserEntity(
                "Admin",
                "admin",
                "encodedPassword",
                "ROLE_ADMIN"
        );

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(admin));

        UserDetails result = customUserDetailsService.loadUserByUsername("admin");

        assertEquals("admin", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")));

        verify(userRepository).findByUsername("admin");
    }

    @Test
    void shouldThrowExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("unknown")
        );

        assertEquals("User not found: unknown", exception.getMessage());
        verify(userRepository).findByUsername("unknown");
    }
}
