package com.tpi.solicitudes.service;

import com.tpi.solicitudes.domain.EstadoRuta;
import com.tpi.solicitudes.domain.EstadoSolicitud;
import com.tpi.solicitudes.domain.Ruta;
import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.domain.Tramo;
import com.tpi.solicitudes.dto.RutaCreateDto;
import com.tpi.solicitudes.dto.RutaUpdateDto;
import com.tpi.solicitudes.repository.RutaRepository;
import com.tpi.solicitudes.repository.SolicitudRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RutaService {

    private final RutaRepository rutaRepository;
    private final SolicitudRepository solicitudRepository;
    private final TramoService tramoService;

    public RutaService(RutaRepository rutaRepository, SolicitudRepository solicitudRepository, TramoService tramoService) {
        this.rutaRepository = rutaRepository;
        this.solicitudRepository = solicitudRepository;
        this.tramoService = tramoService;
    }

    /**
     * Obtiene una ruta por su ID
     */
    public Optional<Ruta> obtenerPorId(Long idRuta) {
        return rutaRepository.findById(idRuta);
    }

    /**
     * Obtiene todas las rutas de una solicitud
     */
    public List<Ruta> obtenerRutasPorSolicitud(Long nroSolicitud) {
        return rutaRepository.findBySolicitud_NroSolicitud(nroSolicitud);
    }

    /**
     * Obtiene todas las rutas de una solicitud paginadas
     */
    public Page<Ruta> obtenerRutasPorSolicitudPaginadas(Long nroSolicitud, Pageable pageable) {
        return rutaRepository.findBySolicitud_NroSolicitud(nroSolicitud, pageable);
    }

    /**
     * Obtiene todas las rutas
     */
    public List<Ruta> obtenerTodas() {
        return rutaRepository.findAll();
    }

    /**
     * Crea una nueva ruta alternativa para una solicitud
     */
    public Ruta crearRuta(Long nroSolicitud, RutaCreateDto dto) {
        Solicitud solicitud = solicitudRepository.findById(nroSolicitud)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada: " + nroSolicitud));

        Ruta ruta = new Ruta();
        ruta.setSolicitud(solicitud);
        ruta.setNombre(dto.getNombre());
        ruta.setDescripcion(dto.getDescripcion());
        ruta.setEstado(EstadoRuta.PENDIENTE);
        ruta.setEsRutaSeleccionada(false);

        return rutaRepository.save(ruta);
    }

    /**
     * Actualiza una ruta existente
     */
    public Ruta actualizarRuta(Long idRuta, RutaUpdateDto dto) {
        Ruta ruta = rutaRepository.findById(idRuta)
            .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));

        if (dto.getEstado() != null) {
            ruta.setEstado(EstadoRuta.valueOf(dto.getEstado()));
        }
        if (dto.getDistanciaTotalKm() != null) {
            ruta.setDistanciaTotalKm(dto.getDistanciaTotalKm());
        }
        if (dto.getDuracionEstimadaHoras() != null) {
            ruta.setDuracionEstimadaHoras(dto.getDuracionEstimadaHoras());
        }
        if (dto.getCostoEstimado() != null) {
            ruta.setCostoEstimado(dto.getCostoEstimado());
        }
        if (dto.getCostoReal() != null) {
            ruta.setCostoReal(dto.getCostoReal());
        }

        return rutaRepository.save(ruta);
    }

    /**
     * Elimina una ruta
     */
    public void eliminarRuta(Long idRuta) {
        Ruta ruta = rutaRepository.findById(idRuta)
            .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));
        
        // Si está seleccionada, desseleccionar primero
        if (Boolean.TRUE.equals(ruta.getEsRutaSeleccionada())) {
            ruta.setEsRutaSeleccionada(false);
        }
        
        rutaRepository.delete(ruta);
    }

    /**
     * Selecciona una ruta para ejecutar (marca como seleccionada y deselecciona otras)
     */
    public Ruta seleccionarRuta(Long idRuta) {
        Ruta ruta = rutaRepository.findById(idRuta)
            .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));

        // Deseleccionar otras rutas de la misma solicitud
        List<Ruta> rutasSolicitud = rutaRepository.findBySolicitud_NroSolicitud(ruta.getSolicitud().getNroSolicitud());
        rutasSolicitud.forEach(r -> r.setEsRutaSeleccionada(false));

        // Seleccionar esta ruta
        ruta.setEsRutaSeleccionada(true);
        ruta.setEstado(EstadoRuta.PENDIENTE);

        Solicitud solicitud = ruta.getSolicitud();
        if (solicitud != null) {
            solicitud.setEstado(EstadoSolicitud.PROGRAMADA);
            solicitudRepository.save(solicitud);
        }

        return rutaRepository.save(ruta);
    }

    /**
     * Agrega un tramo a una ruta
     */
    public Ruta agregarTramoARuta(Long idRuta, Tramo tramo) {
        Ruta ruta = rutaRepository.findById(idRuta)
            .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));

        tramo.setRuta(ruta);
        ruta.getTramos().add(tramo);

        return rutaRepository.save(ruta);
    }

    /**
     * Elimina un tramo de una ruta
     */
    public void eliminarTramoDeRuta(Long idRuta, Long idTramo) {
        Ruta ruta = rutaRepository.findById(idRuta)
            .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));

        ruta.getTramos().removeIf(t -> t.getIdTramo().equals(idTramo));
        rutaRepository.save(ruta);
    }

    /**
     * Obtiene los tramos de una ruta
     */
    @Transactional(readOnly = true)
    public List<Tramo> obtenerTramosDeRuta(Long idRuta) {
        Ruta ruta = rutaRepository.findById(idRuta)
            .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));
        return ruta.getTramos();
    }

    /**
     * Obtiene la ruta seleccionada de una solicitud
     */
    @Transactional(readOnly = true)
    public Optional<Ruta> obtenerRutaSeleccionada(Long nroSolicitud) {
        return rutaRepository.findSelectedRutaBySolicitud(nroSolicitud);
    }
}
