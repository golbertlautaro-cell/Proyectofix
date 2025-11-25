package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.dto.SeguimientoContenedorDTO;
import com.tpi.solicitudes.service.SeguimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/seguimiento")
@Tag(name = "Seguimiento", description = "Endpoints para seguimiento de contenedores y transportes")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    public SeguimientoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    /**
     * Obtiene el seguimiento completo de un contenedor.
     *
     * Retorna:
     * - Datos del contenedor
     * - Solicitud asociada
     * - Ruta seleccionada
     * - Tramos ordenados con estados, costos y tiempos
     * - Ubicación actual
     * - Costos totales (estimados y reales)
     * - Tiempos totales (estimados y reales)
     *
     * @param idContenedor ID del contenedor a rastrear
     * @return Información completa de seguimiento
     */
    @Operation(
        summary = "Obtener seguimiento de contenedor",
        description = "Retorna información completa del seguimiento de un contenedor: " +
                     "solicitud, ruta, tramos, ubicación actual, costos y tiempos totales."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Seguimiento obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = SeguimientoContenedorDTO.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Contenedor no encontrado"
        )
    })
    @GetMapping("/{idContenedor}")
    public ResponseEntity<SeguimientoContenedorDTO> obtenerSeguimiento(
        @Parameter(description = "ID del contenedor", required = true)
        @PathVariable Long idContenedor
    ) {
        log.info("GET /api/seguimiento/{} - Solicitando seguimiento de contenedor", idContenedor);

        SeguimientoContenedorDTO seguimiento = seguimientoService.obtenerSeguimiento(idContenedor);

        log.info("Seguimiento obtenido para contenedor {} - Estado: {}",
            idContenedor, seguimiento.getContenedor().getEstado());

        return ResponseEntity.ok(seguimiento);
    }
}

