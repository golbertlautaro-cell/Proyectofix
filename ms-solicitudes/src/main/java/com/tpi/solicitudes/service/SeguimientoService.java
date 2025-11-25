package com.tpi.solicitudes.service;

import com.tpi.solicitudes.domain.Contenedor;
import com.tpi.solicitudes.domain.EstadoTramo;
import com.tpi.solicitudes.domain.Ruta;
import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.domain.Tramo;
import com.tpi.solicitudes.dto.*;
import com.tpi.solicitudes.repository.RutaRepository;
import com.tpi.solicitudes.repository.SolicitudRepository;
import com.tpi.solicitudes.repository.TramoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class SeguimientoService {

    private final ContenedorService contenedorService;
    private final SolicitudRepository solicitudRepository;
    private final RutaRepository rutaRepository;
    private final TramoRepository tramoRepository;

    public SeguimientoService(ContenedorService contenedorService,
                             SolicitudRepository solicitudRepository,
                             RutaRepository rutaRepository,
                             TramoRepository tramoRepository) {
        this.contenedorService = contenedorService;
        this.solicitudRepository = solicitudRepository;
        this.rutaRepository = rutaRepository;
        this.tramoRepository = tramoRepository;
    }

    /**
     * Obtiene el seguimiento completo de un contenedor.
     * 
     * @param idContenedor ID del contenedor a rastrear
     * @return DTO con toda la información de seguimiento
     * @throws NoSuchElementException si el contenedor no existe
     */
    public SeguimientoContenedorDTO obtenerSeguimiento(Long idContenedor) {
        log.info("Obteniendo seguimiento para contenedor {}", idContenedor);
        
        // Obtener contenedor
        Contenedor contenedor = contenedorService.obtenerPorId(idContenedor)
            .orElseThrow(() -> new NoSuchElementException("Contenedor no encontrado: " + idContenedor));
        
        // Buscar solicitud asociada
        Optional<Solicitud> solicitudOpt = solicitudRepository.findByIdContenedor(idContenedor)
            .stream()
            .findFirst(); // Tomar la más reciente o activa
        
        if (solicitudOpt.isEmpty()) {
            // Contenedor sin solicitud asociada
            return SeguimientoContenedorDTO.builder()
                .contenedor(contenedor)
                .ubicacionActual(crearUbicacionSinSolicitud(contenedor))
                .build();
        }
        
        Solicitud solicitud = solicitudOpt.get();
        
        // Obtener ruta seleccionada
        Optional<Ruta> rutaOpt = rutaRepository.findSelectedRutaBySolicitud(solicitud.getNroSolicitud());
        
        if (rutaOpt.isEmpty()) {
            // Solicitud sin ruta seleccionada
            return SeguimientoContenedorDTO.builder()
                .contenedor(contenedor)
                .solicitud(solicitud)
                .ubicacionActual(crearUbicacionSinRuta(contenedor))
                .build();
        }
        
        Ruta ruta = rutaOpt.get();
        
        // Obtener tramos ordenados
        List<Tramo> tramos = tramoRepository.findByRutaIdRuta(ruta.getIdRuta())
            .stream()
            .sorted(Comparator.comparing(Tramo::getOrden))
            .collect(Collectors.toList());
        
        // Convertir tramos a DTO
        List<TramoSeguimientoDTO> tramosDTO = tramos.stream()
            .map(this::convertirTramoADTO)
            .collect(Collectors.toList());
        
        // Calcular ubicación actual
        UbicacionActualDTO ubicacion = calcularUbicacionActual(contenedor, tramos);
        
        // Calcular costos totales
        CostosTotalesDTO costos = calcularCostosTotales(solicitud, tramos);
        
        // Calcular tiempos totales
        TiemposTotalesDTO tiempos = calcularTiemposTotales(solicitud, tramos);
        
        return SeguimientoContenedorDTO.builder()
            .contenedor(contenedor)
            .solicitud(solicitud)
            .rutaSeleccionada(ruta)
            .tramos(tramosDTO)
            .ubicacionActual(ubicacion)
            .costosTotales(costos)
            .tiemposTotales(tiempos)
            .build();
    }

    /**
     * Convierte un Tramo a TramoSeguimientoDTO.
     */
    private TramoSeguimientoDTO convertirTramoADTO(Tramo tramo) {
        return TramoSeguimientoDTO.builder()
            .idTramo(tramo.getIdTramo())
            .orden(tramo.getOrden())
            .origen(tramo.getOrigen())
            .destino(tramo.getDestino())
            .estado(tramo.getEstado())
            .dominioCamion(tramo.getDominioCamion())
            .fechaHoraInicioEstimada(tramo.getFechaHoraInicioEstimada())
            .fechaHoraFinEstimada(tramo.getFechaHoraFinEstimada())
            .fechaHoraInicioReal(tramo.getFechaHoraInicioReal())
            .fechaHoraFinReal(tramo.getFechaHoraFinReal())
            .tiempoReal(tramo.getTiempoReal())
            .tiempoEstadiaHoras(tramo.getTiempoEstadiaHoras())
            .costoAproximado(tramo.getCostoAproximado())
            .costoReal(tramo.getCostoReal())
            .costoEstadiaReal(tramo.getCostoEstadiaReal())
            .distanciaEstimadaKm(tramo.getDistanciaEstimadaKm())
            .distanciaRealKm(tramo.getDistanciaRealKm())
            .build();
    }

    /**
     * Calcula la ubicación actual del contenedor basándose en el estado y los tramos.
     */
    private UbicacionActualDTO calcularUbicacionActual(Contenedor contenedor, List<Tramo> tramos) {
        String estadoContenedor = contenedor.getEstado();
        
        // Caso 1: Contenedor EN_TRANSITO
        if ("EN_TRANSITO".equals(estadoContenedor)) {
            // Buscar el tramo actual (INICIADO)
            Optional<Tramo> tramoActual = tramos.stream()
                .filter(t -> t.getEstado() == EstadoTramo.INICIADO)
                .findFirst();
            
            if (tramoActual.isPresent()) {
                Tramo tramo = tramoActual.get();
                return UbicacionActualDTO.builder()
                    .descripcion(String.format("En tránsito desde %s hacia %s (Tramo %d)", 
                        tramo.getOrigen(), tramo.getDestino(), tramo.getOrden()))
                    .tipo("EN_TRANSITO")
                    .tramoActualId(tramo.getIdTramo())
                    .tramoOrden(tramo.getOrden())
                    .origenTramoActual(tramo.getOrigen())
                    .destinoTramoActual(tramo.getDestino())
                    .build();
            }
        }
        
        // Caso 2: Contenedor EN_DEPOSITO
        if ("EN_DEPOSITO".equals(estadoContenedor)) {
            Long depositoId = contenedor.getDepositoActualId();
            return UbicacionActualDTO.builder()
                .descripcion(String.format("En depósito (ID: %d)", depositoId))
                .tipo("EN_DEPOSITO")
                .depositoId(depositoId)
                .build();
        }
        
        // Caso 3: Contenedor ENTREGADO
        if ("ENTREGADO".equals(estadoContenedor)) {
            return UbicacionActualDTO.builder()
                .descripcion("Entregado al destinatario final")
                .tipo("ENTREGADO")
                .build();
        }
        
        // Caso 4: Contenedor EN_ORIGEN o DISPONIBLE
        return UbicacionActualDTO.builder()
            .descripcion("En origen, listo para ser transportado")
            .tipo("EN_ORIGEN")
            .build();
    }

    /**
     * Crea ubicación para contenedor sin solicitud asociada.
     */
    private UbicacionActualDTO crearUbicacionSinSolicitud(Contenedor contenedor) {
        return UbicacionActualDTO.builder()
            .descripcion("Sin solicitud de transporte asociada")
            .tipo(contenedor.getEstado() != null ? contenedor.getEstado() : "DISPONIBLE")
            .build();
    }

    /**
     * Crea ubicación para contenedor sin ruta seleccionada.
     */
    private UbicacionActualDTO crearUbicacionSinRuta(Contenedor contenedor) {
        return UbicacionActualDTO.builder()
            .descripcion("Solicitud sin ruta seleccionada")
            .tipo("EN_ORIGEN")
            .build();
    }

    /**
     * Calcula los costos totales consolidados.
     */
    private CostosTotalesDTO calcularCostosTotales(Solicitud solicitud, List<Tramo> tramos) {
        double costoTransporte = tramos.stream()
            .mapToDouble(t -> t.getCostoReal() != null ? t.getCostoReal() : 0.0)
            .sum();
        
        double costoEstadias = tramos.stream()
            .mapToDouble(t -> t.getCostoEstadiaReal() != null ? t.getCostoEstadiaReal() : 0.0)
            .sum();
        
        double costoTotalEstimado = solicitud.getCostoTotalEstimado() != null 
            ? solicitud.getCostoTotalEstimado() : 0.0;
        
        double costoTotalReal = solicitud.getCostoTotalReal() != null 
            ? solicitud.getCostoTotalReal() : (costoTransporte + costoEstadias);
        
        double diferencia = costoTotalReal - costoTotalEstimado;
        double porcentajeDiferencia = costoTotalEstimado > 0 
            ? (diferencia / costoTotalEstimado) * 100 : 0.0;
        
        return CostosTotalesDTO.builder()
            .costoTotalEstimado(costoTotalEstimado)
            .costoTotalReal(costoTotalReal)
            .costoTransporte(costoTransporte)
            .costoEstadias(costoEstadias)
            .diferencia(diferencia)
            .porcentajeDiferencia(porcentajeDiferencia)
            .build();
    }

    /**
     * Calcula los tiempos totales consolidados.
     */
    private TiemposTotalesDTO calcularTiemposTotales(Solicitud solicitud, List<Tramo> tramos) {
        double tiempoTransporte = tramos.stream()
            .mapToDouble(t -> t.getTiempoReal() != null ? t.getTiempoReal() : 0.0)
            .sum();
        
        double tiempoEstadias = tramos.stream()
            .mapToDouble(t -> t.getTiempoEstadiaHoras() != null ? t.getTiempoEstadiaHoras() : 0.0)
            .sum();
        
        double tiempoTotalEstimado = solicitud.getTiempoTotalEstimadoHoras() != null 
            ? solicitud.getTiempoTotalEstimadoHoras() : 0.0;
        
        double tiempoTotalReal = solicitud.getTiempoTotalRealHoras() != null 
            ? solicitud.getTiempoTotalRealHoras() : (tiempoTransporte + tiempoEstadias);
        
        double diferencia = tiempoTotalReal - tiempoTotalEstimado;
        double porcentajeDiferencia = tiempoTotalEstimado > 0 
            ? (diferencia / tiempoTotalEstimado) * 100 : 0.0;
        
        return TiemposTotalesDTO.builder()
            .tiempoTotalEstimadoHoras(tiempoTotalEstimado)
            .tiempoTotalRealHoras(tiempoTotalReal)
            .tiempoTransporte(tiempoTransporte)
            .tiempoEstadias(tiempoEstadias)
            .diferencia(diferencia)
            .porcentajeDiferencia(porcentajeDiferencia)
            .build();
    }
}

