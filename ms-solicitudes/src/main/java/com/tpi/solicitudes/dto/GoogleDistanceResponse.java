package com.tpi.solicitudes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO que representa la respuesta completa de la Google Distance Matrix API.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDistanceResponse {

    /**
     * Estado de la respuesta (OK, ZERO_RESULTS, INVALID_REQUEST, OVER_QUERY_LIMIT, etc.)
     */
    @JsonProperty("status")
    private String status;

    /**
     * Lista de orígenes procesados
     */
    @JsonProperty("origin_addresses")
    private List<String> originAddresses;

    /**
     * Lista de destinos procesados
     */
    @JsonProperty("destination_addresses")
    private List<String> destinationAddresses;

    /**
     * Filas de resultados (una por cada origen)
     */
    @JsonProperty("rows")
    private List<GoogleDistanceRow> rows;

    /**
     * Mensaje de error (si aplica)
     */
    @JsonProperty("error_message")
    private String errorMessage;
}
