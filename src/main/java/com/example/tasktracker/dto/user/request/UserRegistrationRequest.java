package com.example.tasktracker.dto.user.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class UserRegistrationRequest {

    @NotBlank(message = "{validation.user.name.is-required}")
    @Size(
            min = 3,
            max = 50,
            message = "{validation.user.name.length}")
    private String name;

    @NotBlank(message = "{validation.user.username.is-required}")
    @Size(
            min = 3,
            max = 50,
            message = "{validation.user.username.length}")
    @Pattern(
            regexp = "^[A-Za-z0-9._]+$",
            message = "{validation.user.username.pattern}"
    )
    private String username;

    @NotBlank(message = "{validation.user.password.is-required}")
    @Size(
            min = 8,
            max = 72,
            message = "{validation.user.password.length}")
    @Pattern(
            regexp = """
                    ^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-]).*$""",
            message = "{validation.user.password.pattern}"
    )
    private String password;
}
