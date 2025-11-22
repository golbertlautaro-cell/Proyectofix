package com.tpi.solicitudes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Payload para finalizar un tramo")
public class TramoFinishRequest {

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Fecha y hora real de finalización")
    private LocalDateTime fechaHoraFin;

    @Schema(description = "Lectura del odómetro al finalizar", example = "250.5")
    private Double odometroFinal;

    @Schema(description = "Tiempo real en horas", example = "3.5")
    private Double tiempoReal;
}
