package com.tpi.solicitudes.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearSolicitudRequest(
        @NotNull Long idContenedor,
        @NotNull Long idCliente,
        @NotBlank String origen,
        @NotBlank String destino
) {}
