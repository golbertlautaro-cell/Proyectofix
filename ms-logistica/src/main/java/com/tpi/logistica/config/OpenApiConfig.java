package com.tpi.logistica.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI logisticaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MS Logística API")
                        .description("Microservicio de gestión de camiones y depósitos")
                        .version("v1.0")
                        .contact(new Contact()
                                .name("TPI Logística")
                                .email("contacto@tpi-logistica.com")));
    }
}
