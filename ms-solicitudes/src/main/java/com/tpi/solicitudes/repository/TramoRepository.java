package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.domain.Tramo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {
    // Legacy: Tramos by Solicitud (mantener compatibilidad)
    @Query("SELECT t FROM Tramo t WHERE t.ruta.solicitud.nroSolicitud = :solicitudId")
    List<Tramo> findAllBySolicitudNroSolicitud(Long solicitudId);
    
    @Query("SELECT t FROM Tramo t WHERE t.ruta.solicitud.nroSolicitud = :solicitudId")
    Page<Tramo> findPageBySolicitudNroSolicitud(Long solicitudId, Pageable pageable);

    // New: Tramos by Ruta
    List<Tramo> findByRutaIdRuta(Long rutaId);
    Page<Tramo> findByRutaIdRuta(Long rutaId, Pageable pageable);

    Page<Tramo> findByEstado(String estado, Pageable pageable);
    Page<Tramo> findByDominioCamion(String dominioCamion, Pageable pageable);
    Page<Tramo> findByEstadoAndDominioCamion(String estado, String dominioCamion, Pageable pageable);

    // Rango por fechaHoraInicioReal
    Page<Tramo> findByFechaHoraInicioRealBetween(java.time.LocalDateTime desde, java.time.LocalDateTime hasta, Pageable pageable);
    Page<Tramo> findByEstadoAndFechaHoraInicioRealBetween(String estado, java.time.LocalDateTime desde, java.time.LocalDateTime hasta, Pageable pageable);
    Page<Tramo> findByDominioCamionAndFechaHoraInicioRealBetween(String dominioCamion, java.time.LocalDateTime desde, java.time.LocalDateTime hasta, Pageable pageable);
    Page<Tramo> findByEstadoAndDominioCamionAndFechaHoraInicioRealBetween(String estado, String dominioCamion, java.time.LocalDateTime desde, java.time.LocalDateTime hasta, Pageable pageable);
}

