package com.tpi.logistica.web;

import com.tpi.logistica.dto.DistanciaDTO;
import com.tpi.logistica.service.GoogleMapsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST temporal para pruebas de integración con Google Maps.
 * Expone endpoints para calcular distancias entre puntos geográficos.
 * 
 * NOTA: Este es un controlador temporal para verificación. 
 * Debe ser removido o refactorizado en producción.
 */
@Slf4j
@RestController
@RequestMapping("/api/distancia")
@Tag(name = "Geolocalización", description = "Endpoints para cálculos de distancia y geolocalización")
public class GeoController {

    @Autowired
    private GoogleMapsService googleMapsService;

    /**
     * Calcula la distancia y duración entre dos puntos geográficos.
     * 
     * @param origen Dirección o coordenadas del punto de origen (ej: "Buenos Aires, Argentina")
     * @param destino Dirección o coordenadas del punto de destino (ej: "Córdoba, Argentina")
     * @return ResponseEntity con DistanciaDTO conteniendo la distancia en km y duración
     */
    @GetMapping
    @Operation(
            summary = "Calcular distancia entre dos puntos",
            description = "Utiliza Google Maps Distance Matrix API para calcular la distancia y duración de viaje entre dos puntos geográficos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Distancia calculada exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DistanciaDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parámetros inválidos o faltantes"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error en la API de Google Maps o error interno del servidor"
            )
    })
    public ResponseEntity<DistanciaDTO> calcularDistancia(
            @Parameter(
                    description = "Punto de origen (dirección o coordenadas lat,lng)",
                    example = "Buenos Aires, Argentina",
                    required = true
            )
            @RequestParam String origen,
            
            @Parameter(
                    description = "Punto de destino (dirección o coordenadas lat,lng)",
                    example = "Córdoba, Argentina",
                    required = true
            )
            @RequestParam String destino
    ) {
        log.info("Solicitud recibida: calcular distancia desde '{}' hasta '{}'", origen, destino);
        
        try {
            DistanciaDTO resultado = googleMapsService.calcularDistancia(origen, destino);
            log.info("Respuesta exitosa: {} km en {}", resultado.getKilometros(), resultado.getDuracionTexto());
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            log.error("Error al calcular distancia: {}", e.getMessage());
            throw e;
        }
    }
}
