package com.tpi.solicitudes.service;

import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.repository.SolicitudRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class SolicitudService {

    private final SolicitudRepository repository;

    public SolicitudService(SolicitudRepository repository) {
        this.repository = repository;
    }

    public List<Solicitud> findAll() { // legacy
        return repository.findAll();
    }

    public Page<Solicitud> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Solicitud findById(Long id) {
        log.debug("Buscando solicitud por ID: {}", id);
        return repository.findById(id).orElseThrow(() -> {
            log.warn("Solicitud no encontrada: {}", id);
            return new NoSuchElementException("Solicitud no encontrada: " + id);
        });
    }

    public Solicitud create(Solicitud s) {
        log.info("Creando nueva solicitud para cliente: {}", s.getIdCliente());
        s.setNroSolicitud(null); // Generado por BD
        Solicitud created = repository.save(s);
        log.info("Solicitud creada exitosamente con ID: {}", created.getNroSolicitud());
        return created;
    }

    public Solicitud update(Long id, Solicitud s) {
        log.info("Actualizando solicitud ID: {}", id);
        Solicitud actual = findById(id);
        actual.setIdContenedor(s.getIdContenedor());
        actual.setIdCliente(s.getIdCliente());
        actual.setEstado(s.getEstado());
        actual.setCostoEstimado(s.getCostoEstimado());
        actual.setCostoFinal(s.getCostoFinal());
        actual.setTiempoReal(s.getTiempoReal());
        Solicitud updated = repository.save(actual);
        log.info("Solicitud actualizada exitosamente ID: {}", id);
        return updated;
    }

    public void delete(Long id) {
        log.info("Eliminando solicitud ID: {}", id);
        if (!repository.existsById(id)) {
            log.error("Intento de eliminar solicitud no existente: {}", id);
            throw new NoSuchElementException("Solicitud no encontrada: " + id);
        }
        repository.deleteById(id);
        log.info("Solicitud eliminada exitosamente ID: {}", id);
    }
}
