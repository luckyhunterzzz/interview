package com.interview.notification.dto;

import com.interview.common.event.UserOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request for sending an email notification")
public record SendEmailRequest(
        @Schema(description = "Recipient email", example = "ivan@example.com")
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email has invalid format")
        String email,
        @Schema(description = "User operation that triggered the notification", example = "CREATED")
        @NotNull(message = "Operation must not be null")
        UserOperation operation
) {
}