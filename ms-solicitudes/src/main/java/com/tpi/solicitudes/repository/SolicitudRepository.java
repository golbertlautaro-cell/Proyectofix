package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.domain.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    /**
     * Busca solicitudes por ID de contenedor.
     *
     * @param idContenedor ID del contenedor
     * @return Lista de solicitudes asociadas al contenedor
     */
    List<Solicitud> findByIdContenedor(Long idContenedor);
}
