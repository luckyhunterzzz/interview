package com.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.event.UserNotificationEvent;
import com.interview.domain.entity.OutboxEvent;
import com.interview.domain.enums.OutboxEventStatus;
import com.interview.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.user-events-topic}")
    private String userEventsTopic;

    public void saveCreatedUserEvent(String email) {
        save(UserNotificationEvent.created(email));
    }

    public void saveDeletedUserEvent(String email) {
        save(UserNotificationEvent.deleted(email));
    }

    private void save(UserNotificationEvent event) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .eventId(event.eventId())
                    .topic(userEventsTopic)
                    .eventType(event.operation().name())
                    .payload(objectMapper.writeValueAsString(event))
                    .status(OutboxEventStatus.NEW)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize outbox event", exception);
        }
    }
}
