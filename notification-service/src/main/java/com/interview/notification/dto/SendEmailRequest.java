package com.interview.notification.dto;

import com.interview.common.event.UserOperation;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SendEmailRequest(
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email has invalid format")
        String email,
        @NotNull(message = "Operation must not be null")
        UserOperation operation
) {
}
