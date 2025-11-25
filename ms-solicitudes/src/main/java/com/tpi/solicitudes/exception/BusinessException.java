package com.tpi.solicitudes.exception;

/**
 * Excepción para conflictos de negocio (409 Conflict).
 * Ejemplo: Recurso duplicado, estado inválido para la operación.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

