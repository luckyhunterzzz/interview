package com.interview.notification.integration;

import jakarta.mail.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationApiIntegrationTest extends AbstractNotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendEndpointShouldSendCreatedEmail() throws Exception {
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "ivan@example.com",
                                  "operation": "CREATED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email sent successfully"));

        assertTrue(GREEN_MAIL.waitForIncomingEmail(5_000, 1));
        Message[] messages = GREEN_MAIL.getReceivedMessages();

        assertEquals(1, messages.length);
        assertEquals("ivan@example.com", messages[0].getAllRecipients()[0].toString());
        assertEquals("Account created", messages[0].getSubject());
        assertEquals(
                "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.",
                messages[0].getContent().toString().trim()
        );
    }
}
