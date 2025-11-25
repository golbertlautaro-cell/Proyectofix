package com.tpi.solicitudes.dto;

import com.tpi.solicitudes.domain.EstadoTramo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO simplificado de un tramo para seguimiento.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramoSeguimientoDTO {

    private Long idTramo;
    private Integer orden;
    private String origen;
    private String destino;
    private EstadoTramo estado;
    private String dominioCamion;

    // Tiempos
    private LocalDateTime fechaHoraInicioEstimada;
    private LocalDateTime fechaHoraFinEstimada;
    private LocalDateTime fechaHoraInicioReal;
    private LocalDateTime fechaHoraFinReal;
    private Double tiempoReal;
    private Double tiempoEstadiaHoras;

    // Costos
    private Double costoAproximado;
    private Double costoReal;
    private Double costoEstadiaReal;

    // Distancias
    private Double distanciaEstimadaKm;
    private Double distanciaRealKm;
}

