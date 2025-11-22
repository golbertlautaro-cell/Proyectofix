package com.tpi.solicitudes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO para crear un nuevo tramo")
public class TramoCreateDto {

    @NotBlank
    @Schema(description = "Punto de origen", example = "Buenos Aires")
    private String origen;

    @NotBlank
    @Schema(description = "Punto de destino", example = "Rosario")
    private String destino;

    @Pattern(regexp = "^([A-Z]{3}[0-9]{3}|[A-Z]{2}[0-9]{3}[A-Z]{2})$", message = "Formato de dominio inválido")
    @Schema(description = "Dominio del camión", example = "ABC123")
    private String dominioCamion;

    @Schema(description = "Fecha y hora de inicio estimada")
    private LocalDateTime fechaHoraInicioEstimada;

    @Schema(description = "Fecha y hora de fin estimada")
    private LocalDateTime fechaHoraFinEstimada;

    @Schema(description = "Días estimados de estadía en depósito", example = "1.5")
    private Double diasEstimadosDeposito;

    @Schema(description = "Identificador del depósito asociado al tramo", example = "3")
    private Long depositoId;
}
