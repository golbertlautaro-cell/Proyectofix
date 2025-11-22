package com.tpi.solicitudes.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de respuesta para un contenedor disponible")
public record ContenedorResponse(
    @Schema(description = "ID único del contenedor", example = "1")
    Long idContenedor,
    
    @Schema(description = "Tipo de contenedor", example = "Refrigerado")
    String tipo,
    
    @Schema(description = "Capacidad de peso en kg", example = "15000.0")
    Double capacidadPeso,
    
    @Schema(description = "Capacidad de volumen en m³", example = "30.0")
    Double capacidadVolumen
) {
}
