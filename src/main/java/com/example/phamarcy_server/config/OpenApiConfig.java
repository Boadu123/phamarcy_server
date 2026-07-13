package com.example.phamarcy_server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pharmacyOpenApi() {
        String pharmacyTokenScheme = "pharmacyToken";

        return new OpenAPI()
                .info(new Info()
                        .title("Pharmacy Management Sync API")
                        .description("Offline-first synchronization and branch reporting APIs for the pharmacy server.")
                        .version("v1")
                        .contact(new Contact().name("Pharmacy Server Team")))
                .addSecurityItem(new SecurityRequirement().addList(pharmacyTokenScheme))
                .components(new Components()
                        .addSecuritySchemes(pharmacyTokenScheme, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Pharmacy-Token")
                                .description("Branch API token required for synchronization requests.")));
    }
}