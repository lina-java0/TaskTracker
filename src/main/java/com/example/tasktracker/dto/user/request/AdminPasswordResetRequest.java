package com.example.tasktracker.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminPasswordResetRequest {

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
    private String newPassword;
}
