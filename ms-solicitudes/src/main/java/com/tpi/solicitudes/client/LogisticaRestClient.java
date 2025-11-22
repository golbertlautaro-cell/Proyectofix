package com.tpi.solicitudes.client;

import com.tpi.solicitudes.web.dto.CamionValidacionRequest;
import com.tpi.solicitudes.web.dto.CamionValidacionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Cliente REST moderno (Spring 6.1+) para comunicación con ms-logistica
 * Reemplaza LogisticaClient basado en WebClient (reactivo)
 * Mejor para operaciones síncronas sin overhead reactivo
 */
@Slf4j
//@Service
@RequiredArgsConstructor
public class LogisticaRestClient {

    private final RestClient restClientLogistica;

    /**
     * Consulta el estado de los camiones (libres/ocupados) en ms-logistica.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerEstadoCamiones() {
        try {
            log.info("Consultando estado de camiones en ms-logistica");
            return (Map<String, Object>) restClientLogistica.get()
                    .uri("/api/camiones/estado")
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            log.error("Error al consultar estado de camiones", e);
            throw e;
        }
    }

    /**
     * Valida si un camión tiene capacidad suficiente para un contenedor.
     * Implementa RF11 consultando ms-logistica.
     */
    public boolean validarCapacidadCamion(String dominio, Double peso, Double volumen) {
        try {
            log.debug("Validando capacidad del camión {} (peso: {}kg, volumen: {}m³)", 
                    dominio, peso, volumen);
            
            CamionValidacionRequest request = new CamionValidacionRequest(dominio, peso, volumen);
            
            CamionValidacionResponse response = restClientLogistica.post()
                    .uri("/api/camiones/validar-capacidad")
                    .body(request)
                    .retrieve()
                    .body(CamionValidacionResponse.class);
            
            boolean valido = response != null && response.valido();
            log.debug("Resultado validación: {}", valido);
            return valido;
            
        } catch (RestClientException e) {
            log.error("Error validando capacidad del camión {}", dominio, e);
            throw e;
        }
    }

    /**
     * Obtiene los datos de un camión específico por su dominio.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerCamion(String dominio) {
        try {
            log.info("Obteniendo datos del camión {}", dominio);
            return (Map<String, Object>) restClientLogistica.get()
                    .uri("/api/camiones/{dominio}", dominio)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            log.error("Error al obtener camión {}", dominio, e);
            throw e;
        }
    }
}
