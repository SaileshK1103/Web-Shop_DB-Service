package com.webshop.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Web Shop DB Service API",
                version = "v1",
                description = "REST API for managing the web shop catalog, customers, suppliers, and orders."
        )
)
public class OpenApiConfig {
}