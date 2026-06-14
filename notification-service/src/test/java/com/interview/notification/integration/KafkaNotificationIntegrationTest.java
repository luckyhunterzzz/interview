package com.interview.notification.integration;

import com.interview.common.event.UserNotificationEvent;
import com.interview.common.event.UserOperation;
import jakarta.mail.Message;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = "spring.kafka.listener.auto-startup=true")
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = "user-events", bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class KafkaNotificationIntegrationTest extends AbstractNotificationIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void kafkaConsumerShouldSendDeletedEmail() throws Exception {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        DefaultKafkaProducerFactory<String, UserNotificationEvent> producerFactory =
                new DefaultKafkaProducerFactory<>(producerProps);

        try {
            KafkaTemplate<String, UserNotificationEvent> kafkaTemplate = new KafkaTemplate<>(producerFactory);
            kafkaTemplate.send(
                    "user-events",
                    "ivan@example.com",
                    new UserNotificationEvent(UUID.randomUUID(), UserOperation.DELETED, "ivan@example.com", Instant.now())
            );
            kafkaTemplate.flush();
        } finally {
            producerFactory.destroy();
        }

        assertTrue(GREEN_MAIL.waitForIncomingEmail(5_000, 1));
        Message[] messages = GREEN_MAIL.getReceivedMessages();

        assertEquals(1, messages.length);
        assertEquals("ivan@example.com", messages[0].getAllRecipients()[0].toString());
        assertEquals("Account deleted", messages[0].getSubject());
        assertEquals(
                "Здравствуйте! Ваш аккаунт был удалён.",
                messages[0].getContent().toString().trim()
        );
    }
}
