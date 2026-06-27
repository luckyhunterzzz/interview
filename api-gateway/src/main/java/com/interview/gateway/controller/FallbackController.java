package com.interview.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping(path = "/fallback/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> notificationsFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message", "Notification service is temporarily unavailable",
                        "service", "notification-service"
                ));
    }
}
