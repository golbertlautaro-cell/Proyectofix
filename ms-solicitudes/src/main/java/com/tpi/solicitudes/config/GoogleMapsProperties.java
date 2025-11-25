package com.tpi.solicitudes.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades de configuración para Google Maps
 */
@Configuration
@ConfigurationProperties(prefix = "google.maps")
@Data
public class GoogleMapsProperties {
    private String baseUrl;
    private String apiKey;
}

