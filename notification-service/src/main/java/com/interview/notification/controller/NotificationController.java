package com.interview.notification.controller;

import com.interview.notification.dto.ErrorResponse;
import com.interview.notification.dto.NotificationResponse;
import com.interview.notification.dto.SendEmailRequest;
import com.interview.notification.service.EmailNotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Operations for sending user notifications")
public class NotificationController {

    private final EmailNotificationService emailNotificationService;

    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Send email notification", description = "Sends an email notification for a user operation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Email service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<NotificationResponse> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        emailNotificationService.sendUserNotification(request);
        return ResponseEntity.ok(new NotificationResponse("Email sent successfully"));
    }
}