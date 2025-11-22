package com.tpi.solicitudes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para crear una nueva ruta")
public class RutaCreateDto {

    @NotBlank
    @Schema(description = "Nombre descriptivo de la ruta", example = "Ruta principal por Ruta 9")
    private String nombre;

    @Schema(description = "Descripción detallada de la ruta", example = "Ruta que utiliza Ruta 9 como eje principal")
    private String descripcion;
}
