package com.tpi.solicitudes.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response con resultado de validación de capacidad")
public record CamionValidacionResponse(
    @Schema(description = "True si el camión tiene capacidad suficiente", example = "true")
    Boolean valido,
    
    @Schema(description = "Mensaje descriptivo del resultado", example = "Camión tiene suficiente capacidad")
    String mensaje
) {
}
