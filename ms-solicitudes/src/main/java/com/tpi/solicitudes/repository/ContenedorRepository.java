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
}
