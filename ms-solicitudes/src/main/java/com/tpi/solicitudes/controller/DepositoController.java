package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.domain.Contenedor;
import com.tpi.solicitudes.service.ContenedorService;
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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/depositos")
@Tag(name = "Depósitos", description = "Endpoints relacionados con depósitos")
public class DepositoController {

    private final ContenedorService contenedorService;

    public DepositoController(ContenedorService contenedorService) {
        this.contenedorService = contenedorService;
    }

    /**
     * Lista todos los contenedores que están actualmente en un depósito específico.
     *
     * Filtra contenedores con:
     * - estado = "EN_DEPOSITO"
     * - depositoActualId = id especificado
     *
     * @param id ID del depósito
     * @return Lista de contenedores en el depósito
     */
    @Operation(
        summary = "Listar contenedores en depósito",
        description = "Retorna todos los contenedores que están actualmente en estado EN_DEPOSITO " +
                     "dentro del depósito especificado."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de contenedores obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = Contenedor.class))
        )
    })
    @GetMapping("/{id}/contenedores")
    public ResponseEntity<List<Contenedor>> listarContenedoresEnDeposito(
        @Parameter(description = "ID del depósito", required = true)
        @PathVariable Long id
    ) {
        log.info("GET /api/depositos/{}/contenedores - Listando contenedores en depósito", id);

        List<Contenedor> contenedores = contenedorService.obtenerContenedoresPorDeposito(id);

        log.info("Se encontraron {} contenedores en depósito {}", contenedores.size(), id);

        return ResponseEntity.ok(contenedores);
    }
}

