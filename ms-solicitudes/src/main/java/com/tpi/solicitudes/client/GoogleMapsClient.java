package com.tpi.solicitudes.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;

//@Component
public class GoogleMapsClient {

    private final Optional<WebClient> webClient;
    private final String apiKey;

    public GoogleMapsClient(@Autowired(required = false) WebClient.Builder builder,
                            @Value("${google.maps.base-url:https://maps.googleapis.com/maps/api/directions/json}") String baseUrl,
                            @Value("${google.maps.api-key:}") String apiKey) {
        this.webClient = Optional.ofNullable(builder != null ? builder.baseUrl(baseUrl).build() : null);
        this.apiKey = apiKey;
    }

    /**
     * Llama a la API de Google Directions y devuelve distancia (km) y duración (minutos).
     */
    public Mono<Map<String, Object>> obtenerDistanciaYDuracion(double origenLat, double origenLng, double destinoLat, double destinoLng) {
        if (webClient.isEmpty()) {
            return Mono.just(Map.of("distanciaKm", 0.0, "duracionMinutos", 0.0));
        }
        String origin = origenLat + "," + origenLng;
        String destination = destinoLat + "," + destinoLng;

        return webClient.get().get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("origin", origin)
                        .queryParam("destination", destination)
                        .queryParam("key", apiKey)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .flatMap(body -> {
                    Object status = body.get("status");
                    if (status == null || !"OK".equalsIgnoreCase(String.valueOf(status))) {
                        return Mono.error(new IllegalStateException("Google Directions respondió con estado: " + status));
                    }
                    Object routesObj = body.get("routes");
                    if (!(routesObj instanceof List<?> routes) || routes.isEmpty()) {
                        return Mono.error(new IllegalStateException("Sin rutas en la respuesta de Google Directions"));
                    }
                    Object route0 = routes.get(0);
                    if (!(route0 instanceof Map<?,?>)) {
                        return Mono.error(new IllegalStateException("Formato inesperado de routes[0]"));
                    }
                    Object legsObj = ((Map<?,?>) route0).get("legs");
                    if (!(legsObj instanceof List<?> legs) || legs.isEmpty()) {
                        return Mono.error(new IllegalStateException("Sin legs en la ruta"));
                    }
                    Object leg0 = legs.get(0);
                    if (!(leg0 instanceof Map<?,?>)) {
                        return Mono.error(new IllegalStateException("Formato inesperado de legs[0]"));
                    }
                    Object distanceObj = ((Map<?,?>) leg0).get("distance");
                    Object durationObj = ((Map<?,?>) leg0).get("duration");
                    if (!(distanceObj instanceof Map<?,?>)) {
                        return Mono.error(new IllegalStateException("Sin distancia en la leg"));
                    }
                    if (!(durationObj instanceof Map<?,?>)) {
                        return Mono.error(new IllegalStateException("Sin duración en la leg"));
                    }
                    Object meters = ((Map<?,?>) distanceObj).get("value");
                    Object seconds = ((Map<?,?>) durationObj).get("value");
                    if (!(meters instanceof Number numM) || !(seconds instanceof Number numS)) {
                        return Mono.error(new IllegalStateException("Valor de distancia o duración no numérico"));
                    }
                    double km = numM.doubleValue() / 1000.0;
                    long minutos = Math.round(numS.doubleValue() / 60.0);
                    return Mono.just(Map.of("distanciaKm", km, "duracionMinutos", minutos));
                });
    }
}
