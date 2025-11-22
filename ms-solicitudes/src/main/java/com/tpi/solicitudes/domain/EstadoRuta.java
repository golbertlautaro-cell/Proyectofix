package com.tpi.solicitudes.domain;

public enum EstadoRuta {
    PENDIENTE("Pendiente"),
    EJECUTANDOSE("En ejecución"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada"),
    DESCARTADA("Descartada");

    private final String descripcion;

    EstadoRuta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
