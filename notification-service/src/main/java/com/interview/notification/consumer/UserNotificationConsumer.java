package com.interview.notification.consumer;

import com.interview.common.event.UserNotificationEvent;
import com.interview.notification.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationConsumer {

    private final EmailNotificationService emailNotificationService;

    @KafkaListener(topics = "${app.kafka.user-events-topic}")
    public void consume(UserNotificationEvent event) {
        emailNotificationService.sendUserNotification(event);
        log.info("Processed {} event for {}", event.operation(), event.email());
    }
}
