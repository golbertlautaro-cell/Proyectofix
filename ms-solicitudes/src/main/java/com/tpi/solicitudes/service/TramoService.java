package com.tpi.solicitudes.service;

import com.tpi.solicitudes.client.GoogleMapsClient;
import com.tpi.solicitudes.client.LogisticaClient;
import com.tpi.solicitudes.config.PricingProperties;
import com.tpi.solicitudes.domain.EstadoRuta;
import com.tpi.solicitudes.domain.EstadoSolicitud;
import com.tpi.solicitudes.domain.EstadoTramo;
import com.tpi.solicitudes.domain.Ruta;
import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.domain.Tramo;
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

    public TramoService(TramoRepository tramoRepository,
                        SolicitudRepository solicitudRepository,
                        RutaRepository rutaRepository,
                        @Autowired(required = false) LogisticaClient logisticaClient,
                        @Autowired(required = false) GoogleMapsClient googleMapsClient,
                        GoogleMapsService googleMapsService,
                        PricingProperties pricingProperties) {
        this.tramoRepository = tramoRepository;
        this.solicitudRepository = solicitudRepository;
        this.rutaRepository = rutaRepository;
        this.logisticaClient = Optional.ofNullable(logisticaClient);
        this.googleMapsClient = Optional.ofNullable(googleMapsClient);
        this.googleMapsService = googleMapsService;
        this.pricingProperties = pricingProperties;
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
        
        Tramo tramo = new Tramo();
        tramo.setOrigen(dto.getOrigen());
        tramo.setDestino(dto.getDestino());
        tramo.setDominioCamion(dto.getDominioCamion());
        tramo.setFechaHoraInicioEstimada(dto.getFechaHoraInicioEstimada());
        tramo.setFechaHoraFinEstimada(dto.getFechaHoraFinEstimada());
        double diasDepositoEstimados = resolverDiasDepositoEstimados(dto);
        tramo.setDiasDepositoEstimados(diasDepositoEstimados);
        tramo.setDepositoId(dto.getDepositoId());
        completarEstimacionesTramo(tramo, dto.getOrigen(), dto.getDestino(), diasDepositoEstimados);
        tramo.setRuta(ruta);
        tramo.setEstado(EstadoTramo.PENDIENTE);
        
        return tramoRepository.save(tramo);
    }

    public Tramo actualizar(Long id, Tramo tramo) {
        Tramo actual = obtener(id);
        actual.setOrigen(tramo.getOrigen());
        actual.setDestino(tramo.getDestino());
        actual.setDominioCamion(tramo.getDominioCamion());
        actual.setEstado(tramo.getEstado());
        actual.setFechaHoraInicioReal(tramo.getFechaHoraInicioReal());
        actual.setFechaHoraFinReal(tramo.getFechaHoraFinReal());
        actual.setCostoReal(tramo.getCostoReal());
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

    public Mono<Tramo> asignarACamion(Long idTramo, String dominioCamion) {
        // NOTA: No tenemos peso/volumen del contenedor en el modelo actual.
        // Por ahora, se parametriza con 0.0. Ajustar cuando haya origen real de datos.
        Double pesoContenedor = 0.0;
        Double volumenContenedor = 0.0;

        return Mono.fromCallable(() -> obtener(idTramo))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(tramo -> {
                    if (logisticaClient.isEmpty()) {
                        return Mono.just(true);
                    }
                    return logisticaClient.get().validarCapacidadCamion(dominioCamion, pesoContenedor, volumenContenedor)
                        .defaultIfEmpty(false);
                })
                .flatMap(valido -> {
                    if (!valido) {
                        return Mono.error(new IllegalStateException("Capacidad insuficiente del camión para el contenedor"));
                    }
                    Tramo tramo = obtener(idTramo);
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

    public Tramo iniciarTramo(Long idTramo, Double odometroInicial) {
        Tramo tramo = obtener(idTramo);
        tramo.setEstado(EstadoTramo.INICIADO);
        tramo.setFechaHoraInicioReal(LocalDateTime.now());
        if (odometroInicial != null) {
            tramo.setOdometroInicial(odometroInicial);
        }
        Tramo actualizado = tramoRepository.save(tramo);

        actualizarEstadoRutaYSolicitudAlIniciar(tramo);

        return actualizado;
    }

    public Tramo finalizarTramo(Long idTramo, LocalDateTime fechaHoraFin, Double odometroFinal,
                                 Double tiempoReal) {
        Tramo tramo = obtener(idTramo);
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

        double diasDepositoReales = calcularDiasDeposito(tramo.getFechaHoraInicioReal(), tramo.getFechaHoraFinReal());
        tramo.setDiasDepositoReales(diasDepositoReales);

        double costoCalculado = calcularCostoReal(tramo, distanciaReal, diasDepositoReales);
        tramo.setCostoReal(costoCalculado);
        Tramo actualizado = tramoRepository.save(tramo);

        actualizarEstadoRutaYSolicitudAlFinalizar(tramo);

        return actualizado;
    }

    private void actualizarEstadoRutaYSolicitudAlIniciar(Tramo tramo) {
        if (tramo.getRuta() == null) {
            return;
        }
        Long rutaId = tramo.getRuta().getIdRuta();
        if (rutaId == null) {
            return;
        }
        rutaRepository.findById(rutaId).ifPresent(ruta -> {
            if (ruta.getEstado() == null || ruta.getEstado() == EstadoRuta.PENDIENTE) {
                ruta.setEstado(EstadoRuta.EJECUTANDOSE);
                rutaRepository.save(ruta);
            }
            Solicitud solicitud = ruta.getSolicitud();
            if (solicitud != null && solicitud.getEstado() == EstadoSolicitud.PROGRAMADA) {
                solicitud.setEstado(EstadoSolicitud.EN_TRANSITO);
                solicitudRepository.save(solicitud);
            }
        });
    }

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
                ruta.setEstado(EstadoRuta.COMPLETADA);
                rutaRepository.save(ruta);
                Solicitud solicitud = ruta.getSolicitud();
                if (solicitud != null) {
                    solicitud.setEstado(EstadoSolicitud.COMPLETADA);
                    solicitudRepository.save(solicitud);
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

    private void completarEstimacionesTramo(Tramo tramo, String origen, String destino, double diasDeposito) {
        try {
            DistanciaDTO distancia = googleMapsService.calcularDistancia(origen, destino);
            double distanciaKm = distancia != null ? distancia.getKilometros() : 0d;
            tramo.setDistanciaEstimadaKm(distanciaKm);
            double costoEstimado = calcularCostoAproximado(distanciaKm, diasDeposito);
            tramo.setCostoAproximado(costoEstimado);
        } catch (RuntimeException ex) {
            log.warn("No se pudo calcular distancia estimada para el tramo entre {} y {}: {}", origen, destino, ex.getMessage());
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

    private double calcularCostoReal(Tramo tramo, double distanciaReal, double diasDepositoReales) {
        Map<String, Object> camion = consultarCamion(tramo.getDominioCamion());
        double costoBaseKm = extraerValor(camion.get("costoBaseKm"), pricingProperties.getTarifaBasePromedio());
        double consumoCombustible = extraerValor(camion.get("consumoCombustible"), pricingProperties.getConsumoPromedioGeneral());

        double costoTransporte = distanciaReal * costoBaseKm;
        double costoCombustible = (distanciaReal / 100d) * consumoCombustible * pricingProperties.getPrecioLitroCombustible();
        double costoDiarioDeposito = obtenerCostoDiarioDeposito(tramo.getDepositoId());
        double costoEstadia = diasDepositoReales * costoDiarioDeposito;

        return costoTransporte + costoCombustible + costoEstadia;
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
}
