package com.tpi.solicitudes.service;

import com.tpi.solicitudes.domain.Cliente;
import com.tpi.solicitudes.domain.Contenedor;
import com.tpi.solicitudes.dto.ContenedorCreateDto;
import com.tpi.solicitudes.dto.ContenedorUpdateDto;
import com.tpi.solicitudes.repository.ClienteRepository;
import com.tpi.solicitudes.repository.ContenedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ContenedorService {

    private final ContenedorRepository contenedorRepository;
    private final ClienteRepository clienteRepository;

    public ContenedorService(ContenedorRepository contenedorRepository, ClienteRepository clienteRepository) {
        this.contenedorRepository = contenedorRepository;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Obtiene todos los contenedores de un cliente
     */
    public List<Contenedor> obtenerContenedoresPorCliente(Long idCliente) {
        return contenedorRepository.findByCliente_IdCliente(idCliente);
    }

    /**
     * Obtiene un contenedor por su ID
     */
    public Optional<Contenedor> obtenerPorId(Long idContenedor) {
        return contenedorRepository.findById(idContenedor);
    }

    /**
     * Crea un nuevo contenedor para un cliente
     */
    public Contenedor crearContenedor(Long idCliente, ContenedorCreateDto dto) {
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + idCliente));

        Contenedor contenedor = new Contenedor();
        contenedor.setCliente(cliente);
        contenedor.setDescripcion(dto.getDescripcion());
        contenedor.setTipo(dto.getTipo());
        contenedor.setCapacidadKg(dto.getCapacidadKg());
        contenedor.setEstado("DISPONIBLE");

        return contenedorRepository.save(contenedor);
    }

    /**
     * Actualiza un contenedor existente
     */
    public Contenedor actualizarContenedor(Long idContenedor, ContenedorUpdateDto dto) {
        Contenedor contenedor = contenedorRepository.findById(idContenedor)
            .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado: " + idContenedor));

        if (dto.getDescripcion() != null) {
            contenedor.setDescripcion(dto.getDescripcion());
        }
        if (dto.getTipo() != null) {
            contenedor.setTipo(dto.getTipo());
        }
        if (dto.getCapacidadKg() != null) {
            contenedor.setCapacidadKg(dto.getCapacidadKg());
        }
        if (dto.getEstado() != null) {
            contenedor.setEstado(dto.getEstado());
        }

        return contenedorRepository.save(contenedor);
    }

    /**
     * Elimina un contenedor
     */
    public void eliminarContenedor(Long idContenedor) {
        Contenedor contenedor = contenedorRepository.findById(idContenedor)
            .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado: " + idContenedor));
        
        contenedorRepository.delete(contenedor);
    }

    /**
     * Elimina un contenedor de un cliente específico
     */
    public void eliminarContenedorDeCliente(Long idCliente, Long idContenedor) {
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + idCliente));

        Contenedor contenedor = contenedorRepository.findById(idContenedor)
            .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado: " + idContenedor));

        if (!contenedor.getCliente().getIdCliente().equals(idCliente)) {
            throw new IllegalArgumentException("El contenedor no pertenece al cliente especificado");
        }

        contenedorRepository.delete(contenedor);
    }
}
