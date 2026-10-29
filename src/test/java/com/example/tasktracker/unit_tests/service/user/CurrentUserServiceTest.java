
package com.example.tasktracker.unit_tests.service.user;

import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.exception.custom_exceptions.AuthenticatedUserNotFoundException;
import com.example.tasktracker.repository.user.UserRepository;
import com.example.tasktracker.service.user.CurrentUserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnCurrentUser() {
        UserEntity user = new UserEntity(
                "Test User",
                "testuser",
                "encodedPassword",
                "ROLE_USER"
        );

        setAuthentication("testuser", "ROLE_USER");

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));

        UserEntity result = currentUserService.getCurrentUser();

        assertSame(user, result);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationIsMissing() {
        assertThrows(
                AuthenticatedUserNotFoundException.class,
                () -> currentUserService.getCurrentUser()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWhenAuthenticationIsNotAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "testuser",
                        "password"
                )
        );

        assertThrows(
                AuthenticatedUserNotFoundException.class,
                () -> currentUserService.getCurrentUser()
        );

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldThrowExceptionWhenAuthenticatedUserDoesNotExist() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "testuser",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))
                )
        );

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        assertThrows(
                AuthenticatedUserNotFoundException.class,
                () -> currentUserService.getCurrentUser()
        );

        verify(userRepository).findByUsername("testuser");
    }

    static Stream<Arguments> adminRoleCases() {
        return Stream.of(
                Arguments.of(
                        List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                        true
                ),
                Arguments.of(
                        List.of(new SimpleGrantedAuthority("ROLE_USER")),
                        false
                ),
                Arguments.of(
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")),
                        false
                ),
                Arguments.of(
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        ),
                        true
                ),
                Arguments.of(
                        List.of(
                                new SimpleGrantedAuthority("ROLE_USER"),
                                new SimpleGrantedAuthority("ROLE_MODERATOR")
                        ),
                        false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("adminRoleCases")
    void shouldReturnExpectedResultForRoles(
            List<GrantedAuthority> authorities,
            boolean expectedResult
    ) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "testuser",
                        "password",
                        authorities
                )
        );

        assertEquals(expectedResult, currentUserService.isAdmin());

        verifyNoInteractions(userRepository);
    }


    @Test
    void shouldReturnFalseWhenAuthenticationIsMissing() {
        assertFalse(currentUserService.isAdmin());

        verifyNoInteractions(userRepository);
    }

    @Test
    void shouldReturnFalseWhenAuthenticationIsAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "test-key",
                        "anonymousUser",
                        List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
                )
        );

        assertFalse(currentUserService.isAdmin());

        verifyNoInteractions(userRepository);
    }

    private void setAuthentication(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        "password",
                        List.of(new SimpleGrantedAuthority(role))
                )
        );
    }
}
