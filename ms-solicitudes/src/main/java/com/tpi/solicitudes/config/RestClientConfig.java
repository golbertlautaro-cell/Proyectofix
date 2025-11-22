package com.tpi.solicitudes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración de HTTP clients para comunicación entre microservicios
 * Usa WebClient reactivo para máxima compatibilidad con Spring Boot 3.3.5
 */
@Configuration
public class RestClientConfig {

    @Value("${services.logistica.url:http://localhost:8081}")
    private String logisticaUrl;

    @Value("${google.maps.base-url:https://maps.googleapis.com/maps/api}")
    private String googleMapsBaseUrl;

    /**
     * WebClient para ms-logistica
     */
    @Bean(name = "logisticaWebClient")
    public WebClient logisticaWebClient() {
        try {
            return WebClient.create(logisticaUrl);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * WebClient para Google Maps Distance Matrix API
     */
    @Bean(name = "googleMapsWebClient")
    public WebClient googleMapsWebClient() {
        try {
            return WebClient.create(googleMapsBaseUrl);
        } catch (Exception e) {
            return null;
        }
    }
}
