package com.tpi.solicitudes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tpi.solicitudes.domain.EstadoRuta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO de respuesta para Ruta")
public class RutaResponseDto {

    @Schema(description = "Identificador único de la ruta", example = "1")
    private Long idRuta;

    @Schema(description = "Nombre descriptivo de la ruta", example = "Ruta principal por Ruta 9")
    private String nombre;

    @Schema(description = "Descripción detallada de la ruta", example = "Ruta que utiliza Ruta 9 como eje principal")
    private String descripcion;

    @Schema(description = "Lista de tramos que conforman la ruta")
    private List<TramoResponseDto> tramos;

    @Schema(description = "Estado de la ruta", example = "PENDIENTE")
    private EstadoRuta estado;

    @Schema(description = "Distancia total de la ruta en kilómetros", example = "150.5")
    private Double distanciaTotalKm;

    @Schema(description = "Duración estimada en horas", example = "2.5")
    private Double duracionEstimadaHoras;

    @Schema(description = "Costo estimado de la ruta", example = "1500.00")
    private Double costoEstimado;

    @Schema(description = "Costo real de la ruta", example = "1550.00")
    private Double costoReal;

    @Schema(description = "Indica si esta es la ruta seleccionada para ejecutar", example = "false")
    private Boolean esRutaSeleccionada;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    @Schema(description = "Fecha de creación de la ruta")
    private LocalDateTime fechaCreacion;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    @Schema(description = "Fecha de última actualización")
    private LocalDateTime fechaActualizacion;

    @Schema(description = "Número de solicitud a la que pertenece la ruta", example = "1")
    private Long nroSolicitud;
}
