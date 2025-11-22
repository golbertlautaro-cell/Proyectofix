package com.tpi.solicitudes.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @deprecated Reemplazado por {@link TramoAsignacionDTO}. Mantener solo por compatibilidad temporal.
 */
@Deprecated
public record AsignarCamionDto(
        @NotBlank String dominioCamion
) {}
