package com.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Error response payload")
public record ErrorResponse(
        @Schema(description = "Timestamp when the error occurred", example = "2024-01-10T12:00:00")
        LocalDateTime timestamp,
        @Schema(description = "HTTP status code", example = "400")
        int status,
        @Schema(description = "HTTP status reason", example = "Bad Request")
        String error,
        @Schema(description = "Validation or business error messages")
        List<String> messages
) {
}