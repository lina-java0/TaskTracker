package com.example.tasktracker.service.user;

import com.example.tasktracker.dto.user.request.UserRegistrationRequest;
import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.exception.custom_exceptions.UsernameAlreadyExistsException;
import com.example.tasktracker.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private static final String USERNAME_UNIQUE_CONSTRAINT = "uk_users_username";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(UserRegistrationRequest request) {
        UserEntity user = new UserEntity(
                request.getName(),
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                "ROLE_USER"
        );

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            if (isUsernameUniqueConstraintViolation(ex)) {
                throw new UsernameAlreadyExistsException(ex);
            }

            throw ex;
        }
    }

    private boolean isUsernameUniqueConstraintViolation(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof ConstraintViolationException ex) {
                return USERNAME_UNIQUE_CONSTRAINT.equals(ex.getConstraintName());
            }

            current = current.getCause();
        }

        return false;
    }
}