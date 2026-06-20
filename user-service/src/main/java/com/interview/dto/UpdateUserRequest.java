package com.interview.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRequest(
        @NotBlank(message = "Name must not be empty")
        String name,
        @NotBlank(message = "Email must not be empty")
        @Email(message = "Email has invalid format")
        String email,
        @NotNull(message = "Age must be between 0 and 150")
        @Min(value = 0, message = "Age must be between 0 and 150")
        @Max(value = 150, message = "Age must be between 0 and 150")
        Integer age
) {
}
