package com.tpi.logistica.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record CapacidadRequest(
        @NotBlank
        @Pattern(regexp = "^([A-Z]{3}[0-9]{3}|[A-Z]{2}[0-9]{3}[A-Z]{2})$", message = "Formato de dominio inválido")
        String dominio,
        @NotNull @PositiveOrZero Double pesoContenedor,
        @NotNull @PositiveOrZero Double volumenContenedor
) {}
