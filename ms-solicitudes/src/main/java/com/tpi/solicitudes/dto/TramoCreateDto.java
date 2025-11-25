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

    // Reemplazo de depositoId: referencias a depósitos de origen/destino (opcionales)
    @Schema(description = "ID de depósito de origen (opcional si se usa direccion libre)", example = "3")
    private Long origenDepositoId;

    @Schema(description = "ID de depósito de destino (opcional si se usa direccion libre)", example = "5")
    private Long destinoDepositoId;

    // Direcciones libres (si no se usan depósitos)
    @Schema(description = "Dirección libre de origen (opcional)", example = "Calle Falsa 123")
    private String origenDireccionLibre;

    @Schema(description = "Dirección libre de destino (opcional)", example = "Avenida Siempreviva 742")
    private String destinoDireccionLibre;

    // Nuevos campos para estadía y costo real de estadía (opcionales al crear)
    @Schema(description = "Tiempo de estadía en horas (opcional)", example = "24.0")
    private Double tiempoEstadiaHoras;

    @Schema(description = "Costo real de estadía (opcional)", example = "150.0")
    private Double costoEstadiaReal;

    @Schema(description = "Orden del tramo en la ruta", example = "1")
    private Integer orden;
}
