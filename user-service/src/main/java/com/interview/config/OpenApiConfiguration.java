package com.interview.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API")
                        .description("REST API for managing users with HATEOAS navigation links")
                        .version("v1")
                        .contact(new Contact().name("Interview project")));
    }
}