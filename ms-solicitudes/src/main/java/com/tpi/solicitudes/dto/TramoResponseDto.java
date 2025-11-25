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

    // Nuevos: referencias y direcciones libres para origen/destino
    @Schema(description = "ID de depósito de origen (si aplica)", example = "4")
    private Long origenDepositoId;

    @Schema(description = "ID de depósito de destino (si aplica)", example = "6")
    private Long destinoDepositoId;

    @Schema(description = "Dirección libre de origen (si aplica)", example = "Calle 1 123")
    private String origenDireccionLibre;

    @Schema(description = "Dirección libre de destino (si aplica)", example = "Calle 2 456")
    private String destinoDireccionLibre;

    // Nuevos campos: estadía
    @Schema(description = "Tiempo de estadía en horas", example = "24.0")
    private Double tiempoEstadiaHoras;

    @Schema(description = "Costo real de estadía", example = "150.0")
    private Double costoEstadiaReal;

    @Schema(description = "Orden del tramo en la ruta", example = "1")
    private Integer orden;
}
