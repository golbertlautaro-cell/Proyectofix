package com.tpi.solicitudes.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request para validar capacidad de un camión")
public record CamionValidacionRequest(
    @Schema(description = "Dominio/patente del camión", example = "ABC123")
    String dominio,
    
    @Schema(description = "Peso del contenedor en kg", example = "5000")
    Double pesoContenedor,
    
    @Schema(description = "Volumen del contenedor en m³", example = "20")
    Double volumenContenedor
) {
}
