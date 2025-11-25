package com.tpi.solicitudes.dto;

import com.tpi.solicitudes.domain.Contenedor;
import com.tpi.solicitudes.domain.Ruta;
import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.domain.Tramo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO consolidado para el seguimiento de un contenedor.
 * Incluye toda la información relevante del estado actual del transporte.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeguimientoContenedorDTO {

    // Datos del contenedor
    private Contenedor contenedor;

    // Solicitud asociada
    private Solicitud solicitud;

    // Ruta seleccionada
    private Ruta rutaSeleccionada;

    // Tramos ordenados por orden
    private List<TramoSeguimientoDTO> tramos;

    // Ubicación actual
    private UbicacionActualDTO ubicacionActual;

    // Costos totales
    private CostosTotalesDTO costosTotales;

    // Tiempos totales
    private TiemposTotalesDTO tiemposTotales;
}

