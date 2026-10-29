
package com.example.tasktracker.unit_tests.service.user;

import com.example.tasktracker.dto.user.request.UserRegistrationRequest;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.exception.custom_exceptions.UsernameAlreadyExistsException;
import com.example.tasktracker.repository.user.UserRepository;
import com.example.tasktracker.service.user.UserRegistrationService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    @Test
    void shouldRegisterUserWithEncodedPassword() {
        UserRegistrationRequest request = createRequest();

        when(passwordEncoder.encode("Password123!"))
                .thenReturn("encodedPassword");

        userRegistrationService.register(request);

        ArgumentCaptor<UserEntity> captor =
                ArgumentCaptor.forClass(UserEntity.class);

        verify(passwordEncoder).encode("Password123!");
        verify(userRepository).saveAndFlush(captor.capture());

        UserEntity savedUser = captor.getValue();

        assertEquals("Test User", savedUser.getName());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("ROLE_USER", savedUser.getRole());
    }

    @Test
    void shouldThrowUsernameAlreadyExistsExceptionWhenUsernameIsDuplicate() {
        UserRegistrationRequest request = createRequest();

        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");

        ConstraintViolationException constraintException =
                new ConstraintViolationException(
                        "Duplicate username",
                        null,
                        "uk_users_username"
                );

        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Duplicate username",
                        constraintException
                ));

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userRegistrationService.register(request)
        );
    }

    @Test
    void shouldRethrowDataIntegrityViolationForAnotherConstraint() {
        UserRegistrationRequest request = createRequest();

        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");

        ConstraintViolationException constraintException =
                new ConstraintViolationException(
                        "Another constraint violation",
                        null,
                        "uk_other_constraint"
                );

        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Database constraint violation",
                        constraintException
                ));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRegistrationService.register(request)
        );
    }

    @Test
    void shouldRethrowDataIntegrityViolationWithoutHibernateConstraint() {
        UserRegistrationRequest request = createRequest();

        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");

        when(userRepository.saveAndFlush(any(UserEntity.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "Database integrity violation"
                ));

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userRegistrationService.register(request)
        );
    }

    private UserRegistrationRequest createRequest() {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setName("Test User");
        request.setUsername("testuser");
        request.setPassword("Password123!");
        return request;
    }
}
