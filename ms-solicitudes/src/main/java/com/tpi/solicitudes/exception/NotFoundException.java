package com.tpi.solicitudes.exception;

/**
 * Excepción para recursos no encontrados (404 Not Found).
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String resource, Long id) {
        super(String.format("%s no encontrado con ID: %d", resource, id));
    }

    public NotFoundException(String resource, String identifier) {
        super(String.format("%s no encontrado: %s", resource, identifier));
    }
}

