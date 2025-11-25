package com.tpi.solicitudes.service;

import com.tpi.solicitudes.client.GoogleMapsClient;
import com.tpi.solicitudes.client.LogisticaClient;
import com.tpi.solicitudes.config.PricingProperties;
import com.tpi.solicitudes.domain.Contenedor;
import com.tpi.solicitudes.domain.EstadoRuta;
import com.tpi.solicitudes.domain.EstadoSolicitud;
import com.tpi.solicitudes.domain.EstadoTramo;
import com.tpi.solicitudes.domain.Ruta;
import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.domain.Tramo;
import com.tpi.solicitudes.dto.ContenedorUpdateDto;
import com.tpi.solicitudes.dto.DistanciaDTO;
import com.tpi.solicitudes.dto.TramoCreateDto;
import com.tpi.solicitudes.repository.RutaRepository;
import com.tpi.solicitudes.repository.SolicitudRepository;
import com.tpi.solicitudes.repository.TramoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
public class TramoService {

    private final TramoRepository tramoRepository;
    private final SolicitudRepository solicitudRepository;
    private final RutaRepository rutaRepository;
    private final Optional<LogisticaClient> logisticaClient;
    private final Optional<GoogleMapsClient> googleMapsClient;
    private final GoogleMapsService googleMapsService;
    private final PricingProperties pricingProperties;
    private final ContenedorService contenedorService;

    public TramoService(TramoRepository tramoRepository,
                        SolicitudRepository solicitudRepository,
                        RutaRepository rutaRepository,
                        @Autowired(required = false) LogisticaClient logisticaClient,
                        @Autowired(required = false) GoogleMapsClient googleMapsClient,
                        GoogleMapsService googleMapsService,
                        PricingProperties pricingProperties,
                        ContenedorService contenedorService) {
        this.tramoRepository = tramoRepository;
        this.solicitudRepository = solicitudRepository;
        this.rutaRepository = rutaRepository;
        this.logisticaClient = Optional.ofNullable(logisticaClient);
        this.googleMapsClient = Optional.ofNullable(googleMapsClient);
        this.googleMapsService = googleMapsService;
        this.pricingProperties = pricingProperties;
        this.contenedorService = contenedorService;
    }

    public Page<Tramo> findAll(Pageable pageable) {
        return tramoRepository.findAll(pageable);
    }

    public List<Tramo> obtenerTodos() {
        return tramoRepository.findAll();
    }

    public List<Tramo> listarPorSolicitud(Long solicitudId) { // legacy
        return tramoRepository.findAllBySolicitudNroSolicitud(solicitudId);
    }

    public Page<Tramo> listarPorSolicitud(Long solicitudId, Pageable pageable) {
        return tramoRepository.findPageBySolicitudNroSolicitud(solicitudId, pageable);
    }

    public Page<Tramo> listar(Pageable pageable, String estado, String dominioCamion,
                               LocalDateTime desde, LocalDateTime hasta) {
        boolean hasEstado = estado != null && !estado.isBlank();
        boolean hasDominio = dominioCamion != null && !dominioCamion.isBlank();

        // Normalizar rango si no viene alguno de los extremos
        LocalDateTime from = (desde != null) ? desde : LocalDateTime.of(1970, 1, 1, 0, 0);
        LocalDateTime to = (hasta != null) ? hasta : LocalDateTime.of(9999, 12, 31, 23, 59, 59);

        if (hasEstado && hasDominio) {
            return tramoRepository.findByEstadoAndDominioCamionAndFechaHoraInicioRealBetween(estado, dominioCamion, from, to, pageable);
        } else if (hasEstado) {
            return tramoRepository.findByEstadoAndFechaHoraInicioRealBetween(estado, from, to, pageable);
        } else if (hasDominio) {
            return tramoRepository.findByDominioCamionAndFechaHoraInicioRealBetween(dominioCamion, from, to, pageable);
        } else {
            return tramoRepository.findByFechaHoraInicioRealBetween(from, to, pageable);
        }
    }

    public Tramo obtener(Long id) {
        return tramoRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Tramo no encontrado: " + id));
    }

    public Tramo crear(Long solicitudId, Tramo tramo) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new NoSuchElementException("Solicitud no encontrada: " + solicitudId));
        tramo.setIdTramo(null);
        // Legacy: Crear un tramo vinculado a una solicitud (no a ruta)
        // Este método se mantiene por backward compatibility
        return tramoRepository.save(tramo);
    }

    /**
     * Crea un nuevo tramo vinculado a una ruta específica
     * @param rutaId ID de la ruta
     * @param dto Datos del tramo a crear
     * @return Tramo creado y guardado
     */
    public Tramo crearTramoEnRuta(Long rutaId, TramoCreateDto dto) {
        Ruta ruta = rutaRepository.findById(rutaId)
                .orElseThrow(() -> new NoSuchElementException("Ruta no encontrada: " + rutaId));
        
        // Validar reglas de negocio del origen y destino
        validarOrigenDestino(dto.getOrigenDepositoId(), dto.getOrigenDireccionLibre(), "origen");
        validarOrigenDestino(dto.getDestinoDepositoId(), dto.getDestinoDireccionLibre(), "destino");

        Tramo tramo = new Tramo();
        tramo.setOrigen(dto.getOrigen());
        tramo.setDestino(dto.getDestino());
        tramo.setDominioCamion(dto.getDominioCamion());
        tramo.setFechaHoraInicioEstimada(dto.getFechaHoraInicioEstimada());
        tramo.setFechaHoraFinEstimada(dto.getFechaHoraFinEstimada());

        // Setear depósitos y direcciones libres según los campos nuevos del DTO
        tramo.setOrigenDepositoId(dto.getOrigenDepositoId());
        tramo.setDestinoDepositoId(dto.getDestinoDepositoId());
        tramo.setOrigenDireccionLibre(dto.getOrigenDireccionLibre());
        tramo.setDestinoDireccionLibre(dto.getDestinoDireccionLibre());

        // Si el DTO trae valores de estadía reales los propagamos
        if (dto.getTiempoEstadiaHoras() != null) {
            tramo.setTiempoEstadiaHoras(dto.getTiempoEstadiaHoras());
        }
        if (dto.getCostoEstadiaReal() != null) {
            tramo.setCostoEstadiaReal(dto.getCostoEstadiaReal());
        }

        // Setear orden (obligatorio)
        if (dto.getOrden() != null) {
            tramo.setOrden(dto.getOrden());
        } else {
            // Si no viene, asignar el siguiente orden disponible en la ruta
            List<Tramo> tramosExistentes = tramoRepository.findByRutaIdRuta(rutaId);
            int siguienteOrden = tramosExistentes.stream()
                .mapToInt(Tramo::getOrden)
                .max()
                .orElse(0) + 1;
            tramo.setOrden(siguienteOrden);
        }

        completarEstimacionesTramo(tramo, dto.getOrigen(), dto.getDestino(), 0.0);
        tramo.setRuta(ruta);
        tramo.setEstado(EstadoTramo.ESTIMADO);  // Estado inicial: ESTIMADO

        return tramoRepository.save(tramo);
    }

    /**
     * Valida que SOLO haya UNA forma válida de origen/destino:
     * - Depósito (depositoId != null, direccionLibre = null)
     * - Dirección libre (depositoId = null, direccionLibre != null)
     *
     * @param depositoId ID del depósito
     * @param direccionLibre Dirección libre
     * @param tipo "origen" o "destino" (para mensaje de error)
     * @throws IllegalArgumentException si las reglas no se cumplen (error 422)
     */
    private void validarOrigenDestino(Long depositoId, String direccionLibre, String tipo) {
        boolean tieneDeposito = depositoId != null;
        boolean tieneDireccion = direccionLibre != null && !direccionLibre.trim().isEmpty();

        if (!tieneDeposito && !tieneDireccion) {
            throw new IllegalArgumentException(
                String.format("El %s debe especificarse mediante depositoId O direccionLibre. Ambos son null.", tipo)
            );
        }

        if (tieneDeposito && tieneDireccion) {
            throw new IllegalArgumentException(
                String.format("El %s solo puede tener UNA forma: depositoId O direccionLibre, no ambos.", tipo)
            );
        }
    }

    /**
     * Valida las reglas de origen y destino para un tramo completo.
     * Aplica las validaciones tanto para origen como para destino.
     *
     * Reglas para ORIGEN:
     * - Si origenDepositoId != null → origenDireccionLibre debe ser null
     * - Si origenDepositoId == null → origenDireccionLibre debe existir y no estar vacía
     * - Si ambos null → error
     * - Si ambos tienen valor → error
     *
     * Reglas para DESTINO (mismas que origen):
     * - Si destinoDepositoId != null → destinoDireccionLibre debe ser null
     * - Si destinoDepositoId == null → destinoDireccionLibre debe existir y no estar vacía
     * - Si ambos null → error
     * - Si ambos tienen valor → error
     *
     * @param tramo El tramo a validar
     * @throws IllegalArgumentException si alguna regla no se cumple (error 422)
     */
    private void validarOrigenDestino(Tramo tramo) {
        // Validar origen
        validarOrigenDestino(tramo.getOrigenDepositoId(), tramo.getOrigenDireccionLibre(), "origen");

        // Validar destino
        validarOrigenDestino(tramo.getDestinoDepositoId(), tramo.getDestinoDireccionLibre(), "destino");
    }

    /**
     * Valida la transición de estado de un tramo.
     *
     * Transiciones válidas:
     * - ESTIMADO → ASIGNADO (al asignar camión)
     * - ASIGNADO → INICIADO (al iniciar tramo)
     * - INICIADO → FINALIZADO (al finalizar tramo)
     *
     * @param estadoActual Estado actual del tramo
     * @param nuevoEstado Nuevo estado al que se quiere transicionar
     * @throws IllegalStateException si la transición no es válida
     */
    private void validarTransicionEstado(EstadoTramo estadoActual, EstadoTramo nuevoEstado) {
        if (estadoActual == null) {
            return; // Permitir si no hay estado actual (creación)
        }

        boolean transicionValida = false;

        switch (estadoActual) {
            case ESTIMADO:
            case PENDIENTE:  // PENDIENTE también puede transicionar a ASIGNADO (legacy)
                transicionValida = (nuevoEstado == EstadoTramo.ASIGNADO);
                break;
            case ASIGNADO:
                transicionValida = (nuevoEstado == EstadoTramo.INICIADO);
                break;
            case INICIADO:
                transicionValida = (nuevoEstado == EstadoTramo.FINALIZADO);
                break;
            case FINALIZADO:
                // Un tramo finalizado no puede cambiar de estado
                transicionValida = false;
                break;
        }

        if (!transicionValida) {
            throw new IllegalStateException(
                String.format("Transición de estado inválida: %s → %s. Transiciones permitidas desde %s: %s",
                    estadoActual, nuevoEstado, estadoActual, obtenerTransicionesPermitidas(estadoActual))
            );
        }
    }

    /**
     * Obtiene las transiciones permitidas desde un estado dado.
     *
     * @param estado Estado actual
     * @return String con las transiciones permitidas
     */
    private String obtenerTransicionesPermitidas(EstadoTramo estado) {
        return switch (estado) {
            case ESTIMADO, PENDIENTE -> "ASIGNADO";
            case ASIGNADO -> "INICIADO";
            case INICIADO -> "FINALIZADO";
            case FINALIZADO -> "ninguna (estado final)";
        };
    }

    /**
     * Valida que el tramo anterior en la ruta esté finalizado.
     * Solo se puede iniciar un tramo si el tramo anterior (orden - 1) está FINALIZADO.
     *
     * @param tramo El tramo que se quiere iniciar
     * @throws IllegalStateException si el tramo anterior no está finalizado
     */
    private void validarTramoAnteriorFinalizado(Tramo tramo) {
        if (tramo.getRuta() == null || tramo.getOrden() == null) {
            return; // Sin ruta u orden, no validar
        }

        // Si es el primer tramo (orden 1), no hay tramo anterior
        if (tramo.getOrden() <= 1) {
            return;
        }

        // Buscar el tramo anterior (orden - 1)
        List<Tramo> tramosRuta = tramoRepository.findByRutaIdRuta(tramo.getRuta().getIdRuta());
        Optional<Tramo> tramoAnterior = tramosRuta.stream()
            .filter(t -> t.getOrden() != null && t.getOrden().equals(tramo.getOrden() - 1))
            .findFirst();

        if (tramoAnterior.isPresent()) {
            Tramo anterior = tramoAnterior.get();
            if (anterior.getEstado() != EstadoTramo.FINALIZADO) {
                throw new IllegalStateException(
                    String.format("No se puede iniciar el tramo %d (orden %d). El tramo anterior (orden %d) debe estar FINALIZADO. Estado actual: %s",
                        tramo.getIdTramo(), tramo.getOrden(), anterior.getOrden(), anterior.getEstado())
                );
            }
        }
    }

    /**
     * Valida que el contenedor asociado al tramo tenga los datos necesarios
     * para realizar la validación de capacidad del camión.
     *
     * Verifica que el contenedor tenga:
     * - pesoReal (no null y > 0)
     * - volumenReal (no null y > 0)
     *
     * @param tramo El tramo cuyo contenedor se va a validar
     * @param pesoContenedor Peso obtenido del contenedor
     * @param volumenContenedor Volumen obtenido del contenedor
     * @throws IllegalArgumentException si falta información de peso o volumen (error 422)
     */
    private void validarDatosContenedor(Tramo tramo, Double pesoContenedor, Double volumenContenedor) {
        // Obtener información del contenedor para mensajes detallados
        Long idContenedor = null;
        if (tramo.getRuta() != null && tramo.getRuta().getSolicitud() != null) {
            idContenedor = tramo.getRuta().getSolicitud().getIdContenedor();
        }

        // Validar que el peso esté presente
        if (pesoContenedor == null || pesoContenedor <= 0.0) {
            String mensaje = String.format(
                "Falta información de peso del contenedor%s. " +
                "El contenedor debe tener un pesoReal válido para validar la capacidad del camión.",
                idContenedor != null ? " (ID: " + idContenedor + ")" : ""
            );
            log.warn("Validación fallida - {}", mensaje);
            throw new IllegalArgumentException(mensaje);
        }

        // Validar que el volumen esté presente
        if (volumenContenedor == null || volumenContenedor <= 0.0) {
            String mensaje = String.format(
                "Falta información de volumen del contenedor%s. " +
                "El contenedor debe tener un volumenReal válido para validar la capacidad del camión.",
                idContenedor != null ? " (ID: " + idContenedor + ")" : ""
            );
            log.warn("Validación fallida - {}", mensaje);
            throw new IllegalArgumentException(mensaje);
        }

        // Log de éxito
        log.debug("Validación de datos del contenedor{} exitosa: peso={}kg, volumen={}m³",
            idContenedor != null ? " (ID: " + idContenedor + ")" : "",
            pesoContenedor, volumenContenedor);
    }

    public Tramo actualizar(Long id, Tramo tramo) {
        Tramo actual = obtener(id);

        // Validar reglas de negocio del origen y destino antes de actualizar
        validarOrigenDestino(tramo);

        actual.setOrigen(tramo.getOrigen());
        actual.setDestino(tramo.getDestino());
        actual.setDominioCamion(tramo.getDominioCamion());
        actual.setEstado(tramo.getEstado());
        actual.setFechaHoraInicioReal(tramo.getFechaHoraInicioReal());
        actual.setFechaHoraFinReal(tramo.getFechaHoraFinReal());
        actual.setCostoReal(tramo.getCostoReal());

        // Actualizar campos de origen/destino
        actual.setOrigenDepositoId(tramo.getOrigenDepositoId());
        actual.setDestinoDepositoId(tramo.getDestinoDepositoId());
        actual.setOrigenDireccionLibre(tramo.getOrigenDireccionLibre());
        actual.setDestinoDireccionLibre(tramo.getDestinoDireccionLibre());

        return tramoRepository.save(actual);
    }

    public void eliminar(Long id) {
        if (!tramoRepository.existsById(id)) {
            throw new NoSuchElementException("Tramo no encontrado: " + id);
        }
        tramoRepository.deleteById(id);
    }

    public void eliminarTramo(Long idTramo) {
        if (!tramoRepository.existsById(idTramo)) {
            throw new NoSuchElementException("Tramo no encontrado: " + idTramo);
        }
        tramoRepository.deleteById(idTramo);
    }

    /**
     * Asigna un camión a un tramo.
     *
     * Validaciones:
     * - El camión no debe estar en uso (asignado a otro tramo ASIGNADO o INICIADO)
     * - El camión debe tener capacidad suficiente para el contenedor (peso y volumen reales)
     * - El tramo debe estar en estado ESTIMADO o PENDIENTE
     *
     * @param idTramo ID del tramo
     * @param dominioCamion Dominio del camión a asignar
     * @return Mono con el tramo actualizado
     * @throws IllegalStateException si el camión está en uso o no tiene capacidad
     */
    public Mono<Tramo> asignarACamion(Long idTramo, String dominioCamion) {
        return Mono.fromCallable(() -> obtener(idTramo))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tramo -> {
                    // Validar que el camión no esté en uso
                    if (camionEstaEnUso(dominioCamion, idTramo)) {
                        return Mono.error(new IllegalStateException(
                            String.format("El camión %s ya está asignado a otro tramo en estado ASIGNADO o INICIADO",
                                dominioCamion)
                        ));
                    }

                    // Obtener peso y volumen REALES del contenedor asociado
                    Double pesoContenedor = obtenerPesoContenedorDesdeTramo(tramo);
                    Double volumenContenedor = obtenerVolumenContenedorDesdeTramo(tramo);

                    // VALIDACIÓN EXTRA: Verificar que el contenedor tenga peso y volumen reales
                    validarDatosContenedor(tramo, pesoContenedor, volumenContenedor);

                    log.info("Validando capacidad del camión {} para contenedor: peso={}kg, volumen={}m³",
                        dominioCamion, pesoContenedor, volumenContenedor);

                    if (logisticaClient.isEmpty()) {
                        return Mono.just(true);
                    }
                    return logisticaClient.get().validarCapacidadCamion(dominioCamion, pesoContenedor, volumenContenedor)
                        .defaultIfEmpty(false)
                        .doOnNext(valido -> {
                            if (!valido) {
                                log.warn("Validación FALLIDA - Camión {} NO es apto para el contenedor: peso={}kg, volumen={}m³",
                                    dominioCamion, pesoContenedor, volumenContenedor);
                            } else {
                                log.info("Validación EXITOSA - Camión {} es apto para el contenedor", dominioCamion);
                            }
                        });
                })
                .flatMap(valido -> {
                    if (!valido) {
                        Tramo tramo = obtener(idTramo);
                        Double peso = obtenerPesoContenedorDesdeTramo(tramo);
                        Double volumen = obtenerVolumenContenedorDesdeTramo(tramo);

                        log.warn("No se puede asignar camión {} al tramo {}: capacidad insuficiente para peso={}kg, volumen={}m³",
                            dominioCamion, idTramo, peso, volumen);

                        return Mono.error(new IllegalStateException(
                            String.format("El camión %s no es apto para el contenedor. " +
                                "Peso requerido: %.2f kg, Volumen requerido: %.2f m³. " +
                                "Verifique la capacidad del camión.",
                                dominioCamion, peso, volumen)
                        ));
                    }
                    Tramo tramo = obtener(idTramo);

                    // Validar transición de estado: ESTIMADO/PENDIENTE → ASIGNADO
                    validarTransicionEstado(tramo.getEstado(), EstadoTramo.ASIGNADO);

                    tramo.setDominioCamion(dominioCamion);
                    tramo.setEstado(EstadoTramo.ASIGNADO);
                    return Mono.fromCallable(() -> tramoRepository.save(tramo))
                            .subscribeOn(Schedulers.boundedElastic());
                });
    }

    // Alias con el nombre solicitado
    public Mono<Tramo> asignarCamion(Long idTramo, String dominioCamion) {
        return asignarACamion(idTramo, dominioCamion);
    }

    /**
     * Inicia un tramo, cambiando su estado a INICIADO.
     *
     * También calcula el tiempo y costo de estadía en depósito si el tramo anterior
     * finalizó en un depósito.
     *
     * Validaciones:
     * - El tramo debe tener una ruta asociada
     * - La ruta debe estar seleccionada (esRutaSeleccionada = true)
     * - El estado del tramo debe ser ASIGNADO
     *
     * Fórmula estadía:
     * - tiempoEstadiaHoras = fechaHoraInicioReal(N) - fechaHoraFinReal(N-1)
     * - costoEstadiaReal = tiempoEstadiaHoras * deposito.tarifaEstadiaPorHora
     *
     * @param idTramo ID del tramo a iniciar
     * @param odometroInicial Lectura inicial del odómetro (opcional)
     * @return Tramo iniciado con estadía calculada
     * @throws IllegalStateException si la ruta no está seleccionada
     */
    public Tramo iniciarTramo(Long idTramo, Double odometroInicial) {
        Tramo tramo = obtener(idTramo);

        // VALIDACIÓN 1: Verificar que la ruta esté seleccionada
        if (tramo.getRuta() == null) {
            throw new IllegalStateException(
                String.format("No se puede iniciar el tramo %d. El tramo debe estar asociado a una ruta.",
                    idTramo)
            );
        }

        if (!Boolean.TRUE.equals(tramo.getRuta().getEsRutaSeleccionada())) {
            throw new IllegalStateException(
                String.format("No se puede iniciar el tramo %d. La ruta %d debe estar seleccionada primero.",
                    idTramo, tramo.getRuta().getIdRuta())
            );
        }

        // VALIDACIÓN 2: Validar transición de estado: ASIGNADO → INICIADO
        validarTransicionEstado(tramo.getEstado(), EstadoTramo.INICIADO);

        // Validar que el tramo anterior esté finalizado (si no es el primero)
        validarTramoAnteriorFinalizado(tramo);

        tramo.setEstado(EstadoTramo.INICIADO);
        tramo.setFechaHoraInicioReal(LocalDateTime.now());
        if (odometroInicial != null) {
            tramo.setOdometroInicial(odometroInicial);
        }

        // Calcular estadía en depósito si el tramo anterior finalizó en depósito
        calcularEstadiaEnDeposito(tramo);

        Tramo actualizado = tramoRepository.save(tramo);

        // Actualizar estado del contenedor a EN_TRANSITO
        actualizarEstadoContenedor(tramo, "EN_TRANSITO", null);

        actualizarEstadoRutaYSolicitudAlIniciar(tramo);

        return actualizado;
    }

    public Tramo finalizarTramo(Long idTramo, LocalDateTime fechaHoraFin, Double odometroFinal,
                                 Double tiempoReal) {
        Tramo tramo = obtener(idTramo);

        // Validar transición de estado: INICIADO → FINALIZADO
        validarTransicionEstado(tramo.getEstado(), EstadoTramo.FINALIZADO);

        tramo.setEstado(EstadoTramo.FINALIZADO);
        LocalDateTime finReal = fechaHoraFin != null ? fechaHoraFin : LocalDateTime.now();
        tramo.setFechaHoraFinReal(finReal);
        if (odometroFinal != null) {
            tramo.setOdometroFinal(odometroFinal);
        }
        if (tiempoReal != null) {
            tramo.setTiempoReal(tiempoReal);
        } else if (tramo.getFechaHoraInicioReal() != null && tramo.getFechaHoraFinReal() != null) {
            tramo.setTiempoReal(calcularHorasEntre(tramo.getFechaHoraInicioReal(), tramo.getFechaHoraFinReal()));
        }

        double distanciaReal = calcularDistanciaReal(tramo);
        tramo.setDistanciaRealKm(distanciaReal);

        double costoCalculado = calcularCostoReal(tramo, distanciaReal);
        tramo.setCostoReal(costoCalculado);
        Tramo actualizado = tramoRepository.save(tramo);

        // Determinar si es el último tramo de la ruta
        boolean esUltimoTramo = esUltimoTramoDeLaRuta(tramo);

        // Actualizar estado del contenedor según el destino
        if (esUltimoTramo) {
            // Último tramo → ENTREGADO
            actualizarEstadoContenedor(tramo, "ENTREGADO", null);

            // Consolidar costos y tiempos en la Solicitud
            consolidarCostosYTiemposEnSolicitud(tramo);
        } else if (tramo.getDestinoDepositoId() != null) {
            // Finaliza en depósito → EN_DEPOSITO
            actualizarEstadoContenedor(tramo, "EN_DEPOSITO", tramo.getDestinoDepositoId());
        }

        actualizarEstadoRutaYSolicitudAlFinalizar(tramo);

        return actualizado;
    }

    /**
     * Actualiza los estados de Ruta y Solicitud al iniciar un tramo.
     *
     * Transiciones:
     * - Ruta: PENDIENTE → EJECUTANDOSE (al iniciar el primer tramo)
     * - Solicitud: PROGRAMADA → EN_TRANSITO (al iniciar el primer tramo)
     *
     * @param tramo El tramo que se está iniciando
     */
    private void actualizarEstadoRutaYSolicitudAlIniciar(Tramo tramo) {
        if (tramo.getRuta() == null) {
            return;
        }
        Long rutaId = tramo.getRuta().getIdRuta();
        if (rutaId == null) {
            return;
        }
        rutaRepository.findById(rutaId).ifPresent(ruta -> {
            // Cambiar ruta a EJECUTANDOSE si está en PENDIENTE
            if (ruta.getEstado() == null || ruta.getEstado() == EstadoRuta.PENDIENTE) {
                ruta.setEstado(EstadoRuta.EJECUTANDOSE);
                rutaRepository.save(ruta);
                log.info("Ruta {} cambiada a estado EJECUTANDOSE al iniciar tramo {}",
                    rutaId, tramo.getIdTramo());
            }

            // Cambiar solicitud a EN_TRANSITO si está en PROGRAMADA
            Solicitud solicitud = ruta.getSolicitud();
            if (solicitud != null && solicitud.getEstado() == EstadoSolicitud.PROGRAMADA) {
                solicitud.setEstado(EstadoSolicitud.EN_TRANSITO);
                solicitudRepository.save(solicitud);
                log.info("Solicitud {} cambiada a estado EN_TRANSITO al iniciar primer tramo",
                    solicitud.getNroSolicitud());
            }
        });
    }

    /**
     * Actualiza los estados de Ruta y Solicitud al finalizar un tramo.
     *
     * Transiciones:
     * - Ruta: EJECUTANDOSE → COMPLETADA (cuando todos los tramos están FINALIZADOS)
     * - Solicitud: EN_TRANSITO → ENTREGADA (cuando se completa el último tramo de la ruta)
     *
     * @param tramo El tramo que se está finalizando
     */
    private void actualizarEstadoRutaYSolicitudAlFinalizar(Tramo tramo) {
        if (tramo.getRuta() == null) {
            return;
        }
        Long rutaId = tramo.getRuta().getIdRuta();
        if (rutaId == null) {
            return;
        }
        rutaRepository.findById(rutaId).ifPresent(ruta -> {
            List<Tramo> tramosRuta = tramoRepository.findByRutaIdRuta(ruta.getIdRuta());
            boolean todosFinalizados = !tramosRuta.isEmpty() && tramosRuta.stream()
                    .allMatch(t -> t.getEstado() == EstadoTramo.FINALIZADO);

            if (todosFinalizados) {
                // Cambiar ruta a COMPLETADA
                ruta.setEstado(EstadoRuta.COMPLETADA);
                rutaRepository.save(ruta);
                log.info("Ruta {} cambiada a estado COMPLETADA (todos los tramos finalizados)", rutaId);

                // Cambiar solicitud a ENTREGADA
                Solicitud solicitud = ruta.getSolicitud();
                if (solicitud != null) {
                    solicitud.setEstado(EstadoSolicitud.ENTREGADA);
                    solicitudRepository.save(solicitud);
                    log.info("Solicitud {} cambiada a estado ENTREGADA (último tramo finalizado)",
                        solicitud.getNroSolicitud());
                }
            }
        });
    }

    /**
     * Calcula costo y tiempo estimado para un tramo usando Google Directions y datos del camión.
     * - Distancia (km) y duración (min) desde GoogleMapsClient.
     * - costoBaseKm del camión desde LogisticaClient.
     * Guarda costoAproximado, fechaHoraInicioEstimada y fechaHoraFinEstimada.
     */
    public Mono<Tramo> calcularCostoYTiempoEstimado(Long idTramo,
                                                    double origenLat, double origenLng,
                                                    double destinoLat, double destinoLng) {
        return Mono.fromCallable(() -> obtener(idTramo))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tramo -> {
                    if (tramo.getDominioCamion() == null || tramo.getDominioCamion().isBlank()) {
                        return Mono.error(new IllegalStateException("El tramo no tiene camión asignado"));
                    }
                    String dominio = tramo.getDominioCamion();
                    
                    Mono<Map<String, Object>> distanciaMono = googleMapsClient.isPresent() 
                        ? googleMapsClient.get().obtenerDistanciaYDuracion(origenLat, origenLng, destinoLat, destinoLng)
                        : Mono.just(Map.of("distanciaKm", 0.0, "duracionMinutos", 0L));
                    
                    Mono<Map<String, Object>> camionMono = logisticaClient.isPresent()
                        ? logisticaClient.get().obtenerCamion(dominio)
                        : Mono.just(Map.of());
                    
                    return distanciaMono.zipWith(camionMono)
                            .flatMap(tuple -> {
                                Map<String, Object> direccionData = tuple.getT1();
                                Map<String, Object> camion = tuple.getT2();

                                Double distanciaKm = (Double) direccionData.getOrDefault("distanciaKm", 0.0);
                                Long duracionMinutos = ((Number) direccionData.getOrDefault("duracionMinutos", 0L)).longValue();

                                Object costoBaseKmObj = camion != null ? camion.get("costoBaseKm") : null;
                                double costoBaseKm = (costoBaseKmObj instanceof Number) ? ((Number) costoBaseKmObj).doubleValue() : 0.0;
                                double costoEstimado = distanciaKm * costoBaseKm;
                                tramo.setCostoAproximado(costoEstimado);

                                // Usa la duración real de Google Maps
                                if (tramo.getFechaHoraInicioEstimada() == null) {
                                    tramo.setFechaHoraInicioEstimada(LocalDateTime.now());
                                }
                                tramo.setFechaHoraFinEstimada(tramo.getFechaHoraInicioEstimada().plusMinutes(duracionMinutos));

                                return Mono.fromCallable(() -> tramoRepository.save(tramo))
                                        .subscribeOn(Schedulers.boundedElastic());
                            });
                });
    }

    /**
     * Calcula costo y tiempo estimado usando la nueva integración con Google Maps Service.
     * Este método es más directo y sincrónico que calcularCostoYTiempoEstimado.
     *
     * @param idTramo ID del tramo a actualizar
     * @param origen Dirección o coordenadas de origen (ej: "Buenos Aires")
     * @param destino Dirección o coordenadas de destino (ej: "Córdoba")
     * @return Mono con el Tramo actualizado con costos y tiempos estimados
     */
    public Mono<Tramo> calcularCostoYTiempoEstimadoConGoogleMaps(Long idTramo,
                                                                 String origen,
                                                                 String destino) {
        return Mono.fromCallable(() -> obtener(idTramo))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tramo -> {
                    if (tramo.getDominioCamion() == null || tramo.getDominioCamion().isBlank()) {
                        return Mono.error(new IllegalStateException("El tramo no tiene camión asignado"));
                    }

                    String dominio = tramo.getDominioCamion();

                    try {
                        // Paso 1: Obtener distancia y duración desde Google Maps
                        DistanciaDTO distancia = googleMapsService.calcularDistancia(origen, destino);
                        log.info("Distancia obtenida: {} km, Duración: {}", distancia.getKilometros(), distancia.getDuracionTexto());
                        
                        // Paso 2: Obtener datos del camión (costoBaseKm)
                        Mono<Map<String, Object>> camionMono = logisticaClient.isPresent()
                            ? logisticaClient.get().obtenerCamion(dominio)
                            : Mono.just(Map.of());
                            
                        return camionMono.flatMap(camion -> {
                                    Object costoBaseKmObj = camion != null ? camion.get("costoBaseKm") : null;
                                    double costoBaseKm = (costoBaseKmObj instanceof Number) ? ((Number) costoBaseKmObj).doubleValue() : 0.0;
                                    
                                    // Paso 3: Calcular costo estimado
                                    double costoEstimado = distancia.getKilometros() * costoBaseKm;
                                    tramo.setCostoAproximado(costoEstimado);
                                    
                                    // Paso 4: Actualizar tiempos estimados
                                    if (tramo.getFechaHoraInicioEstimada() == null) {
                                        tramo.setFechaHoraInicioEstimada(LocalDateTime.now());
                                    }
                                    
                                    // Extraer minutos de duracionTexto (ej: "7 hours 15 mins" o "30 mins")
                                    long duracionMinutos = extraerMinutosDeDuracion(distancia.getDuracionTexto());
                                    tramo.setFechaHoraFinEstimada(tramo.getFechaHoraInicioEstimada().plusMinutes(duracionMinutos));
                                    
                                    // Paso 5: Guardar en base de datos
                                    log.info("Tramo actualizado - Costo: ${}, Duración: {} minutos", costoEstimado, duracionMinutos);
                                    return Mono.fromCallable(() -> tramoRepository.save(tramo))
                                            .subscribeOn(Schedulers.boundedElastic());
                                });

                    } catch (RuntimeException e) {
                        log.error("Error al calcular distancia y costo para tramo {}: {}", idTramo, e.getMessage(), e);
                        return Mono.error(e);
                    }
                });
    }

    /**
     * Extrae la cantidad de minutos de una cadena de duración de Google Maps.
     * Ejemplos:
     * - "7 hours 15 mins" → 435 minutos
     * - "30 mins" → 30 minutos
     * - "2 hours" → 120 minutos
     * 
     * @param duracionTexto Texto de duración desde Google Maps
     * @return Cantidad de minutos
     */
    private long extraerMinutosDeDuracion(String duracionTexto) {
        if (duracionTexto == null || duracionTexto.isEmpty()) {
            log.warn("Duración en texto vacía, retornando 0");
            return 0L;
        }
        
        long totalMinutos = 0;
        String[] partes = duracionTexto.split(" ");

        for (int i = 0; i < partes.length; i++) {
            try {
                if (i + 1 < partes.length) {
                    String numero = partes[i];
                    String unidad = partes[i + 1];
                    
                    long valor = Long.parseLong(numero);
                    
                    if (unidad.startsWith("hour")) {
                        totalMinutos += valor * 60;
                        i++; // Saltar la unidad
                    } else if (unidad.startsWith("min")) {
                        totalMinutos += valor;
                        i++; // Saltar la unidad
                    }
                }
            } catch (NumberFormatException e) {
                log.warn("No se pudo parsear duración en: {}", duracionTexto);
            }
        }
        
        return totalMinutos;
    }

    /**
     * Completa las estimaciones de un tramo: distancia estimada y costo aproximado.
     *
     * El costo aproximado se calcula basándose en el promedio de camiones aptos
     * para el peso y volumen del contenedor asociado a la solicitud.
     *
     * Fórmula:
     * costoAproximado = distanciaEstimadaKm * costoBaseKmPromedio
     *
     * @param tramo Tramo a completar estimaciones
     * @param origen Punto de origen (puede ser sobrescrito si hay depósito)
     * @param destino Punto de destino (puede ser sobrescrito si hay depósito)
     * @param diasDeposito Días estimados de depósito
     */
    private void completarEstimacionesTramo(Tramo tramo, String origen, String destino, double diasDeposito) {
        try {
            // Resolver direcciones reales (depósito o dirección libre)
            String origenReal = resolverDireccionReal(
                tramo.getOrigenDepositoId(),
                tramo.getOrigenDireccionLibre(),
                origen,
                "origen"
            );

            String destinoReal = resolverDireccionReal(
                tramo.getDestinoDepositoId(),
                tramo.getDestinoDireccionLibre(),
                destino,
                "destino"
            );

            log.debug("Direcciones resueltas para tramo - Origen: '{}', Destino: '{}'", origenReal, destinoReal);

            // Calcular distancia estimada usando Google Maps con direcciones reales
            DistanciaDTO distancia = googleMapsService.calcularDistancia(origenReal, destinoReal);
            double distanciaKm = distancia != null ? distancia.getKilometros() : 0d;
            tramo.setDistanciaEstimadaKm(distanciaKm);

            // Obtener peso y volumen del contenedor
            Double pesoContenedor = obtenerPesoContenedor(tramo);
            Double volumenContenedor = obtenerVolumenContenedor(tramo);

            // Calcular costo aproximado basado en promedio de camiones aptos
            double costoEstimado = calcularCostoAproximadoConCamionesAptos(
                distanciaKm, diasDeposito, pesoContenedor, volumenContenedor);
            tramo.setCostoAproximado(costoEstimado);

        } catch (RuntimeException ex) {
            log.warn("No se pudo calcular estimaciones para el tramo entre {} y {}: {}",
                origen, destino, ex.getMessage());
            if (tramo.getCostoAproximado() == null) {
                tramo.setCostoAproximado(0d);
            }
        }
    }

    private double resolverDiasDepositoEstimados(TramoCreateDto dto) {
        if (dto.getDiasEstimadosDeposito() != null && dto.getDiasEstimadosDeposito() >= 0) {
            return dto.getDiasEstimadosDeposito();
        }
        if (dto.getFechaHoraInicioEstimada() != null && dto.getFechaHoraFinEstimada() != null) {
            double horas = Duration.between(dto.getFechaHoraInicioEstimada(), dto.getFechaHoraFinEstimada()).toHours();
            return horas > 0 ? horas / 24d : 0d;
        }
        return Math.max(0d, pricingProperties.getDiasDepositoEstimadoDefault());
    }

    private double calcularCostoAproximado(double distanciaKm, double diasDeposito) {
        double costoTransporte = distanciaKm * pricingProperties.getTarifaBasePromedio();
        double consumoPromedio = pricingProperties.getConsumoPromedioGeneral();
        double costoCombustible = (distanciaKm / 100d) * consumoPromedio * pricingProperties.getPrecioLitroCombustible();
        double costoEstadia = diasDeposito * pricingProperties.getCostoDiarioDeposito();
        return costoTransporte + costoCombustible + costoEstadia;
    }

    private double calcularDistanciaReal(Tramo tramo) {
        if (tramo.getOdometroInicial() != null && tramo.getOdometroFinal() != null) {
            double distancia = tramo.getOdometroFinal() - tramo.getOdometroInicial();
            if (distancia < 0) {
                log.warn("Lecturas de odómetro inválidas para tramo {}", tramo.getIdTramo());
                return 0d;
            }
            return distancia;
        }
        return Optional.ofNullable(tramo.getDistanciaEstimadaKm()).orElse(0d);
    }

    private double calcularDiasDeposito(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            return 0d;
        }
        double horas = Duration.between(inicio, fin).toHours();
        return horas / 24d;
    }

    private double calcularHorasEntre(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) {
            return 0d;
        }
        double minutos = Duration.between(inicio, fin).toMinutes();
        return minutos / 60d;
    }

    /**
     * Calcula el costo real de un tramo basándose en la distancia real recorrida.
     *
     * Fórmula: costoReal = distanciaRealKm × costoBaseKmDelCamion
     *
     * @param tramo Tramo finalizado
     * @param distanciaReal Distancia real recorrida en km
     * @return Costo real del tramo
     */
    private double calcularCostoReal(Tramo tramo, double distanciaReal) {
        // Obtener datos del camión asignado
        Map<String, Object> camion = consultarCamion(tramo.getDominioCamion());

        // Obtener costoBaseKm del camión (si no existe, usar tarifa base promedio como fallback)
        double costoBaseKm = extraerValor(camion.get("costoBaseKm"), pricingProperties.getTarifaBasePromedio());

        // Fórmula simplificada: costoReal = distanciaRealKm * costoBaseKm
        double costoReal = distanciaReal * costoBaseKm;

        log.debug("Costo real calculado para tramo {}: distancia={}km, costoBaseKm=${}, total=${}",
            tramo.getIdTramo(), distanciaReal, costoBaseKm, costoReal);

        return costoReal;
    }

    private Map<String, Object> consultarCamion(String dominioCamion) {
        if (dominioCamion == null || dominioCamion.isBlank() || logisticaClient.isEmpty()) {
            return Map.of();
        }
        try {
            return logisticaClient.get().obtenerCamion(dominioCamion)
                    .onErrorResume(error -> {
                        log.warn("Error consultando camión {}: {}", dominioCamion, error.getMessage());
                        return Mono.empty();
                    })
                    .blockOptional()
                    .orElse(Map.of());
        } catch (Exception e) {
            log.warn("No se pudo obtener datos del camión {}: {}", dominioCamion, e.getMessage());
            return Map.of();
        }
    }

    private double obtenerCostoDiarioDeposito(Long depositoId) {
        if (depositoId == null || logisticaClient.isEmpty()) {
            return pricingProperties.getCostoDiarioDeposito();
        }
        try {
            return logisticaClient.get().obtenerDeposito(depositoId)
                    .onErrorResume(error -> {
                        log.warn("Error consultando depósito {}: {}", depositoId, error.getMessage());
                        return Mono.empty();
                    })
                    .map(response -> extraerValor(response.get("costoEstadiaDiario"), pricingProperties.getCostoDiarioDeposito()))
                    .blockOptional()
                    .orElse(pricingProperties.getCostoDiarioDeposito());
        } catch (Exception e) {
            log.warn("No se pudo obtener datos del depósito {}: {}", depositoId, e.getMessage());
            return pricingProperties.getCostoDiarioDeposito();
        }
    }

    private double extraerValor(Object value, double defaultValue) {
        if (value instanceof Number numberValue) {
            return numberValue.doubleValue();
        }
        return defaultValue;
    }

    /**
     * Determina si un tramo es el último de su ruta.
     * Un tramo es el último si tiene el orden más alto en la ruta.
     *
     * @param tramo El tramo a verificar
     * @return true si es el último tramo, false en caso contrario
     */
    private boolean esUltimoTramoDeLaRuta(Tramo tramo) {
        if (tramo.getRuta() == null || tramo.getOrden() == null) {
            return false;
        }

        List<Tramo> tramosRuta = tramoRepository.findByRutaIdRuta(tramo.getRuta().getIdRuta());
        int ordenMaximo = tramosRuta.stream()
            .filter(t -> t.getOrden() != null)
            .mapToInt(Tramo::getOrden)
            .max()
            .orElse(0);

        return tramo.getOrden() == ordenMaximo;
    }

    /**
     * Actualiza el estado del contenedor asociado a un tramo.
     * Obtiene el ID del contenedor desde: Tramo → Ruta → Solicitud → idContenedor
     *
     * @param tramo El tramo que está siendo procesado
     * @param nuevoEstado El nuevo estado del contenedor (EN_TRANSITO, EN_DEPOSITO, ENTREGADO)
     * @param depositoId ID del depósito (solo para estado EN_DEPOSITO)
     */
    private void actualizarEstadoContenedor(Tramo tramo, String nuevoEstado, Long depositoId) {
        try {
            // Obtener el ID del contenedor desde la solicitud
            if (tramo.getRuta() == null || tramo.getRuta().getSolicitud() == null) {
                log.warn("No se puede actualizar contenedor: tramo sin ruta o solicitud asociada");
                return;
            }

            Solicitud solicitud = tramo.getRuta().getSolicitud();
            Long idContenedor = solicitud.getIdContenedor();

            if (idContenedor == null) {
                log.warn("No se puede actualizar contenedor: solicitud {} sin contenedor asociado",
                    solicitud.getNroSolicitud());
                return;
            }

            // Obtener el contenedor
            Optional<Contenedor> contenedorOpt = contenedorService.obtenerPorId(idContenedor);
            if (contenedorOpt.isEmpty()) {
                log.warn("Contenedor {} no encontrado", idContenedor);
                return;
            }

            Contenedor contenedor = contenedorOpt.get();

            // Actualizar estado y depositoActualId según el nuevo estado
            contenedor.setEstado(nuevoEstado);

            if ("EN_DEPOSITO".equals(nuevoEstado)) {
                contenedor.setDepositoActualId(depositoId);
            } else if ("EN_TRANSITO".equals(nuevoEstado) || "ENTREGADO".equals(nuevoEstado)) {
                contenedor.setDepositoActualId(null);
            }

            // Crear DTO de actualización
            ContenedorUpdateDto updateDto = ContenedorUpdateDto.builder()
                .estado(nuevoEstado)
                .depositoActualId(contenedor.getDepositoActualId())
                .build();

            // Actualizar contenedor
            contenedorService.actualizarContenedor(idContenedor, updateDto);

            log.info("Contenedor {} actualizado a estado {} (depositoId: {})",
                idContenedor, nuevoEstado, depositoId);

        } catch (Exception e) {
            log.error("Error al actualizar estado del contenedor para tramo {}: {}",
                tramo.getIdTramo(), e.getMessage(), e);
            // No lanzamos la excepción para no interrumpir el flujo del tramo
        }
    }

    /**
     * Calcula el costo aproximado basándose en el promedio de camiones aptos.
     *
     * Pasos:
     * 1. Obtener todos los camiones del servicio de logística
     * 2. Filtrar camiones aptos por peso y volumen del contenedor
     * 3. Si no hay camiones aptos → lanzar error de negocio
     * 4. Calcular promedio de costoBaseKm
     * 5. Aplicar fórmula: costoAproximado = distanciaEstimadaKm * costoBaseKmPromedio
     *
     * @param distanciaKm Distancia estimada en kilómetros
     * @param diasDeposito Días estimados de depósito (no usado en fórmula actual)
     * @param pesoContenedor Peso del contenedor en kg
     * @param volumenContenedor Volumen del contenedor en m³
     * @return Costo aproximado del tramo
     * @throws IllegalStateException si no hay camiones aptos
     */
    private double calcularCostoAproximadoConCamionesAptos(double distanciaKm, double diasDeposito,
                                                           Double pesoContenedor, Double volumenContenedor) {
        // Si no hay cliente de logística, usar cálculo con tarifa base promedio
        if (logisticaClient.isEmpty()) {
            log.warn("Cliente de logística no disponible, usando tarifa base promedio");
            return distanciaKm * pricingProperties.getTarifaBasePromedio();
        }

        try {
            // Obtener todos los camiones del servicio de logística
            List<Map<String, Object>> camiones = obtenerTodosLosCamiones();

            if (camiones == null || camiones.isEmpty()) {
                log.warn("No se obtuvieron camiones del servicio de logística, usando tarifa base promedio");
                return distanciaKm * pricingProperties.getTarifaBasePromedio();
            }

            // Filtrar camiones aptos por capacidad
            double peso = pesoContenedor != null ? pesoContenedor : 0.0;
            double volumen = volumenContenedor != null ? volumenContenedor : 0.0;

            List<Map<String, Object>> camionesAptos = camiones.stream()
                .filter(c -> esCamionApto(c, peso, volumen))
                .toList();

            // Si no hay camiones aptos → error de negocio
            if (camionesAptos.isEmpty()) {
                throw new IllegalStateException(
                    String.format("No hay camiones aptos para contenedor de peso %.2f kg y volumen %.2f m³",
                        peso, volumen)
                );
            }

            // Calcular promedio de costoBaseKm
            double costoBaseKmPromedio = camionesAptos.stream()
                .mapToDouble(c -> extraerValor(c.get("costoBaseKm"), pricingProperties.getTarifaBasePromedio()))
                .average()
                .orElse(pricingProperties.getTarifaBasePromedio());

            // Calcular costo aproximado
            double costoAproximado = distanciaKm * costoBaseKmPromedio;

            log.info("Costo aproximado calculado: {} camiones aptos, costoBaseKmPromedio=${}, distancia={}km, total=${}",
                camionesAptos.size(), costoBaseKmPromedio, distanciaKm, costoAproximado);

            return costoAproximado;

        } catch (IllegalStateException e) {
            // Re-lanzar errores de negocio
            throw e;
        } catch (Exception e) {
            log.warn("Error al calcular costo con camiones aptos: {}. Usando tarifa base promedio", e.getMessage());
            return distanciaKm * pricingProperties.getTarifaBasePromedio();
        }
    }

    /**
     * Obtiene todos los camiones del servicio de logística.
     *
     * @return Lista de camiones (Map con propiedades del camión)
     */
    private List<Map<String, Object>> obtenerTodosLosCamiones() {
        if (logisticaClient.isEmpty()) {
            return List.of();
        }

        try {
            return logisticaClient.get().obtenerTodosLosCamiones()
                .onErrorResume(error -> {
                    log.warn("Error obteniendo camiones: {}", error.getMessage());
                    return Mono.just(List.of());
                })
                .blockOptional()
                .orElse(List.of());
        } catch (Exception e) {
            log.warn("No se pudieron obtener los camiones: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * Verifica si un camión es apto para transportar un contenedor con el peso y volumen dados.
     *
     * Un camión es apto si:
     * - capacidadKg >= pesoContenedor
     * - capacidadVolumen >= volumenContenedor
     *
     * @param camion Map con datos del camión
     * @param pesoContenedor Peso del contenedor en kg
     * @param volumenContenedor Volumen del contenedor en m³
     * @return true si el camión es apto, false en caso contrario
     */
    private boolean esCamionApto(Map<String, Object> camion, double pesoContenedor, double volumenContenedor) {
        try {
            double capacidadKg = extraerValor(camion.get("capacidadKg"), Double.MAX_VALUE);
            double capacidadVolumen = extraerValor(camion.get("capacidadVolumen"), Double.MAX_VALUE);

            boolean aptoParaPeso = capacidadKg >= pesoContenedor;
            boolean aptoParaVolumen = capacidadVolumen >= volumenContenedor;

            return aptoParaPeso && aptoParaVolumen;
        } catch (Exception e) {
            log.warn("Error verificando aptitud de camión: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el peso del contenedor asociado a un tramo.
     * Navega: Tramo → Ruta → Solicitud → Contenedor
     *
     * @param tramo El tramo
     * @return Peso del contenedor en kg, o 0.0 si no se puede obtener
     */
    private Double obtenerPesoContenedor(Tramo tramo) {
        try {
            if (tramo.getRuta() == null || tramo.getRuta().getSolicitud() == null) {
                return 0.0;
            }

            Long idContenedor = tramo.getRuta().getSolicitud().getIdContenedor();
            if (idContenedor == null) {
                return 0.0;
            }

            Optional<Contenedor> contenedorOpt = contenedorService.obtenerPorId(idContenedor);
            if (contenedorOpt.isEmpty()) {
                return 0.0;
            }

            Contenedor contenedor = contenedorOpt.get();

            // Preferir pesoReal si existe, sino usar capacidadKg como estimación
            if (contenedor.getPesoReal() != null && contenedor.getPesoReal() > 0) {
                return contenedor.getPesoReal();
            } else if (contenedor.getCapacidadKg() != null) {
                return contenedor.getCapacidadKg();
            }

            return 0.0;
        } catch (Exception e) {
            log.warn("Error obteniendo peso del contenedor: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Obtiene el volumen del contenedor asociado a un tramo.
     * Navega: Tramo → Ruta → Solicitud → Contenedor
     *
     * @param tramo El tramo
     * @return Volumen del contenedor en m³, o 0.0 si no se puede obtener
     */
    private Double obtenerVolumenContenedor(Tramo tramo) {
        try {
            if (tramo.getRuta() == null || tramo.getRuta().getSolicitud() == null) {
                return 0.0;
            }

            Long idContenedor = tramo.getRuta().getSolicitud().getIdContenedor();
            if (idContenedor == null) {
                return 0.0;
            }

            Optional<Contenedor> contenedorOpt = contenedorService.obtenerPorId(idContenedor);
            if (contenedorOpt.isEmpty()) {
                return 0.0;
            }

            Contenedor contenedor = contenedorOpt.get();

            // Retornar volumenReal si existe
            if (contenedor.getVolumenReal() != null && contenedor.getVolumenReal() > 0) {
                return contenedor.getVolumenReal();
            }

            return 0.0;
        } catch (Exception e) {
            log.warn("Error obteniendo volumen del contenedor: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Alias de obtenerPesoContenedor para uso en asignarACamion.
     * Obtiene el peso real del contenedor asociado al tramo.
     *
     * @param tramo El tramo
     * @return Peso del contenedor en kg
     */
    private Double obtenerPesoContenedorDesdeTramo(Tramo tramo) {
        return obtenerPesoContenedor(tramo);
    }

    /**
     * Alias de obtenerVolumenContenedor para uso en asignarACamion.
     * Obtiene el volumen real del contenedor asociado al tramo.
     *
     * @param tramo El tramo
     * @return Volumen del contenedor en m³
     */
    private Double obtenerVolumenContenedorDesdeTramo(Tramo tramo) {
        return obtenerVolumenContenedor(tramo);
    }

    /**
     * Calcula el tiempo y costo de estadía en depósito.
     *
     * Se ejecuta al iniciar un tramo (N) si el tramo anterior (N-1) finalizó en un depósito.
     *
     * Fórmulas:
     * - tiempoEstadiaHoras = fechaHoraInicioReal(N) - fechaHoraFinReal(N-1)
     * - costoEstadiaReal = tiempoEstadiaHoras * deposito.tarifaEstadiaPorHora
     *
     * @param tramo El tramo que se está iniciando
     */
    private void calcularEstadiaEnDeposito(Tramo tramo) {
        try {
            // No calcular si es el primer tramo
            if (tramo.getRuta() == null || tramo.getOrden() == null || tramo.getOrden() <= 1) {
                return;
            }

            // Buscar tramo anterior (orden N-1)
            List<Tramo> tramosRuta = tramoRepository.findByRutaIdRuta(tramo.getRuta().getIdRuta());
            Optional<Tramo> tramoAnteriorOpt = tramosRuta.stream()
                .filter(t -> t.getOrden() != null && t.getOrden().equals(tramo.getOrden() - 1))
                .findFirst();

            if (tramoAnteriorOpt.isEmpty()) {
                log.debug("No se encontró tramo anterior para calcular estadía");
                return;
            }

            Tramo tramoAnterior = tramoAnteriorOpt.get();

            // Verificar que el tramo anterior finalizó en un depósito
            if (tramoAnterior.getDestinoDepositoId() == null) {
                log.debug("Tramo anterior no finalizó en depósito, no hay estadía");
                return;
            }

            // Verificar que ambos tramos tengan fechas reales
            if (tramoAnterior.getFechaHoraFinReal() == null || tramo.getFechaHoraInicioReal() == null) {
                log.warn("Faltan fechas para calcular estadía: anterior={}, actual={}",
                    tramoAnterior.getFechaHoraFinReal(), tramo.getFechaHoraInicioReal());
                return;
            }

            // Calcular tiempo de estadía en horas
            double tiempoEstadiaHoras = calcularHorasEntre(
                tramoAnterior.getFechaHoraFinReal(),
                tramo.getFechaHoraInicioReal()
            );

            if (tiempoEstadiaHoras <= 0) {
                log.debug("Tiempo de estadía no positivo: {} horas", tiempoEstadiaHoras);
                return;
            }

            // Obtener tarifa del depósito
            Long depositoId = tramoAnterior.getDestinoDepositoId();
            double tarifaPorHora = obtenerTarifaEstadiaPorHora(depositoId);

            // Calcular costo de estadía
            double costoEstadia = tiempoEstadiaHoras * tarifaPorHora;

            // Guardar en el tramo actual
            tramo.setTiempoEstadiaHoras(tiempoEstadiaHoras);
            tramo.setCostoEstadiaReal(costoEstadia);

            log.info("Estadía calculada para tramo {}: {}h en depósito {} = ${} (tarifa ${}/h)",
                tramo.getIdTramo(), tiempoEstadiaHoras, depositoId, costoEstadia, tarifaPorHora);

        } catch (Exception e) {
            log.error("Error calculando estadía en depósito para tramo {}: {}",
                tramo.getIdTramo(), e.getMessage(), e);
        }
    }

    /**
     * Obtiene la tarifa de estadía por hora de un depósito.
     *
     * @param depositoId ID del depósito
     * @return Tarifa por hora, o 0.0 si no se puede obtener
     */
    private double obtenerTarifaEstadiaPorHora(Long depositoId) {
        if (depositoId == null || logisticaClient.isEmpty()) {
            return 0.0;
        }

        try {
            return logisticaClient.get().obtenerDeposito(depositoId)
                .onErrorResume(error -> {
                    log.warn("Error consultando depósito {}: {}", depositoId, error.getMessage());
                    return Mono.empty();
                })
                .map(response -> {
                    // Intentar obtener tarifaEstadiaPorHora
                    Object tarifaObj = response.get("tarifaEstadiaPorHora");
                    if (tarifaObj instanceof Number) {
                        return ((Number) tarifaObj).doubleValue();
                    }

                    // Fallback: calcular desde costoEstadiaDiario
                    Object costoDiarioObj = response.get("costoEstadiaDiario");
                    if (costoDiarioObj instanceof Number) {
                        double costoDiario = ((Number) costoDiarioObj).doubleValue();
                        return costoDiario / 24.0; // Convertir a tarifa por hora
                    }

                    return 0.0;
                })
                .blockOptional()
                .orElse(0.0);
        } catch (Exception e) {
            log.warn("No se pudo obtener tarifa del depósito {}: {}", depositoId, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Consolida los costos y tiempos totales en la Solicitud al finalizar el último tramo.
     *
     * Fórmulas:
     * - costoTotalReal = SUM(costoReal + costoEstadiaReal)
     * - costoTotalEstimado = SUM(costoAproximado)
     * - tiempoTotalEstimadoHoras = SUM(tiempoEstimado)
     * - tiempoTotalRealHoras = SUM(tiempoReal + tiempoEstadiaHoras)
     *
     * @param ultimoTramo El último tramo de la ruta que acaba de finalizarse
     */
    private void consolidarCostosYTiemposEnSolicitud(Tramo ultimoTramo) {
        try {
            if (ultimoTramo.getRuta() == null || ultimoTramo.getRuta().getSolicitud() == null) {
                log.warn("No se puede consolidar: tramo sin ruta o solicitud");
                return;
            }

            Solicitud solicitud = ultimoTramo.getRuta().getSolicitud();
            Long rutaId = ultimoTramo.getRuta().getIdRuta();

            // Obtener todos los tramos de la ruta
            List<Tramo> tramosRuta = tramoRepository.findByRutaIdRuta(rutaId);

            if (tramosRuta.isEmpty()) {
                log.warn("No se encontraron tramos para la ruta {}", rutaId);
                return;
            }

            // Calcular costoTotalReal = SUM(costoReal + costoEstadiaReal)
            double costoTotalReal = tramosRuta.stream()
                .mapToDouble(t -> {
                    double costoReal = t.getCostoReal() != null ? t.getCostoReal() : 0.0;
                    double costoEstadia = t.getCostoEstadiaReal() != null ? t.getCostoEstadiaReal() : 0.0;
                    return costoReal + costoEstadia;
                })
                .sum();

            // Calcular costoTotalEstimado = SUM(costoAproximado)
            double costoTotalEstimado = tramosRuta.stream()
                .mapToDouble(t -> t.getCostoAproximado() != null ? t.getCostoAproximado() : 0.0)
                .sum();

            // Calcular tiempoTotalRealHoras = SUM(tiempoReal + tiempoEstadiaHoras)
            double tiempoTotalRealHoras = tramosRuta.stream()
                .mapToDouble(t -> {
                    double tiempoReal = t.getTiempoReal() != null ? t.getTiempoReal() : 0.0;
                    double tiempoEstadia = t.getTiempoEstadiaHoras() != null ? t.getTiempoEstadiaHoras() : 0.0;
                    return tiempoReal + tiempoEstadia;
                })
                .sum();

            // Calcular tiempoTotalEstimadoHoras basado en fechas estimadas
            double tiempoTotalEstimadoHoras = tramosRuta.stream()
                .mapToDouble(t -> {
                    if (t.getFechaHoraInicioEstimada() != null && t.getFechaHoraFinEstimada() != null) {
                        return calcularHorasEntre(t.getFechaHoraInicioEstimada(), t.getFechaHoraFinEstimada());
                    }
                    return 0.0;
                })
                .sum();

            // Actualizar solicitud
            solicitud.setCostoTotalReal(costoTotalReal);
            solicitud.setCostoTotalEstimado(costoTotalEstimado);
            solicitud.setTiempoTotalRealHoras(tiempoTotalRealHoras);
            solicitud.setTiempoTotalEstimadoHoras(tiempoTotalEstimadoHoras);

            solicitudRepository.save(solicitud);

            log.info("Consolidación en Solicitud {}: costoTotalReal=${}, costoTotalEstimado=${}, " +
                    "tiempoTotalRealHoras={}h, tiempoTotalEstimadoHoras={}h",
                solicitud.getNroSolicitud(), costoTotalReal, costoTotalEstimado,
                tiempoTotalRealHoras, tiempoTotalEstimadoHoras);

        } catch (Exception e) {
            log.error("Error consolidando costos y tiempos en solicitud: {}", e.getMessage(), e);
        }
    }

    /**
     * Verifica si un camión está en uso (asignado a otro tramo en estado ASIGNADO o INICIADO).
     *
     * Un camión está en uso si existe al menos un tramo diferente al actual que:
     * - Tiene el mismo dominio de camión
     * - Está en estado ASIGNADO o INICIADO
     *
     * @param dominioCamion Dominio del camión a verificar
     * @param idTramoActual ID del tramo actual (para excluirlo de la búsqueda)
     * @return true si el camión está en uso, false en caso contrario
     */
    private boolean camionEstaEnUso(String dominioCamion, Long idTramoActual) {
        if (dominioCamion == null || dominioCamion.isBlank()) {
            return false;
        }

        try {
            // Buscar todos los tramos con este camión en estados activos
            List<Tramo> tramosConCamion = tramoRepository.findAll().stream()
                .filter(t -> dominioCamion.equals(t.getDominioCamion()))
                .filter(t -> !t.getIdTramo().equals(idTramoActual)) // Excluir tramo actual
                .filter(t -> t.getEstado() == EstadoTramo.ASIGNADO || t.getEstado() == EstadoTramo.INICIADO)
                .toList();

            if (!tramosConCamion.isEmpty()) {
                log.warn("Camión {} está en uso por {} tramo(s) activo(s)", dominioCamion, tramosConCamion.size());
                tramosConCamion.forEach(t ->
                    log.debug("  - Tramo {} en estado {}", t.getIdTramo(), t.getEstado())
                );
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("Error verificando disponibilidad del camión {}: {}", dominioCamion, e.getMessage());
            // En caso de error, asumir que está en uso por seguridad
            return true;
        }
    }

    /**
     * Resuelve la dirección real de un punto (origen o destino) basándose en las reglas de negocio.
     *
     * Lógica:
     * - Si depositoId != null → obtener dirección del depósito desde ms-logistica
     * - Si depositoId == null → usar direccionLibre
     * - Si ninguno está disponible → usar direccionFallback
     *
     * @param depositoId ID del depósito (puede ser null)
     * @param direccionLibre Dirección libre (puede ser null)
     * @param direccionFallback Dirección de fallback (del DTO original)
     * @param tipo "origen" o "destino" (para logs)
     * @return Dirección real a usar en Google Maps
     */
    private String resolverDireccionReal(Long depositoId, String direccionLibre,
                                        String direccionFallback, String tipo) {
        try {
            // Caso 1: Si hay depositoId, obtener dirección del depósito
            if (depositoId != null) {
                String direccionDeposito = obtenerDireccionDeposito(depositoId);
                if (direccionDeposito != null && !direccionDeposito.isBlank()) {
                    log.debug("Usando dirección de depósito {} para {}: '{}'",
                        depositoId, tipo, direccionDeposito);
                    return direccionDeposito;
                } else {
                    log.warn("No se pudo obtener dirección del depósito {}, usando fallback", depositoId);
                }
            }

            // Caso 2: Si hay direccionLibre, usarla
            if (direccionLibre != null && !direccionLibre.isBlank()) {
                log.debug("Usando dirección libre para {}: '{}'", tipo, direccionLibre);
                return direccionLibre;
            }

            // Caso 3: Usar direccionFallback del DTO original
            log.debug("Usando dirección fallback para {}: '{}'", tipo, direccionFallback);
            return direccionFallback != null ? direccionFallback : "";

        } catch (Exception e) {
            log.error("Error resolviendo dirección para {}: {}. Usando fallback", tipo, e.getMessage());
            return direccionFallback != null ? direccionFallback : "";
        }
    }

    /**
     * Obtiene la dirección de un depósito desde el servicio de logística.
     *
     * @param depositoId ID del depósito
     * @return Dirección del depósito, o null si no se puede obtener
     */
    private String obtenerDireccionDeposito(Long depositoId) {
        if (depositoId == null || logisticaClient.isEmpty()) {
            return null;
        }

        try {
            return logisticaClient.get().obtenerDeposito(depositoId)
                .onErrorResume(error -> {
                    log.warn("Error consultando depósito {}: {}", depositoId, error.getMessage());
                    return Mono.empty();
                })
                .map(response -> {
                    // Intentar obtener el campo "direccion"
                    Object direccionObj = response.get("direccion");
                    if (direccionObj instanceof String) {
                        return (String) direccionObj;
                    }

                    // Intentar campo alternativo "ubicacion"
                    Object ubicacionObj = response.get("ubicacion");
                    if (ubicacionObj instanceof String) {
                        return (String) ubicacionObj;
                    }

                    // Intentar construir dirección desde componentes
                    String calle = extraerString(response.get("calle"));
                    String ciudad = extraerString(response.get("ciudad"));
                    String provincia = extraerString(response.get("provincia"));

                    if (calle != null || ciudad != null) {
                        StringBuilder dir = new StringBuilder();
                        if (calle != null) dir.append(calle);
                        if (ciudad != null) {
                            if (dir.length() > 0) dir.append(", ");
                            dir.append(ciudad);
                        }
                        if (provincia != null) {
                            if (dir.length() > 0) dir.append(", ");
                            dir.append(provincia);
                        }
                        return dir.toString();
                    }

                    return null;
                })
                .blockOptional()
                .orElse(null);
        } catch (Exception e) {
            log.warn("No se pudo obtener dirección del depósito {}: {}", depositoId, e.getMessage());
            return null;
        }
    }

    /**
     * Extrae un String de un Object, con manejo seguro de tipos.
     *
     * @param obj Objeto a convertir
     * @return String extraído, o null si no es posible
     */
    private String extraerString(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }
        if (obj != null) {
            return obj.toString();
        }
        return null;
    }
}
