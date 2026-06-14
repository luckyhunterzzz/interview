package com.interview.notification.controller;

import com.interview.notification.dto.NotificationResponse;
import com.interview.notification.dto.SendEmailRequest;
import com.interview.notification.service.EmailNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailNotificationService emailNotificationService;

    @PostMapping("/send")
    public ResponseEntity<NotificationResponse> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        emailNotificationService.sendUserNotification(request);
        return ResponseEntity.ok(new NotificationResponse("Email sent successfully"));
    }
}
