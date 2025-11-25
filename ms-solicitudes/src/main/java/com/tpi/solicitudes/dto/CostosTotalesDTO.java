package com.tpi.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para costos totales consolidados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostosTotalesDTO {

    private Double costoTotalEstimado;
    private Double costoTotalReal;
    private Double costoTransporte; // Suma de costoReal de tramos
    private Double costoEstadias; // Suma de costoEstadiaReal de tramos
    private Double diferencia; // costoTotalReal - costoTotalEstimado
    private Double porcentajeDiferencia; // (diferencia / costoTotalEstimado) * 100
}

