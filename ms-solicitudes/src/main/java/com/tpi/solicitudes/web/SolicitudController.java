package com.tpi.solicitudes.web;

import com.tpi.solicitudes.domain.EstadoSolicitud;
import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.service.SolicitudService;
import com.tpi.solicitudes.web.dto.CrearSolicitudRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Solicitudes", description = "Gestión de solicitudes de transporte")
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService service;

    public SolicitudController(SolicitudService service) {
        this.service = service;
    }

    @Operation(
        summary = "Listar todas las solicitudes",
        description = "Retorna una página de solicitudes con paginación"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de solicitudes obtenida correctamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public Page<Solicitud> listar(
        @Parameter(description = "Información de paginación") Pageable pageable
    ) {
        log.info("Listando solicitudes - página: {}, tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        return service.findAll(pageable);
    }

    @Operation(
        summary = "Obtener solicitud por ID",
        description = "Retorna una solicitud específica según su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitud encontrada"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}")
    public Solicitud obtener(
        @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long id
    ) {
        log.info("Obteniendo solicitud por ID: {}", id);
        return service.findById(id);
    }

    @Operation(
        summary = "Crear nueva solicitud de transporte",
        description = "Crea una nueva solicitud. Solo usuarios con rol CLIENTE pueden crear"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Solicitud creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - rol requerido: CLIENTE"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Solicitud crear(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos para crear la solicitud",
            required = true,
            content = @Content(schema = @Schema(implementation = CrearSolicitudRequest.class))
        )
        @RequestBody @Valid CrearSolicitudRequest request
    ) {
        Solicitud solicitud = Solicitud.builder()
                .idContenedor(request.idContenedor())
                .idCliente(request.idCliente())
                .estado(EstadoSolicitud.BORRADOR)
                .build();
        return service.create(solicitud);
    }

    @Operation(
        summary = "Actualizar solicitud",
        description = "Actualiza una solicitud existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Solicitud actualizada correctamente"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/{id}")
    public Solicitud actualizar(
        @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos actualizados de la solicitud",
            required = true
        )
        @RequestBody @Valid Solicitud s
    ) {
        return service.update(id, s);
    }

    @Operation(
        summary = "Eliminar solicitud",
        description = "Elimina una solicitud por su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Solicitud eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
        @Parameter(description = "ID de la solicitud", required = true) @PathVariable Long id
    ) {
        service.delete(id);
    }
}
