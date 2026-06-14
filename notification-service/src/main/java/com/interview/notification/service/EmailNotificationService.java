package com.interview.notification.service;

import com.interview.common.event.UserNotificationEvent;
import com.interview.common.event.UserOperation;
import com.interview.notification.dto.SendEmailRequest;
import com.interview.notification.exception.NotificationDeliveryException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String fromAddress;

    public void sendUserNotification(UserNotificationEvent event) {
        send(event.email(), event.operation());
    }

    public void sendUserNotification(SendEmailRequest request) {
        send(request.email(), request.operation());
    }

    private void send(String email, UserOperation operation) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(email);
        message.setSubject(resolveSubject(operation));
        message.setText(resolveBody(operation));

        try {
            javaMailSender.send(message);
        } catch (MailException exception) {
            throw new NotificationDeliveryException("Failed to send email", exception);
        }
    }

    private String resolveSubject(UserOperation operation) {
        return switch (operation) {
            case CREATED -> "Account created";
            case DELETED -> "Account deleted";
        };
    }

    private String resolveBody(UserOperation operation) {
        return switch (operation) {
            case CREATED ->
                    "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.";
            case DELETED ->
                    "Здравствуйте! Ваш аккаунт был удалён.";
        };
    }
}
