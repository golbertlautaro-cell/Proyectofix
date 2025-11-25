package com.tpi.solicitudes.exception;

/**
 * Excepción para validaciones de negocio fallidas (422 Unprocessable Entity).
 * Ejemplo: Datos inválidos, reglas de negocio no cumplidas.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

