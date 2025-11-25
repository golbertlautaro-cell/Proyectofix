package com.tpi.solicitudes.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

//@Component
public class LogisticaClient {

    private final Optional<WebClient> webClient;

    public LogisticaClient(@Autowired(required = false) WebClient webClientLogistica) {
        this.webClient = Optional.ofNullable(webClientLogistica);
    }

    /**
     * Consulta el estado de los camiones (libres/ocupados) en ms-logistica.
     */
    public Mono<Map<String, Object>> obtenerEstadoCamiones() {
        if (webClient.isEmpty()) {
            return Mono.empty();
        }
        return webClient.get().get()
                .uri("/api/camiones/estado")
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * Valida si un camión tiene capacidad suficiente para un contenedor.
     * Implementa RF11 consultando ms-logistica.
     */
    public Mono<Boolean> validarCapacidadCamion(String dominio, Double peso, Double volumen) {
        if (webClient.isEmpty()) {
            return Mono.just(true);
        }
        Map<String, Object> request = Map.of(
                "dominio", dominio,
                "pesoContenedor", peso,
                "volumenContenedor", volumen
        );

    return webClient.get().post()
        .uri("/api/camiones/validar-capacidad")
        .bodyValue(request)
        .retrieve()
        .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
        .map(resp -> resp != null && Boolean.TRUE.equals(resp.get("valido")));
    }

    /**
     * Obtiene los datos de un camión específico por su dominio.
     */
    public Mono<Map<String, Object>> obtenerCamion(String dominio) {
        if (webClient.isEmpty()) {
            return Mono.empty();
        }
        return webClient.get().get()
                .uri("/api/camiones/{dominio}", dominio)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * Obtiene los datos de un depósito específico por su identificador.
     */
    public Mono<Map<String, Object>> obtenerDeposito(Long depositoId) {
        if (webClient.isEmpty()) {
            return Mono.empty();
        }
        return webClient.get().get()
                .uri("/api/depositos/{id}", depositoId)
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});
    }

    /**
     * Obtiene todos los camiones disponibles en ms-logistica.
     *
     * @return Mono con lista de camiones (cada camión es un Map con sus propiedades)
     */
    public Mono<java.util.List<Map<String, Object>>> obtenerTodosLosCamiones() {
        if (webClient.isEmpty()) {
            return Mono.just(java.util.List.of());
        }
        return webClient.get().get()
                .uri("/api/camiones")
                .retrieve()
                .bodyToMono(new org.springframework.core.ParameterizedTypeReference<java.util.List<Map<String, Object>>>() {});
    }
}
