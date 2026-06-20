package com.interview.notification.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI notificationServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .description("REST API for sending user notification emails")
                        .version("v1")
                        .contact(new Contact().name("Interview project")));
    }
}