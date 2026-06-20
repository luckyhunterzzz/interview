package com.interview.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(itemRelation = "user", collectionRelation = "users")
@Schema(description = "User response payload")
public record UserResponse(
        @Schema(description = "User identifier", example = "1")
        Long id,
        @Schema(description = "User name", example = "Ivan Ivanov")
        String name,
        @Schema(description = "User email", example = "ivan@example.com")
        String email,
        @Schema(description = "User age", example = "30")
        Integer age,
        @Schema(description = "Timestamp when the user was created", example = "2024-01-10T12:00:00")
        LocalDateTime createdAt
) {
}