package com.interview.notification.config;

import com.interview.common.event.UserNotificationEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class NotificationKafkaConfiguration {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserNotificationEvent> manualAckKafkaListenerContainerFactory(
            ConsumerFactory<String, UserNotificationEvent> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, UserNotificationEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }
}
