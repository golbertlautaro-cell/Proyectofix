package com.tpi.solicitudes.web.dto;

import com.tpi.solicitudes.domain.EstadoSolicitud;

import java.time.LocalDateTime;

public record SolicitudResponse(
        Long nroSolicitud,
        Long idContenedor,
        Long idCliente,
        EstadoSolicitud estado,
        Double costoEstimado,
        Double costoFinal,
        Double tiempoReal,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {}
