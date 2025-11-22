package com.tpi.solicitudes.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Payload para iniciar un tramo")
public class TramoStartRequest {

    @Schema(description = "Lectura del odómetro al inicio del viaje", example = "100.0")
    private Double odometroInicial;
}
