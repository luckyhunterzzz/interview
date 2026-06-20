package com.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request for updating a user")
public record UpdateUserRequest(
        @Schema(description = "User name", example = "Ivan Ivanov")
        @NotBlank(message = "Name must not be empty")
        String name,
        @Schema(description = "User email", example = "ivan.ivanov@example.com")
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email has invalid format")
        String email,
        @Schema(description = "User age", example = "31")
        @NotNull(message = "Age must be between 0 and 150")
        @Min(value = 0, message = "Age must be between 0 and 150")
        @Max(value = 150, message = "Age must be between 0 and 150")
        Integer age
) {
}