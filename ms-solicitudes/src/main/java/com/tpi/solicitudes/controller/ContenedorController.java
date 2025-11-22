package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.domain.Contenedor;
import com.tpi.solicitudes.dto.ContenedorCreateDto;
import com.tpi.solicitudes.service.ContenedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes/{idCliente}/contenedores")
@Tag(name = "Clientes", description = "Gestión de clientes y sus contenedores")
public class ContenedorController {

    private final ContenedorService contenedorService;

    public ContenedorController(ContenedorService contenedorService) {
        this.contenedorService = contenedorService;
    }

    /**
     * Lista todos los contenedores de un cliente
     */
    @GetMapping
    @Operation(summary = "Listar contenedores de cliente")
    public ResponseEntity<List<Contenedor>> listarContenedoresDelCliente(@PathVariable Long idCliente) {
        List<Contenedor> contenedores = contenedorService.obtenerContenedoresPorCliente(idCliente);
        return ResponseEntity.ok(contenedores);
    }

    /**
     * Crea un nuevo contenedor para un cliente
     */
    @PostMapping
    @Operation(summary = "Crear contenedor para cliente")
    public ResponseEntity<Contenedor> crearContenedor(
            @PathVariable Long idCliente,
            @RequestBody ContenedorCreateDto dto) {
        try {
            Contenedor contenedor = contenedorService.crearContenedor(idCliente, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(contenedor);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un contenedor de un cliente
     */
    @DeleteMapping("/{contenedorId}")
    @Operation(summary = "Eliminar contenedor de cliente")
    public ResponseEntity<Void> eliminarContenedor(
            @PathVariable Long idCliente,
            @PathVariable Long contenedorId) {
        try {
            contenedorService.eliminarContenedorDeCliente(idCliente, contenedorId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
