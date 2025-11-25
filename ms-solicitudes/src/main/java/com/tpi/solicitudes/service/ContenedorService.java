package com.tpi.solicitudes.service;

import com.tpi.solicitudes.domain.Cliente;
import com.tpi.solicitudes.domain.Contenedor;
import com.tpi.solicitudes.domain.EstadoSolicitud;
import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.dto.ContenedorCreateDto;
import com.tpi.solicitudes.dto.ContenedorUpdateDto;
import com.tpi.solicitudes.repository.ClienteRepository;
import com.tpi.solicitudes.repository.ContenedorRepository;
import com.tpi.solicitudes.repository.SolicitudRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class ContenedorService {

    private final ContenedorRepository contenedorRepository;
    private final ClienteRepository clienteRepository;
    private final SolicitudRepository solicitudRepository;

    public ContenedorService(ContenedorRepository contenedorRepository,
                           ClienteRepository clienteRepository,
                           SolicitudRepository solicitudRepository) {
        this.contenedorRepository = contenedorRepository;
        this.clienteRepository = clienteRepository;
        this.solicitudRepository = solicitudRepository;
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

        // Estado por defecto o del DTO
        String estado = dto.getEstado() != null ? dto.getEstado() : "DISPONIBLE";
        contenedor.setEstado(estado);

        // Nuevos campos opcionales
        contenedor.setPesoReal(dto.getPesoReal());
        contenedor.setVolumenReal(dto.getVolumenReal());
        contenedor.setDepositoActualId(dto.getDepositoActualId());

        // Validar reglas de negocio antes de guardar
        validarReglasDeNegocio(contenedor);

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

        // Nuevos campos opcionales
        if (dto.getPesoReal() != null) {
            contenedor.setPesoReal(dto.getPesoReal());
        }
        if (dto.getVolumenReal() != null) {
            contenedor.setVolumenReal(dto.getVolumenReal());
        }
        if (dto.getDepositoActualId() != null) {
            contenedor.setDepositoActualId(dto.getDepositoActualId());
        }

        // Validar reglas de negocio antes de guardar
        validarReglasDeNegocio(contenedor);

        return contenedorRepository.save(contenedor);
    }

    /**
     * Elimina un contenedor aplicando validaciones de negocio.
     *
     * Validaciones:
     * 1. El contenedor NO debe estar EN_TRANSITO o EN_DEPOSITO
     * 2. El contenedor NO debe tener solicitudes activas (PROGRAMADA, EN_TRANSITO, ENTREGADA)
     *
     * @param idContenedor ID del contenedor a eliminar
     * @throws IllegalArgumentException si el contenedor no existe
     * @throws IllegalStateException si el contenedor está en tránsito/depósito o tiene solicitudes activas
     */
    public void eliminarContenedor(Long idContenedor) {
        // 1. Obtener el contenedor
        Contenedor contenedor = contenedorRepository.findById(idContenedor)
            .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado: " + idContenedor));
        
        // 2. VALIDACIÓN: No eliminar si está EN_TRANSITO o EN_DEPOSITO
        if ("EN_TRANSITO".equals(contenedor.getEstado()) || "EN_DEPOSITO".equals(contenedor.getEstado())) {
            log.warn("Intento de eliminar contenedor {} que está en estado: {}", idContenedor, contenedor.getEstado());
            throw new IllegalStateException(
                String.format("No se puede eliminar un contenedor que está en tránsito o en depósito. Estado actual: %s",
                    contenedor.getEstado())
            );
        }

        // 3. VALIDACIÓN: No eliminar si tiene solicitudes activas o históricas
        List<Solicitud> solicitudesAsociadas = solicitudRepository.findByIdContenedor(idContenedor);

        if (!solicitudesAsociadas.isEmpty()) {
            // Filtrar solicitudes en estados activos
            List<Solicitud> solicitudesActivas = solicitudesAsociadas.stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.PROGRAMADA
                          || s.getEstado() == EstadoSolicitud.EN_TRANSITO
                          || s.getEstado() == EstadoSolicitud.ENTREGADA)
                .toList();

            if (!solicitudesActivas.isEmpty()) {
                log.warn("Intento de eliminar contenedor {} con {} solicitudes activas",
                    idContenedor, solicitudesActivas.size());

                String estadosSolicitudes = solicitudesActivas.stream()
                    .map(s -> String.format("Solicitud %d (%s)", s.getNroSolicitud(), s.getEstado()))
                    .limit(3)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

                throw new IllegalStateException(
                    String.format("No se puede eliminar un contenedor con solicitudes activas o históricas. " +
                        "Solicitudes encontradas: %s%s",
                        estadosSolicitudes,
                        solicitudesActivas.size() > 3 ? " y " + (solicitudesActivas.size() - 3) + " más" : "")
                );
            }
        }

        // 4. Si pasa todas las validaciones, eliminar
        log.info("Eliminando contenedor {} (estado: {}, sin solicitudes activas)",
            idContenedor, contenedor.getEstado());
        contenedorRepository.delete(contenedor);
    }

    /**
     * Elimina un contenedor de un cliente específico aplicando validaciones de negocio.
     *
     * Validaciones:
     * 1. El contenedor debe pertenecer al cliente especificado
     * 2. El contenedor NO debe estar EN_TRANSITO o EN_DEPOSITO
     * 3. El contenedor NO debe tener solicitudes activas
     *
     * @param idCliente ID del cliente propietario
     * @param idContenedor ID del contenedor a eliminar
     * @throws IllegalArgumentException si el cliente o contenedor no existe, o no están relacionados
     * @throws IllegalStateException si el contenedor está en tránsito/depósito o tiene solicitudes activas
     */
    public void eliminarContenedorDeCliente(Long idCliente, Long idContenedor) {
        // 1. Validar que el cliente existe
        Cliente cliente = clienteRepository.findById(idCliente)
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado: " + idCliente));

        // 2. Validar que el contenedor existe
        Contenedor contenedor = contenedorRepository.findById(idContenedor)
            .orElseThrow(() -> new IllegalArgumentException("Contenedor no encontrado: " + idContenedor));

        // 3. Validar que el contenedor pertenece al cliente
        if (!contenedor.getCliente().getIdCliente().equals(idCliente)) {
            log.warn("Intento de eliminar contenedor {} que no pertenece al cliente {}", idContenedor, idCliente);
            throw new IllegalArgumentException("El contenedor no pertenece al cliente especificado");
        }

        // 4. VALIDACIÓN: No eliminar si está EN_TRANSITO o EN_DEPOSITO
        if ("EN_TRANSITO".equals(contenedor.getEstado()) || "EN_DEPOSITO".equals(contenedor.getEstado())) {
            log.warn("Intento de eliminar contenedor {} del cliente {} que está en estado: {}",
                idContenedor, idCliente, contenedor.getEstado());
            throw new IllegalStateException(
                String.format("No se puede eliminar un contenedor que está en tránsito o en depósito. Estado actual: %s",
                    contenedor.getEstado())
            );
        }

        // 5. VALIDACIÓN: No eliminar si tiene solicitudes activas
        List<Solicitud> solicitudesAsociadas = solicitudRepository.findByIdContenedor(idContenedor);

        if (!solicitudesAsociadas.isEmpty()) {
            List<Solicitud> solicitudesActivas = solicitudesAsociadas.stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.PROGRAMADA
                          || s.getEstado() == EstadoSolicitud.EN_TRANSITO
                          || s.getEstado() == EstadoSolicitud.ENTREGADA)
                .toList();

            if (!solicitudesActivas.isEmpty()) {
                log.warn("Intento de eliminar contenedor {} del cliente {} con {} solicitudes activas",
                    idContenedor, idCliente, solicitudesActivas.size());

                String estadosSolicitudes = solicitudesActivas.stream()
                    .map(s -> String.format("Solicitud %d (%s)", s.getNroSolicitud(), s.getEstado()))
                    .limit(3)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

                throw new IllegalStateException(
                    String.format("No se puede eliminar un contenedor con solicitudes activas o históricas. " +
                        "Solicitudes encontradas: %s%s",
                        estadosSolicitudes,
                        solicitudesActivas.size() > 3 ? " y " + (solicitudesActivas.size() - 3) + " más" : "")
                );
            }
        }

        // 6. Si pasa todas las validaciones, eliminar
        log.info("Eliminando contenedor {} del cliente {} (estado: {}, sin solicitudes activas)",
            idContenedor, idCliente, contenedor.getEstado());
        contenedorRepository.delete(contenedor);
    }

    /**
     * Valida las reglas de negocio del estado del contenedor con respecto a depositoActualId.
     *
     * Reglas:
     * - EN_ORIGEN: no puede tener depositoActualId
     * - EN_TRANSITO: nunca debe tener depositoActualId
     * - EN_DEPOSITO: debe tener depositoActualId obligatorio
     * - ENTREGADO: debe tener depositoActualId = null
     *
     * @param contenedor el contenedor a validar
     * @throws IllegalArgumentException si las reglas no se cumplen
     */
    private void validarReglasDeNegocio(Contenedor contenedor) {
        String estado = contenedor.getEstado();
        Long depositoId = contenedor.getDepositoActualId();

        if (estado == null) {
            return; // Si no hay estado, no validamos
        }

        switch (estado.toUpperCase()) {
            case "EN_ORIGEN":
                if (depositoId != null) {
                    throw new IllegalArgumentException(
                        "Un contenedor EN_ORIGEN no puede tener depositoActualId. Debe ser null."
                    );
                }
                break;

            case "EN_TRANSITO":
                if (depositoId != null) {
                    throw new IllegalArgumentException(
                        "Un contenedor EN_TRANSITO no puede tener depositoActualId. Debe ser null."
                    );
                }
                break;

            case "EN_DEPOSITO":
                if (depositoId == null) {
                    throw new IllegalArgumentException(
                        "Un contenedor EN_DEPOSITO debe tener depositoActualId obligatorio. No puede ser null."
                    );
                }
                break;

            case "ENTREGADO":
                if (depositoId != null) {
                    throw new IllegalArgumentException(
                        "Un contenedor ENTREGADO no puede tener depositoActualId. Debe ser null."
                    );
                }
                break;

            case "DISPONIBLE":
                // DISPONIBLE no tiene restricciones sobre depositoActualId
                break;

            default:
                // Estados desconocidos no generan error, solo se ignoran
                break;
        }
    }

    /**
     * Obtiene todos los contenedores en un depósito específico.
     *
     * Filtra contenedores con:
     * - estado = "EN_DEPOSITO"
     * - depositoActualId = id especificado
     *
     * @param depositoId ID del depósito
     * @return Lista de contenedores en el depósito
     */
    public List<Contenedor> obtenerContenedoresPorDeposito(Long depositoId) {
        return contenedorRepository.findByEstadoAndDepositoActualId("EN_DEPOSITO", depositoId);
    }
}
