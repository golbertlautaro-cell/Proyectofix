package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.domain.Tramo;
import com.tpi.solicitudes.dto.TramoCreateDto;
import com.tpi.solicitudes.dto.TramoFinishRequest;
import com.tpi.solicitudes.dto.TramoStartRequest;
import com.tpi.solicitudes.service.TramoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/tramos")
@Tag(name = "Tramos", description = "Gestión de tramos (segmentos) de transporte")
public class TramoController {

    private final TramoService tramoService;

    public TramoController(TramoService tramoService) {
        this.tramoService = tramoService;
    }

    /**
     * Inicia un tramo
     */
    @PutMapping("/{idTramo}/iniciar")
    @Operation(summary = "Iniciar tramo")
    public ResponseEntity<Tramo> iniciarTramo(
            @PathVariable Long idTramo,
            @RequestBody(required = false) TramoStartRequest request) {
        try {
            Double odometroInicial = request != null ? request.getOdometroInicial() : null;
            Tramo tramo = tramoService.iniciarTramo(idTramo, odometroInicial);
            return ResponseEntity.ok(tramo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Finaliza un tramo (actualiza con tiempos y costos reales)
     */
    @PutMapping("/{idTramo}/finalizar")
    @Operation(summary = "Finalizar tramo")
    public ResponseEntity<Tramo> finalizarTramo(
            @PathVariable Long idTramo,
            @RequestBody(required = false) TramoFinishRequest request) {
        try {
            var payload = request != null ? request : new TramoFinishRequest();
            var fin = payload.getFechaHoraFin() != null ? payload.getFechaHoraFin() : java.time.LocalDateTime.now();
            Tramo tramo = tramoService.finalizarTramo(
                    idTramo,
                    fin,
                    payload.getOdometroFinal(),
                    payload.getTiempoReal());
            return ResponseEntity.ok(tramo);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Asigna un camión a un tramo
     */
    @PutMapping("/{idTramo}/asignar-camion")
    @Operation(summary = "Asignar camión a tramo")
    public ResponseEntity<Tramo> asignarCamionATramo(
            @PathVariable Long idTramo,
            @RequestParam String dominioCamion) {
        try {
            // asignarCamion devuelve Mono<Tramo>, se bloquea para obtener el resultado
            Tramo tramo = tramoService.asignarCamion(idTramo, dominioCamion)
                    .doOnError(error -> {
                        if (error instanceof IllegalStateException) {
                            throw new RuntimeException(error.getMessage());
                        }
                    })
                    .block();
            return ResponseEntity.ok(tramo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Crea un nuevo tramo dentro de una ruta
     */
    @PostMapping("/rutas/{rutaId}/tramos")
    @Operation(summary = "Crear nuevo tramo en ruta")
    public ResponseEntity<Tramo> crearTramoEnRuta(
            @PathVariable Long rutaId,
            @RequestBody TramoCreateDto dto) {
        try {
            Tramo tramo = tramoService.crearTramoEnRuta(rutaId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(tramo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lista todos los tramos
     */
    @GetMapping
    @Operation(summary = "Listar todos los tramos")
    public ResponseEntity<List<Tramo>> listarTodosTramos() {
        List<Tramo> tramos = tramoService.obtenerTodos();
        return ResponseEntity.ok(tramos);
    }

    /**
     * Elimina un tramo
     */
    @DeleteMapping("/{idTramo}")
    @Operation(summary = "Eliminar un tramo")
    public ResponseEntity<Void> eliminarTramo(@PathVariable Long idTramo) {
        try {
            tramoService.eliminarTramo(idTramo);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
