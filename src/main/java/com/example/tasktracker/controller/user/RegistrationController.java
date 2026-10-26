package com.example.tasktracker.controller.user;

import com.example.tasktracker.dto.user.request.UserRegistrationRequest;
import com.example.tasktracker.service.user.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRegistrationService userRegistrationService;

    @PostMapping
    public ResponseEntity<Void> register(
            @Valid @RequestBody UserRegistrationRequest request
    ) {
        userRegistrationService.register(request);

        return ResponseEntity.status(201).build();
    }
}
