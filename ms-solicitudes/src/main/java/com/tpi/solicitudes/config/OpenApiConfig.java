package com.tpi.solicitudes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI solicitudesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Solicitudes API")
                        .description("Microservicio de gestión de solicitudes, tramos y clientes")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("TPI Logística")
                                .email("contacto@tpi-logistica.com")));
    }
}
