package com.interview.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Notification API response payload")
public record NotificationResponse(
        @Schema(description = "Operation result message", example = "Email sent successfully")
        String message
) {
}