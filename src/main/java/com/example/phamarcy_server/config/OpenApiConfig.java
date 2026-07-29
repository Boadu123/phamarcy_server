package com.example.phamarcy_server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pharmacyOpenApi() {
        return new OpenAPI()
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Local Docker or local Spring Boot server")))
                .info(new Info()
                        .title("Pharmacy Synchronization and Monitoring API")
                        .description("Offline-first desktop synchronization plus frontend-ready pharmacy dashboard, details, inventory, sales, and activity APIs. Use only versioned /api/v1 endpoints.")
                        .version("v1")
                        .contact(new Contact().name("Pharmacy Server Team")));
    }
}
