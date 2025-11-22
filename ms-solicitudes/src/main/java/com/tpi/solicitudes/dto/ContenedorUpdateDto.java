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
@Schema(description = "DTO para actualizar un contenedor")
public class ContenedorUpdateDto {

    @Schema(description = "Descripción del contenedor", example = "Contenedor de 20 pies")
    private String descripcion;

    @Schema(description = "Tipo de contenedor", example = "DRY20")
    private String tipo;

    @Schema(description = "Capacidad máxima en kilogramos", example = "25000")
    private Double capacidadKg;

    @Schema(description = "Estado del contenedor", example = "DISPONIBLE")
    private String estado;
}
