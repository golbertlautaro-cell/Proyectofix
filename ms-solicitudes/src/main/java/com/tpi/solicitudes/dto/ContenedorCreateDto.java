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

    @Schema(description = "Estado del contenedor: EN_ORIGEN, EN_TRANSITO, EN_DEPOSITO, ENTREGADO, DISPONIBLE", example = "DISPONIBLE")
    private String estado;

    @Schema(description = "Peso real actual del contenedor en kilogramos", example = "1200.5")
    private Double pesoReal;

    @Schema(description = "Volumen real actual del contenedor en metros cúbicos", example = "12.3")
    private Double volumenReal;

    @Schema(description = "ID del depósito actual donde se encuentra el contenedor", example = "5")
    private Long depositoActualId;
}
