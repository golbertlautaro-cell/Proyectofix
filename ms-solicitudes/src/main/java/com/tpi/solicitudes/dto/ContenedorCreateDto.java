package com.tpi.solicitudes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para crear un nuevo contenedor")
public class ContenedorCreateDto {

    @NotBlank
    @Schema(description = "Descripción del contenedor", example = "Contenedor de 20 pies")
    private String descripcion;

    @NotBlank
    @Schema(description = "Tipo de contenedor", example = "DRY20")
    private String tipo;

    @NotNull
    @Schema(description = "Capacidad máxima en kilogramos", example = "25000")
    private Double capacidadKg;
}
