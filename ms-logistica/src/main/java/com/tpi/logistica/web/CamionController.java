package com.tpi.logistica.web;

import com.tpi.logistica.domain.Camion;
import com.tpi.logistica.service.CamionService;
import com.tpi.logistica.web.dto.CapacidadRequest;
import com.tpi.logistica.web.dto.CapacidadResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Tag(name = "Camiones", description = "Gestión de camiones y su capacidad")
@RestController
@RequestMapping("/api/camiones")
public class CamionController {

    private final CamionService camionService;

    public CamionController(CamionService camionService) {
        this.camionService = camionService;
    }

    @Operation(
        summary = "Listar todos los camiones",
        description = "Retorna una página de camiones con opciones de filtrado por capacidad"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de camiones obtenida"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public Page<Camion> listar(
        @Parameter(description = "Información de paginación") Pageable pageable,
        @Parameter(description = "Capacidad de peso mínima (en kg)") @RequestParam(required = false) Double minCapacidadPeso,
        @Parameter(description = "Capacidad de peso máxima (en kg)") @RequestParam(required = false) Double maxCapacidadPeso,
        @Parameter(description = "Capacidad de volumen mínima (en m³)") @RequestParam(required = false) Double minCapacidadVolumen,
        @Parameter(description = "Capacidad de volumen máxima (en m³)") @RequestParam(required = false) Double maxCapacidadVolumen
    ) {
        return camionService.listar(pageable, minCapacidadPeso, maxCapacidadPeso, minCapacidadVolumen, maxCapacidadVolumen);
    }

    @Operation(
        summary = "Obtener estado de camiones",
        description = "Retorna estadísticas generales del estado de todos los camiones"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado obtenido correctamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstado() {
        Map<String, Object> estado = camionService.obtenerEstadoCamiones();
        return ResponseEntity.ok(estado);
    }

    @Operation(
        summary = "Obtener camión por dominio",
        description = "Retorna un camión específico según su dominio (patente)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Camión encontrado"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{dominio}")
    public Camion obtener(
        @Parameter(description = "Dominio (patente) del camión", required = true) @PathVariable String dominio
    ) {
        return camionService.obtener(dominio);
    }

    @Operation(
        summary = "Crear nuevo camión",
        description = "Registra un nuevo camión en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Camión creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Camión con ese dominio ya existe"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping
    public ResponseEntity<Camion> crear(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del camión a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = Camion.class))
        )
        @RequestBody @Valid Camion camion
    ) {
        Camion creado = camionService.crear(camion);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Operation(
        summary = "Actualizar camión",
        description = "Actualiza los datos de un camión existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Camión actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/{dominio}")
    public Camion actualizar(
        @Parameter(description = "Dominio (patente) del camión", required = true) @PathVariable String dominio,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos actualizados del camión",
            required = true
        )
        @RequestBody @Valid Camion camion
    ) {
        return camionService.actualizar(dominio, camion);
    }

    @Operation(
        summary = "Eliminar camión",
        description = "Elimina un camión del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Camión eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Camión no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{dominio}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
        @Parameter(description = "Dominio (patente) del camión", required = true) @PathVariable String dominio
    ) {
        camionService.eliminar(dominio);
    }

    @Operation(
        summary = "Validar capacidad de camión",
        description = "Verifica si un camión tiene capacidad suficiente para un contenedor"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validación completada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping("/validar-capacidad")
    public ResponseEntity<CapacidadResponse> validarCapacidad(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos para validación de capacidad",
            required = true,
            content = @Content(schema = @Schema(implementation = CapacidadRequest.class))
        )
        @RequestBody @Valid CapacidadRequest request
    ) {
        boolean valido = camionService.validarCapacidad(
                request.dominio(), request.pesoContenedor(), request.volumenContenedor()
        );
        return ResponseEntity.ok(new CapacidadResponse(valido));
    }
}
