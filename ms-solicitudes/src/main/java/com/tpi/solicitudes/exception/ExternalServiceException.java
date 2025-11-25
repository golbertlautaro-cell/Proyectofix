package com.tpi.solicitudes.exception;

/**
 * Excepción para fallos en servicios externos (503 Service Unavailable).
 * Ejemplo: Google Maps API no disponible, ms-logistica caído.
 */
public class ExternalServiceException extends RuntimeException {

    private final String serviceName;

    public ExternalServiceException(String serviceName, String message) {
        super(String.format("Error en servicio externo '%s': %s", serviceName, message));
        this.serviceName = serviceName;
    }

    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("Error en servicio externo '%s': %s", serviceName, message), cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}

