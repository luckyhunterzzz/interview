package com.interview.event;

import com.interview.common.event.UserNotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserNotificationEvent> kafkaTemplate;

    @Value("${app.kafka.user-events-topic}")
    private String userEventsTopic;

    public void publishCreated(String email) {
        UserNotificationEvent event = UserNotificationEvent.created(email);
        kafkaTemplate.send(userEventsTopic, email, event);
        log.info("Published CREATED event for {}", email);
    }

    public void publishDeleted(String email) {
        UserNotificationEvent event = UserNotificationEvent.deleted(email);
        kafkaTemplate.send(userEventsTopic, email, event);
        log.info("Published DELETED event for {}", email);
    }
}
