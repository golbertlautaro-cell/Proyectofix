package com.tpi.logistica.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuración de RestClient para integraciones con APIs externas.
 * Define beans de RestClient reutilizables en toda la aplicación.
 */
@Configuration
public class RestClientConfig {

    /**
     * Crea un RestClient configurado para consumir la API de Google Maps.
     * 
     * @param googleMapsBaseUrl URL base de Google Maps inyectada desde application.yml
     * @return RestClient configurado con la URL base de Google Maps
     */
    @Bean
    public RestClient googleMapsRestClient(@Value("${google.maps.base-url}") String googleMapsBaseUrl) {
        return RestClient.builder()
                .baseUrl(googleMapsBaseUrl)
                .build();
    }
}
