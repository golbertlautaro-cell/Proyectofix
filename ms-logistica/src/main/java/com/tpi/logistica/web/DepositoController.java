package com.tpi.logistica.web;

import com.tpi.logistica.domain.Deposito;
import com.tpi.logistica.service.DepositoService;
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

@Slf4j
@Tag(name = "Depósitos", description = "Gestión de depósitos y centros de distribución")
@RestController
@RequestMapping("/api/depositos")
public class DepositoController {

    private final DepositoService depositoService;

    public DepositoController(DepositoService depositoService) {
        this.depositoService = depositoService;
    }

    @Operation(
        summary = "Listar todos los depósitos",
        description = "Retorna una página de depósitos con información de ubicación y capacidad"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de depósitos obtenida"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public Page<Deposito> listar(
        @Parameter(description = "Información de paginación") Pageable pageable
    ) {
        return depositoService.listar(pageable);
    }

    @Operation(
        summary = "Obtener depósito por ID",
        description = "Retorna un depósito específico según su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Depósito encontrado"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}")
    public Deposito obtener(
        @Parameter(description = "ID del depósito", required = true) @PathVariable Long id
    ) {
        return depositoService.obtener(id);
    }

    @Operation(
        summary = "Crear nuevo depósito",
        description = "Registra un nuevo depósito o centro de distribución"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Depósito creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping
    public ResponseEntity<Deposito> crear(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del depósito a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = Deposito.class))
        )
        @RequestBody @Valid Deposito deposito
    ) {
        Deposito creado = depositoService.crear(deposito);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @Operation(
        summary = "Actualizar depósito",
        description = "Actualiza los datos de un depósito existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Depósito actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/{id}")
    public Deposito actualizar(
        @Parameter(description = "ID del depósito", required = true) @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos actualizados del depósito",
            required = true
        )
        @RequestBody @Valid Deposito deposito
    ) {
        return depositoService.actualizar(id, deposito);
    }

    @Operation(
        summary = "Eliminar depósito",
        description = "Elimina un depósito del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Depósito eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Depósito no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
        @Parameter(description = "ID del depósito", required = true) @PathVariable Long id
    ) {
        depositoService.eliminar(id);
    }
}
