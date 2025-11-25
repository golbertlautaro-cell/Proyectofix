package com.tpi.solicitudes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la ubicación actual del contenedor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UbicacionActualDTO {

    private String descripcion;
    private String tipo; // "EN_TRANSITO", "EN_DEPOSITO", "ENTREGADO", "EN_ORIGEN"
    private Long depositoId; // Si está en depósito
    private String depositoNombre; // Nombre del depósito
    private Long tramoActualId; // Si está en tránsito
    private Integer tramoOrden;
    private String origenTramoActual;
    private String destinoTramoActual;
}

