package com.tpi.solicitudes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para actualizar una ruta")
public class RutaUpdateDto {

    @Schema(description = "Estado de la ruta", example = "PENDIENTE")
    private String estado;

    @Schema(description = "Distancia total en kilómetros", example = "150.5")
    private Double distanciaTotalKm;

    @Schema(description = "Duración estimada en horas", example = "2.5")
    private Double duracionEstimadaHoras;

    @Schema(description = "Costo estimado", example = "1500.00")
    private Double costoEstimado;

    @Schema(description = "Costo real", example = "1550.00")
    private Double costoReal;
}
