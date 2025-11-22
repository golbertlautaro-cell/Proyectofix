package com.tpi.solicitudes.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;

public record FinalizarTramoRequest(
        @NotNull LocalDateTime fechaHoraFin,
        @PositiveOrZero Double odometroFinal,
        @NotNull @PositiveOrZero Double tiempoReal
) {}
