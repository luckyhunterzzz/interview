package com.interview.common.event;

import java.time.Instant;
import java.util.UUID;

public record UserNotificationEvent(
        UUID eventId,
        UserOperation operation,
        String email,
        Instant occurredAt
) {
    public static UserNotificationEvent created(String email) {
        return new UserNotificationEvent(UUID.randomUUID(), UserOperation.CREATED, email, Instant.now());
    }

    public static UserNotificationEvent deleted(String email) {
        return new UserNotificationEvent(UUID.randomUUID(), UserOperation.DELETED, email, Instant.now());
    }
}
