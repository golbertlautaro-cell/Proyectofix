package com.tpi.solicitudes.controller;

import com.tpi.solicitudes.domain.Ruta;
import com.tpi.solicitudes.domain.Tramo;
import com.tpi.solicitudes.dto.RutaCreateDto;
import com.tpi.solicitudes.dto.RutaResponseDto;
import com.tpi.solicitudes.dto.RutaUpdateDto;
import com.tpi.solicitudes.dto.TramoResponseDto;
import com.tpi.solicitudes.service.RutaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rutas")
@Tag(name = "Rutas", description = "Gestión de rutas alternativas para solicitudes")
public class RutaController {

    private final RutaService rutaService;

    public RutaController(RutaService rutaService) {
        this.rutaService = rutaService;
    }

    /**
     * Obtiene una ruta por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener ruta por ID")
    public ResponseEntity<RutaResponseDto> obtenerRutaPorId(@PathVariable Long id) {
        return rutaService.obtenerPorId(id)
            .map(this::convertirADto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza una ruta
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ruta")
    public ResponseEntity<RutaResponseDto> actualizarRuta(@PathVariable Long id, @RequestBody RutaUpdateDto dto) {
        try {
            Ruta ruta = rutaService.actualizarRuta(id, dto);
            return ResponseEntity.ok(convertirADto(ruta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina una ruta
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ruta")
    public ResponseEntity<Void> eliminarRuta(@PathVariable Long id) {
        try {
            rutaService.eliminarRuta(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Selecciona una ruta para ejecutar
     */
    @PutMapping("/{id}/seleccionar")
    @Operation(summary = "Seleccionar ruta para ejecutar")
    public ResponseEntity<RutaResponseDto> seleccionarRuta(@PathVariable Long id) {
        try {
            Ruta ruta = rutaService.seleccionarRuta(id);
            return ResponseEntity.ok(convertirADto(ruta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lista todas las rutas de una solicitud
     */
    @GetMapping("/solicitudes/{solicitudId}")
    @Operation(summary = "Listar rutas por solicitud (paginado)")
    public ResponseEntity<Page<RutaResponseDto>> listarRutasPorSolicitud(
            @PathVariable Long solicitudId,
            Pageable pageable) {
        Page<Ruta> rutas = rutaService.obtenerRutasPorSolicitudPaginadas(solicitudId, pageable);
        List<RutaResponseDto> rutaDtos = rutas.getContent().stream()
            .map(this::convertirADto)
            .collect(Collectors.toList());
        Page<RutaResponseDto> rutasDto = new PageImpl<>(rutaDtos, pageable, rutas.getTotalElements());
        return ResponseEntity.ok(rutasDto);
    }

    /**
     * Crea una nueva ruta alternativa para una solicitud
     */
    @PostMapping("/solicitudes/{solicitudId}")
    @Operation(summary = "Crear nueva ruta alternativa")
    public ResponseEntity<RutaResponseDto> crearRuta(
            @PathVariable Long solicitudId,
            @RequestBody RutaCreateDto dto) {
        try {
            Ruta ruta = rutaService.crearRuta(solicitudId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADto(ruta));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lista todas las rutas
     */
    @GetMapping
    @Operation(summary = "Listar todas las rutas")
    public ResponseEntity<List<RutaResponseDto>> listarTodasLasRutas() {
        List<Ruta> rutas = rutaService.obtenerTodas();
        List<RutaResponseDto> rutasDtos = rutas.stream()
            .map(this::convertirADto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(rutasDtos);
    }

    /**
     * Lista los tramos de una ruta
     */
    @GetMapping("/{rutaId}/tramos")
    @Operation(summary = "Listar tramos de una ruta")
    public ResponseEntity<List<TramoResponseDto>> listarTramosDeRuta(@PathVariable Long rutaId) {
        try {
            List<Tramo> tramos = rutaService.obtenerTramosDeRuta(rutaId);
            List<TramoResponseDto> tramosDtos = tramos.stream()
                .map(this::convertirTramoADto)
                .collect(Collectors.toList());
            return ResponseEntity.ok(tramosDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un tramo de una ruta
     */
    @DeleteMapping("/{rutaId}/tramos/{tramoId}")
    @Operation(summary = "Eliminar un tramo de una ruta")
    public ResponseEntity<Void> eliminarTramoDeRuta(
            @PathVariable Long rutaId,
            @PathVariable Long tramoId) {
        try {
            rutaService.eliminarTramoDeRuta(rutaId, tramoId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Métodos de conversión
    private RutaResponseDto convertirADto(Ruta ruta) {
        return RutaResponseDto.builder()
            .idRuta(ruta.getIdRuta())
            .nombre(ruta.getNombre())
            .descripcion(ruta.getDescripcion())
            .estado(ruta.getEstado())
            .distanciaTotalKm(ruta.getDistanciaTotalKm())
            .duracionEstimadaHoras(ruta.getDuracionEstimadaHoras())
            .costoEstimado(ruta.getCostoEstimado())
            .costoReal(ruta.getCostoReal())
            .esRutaSeleccionada(ruta.getEsRutaSeleccionada())
            .fechaCreacion(ruta.getFechaCreacion())
            .fechaActualizacion(ruta.getFechaActualizacion())
            .nroSolicitud(ruta.getSolicitud() != null ? ruta.getSolicitud().getNroSolicitud() : null)
            .tramos(ruta.getTramos() != null ? 
                ruta.getTramos().stream().map(this::convertirTramoADto).collect(Collectors.toList()) : 
                null)
            .build();
    }

    private TramoResponseDto convertirTramoADto(Tramo tramo) {
        return TramoResponseDto.builder()
            .idTramo(tramo.getIdTramo())
            .origen(tramo.getOrigen())
            .destino(tramo.getDestino())
            .dominioCamion(tramo.getDominioCamion())
            .estado(tramo.getEstado())
            .fechaHoraInicioReal(tramo.getFechaHoraInicioReal())
            .fechaHoraFinReal(tramo.getFechaHoraFinReal())
            .odometroFinal(tramo.getOdometroFinal())
            .costoReal(tramo.getCostoReal())
            .tiempoReal(tramo.getTiempoReal())
            .tipo(tramo.getTipo())
            .costoAproximado(tramo.getCostoAproximado())
            .fechaHoraInicioEstimada(tramo.getFechaHoraInicioEstimada())
            .fechaHoraFinEstimada(tramo.getFechaHoraFinEstimada())
            .distanciaEstimadaKm(tramo.getDistanciaEstimadaKm())
            .distanciaRealKm(tramo.getDistanciaRealKm())
            .diasDepositoEstimados(tramo.getDiasDepositoEstimados())
            .diasDepositoReales(tramo.getDiasDepositoReales())
            .depositoId(tramo.getDepositoId())
            .build();
    }
}
