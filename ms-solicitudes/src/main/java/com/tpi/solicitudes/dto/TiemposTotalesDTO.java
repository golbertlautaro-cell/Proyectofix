package com.tpi.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para tiempos totales consolidados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TiemposTotalesDTO {

    private Double tiempoTotalEstimadoHoras;
    private Double tiempoTotalRealHoras;
    private Double tiempoTransporte; // Suma de tiempoReal de tramos
    private Double tiempoEstadias; // Suma de tiempoEstadiaHoras de tramos
    private Double diferencia; // tiempoTotalReal - tiempoTotalEstimado
    private Double porcentajeDiferencia; // (diferencia / tiempoTotalEstimado) * 100
}

