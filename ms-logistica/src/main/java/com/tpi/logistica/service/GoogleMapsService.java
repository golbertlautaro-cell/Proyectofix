package com.tpi.logistica.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpi.logistica.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Servicio para consumir la API de Google Maps.
 * Proporciona funcionalidades para calcular distancias y duraciones entre puntos.
 */
@Slf4j
@Service
public class GoogleMapsService {

    private final RestClient googleMapsRestClient;
    
    @Value("${google.maps.api-key}")
    private String apiKey;

    /**
     * Constructor que inyecta el RestClient configurado.
     * 
     * @param googleMapsRestClient RestClient configurado con la URL base de Google Maps
     */
    public GoogleMapsService(@Qualifier("googleMapsRestClient") RestClient googleMapsRestClient) {
        this.googleMapsRestClient = googleMapsRestClient;
    }

    /**
     * Calcula la distancia y duración entre un punto de origen y uno de destino.
     * 
     * @param origen Dirección o coordenadas del punto de origen
     * @param destino Dirección o coordenadas del punto de destino
     * @return DistanciaDTO con información de kilómetros y duración del viaje
     * @throws RuntimeException Si la API de Google Maps devuelve un error
     */
    public DistanciaDTO calcularDistancia(String origen, String destino) {
        log.info("Calculando distancia desde '{}' hasta '{}'", origen, destino);
        
        try {
            // Realizar la llamada a Google Maps Distance Matrix API
            GoogleDistanceResponse response = googleMapsRestClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/distancematrix/json")
                            .queryParam("origins", origen)
                            .queryParam("destinations", destino)
                            .queryParam("units", "metric")
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .toEntity(GoogleDistanceResponse.class)
                    .getBody();

            // Validar que la respuesta sea válida
            if (response == null) {
                log.error("Respuesta nula de Google Maps");
                throw new RuntimeException("Google Maps devolvió una respuesta nula");
            }

            // Validar que el estado sea exitoso
            if (!"OK".equals(response.getStatus())) {
                log.error("Error en la respuesta de Google Maps. Status: {}, Error: {}", 
                         response.getStatus(), response.getErrorMessage());
                throw new RuntimeException("Google Maps API error: " + response.getStatus());
            }

            // Validar que existan filas de resultados
            if (response.getRows() == null || response.getRows().isEmpty()) {
                log.error("No hay filas de resultados en la respuesta de Google Maps");
                throw new RuntimeException("Google Maps devolvió sin resultados");
            }

            // Extraer la primera fila (corresponde al primer origen)
            GoogleDistanceRow row = response.getRows().get(0);
            
            // Validar que existan elementos en la fila
            if (row.getElements() == null || row.getElements().isEmpty()) {
                log.error("No hay elementos en la primera fila de Google Maps");
                throw new RuntimeException("Google Maps devolvió sin elementos");
            }

            // Extraer el primer elemento (corresponde al primer destino)
            GoogleDistanceElement element = row.getElements().get(0);

            // Validar que el elemento sea válido
            if (!"OK".equals(element.getStatus())) {
                log.error("Error al procesar el elemento. Status: {}", element.getStatus());
                throw new RuntimeException("Google Maps element error: " + element.getStatus());
            }

            // Validar que la distancia y duración existan
            if (element.getDistance() == null || element.getDuration() == null) {
                log.error("Distancia o duración nula en la respuesta de Google Maps");
                throw new RuntimeException("Google Maps devolvió distancia o duración nula");
            }

            // Extraer distancia en metros y convertir a kilómetros
            double distanceMetros = element.getDistance().getValue();
            double kilometros = distanceMetros / 1000.0;

            // Extraer duración en texto
            String duracionTexto = element.getDuration().getText();

            // Crear y retornar el DTO
            DistanciaDTO distancia = new DistanciaDTO();
            distancia.setOrigen(origen);
            distancia.setDestino(destino);
            distancia.setKilometros(kilometros);
            distancia.setDuracionTexto(duracionTexto);

            log.info("Distancia calculada: {} km, Duración: {}", kilometros, duracionTexto);

            return distancia;

        } catch (RuntimeException e) {
            log.error("Error de negocio al calcular distancia desde '{}' hasta '{}'", 
                     origen, destino, e);
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al calcular distancia desde '{}' hasta '{}'", 
                     origen, destino, e);
            throw new RuntimeException("Error al calcular distancia: " + e.getMessage(), e);
        }
    }
}
