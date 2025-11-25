package com.tpi.solicitudes.repository;

import com.tpi.solicitudes.domain.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {
    
    /**
     * Obtiene todos los contenedores de un cliente
     */
    List<Contenedor> findByCliente_IdCliente(Long idCliente);

    /**
     * Obtiene contenedores por estado y depósito actual.
     *
     * @param estado Estado del contenedor (ej: "EN_DEPOSITO")
     * @param depositoActualId ID del depósito actual
     * @return Lista de contenedores que coinciden con los criterios
     */
    List<Contenedor> findByEstadoAndDepositoActualId(String estado, Long depositoActualId);
}
