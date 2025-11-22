package com.tpi.solicitudes.web;

import com.tpi.solicitudes.client.LogisticaClient;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/integracion")
public class IntegracionController {

    private final Optional<LogisticaClient> logisticaClient;

    public IntegracionController(@Autowired(required = false) LogisticaClient logisticaClient) {
        this.logisticaClient = Optional.ofNullable(logisticaClient);
    }

    /**
     * Endpoint que consulta el estado de camiones desde ms-logistica.
     */
    @GetMapping("/camiones/estado")
    public Mono<Map<String, Object>> obtenerEstadoCamiones() {
        if (logisticaClient.isEmpty()) {
            return Mono.just(Map.of());
        }
        return logisticaClient.get().obtenerEstadoCamiones();
    }

    /**
     * Endpoint para validar capacidad de un camión consultando ms-logistica.
     */
    @PostMapping("/camiones/validar-capacidad")
    public Mono<Map<String, Object>> validarCapacidad(
            @RequestParam String dominio,
            @RequestParam Double peso,
            @RequestParam Double volumen) {
        if (logisticaClient.isEmpty()) {
            return Mono.just(Map.of("valido", true));
        }
        return logisticaClient.get().validarCapacidadCamion(dominio, peso, volumen)
                .map(valido -> Map.of("valido", valido));
    }

    /**
     * Endpoint para obtener datos de un camión desde ms-logistica.
     */
    @GetMapping("/camiones/{dominio}")
    public Mono<Map<String, Object>> obtenerCamion(@PathVariable String dominio) {
        if (logisticaClient.isEmpty()) {
            return Mono.just(Map.of());
        }
        return logisticaClient.get().obtenerCamion(dominio);
    }
}
