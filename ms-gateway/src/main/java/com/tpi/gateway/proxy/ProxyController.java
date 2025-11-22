package com.tpi.gateway.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class ProxyController {

    private final WebClient webClientLogistica;
    private final WebClient webClientSolicitudes;

    public ProxyController(@Value("${services.logistica:http://localhost:8081}") String logisticaBase,
                           @Value("${services.solicitudes:http://localhost:8082}") String solicitudesBase) {
        this.webClientLogistica = WebClient.builder().baseUrl(logisticaBase).build();
        this.webClientSolicitudes = WebClient.builder().baseUrl(solicitudesBase).build();
    }

    @GetMapping(value = "/solicitudes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getSolicitudes(@RequestHeader HttpHeaders headers) {
        return webClientSolicitudes.get()
                .uri("/api/solicitudes")
                .headers(h -> h.addAll(headers))
                .retrieve()
                .toEntity(String.class);
    }

    @GetMapping(value = "/logistica/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<String>> getLogisticaById(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        return webClientLogistica.get()
                .uri(uriBuilder -> uriBuilder.path("/api/logistica/{id}").build(id))
                .headers(h -> h.addAll(headers))
                .retrieve()
                .toEntity(String.class);
    }
}
