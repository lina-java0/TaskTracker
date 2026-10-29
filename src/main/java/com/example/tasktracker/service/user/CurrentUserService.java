package com.example.tasktracker.service.user;

import com.example.tasktracker.entities.user.UserEntity;
import com.example.tasktracker.repository.user.UserRepository;
import com.example.tasktracker.exception.custom_exceptions.AuthenticatedUserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    private final UserRepository userRepository;

    public UserEntity getCurrentUser() {
        Authentication authentication = getAuthenticatedUser();

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(AuthenticatedUserNotFoundException::new);
    }

    public boolean isAdmin() {
        Authentication authentication = getAuthenticatedUserOrNull();

        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        ADMIN_ROLE.equals(authority.getAuthority()));
    }

    private Authentication getAuthenticatedUser() {
        Authentication authentication = getAuthenticatedUserOrNull();

        if (authentication == null) {
            throw new AuthenticatedUserNotFoundException();
        }

        return authentication;
    }

    private Authentication getAuthenticatedUserOrNull() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return authentication;
    }
}
