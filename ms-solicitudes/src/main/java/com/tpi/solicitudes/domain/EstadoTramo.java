package com.tpi.solicitudes.domain;

public enum EstadoTramo {
    ESTIMADO,     // Tramo creado con cálculos de costo/tiempo estimados
    PENDIENTE,    // Tramo pendiente de asignación (legacy, puede ser igual a ESTIMADO)
    ASIGNADO,     // Camión asignado al tramo
    INICIADO,     // Tramo en ejecución
    FINALIZADO    // Tramo completado
}
