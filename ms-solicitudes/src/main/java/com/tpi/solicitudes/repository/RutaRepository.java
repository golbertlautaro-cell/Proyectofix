package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.domain.Ruta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
    /**
     * Obtiene todas las rutas de una solicitud específica
     */
    List<Ruta> findBySolicitud_NroSolicitud(Long nroSolicitud);
    
    /**
     * Obtiene todas las rutas de una solicitud paginadas
     */
    Page<Ruta> findBySolicitud_NroSolicitud(Long nroSolicitud, Pageable pageable);
    
    /**
     * Obtiene la ruta seleccionada de una solicitud (si existe)
     */
    @Query("SELECT r FROM Ruta r WHERE r.solicitud.nroSolicitud = :nroSolicitud AND r.esRutaSeleccionada = true")
    Optional<Ruta> findSelectedRutaBySolicitud(@Param("nroSolicitud") Long nroSolicitud);
    
    /**
     * Obtiene rutas por estado
     */
    List<Ruta> findByEstado(String estado);
}
