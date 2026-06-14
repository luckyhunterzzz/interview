package com.interview.notification.integration;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

abstract class AbstractNotificationIntegrationTest {

    protected static final GreenMail GREEN_MAIL = new GreenMail(ServerSetupTest.SMTP);

    @BeforeAll
    static void beforeAll() {
        GREEN_MAIL.start();
    }

    @AfterEach
    void afterEach() throws Exception {
        GREEN_MAIL.purgeEmailFromAllMailboxes();
    }

    @AfterAll
    static void afterAll() {
        GREEN_MAIL.stop();
    }

    @DynamicPropertySource
    static void registerMailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> GREEN_MAIL.getSmtp().getPort());
        registry.add("app.mail.from", () -> "test@your-site.local");
    }
}
