package com.tpi.solicitudes.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa un valor de distancia o duración en la respuesta de Google Distance Matrix API.
 * Contiene tanto el valor numérico como su representación en texto.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDistanceValue {

    /**
     * Valor numérico en metros (para distancia) o segundos (para duración)
     */
    @JsonProperty("value")
    private Long value;

    /**
     * Representación en texto legible del valor (ej: "2.5 km", "30 mins")
     */
    @JsonProperty("text")
    private String text;
}
