package com.tpi.logistica.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un elemento (par origen-destino) en la respuesta de Google Distance Matrix API.
 * Contiene distancia, duración y estado del cálculo.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDistanceElement {

    /**
     * Estado del cálculo (OK, NOT_FOUND, ZERO_RESULTS, MAX_ROUTE_LENGTH_EXCEEDED)
     */
    @JsonProperty("status")
    private String status;

    /**
     * Información de distancia
     */
    @JsonProperty("distance")
    private GoogleDistanceValue distance;

    /**
     * Información de duración
     */
    @JsonProperty("duration")
    private GoogleDistanceValue duration;

    /**
     * Información de duración en tráfico (tráfico en tiempo real)
     */
    @JsonProperty("duration_in_traffic")
    private GoogleDistanceValue durationInTraffic;
}
