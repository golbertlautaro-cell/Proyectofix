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
import com.tpi.solicitudes.repository.TramoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class RutaService {

    private final RutaRepository rutaRepository;
    private final SolicitudRepository solicitudRepository;
    private final TramoRepository tramoRepository;
    private final TramoService tramoService;
    private final SolicitudService solicitudService;

    public RutaService(RutaRepository rutaRepository,
                       SolicitudRepository solicitudRepository,
                       TramoRepository tramoRepository,
                       TramoService tramoService,
                       SolicitudService solicitudService) {
        this.rutaRepository = rutaRepository;
        this.solicitudRepository = solicitudRepository;
        this.tramoRepository = tramoRepository;
        this.tramoService = tramoService;
        this.solicitudService = solicitudService;
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
     * Elimina una ruta aplicando validaciones de negocio.
     *
     * Validaciones:
     * 1. Si la ruta está seleccionada (esRutaSeleccionada = true) y tiene tramos en estado
     *    ASIGNADO, INICIADO o FINALIZADO → NO se puede eliminar
     * 2. Si la solicitud asociada está EN_TRANSITO o ENTREGADA → NO se puede eliminar
     *
     * @param idRuta ID de la ruta a eliminar
     * @throws IllegalArgumentException si la ruta no existe
     * @throws IllegalStateException si la ruta tiene tramos en ejecución o la solicitud está activa
     */
    public void eliminarRuta(Long idRuta) {
        // 1. Obtener la ruta
        Ruta ruta = rutaRepository.findById(idRuta)
            .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));
        
        // 2. VALIDACIÓN: Si la ruta está seleccionada, verificar estado de tramos
        if (Boolean.TRUE.equals(ruta.getEsRutaSeleccionada())) {
            List<Tramo> tramosRuta = tramoRepository.findByRutaIdRuta(idRuta);

            // Verificar si hay tramos en estado ASIGNADO, INICIADO o FINALIZADO
            List<Tramo> tramosEnEjecucionOFinalizados = tramosRuta.stream()
                .filter(t -> t.getEstado() != null)
                .filter(t -> t.getEstado().name().equals("ASIGNADO")
                          || t.getEstado().name().equals("INICIADO")
                          || t.getEstado().name().equals("FINALIZADO"))
                .toList();

            if (!tramosEnEjecucionOFinalizados.isEmpty()) {
                log.warn("Intento de eliminar ruta seleccionada {} con {} tramos en ejecución/finalizados",
                    idRuta, tramosEnEjecucionOFinalizados.size());

                String estadosTramos = tramosEnEjecucionOFinalizados.stream()
                    .map(t -> String.format("Tramo %d (%s)", t.getIdTramo(), t.getEstado()))
                    .limit(3)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");

                throw new IllegalStateException(
                    String.format("No se puede eliminar una ruta seleccionada con tramos en ejecución o finalizados. " +
                        "Tramos encontrados: %s%s",
                        estadosTramos,
                        tramosEnEjecucionOFinalizados.size() > 3 ? " y " + (tramosEnEjecucionOFinalizados.size() - 3) + " más" : "")
                );
            }
        }

        // 3. VALIDACIÓN: Verificar estado de la solicitud asociada
        if (ruta.getSolicitud() != null) {
            Solicitud solicitud = ruta.getSolicitud();

            if (solicitud.getEstado() == EstadoSolicitud.EN_TRANSITO
                || solicitud.getEstado() == EstadoSolicitud.ENTREGADA) {
                log.warn("Intento de eliminar ruta {} de solicitud {} que está en estado: {}",
                    idRuta, solicitud.getNroSolicitud(), solicitud.getEstado());

                throw new IllegalStateException(
                    String.format("No se puede eliminar rutas de una solicitud en tránsito o entregada. " +
                        "Solicitud %d está en estado: %s",
                        solicitud.getNroSolicitud(), solicitud.getEstado())
                );
            }
        }

        // 4. Si pasa todas las validaciones, eliminar
        // Si estaba seleccionada, deseleccionar primero
        if (Boolean.TRUE.equals(ruta.getEsRutaSeleccionada())) {
            ruta.setEsRutaSeleccionada(false);
        }
        
        log.info("Eliminando ruta {} (seleccionada: {}, solicitud estado: {})",
            idRuta,
            ruta.getEsRutaSeleccionada(),
            ruta.getSolicitud() != null ? ruta.getSolicitud().getEstado() : "N/A");

        rutaRepository.delete(ruta);
    }

    /**
     * Selecciona una ruta para ejecutar y elimina automáticamente las rutas alternativas.
     * Al seleccionar una ruta, la solicitud pasa de BORRADOR a PROGRAMADA.
     *
     * Validaciones:
     * - La ruta DEBE estar asociada a una solicitud
     * - La solicitud NO debe estar EN_TRANSITO (ya en ejecución)
     * - La solicitud NO debe estar ENTREGADA (ya completada)
     *
     * Acción automática:
     * - Elimina permanentemente todas las rutas NO seleccionadas de la misma solicitud
     *
     * Transición de Solicitud: BORRADOR → PROGRAMADA
     *
     * @param idRuta ID de la ruta a seleccionar
     * @return Ruta seleccionada
     * @throws IllegalArgumentException si la ruta no tiene solicitud asociada
     * @throws IllegalStateException si la solicitud está EN_TRANSITO o ENTREGADA
     */
    public Ruta seleccionarRuta(Long idRuta) {
        Ruta ruta = rutaRepository.findById(idRuta)
            .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));

        // VALIDACIÓN 1: Verificar que la ruta esté asociada a una solicitud
        Solicitud solicitud = ruta.getSolicitud();
        if (solicitud == null) {
            throw new IllegalArgumentException(
                String.format("No se puede seleccionar la ruta %d. La ruta debe estar asociada a una solicitud.",
                    idRuta)
            );
        }

        // VALIDACIÓN 2: Validar estado de la solicitud
        validarSolicitudPermiteCambioRuta(solicitud);

        Long nroSolicitud = solicitud.getNroSolicitud();

        // Obtener todas las rutas de la solicitud para eliminar las no seleccionadas
        List<Ruta> rutasSolicitud = rutaRepository.findBySolicitud_NroSolicitud(nroSolicitud);

        // Filtrar las rutas que NO son la que se está seleccionando
        List<Ruta> rutasAEliminar = rutasSolicitud.stream()
            .filter(r -> !r.getIdRuta().equals(idRuta))
            .toList();

        // Seleccionar esta ruta
        ruta.setEsRutaSeleccionada(true);
        ruta.setEstado(EstadoRuta.PENDIENTE);

        // Guardar la ruta seleccionada primero
        Ruta rutaSeleccionada = rutaRepository.save(ruta);

        // Eliminar las rutas alternativas automáticamente
        if (!rutasAEliminar.isEmpty()) {
            rutasAEliminar.forEach(rutaAlternativa -> {
                log.info("Eliminando automáticamente ruta alternativa {} de solicitud {}",
                    rutaAlternativa.getIdRuta(), nroSolicitud);
                rutaRepository.delete(rutaAlternativa);
            });
            log.info("Se eliminaron automáticamente {} rutas alternativas de la solicitud {}",
                rutasAEliminar.size(), nroSolicitud);
        }

        // Cambiar solicitud a PROGRAMADA usando el método del servicio
        solicitudService.cambiarAProgramada(nroSolicitud);

        log.info("Ruta {} seleccionada para solicitud {}. Rutas alternativas eliminadas: {}",
            idRuta, nroSolicitud, rutasAEliminar.size());

        return rutaSeleccionada;
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

    /**
     * Cambia el estado de una ruta a EJECUTANDOSE.
     * Esto ocurre cuando se inicia el primer tramo de la ruta.
     *
     * Transición válida: PENDIENTE → EJECUTANDOSE
     *
     * @param idRuta ID de la ruta
     * @throws IllegalStateException si la transición no es válida
     */
    public void cambiarAEjecutandose(Long idRuta) {
        Ruta ruta = rutaRepository.findById(idRuta)
            .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));

        // Validar transición
        if (ruta.getEstado() != null && ruta.getEstado() != EstadoRuta.PENDIENTE) {
            throw new IllegalStateException(
                String.format("No se puede cambiar a EJECUTANDOSE desde estado %s. Estado actual debe ser PENDIENTE.",
                    ruta.getEstado())
            );
        }

        ruta.setEstado(EstadoRuta.EJECUTANDOSE);
        rutaRepository.save(ruta);
    }

    /**
     * Cambia el estado de una ruta a COMPLETADA.
     * Esto ocurre cuando se finalizan todos los tramos de la ruta.
     *
     * Transición válida: EJECUTANDOSE → COMPLETADA
     *
     * @param idRuta ID de la ruta
     * @throws IllegalStateException si la transición no es válida
     */
    public void cambiarACompletada(Long idRuta) {
        Ruta ruta = rutaRepository.findById(idRuta)
            .orElseThrow(() -> new IllegalArgumentException("Ruta no encontrada: " + idRuta));

        // Validar transición
        if (ruta.getEstado() != EstadoRuta.EJECUTANDOSE) {
            throw new IllegalStateException(
                String.format("No se puede cambiar a COMPLETADA desde estado %s. Estado actual debe ser EJECUTANDOSE.",
                    ruta.getEstado())
            );
        }

        ruta.setEstado(EstadoRuta.COMPLETADA);
        rutaRepository.save(ruta);
    }

    /**
     * Valida si una transición de estado es válida para una ruta.
     *
     * Ciclo válido:
     * PENDIENTE → EJECUTANDOSE → COMPLETADA
     *
     * @param estadoActual Estado actual de la ruta
     * @param nuevoEstado Estado al que se quiere transicionar
     * @return true si la transición es válida, false en caso contrario
     */
    public boolean esTransicionValida(EstadoRuta estadoActual, EstadoRuta nuevoEstado) {
        if (estadoActual == null || nuevoEstado == null) {
            return false;
        }

        return switch (estadoActual) {
            case PENDIENTE -> nuevoEstado == EstadoRuta.EJECUTANDOSE || nuevoEstado == EstadoRuta.CANCELADA;
            case EJECUTANDOSE -> nuevoEstado == EstadoRuta.COMPLETADA;
            case COMPLETADA, CANCELADA, DESCARTADA -> false; // Estados finales
        };
    }

    /**
     * Valida que una solicitud permita cambiar/seleccionar una ruta.
     *
     * No se puede cambiar de ruta si la solicitud está:
     * - EN_TRANSITO: Ya se está ejecutando un transporte
     * - ENTREGADA: El transporte ya fue completado
     *
     * Estados que permiten cambio de ruta:
     * - BORRADOR: Aún no se ha iniciado nada
     * - PROGRAMADA: Se puede cambiar la ruta seleccionada
     * - CANCELADA: Se podría reactivar (no implementado aún)
     *
     * @param solicitud La solicitud a validar
     * @throws IllegalStateException si la solicitud está EN_TRANSITO o ENTREGADA
     */
    private void validarSolicitudPermiteCambioRuta(Solicitud solicitud) {
        EstadoSolicitud estado = solicitud.getEstado();

        if (estado == EstadoSolicitud.EN_TRANSITO) {
            throw new IllegalStateException(
                String.format("No se puede cambiar la ruta de la solicitud %d porque está EN_TRANSITO. " +
                    "El transporte ya está en ejecución.", solicitud.getNroSolicitud())
            );
        }

        if (estado == EstadoSolicitud.ENTREGADA) {
            throw new IllegalStateException(
                String.format("No se puede cambiar la ruta de la solicitud %d porque está ENTREGADA. " +
                    "El transporte ya fue completado.", solicitud.getNroSolicitud())
            );
        }

        // Estados permitidos: BORRADOR, PROGRAMADA, CANCELADA, COMPLETADA
    }

    /**
     * Elimina todas las rutas NO seleccionadas de una solicitud.
     *
     * Este método es útil para limpiar rutas alternativas una vez que
     * se ha seleccionado y confirmado una ruta definitiva.
     *
     * ADVERTENCIA: Esta operación es irreversible y eliminará permanentemente
     * todas las rutas alternativas junto con sus tramos asociados.
     *
     * @param nroSolicitud ID de la solicitud
     * @return Número de rutas eliminadas
     */
    @Transactional
    public int eliminarRutasNoSeleccionadas(Long nroSolicitud) {
        List<Ruta> todasLasRutas = rutaRepository.findBySolicitud_NroSolicitud(nroSolicitud);

        List<Ruta> rutasNoSeleccionadas = todasLasRutas.stream()
            .filter(r -> !Boolean.TRUE.equals(r.getEsRutaSeleccionada()))
            .toList();

        if (rutasNoSeleccionadas.isEmpty()) {
            log.info("No hay rutas no seleccionadas para eliminar en solicitud {}", nroSolicitud);
            return 0;
        }

        // Eliminar cada ruta no seleccionada
        rutasNoSeleccionadas.forEach(ruta -> {
            log.info("Eliminando ruta no seleccionada {} de solicitud {}",
                ruta.getIdRuta(), nroSolicitud);
            rutaRepository.delete(ruta);
        });

        int eliminadas = rutasNoSeleccionadas.size();
        log.info("Se eliminaron {} rutas no seleccionadas de la solicitud {}",
            eliminadas, nroSolicitud);

        return eliminadas;
    }

    /**
     * Selecciona una ruta y elimina automáticamente las rutas alternativas.
     *
     * NOTA: Este método ahora es equivalente a seleccionarRuta() ya que
     * la eliminación automática de rutas alternativas es el comportamiento por defecto.
     * Se mantiene por compatibilidad con código existente.
     *
     * @param idRuta ID de la ruta a seleccionar
     * @return Ruta seleccionada
     * @deprecated Usar seleccionarRuta() directamente, que ahora elimina automáticamente las alternativas
     */
    @Transactional
    @Deprecated
    public Ruta seleccionarRutaYEliminarAlternativas(Long idRuta) {
        // Simplemente delegar a seleccionarRuta que ahora hace todo automáticamente
        return seleccionarRuta(idRuta);
    }
}
