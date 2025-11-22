package com.tpi.solicitudes.config;

import com.tpi.solicitudes.client.LogisticaClient;
import com.tpi.solicitudes.client.GoogleMapsClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración para crear beans de clientes que son opcionales
 */
@Configuration
public class ClientConfig {

    /**
     * Bean de LogisticaClient - puede ser null si no hay WebClient disponible
     */
    @Bean
    public LogisticaClient logisticaClient(@Qualifier("logisticaWebClient") WebClient logisticaWebClient) {
        return new LogisticaClient(logisticaWebClient);
    }

    /**
     * Bean de GoogleMapsClient - necesita WebClient.Builder
     */
    @Bean
    public GoogleMapsClient googleMapsClient(WebClient.Builder builder,
                                              @Value("${google.maps.base-url:https://maps.googleapis.com/maps/api/directions/json}") String baseUrl,
                                              @Value("${google.maps.api-key:}") String apiKey) {
        return new GoogleMapsClient(builder, baseUrl, apiKey);
    }
}
