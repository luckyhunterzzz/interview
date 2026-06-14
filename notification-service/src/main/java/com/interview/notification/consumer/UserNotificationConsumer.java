package com.interview.notification.consumer;

import com.interview.common.event.UserNotificationEvent;
import com.interview.notification.domain.entity.ProcessedEvent;
import com.interview.notification.domain.enums.ProcessedEventStatus;
import com.interview.notification.repository.ProcessedEventRepository;
import com.interview.notification.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationConsumer {

    private final EmailNotificationService emailNotificationService;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    @KafkaListener(
            topics = "${app.kafka.user-events-topic}",
            containerFactory = "manualAckKafkaListenerContainerFactory"
    )
    public void consume(UserNotificationEvent event, Acknowledgment acknowledgment) {
        ProcessedEvent processedEvent = processedEventRepository.findById(event.eventId())
                .orElse(null);

        if (processedEvent != null && processedEvent.getStatus() == ProcessedEventStatus.COMPLETED) {
            acknowledgment.acknowledge();
            log.info("Skipped duplicate event {}", event.eventId());
            return;
        }

        if (processedEvent == null) {
            processedEvent = ProcessedEvent.builder()
                    .eventId(event.eventId())
                    .email(event.email())
                    .operation(event.operation().name())
                    .status(ProcessedEventStatus.PROCESSING)
                    .build();
        } else {
            processedEvent.setStatus(ProcessedEventStatus.PROCESSING);
            processedEvent.setErrorMessage(null);
        }

        processedEventRepository.save(processedEvent);

        try {
            emailNotificationService.sendUserNotification(event);

            processedEvent.setStatus(ProcessedEventStatus.COMPLETED);
            processedEvent.setProcessedAt(LocalDateTime.now());
            processedEventRepository.save(processedEvent);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    acknowledgment.acknowledge();
                }
            });

            log.info("Processed {} event for {}", event.operation(), event.email());
        } catch (RuntimeException exception) {
            processedEvent.setStatus(ProcessedEventStatus.FAILED);
            processedEvent.setErrorMessage(exception.getMessage());
            processedEventRepository.save(processedEvent);
            throw exception;
        }
    }
}
