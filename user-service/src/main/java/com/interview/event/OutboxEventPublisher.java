package com.interview.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.event.UserNotificationEvent;
import com.interview.domain.entity.OutboxEvent;
import com.interview.domain.enums.OutboxEventStatus;
import com.interview.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, UserNotificationEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.publish-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(OutboxEventStatus.NEW, OutboxEventStatus.FAILED),
                PageRequest.of(0, 100)
        );

        for (OutboxEvent outboxEvent : events) {
            publish(outboxEvent);
        }
    }

    private void publish(OutboxEvent outboxEvent) {
        try {
            UserNotificationEvent event = objectMapper.readValue(outboxEvent.getPayload(), UserNotificationEvent.class);
            kafkaTemplate.send(outboxEvent.getTopic(), event.email(), event).get();

            outboxEvent.setStatus(OutboxEventStatus.SENT);
            outboxEvent.setSentAt(LocalDateTime.now());
            outboxEvent.setErrorMessage(null);

            log.info("Published {} outbox event {}", outboxEvent.getEventType(), outboxEvent.getEventId());
        } catch (JsonProcessingException exception) {
            markAsFailed(outboxEvent, "Failed to deserialize outbox payload");
            log.error("Failed to deserialize outbox payload for event {}", outboxEvent.getEventId(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            markAsFailed(outboxEvent, "Thread was interrupted while publishing outbox event");
            log.error("Thread was interrupted while publishing outbox event {}", outboxEvent.getEventId(), exception);
        } catch (ExecutionException exception) {
            String errorMessage = exception.getCause() != null
                    ? exception.getCause().getMessage()
                    : exception.getMessage();
            markAsFailed(outboxEvent, errorMessage);
            log.error("Kafka publish failed for outbox event {}", outboxEvent.getEventId(), exception);
        } catch (RuntimeException exception) {
            markAsFailed(outboxEvent, exception.getMessage());
            log.error("Unexpected error while publishing outbox event {}", outboxEvent.getEventId(), exception);
        }
    }

    private void markAsFailed(OutboxEvent outboxEvent, String errorMessage) {
        outboxEvent.setStatus(OutboxEventStatus.FAILED);
        outboxEvent.setErrorMessage(errorMessage);
    }
}
