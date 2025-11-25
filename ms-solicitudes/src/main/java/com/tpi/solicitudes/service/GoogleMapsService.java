package com.tpi.solicitudes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpi.solicitudes.dto.GoogleDistanceResponse;
import com.tpi.solicitudes.dto.GoogleDistanceRow;
import com.tpi.solicitudes.dto.GoogleDistanceElement;
import com.tpi.solicitudes.dto.DistanciaDTO;
import com.tpi.solicitudes.config.GoogleMapsProperties;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

/**
 * Servicio para consumir la API de Google Maps.
 * Proporciona funcionalidades para calcular distancias y duraciones entre puntos.
 */
@Slf4j
@Service
public class GoogleMapsService {

    private final Optional<WebClient> googleMapsWebClient;

    private final GoogleMapsProperties googleMapsProperties;

    public GoogleMapsService(@Qualifier("googleMapsWebClient") WebClient googleMapsWebClient, GoogleMapsProperties googleMapsProperties) {
        this.googleMapsWebClient = Optional.ofNullable(googleMapsWebClient);
        this.googleMapsProperties = googleMapsProperties;
    }

    @PostConstruct
    public void init() {
        String apiKey = googleMapsProperties != null ? googleMapsProperties.getApiKey() : null;
        log.info("GoogleMapsService (ms-solicitudes) inicializado con API Key: {}", apiKey != null && !apiKey.isEmpty() ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "VACÍA");
    }

    /**
     * Calcula la distancia y duración entre dos ubicaciones usando Google Distance Matrix API.
     */
    public DistanciaDTO calcularDistancia(String origen, String destino) {
        log.info("Calculando distancia desde '{}' hasta '{}'", origen, destino);

        if (googleMapsWebClient.isEmpty()) {
            log.warn("GoogleMapsWebClient no disponible, retornando valores por defecto");
            DistanciaDTO distancia = new DistanciaDTO();
            distancia.setOrigen(origen);
            distancia.setDestino(destino);
            distancia.setKilometros(100.0);  // valor por defecto
            distancia.setDuracionTexto("2 horas aprox.");
            return distancia;
        }

        try {
            String apiKey = googleMapsProperties != null ? googleMapsProperties.getApiKey() : null;
            log.debug("Usando API Key: {}", apiKey != null && !apiKey.isEmpty() ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..." : "VACÍA");

            GoogleDistanceResponse response = googleMapsWebClient.get()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/distancematrix/json")
                            .queryParam("origins", origen)
                            .queryParam("destinations", destino)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(GoogleDistanceResponse.class)
                    .block();

            if (response == null) {
                log.error("Respuesta nula de Google Maps");
                throw new RuntimeException("Google Maps devolvió una respuesta nula");
            }

            if (!"OK".equals(response.getStatus())) {
                log.error("Error en la respuesta de Google Maps. Status: {}, Error: {}", response.getStatus(), response.getErrorMessage());
                throw new RuntimeException("Google Maps API error: " + response.getStatus());
            }

            if (response.getRows() == null || response.getRows().isEmpty()) {
                log.error("No hay filas de resultados en la respuesta de Google Maps");
                throw new RuntimeException("Google Maps devolvió sin resultados");
            }

            GoogleDistanceRow row = response.getRows().get(0);

            if (row.getElements() == null || row.getElements().isEmpty()) {
                log.error("No hay elementos en la primera fila de Google Maps");
                throw new RuntimeException("Google Maps devolvió sin elementos");
            }

            GoogleDistanceElement element = row.getElements().get(0);

            if (!"OK".equals(element.getStatus())) {
                log.error("Error al procesar el elemento. Status: {}", element.getStatus());
                throw new RuntimeException("Google Maps element error: " + element.getStatus());
            }

            if (element.getDistance() == null || element.getDuration() == null) {
                log.error("Distancia o duración nula en la respuesta de Google Maps");
                throw new RuntimeException("Google Maps devolvió distancia o duración nula");
            }

            double distanceMetros = element.getDistance().getValue();
            double kilometros = distanceMetros / 1000.0;
            String duracionTexto = element.getDuration().getText();

            DistanciaDTO distancia = new DistanciaDTO();
            distancia.setOrigen(origen);
            distancia.setDestino(destino);
            distancia.setKilometros(kilometros);
            distancia.setDuracionTexto(duracionTexto);

            log.info("Distancia calculada: {} km, Duración: {}", kilometros, duracionTexto);

            return distancia;

        } catch (RuntimeException e) {
            log.error("Error de negocio al calcular distancia desde '{}' hasta '{}'", origen, destino, e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al calcular distancia desde '{}' hasta '{}'", origen, destino, e);
            throw new RuntimeException("Error al calcular distancia: " + e.getMessage(), e);
        }
    }
}
