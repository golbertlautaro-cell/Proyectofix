package com.tpi.logistica.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO que representa una fila de resultados en la respuesta de Google Distance Matrix API.
 * Una fila contiene los elementos (resultados) para un origen específico.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleDistanceRow {

    /**
     * Lista de elementos (resultados para cada destino del origen)
     */
    @JsonProperty("elements")
    private List<GoogleDistanceElement> elements;
}
