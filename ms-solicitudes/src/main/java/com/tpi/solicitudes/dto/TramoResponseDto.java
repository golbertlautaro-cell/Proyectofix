package com.tpi.solicitudes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tpi.solicitudes.domain.EstadoTramo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de respuesta para Tramo")
public class TramoResponseDto {

    @Schema(description = "Identificador único del tramo", example = "1")
    private Long idTramo;

    @Schema(description = "Punto de origen del tramo", example = "La Plata")
    private String origen;

    @Schema(description = "Punto de destino del tramo", example = "Buenos Aires")
    private String destino;

    @Schema(description = "Dominio del camión", example = "ABC123")
    private String dominioCamion;

    @Schema(description = "Estado del tramo", example = "PENDIENTE")
    private EstadoTramo estado;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    @Schema(description = "Fecha y hora de inicio real")
    private LocalDateTime fechaHoraInicioReal;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    @Schema(description = "Fecha y hora de fin real")
    private LocalDateTime fechaHoraFinReal;

    @Schema(description = "Lectura del odómetro al inicio", example = "100.0")
    private Double odometroInicial;

    @Schema(description = "Lectura del odómetro al finalizar", example = "150.5")
    private Double odometroFinal;

    @Schema(description = "Costo real del tramo", example = "500.00")
    private Double costoReal;

    @Schema(description = "Tiempo real en horas", example = "2.5")
    private Double tiempoReal;

    @Schema(description = "Tipo de tramo", example = "NORMAL")
    private String tipo;

    @Schema(description = "Costo aproximado del tramo", example = "480.00")
    private Double costoAproximado;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    @Schema(description = "Fecha y hora de inicio estimada")
    private LocalDateTime fechaHoraInicioEstimada;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    @Schema(description = "Fecha y hora de fin estimada")
    private LocalDateTime fechaHoraFinEstimada;

    @Schema(description = "Distancia estimada en kilómetros", example = "350.0")
    private Double distanciaEstimadaKm;

    @Schema(description = "Distancia real recorrida en kilómetros", example = "342.7")
    private Double distanciaRealKm;

    @Schema(description = "Días estimados de estadía en depósito", example = "1.2")
    private Double diasDepositoEstimados;

    @Schema(description = "Días reales de estadía en depósito", example = "1.0")
    private Double diasDepositoReales;

    @Schema(description = "Identificador del depósito asociado", example = "4")
    private Long depositoId;
}
