package com.tpi.solicitudes.service;

import com.tpi.solicitudes.domain.Contenedor;
import com.tpi.solicitudes.domain.EstadoSolicitud;
import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.repository.ClienteRepository;
import com.tpi.solicitudes.repository.SolicitudRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
public class SolicitudService {

    private final SolicitudRepository repository;
    private final ContenedorService contenedorService;
    private final ClienteRepository clienteRepository;

    public SolicitudService(SolicitudRepository repository,
                          ContenedorService contenedorService,
                          ClienteRepository clienteRepository) {
        this.repository = repository;
        this.contenedorService = contenedorService;
        this.clienteRepository = clienteRepository;
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

    /**
     * Crea una nueva solicitud con estado inicial BORRADOR.
     *
     * Validaciones:
     * - El cliente DEBE existir en el sistema
     * - El contenedor NO debe estar EN_TRANSITO (ya en uso)
     * - El contenedor NO debe estar EN_DEPOSITO (esperando otro transporte)
     *
     * Ciclo de estados:
     * BORRADOR → PROGRAMADA (al seleccionar ruta)
     * PROGRAMADA → EN_TRANSITO (al iniciar primer tramo)
     * EN_TRANSITO → ENTREGADA (al finalizar último tramo)
     *
     * @param s Solicitud a crear
     * @return Solicitud creada con estado BORRADOR
     * @throws IllegalArgumentException si el cliente no existe
     * @throws IllegalStateException si el contenedor está en tránsito o en depósito
     */
    public Solicitud create(Solicitud s) {
        log.info("Creando nueva solicitud para cliente: {}", s.getIdCliente());

        // VALIDACIÓN 1: Verificar que el cliente exista
        if (s.getIdCliente() != null) {
            boolean clienteExiste = clienteRepository.existsById(s.getIdCliente());
            if (!clienteExiste) {
                log.error("Intento de crear solicitud con cliente inexistente: {}", s.getIdCliente());
                throw new IllegalArgumentException(
                    String.format("No se puede crear la solicitud. El cliente con ID %d no está registrado en el sistema.",
                        s.getIdCliente())
                );
            }
            log.debug("Cliente {} validado correctamente", s.getIdCliente());
        }

        // VALIDACIÓN 2: Validar estado del contenedor si se especifica
        if (s.getIdContenedor() != null) {
            validarEstadoContenedor(s.getIdContenedor());
        }

        s.setNroSolicitud(null); // Generado por BD

        // Establecer estado inicial BORRADOR si no viene especificado
        if (s.getEstado() == null) {
            s.setEstado(EstadoSolicitud.BORRADOR);
            log.debug("Estado inicial establecido: BORRADOR");
        }

        Solicitud created = repository.save(s);
        log.info("Solicitud creada exitosamente con ID: {} en estado: {}",
            created.getNroSolicitud(), created.getEstado());
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

    /**
     * Cambia el estado de una solicitud a PROGRAMADA.
     * Esto ocurre cuando se selecciona una ruta para ejecutar.
     *
     * Transición válida: BORRADOR → PROGRAMADA
     *
     * @param nroSolicitud ID de la solicitud
     * @throws IllegalStateException si la transición no es válida
     */
    public void cambiarAProgramada(Long nroSolicitud) {
        Solicitud solicitud = findById(nroSolicitud);

        // Validar transición
        if (solicitud.getEstado() != EstadoSolicitud.BORRADOR) {
            log.warn("Intento de cambiar a PROGRAMADA desde estado inválido: {} para solicitud {}",
                solicitud.getEstado(), nroSolicitud);
            throw new IllegalStateException(
                String.format("No se puede cambiar a PROGRAMADA desde estado %s. Estado actual debe ser BORRADOR.",
                    solicitud.getEstado())
            );
        }

        solicitud.setEstado(EstadoSolicitud.PROGRAMADA);
        repository.save(solicitud);
        log.info("Solicitud {} cambiada a estado PROGRAMADA", nroSolicitud);
    }

    /**
     * Cambia el estado de una solicitud a EN_TRANSITO.
     * Esto ocurre cuando se inicia el primer tramo de la ruta seleccionada.
     *
     * Transición válida: PROGRAMADA → EN_TRANSITO
     *
     * @param nroSolicitud ID de la solicitud
     * @throws IllegalStateException si la transición no es válida
     */
    public void cambiarAEnTransito(Long nroSolicitud) {
        Solicitud solicitud = findById(nroSolicitud);

        // Validar transición
        if (solicitud.getEstado() != EstadoSolicitud.PROGRAMADA) {
            log.warn("Intento de cambiar a EN_TRANSITO desde estado inválido: {} para solicitud {}",
                solicitud.getEstado(), nroSolicitud);
            throw new IllegalStateException(
                String.format("No se puede cambiar a EN_TRANSITO desde estado %s. Estado actual debe ser PROGRAMADA.",
                    solicitud.getEstado())
            );
        }

        solicitud.setEstado(EstadoSolicitud.EN_TRANSITO);
        repository.save(solicitud);
        log.info("Solicitud {} cambiada a estado EN_TRANSITO", nroSolicitud);
    }

    /**
     * Cambia el estado de una solicitud a ENTREGADA.
     * Esto ocurre cuando se finaliza el último tramo de la ruta.
     *
     * Transición válida: EN_TRANSITO → ENTREGADA
     * También se acepta: COMPLETADA → ENTREGADA (legacy)
     *
     * @param nroSolicitud ID de la solicitud
     * @throws IllegalStateException si la transición no es válida
     */
    public void cambiarAEntregada(Long nroSolicitud) {
        Solicitud solicitud = findById(nroSolicitud);

        // Validar transición (permitir desde EN_TRANSITO o COMPLETADA)
        if (solicitud.getEstado() != EstadoSolicitud.EN_TRANSITO &&
            solicitud.getEstado() != EstadoSolicitud.COMPLETADA) {
            log.warn("Intento de cambiar a ENTREGADA desde estado inválido: {} para solicitud {}",
                solicitud.getEstado(), nroSolicitud);
            throw new IllegalStateException(
                String.format("No se puede cambiar a ENTREGADA desde estado %s. Estado actual debe ser EN_TRANSITO o COMPLETADA.",
                    solicitud.getEstado())
            );
        }

        solicitud.setEstado(EstadoSolicitud.ENTREGADA);
        repository.save(solicitud);
        log.info("Solicitud {} cambiada a estado ENTREGADA", nroSolicitud);
    }

    /**
     * Valida si una transición de estado es válida para una solicitud.
     *
     * Ciclo válido:
     * BORRADOR → PROGRAMADA → EN_TRANSITO → ENTREGADA
     *
     * @param estadoActual Estado actual de la solicitud
     * @param nuevoEstado Estado al que se quiere transicionar
     * @return true si la transición es válida, false en caso contrario
     */
    public boolean esTransicionValida(EstadoSolicitud estadoActual, EstadoSolicitud nuevoEstado) {
        if (estadoActual == null || nuevoEstado == null) {
            return false;
        }

        return switch (estadoActual) {
            case BORRADOR -> nuevoEstado == EstadoSolicitud.PROGRAMADA || nuevoEstado == EstadoSolicitud.CANCELADA;
            case PROGRAMADA -> nuevoEstado == EstadoSolicitud.EN_TRANSITO || nuevoEstado == EstadoSolicitud.CANCELADA;
            case EN_TRANSITO -> nuevoEstado == EstadoSolicitud.ENTREGADA || nuevoEstado == EstadoSolicitud.COMPLETADA;
            case COMPLETADA -> nuevoEstado == EstadoSolicitud.ENTREGADA; // Permitir transición legacy
            case ENTREGADA, CANCELADA -> false; // Estados finales
        };
    }

    /**
     * Valida que un contenedor esté disponible para ser usado en una nueva solicitud.
     *
     * Un contenedor NO puede usarse si está:
     * - EN_TRANSITO: Ya está siendo transportado en otra solicitud
     * - EN_DEPOSITO: Está esperando ser transportado en otra solicitud
     *
     * Estados válidos para nueva solicitud:
     * - DISPONIBLE: Listo para usar
     * - ENTREGADO: Entregado previamente, puede reutilizarse
     * - null: Sin estado definido (se asume disponible)
     *
     * @param idContenedor ID del contenedor a validar
     * @throws IllegalStateException si el contenedor está EN_TRANSITO o EN_DEPOSITO
     */
    private void validarEstadoContenedor(Long idContenedor) {
        try {
            Optional<Contenedor> contenedorOpt = contenedorService.obtenerPorId(idContenedor);

            if (contenedorOpt.isEmpty()) {
                log.warn("Contenedor {} no encontrado, se permite crear solicitud", idContenedor);
                return; // Permitir si el contenedor no existe (validación de FK en BD)
            }

            Contenedor contenedor = contenedorOpt.get();
            String estado = contenedor.getEstado();

            // Validar estados no permitidos
            if ("EN_TRANSITO".equals(estado)) {
                log.error("Intento de crear solicitud con contenedor {} en tránsito", idContenedor);
                throw new IllegalStateException(
                    String.format("No se puede crear una solicitud con el contenedor %d porque está EN_TRANSITO. " +
                        "El contenedor ya está siendo transportado en otra solicitud.", idContenedor)
                );
            }

            if ("EN_DEPOSITO".equals(estado)) {
                log.error("Intento de crear solicitud con contenedor {} en depósito", idContenedor);
                throw new IllegalStateException(
                    String.format("No se puede crear una solicitud con el contenedor %d porque está EN_DEPOSITO. " +
                        "El contenedor está esperando ser transportado en otra solicitud.", idContenedor)
                );
            }

            // Estados válidos: DISPONIBLE, ENTREGADO, EN_ORIGEN, o null
            log.debug("Contenedor {} en estado '{}' - válido para nueva solicitud", idContenedor, estado);

        } catch (IllegalStateException e) {
            // Re-lanzar errores de validación
            throw e;
        } catch (Exception e) {
            log.error("Error validando estado del contenedor {}: {}", idContenedor, e.getMessage(), e);
            // En caso de error técnico, permitir la creación (la validación de negocio se hará después)
        }
    }
}
