package com.tpi.logistica.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * DTO para información de distancia entre dos puntos.
 * Contiene datos obtenidos de la API de Google Maps.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DistanciaDTO {
    
    /**
     * Ubicación de origen (dirección o coordenadas)
     */
    private String origen;
    
    /**
     * Ubicación de destino (dirección o coordenadas)
     */
    private String destino;
    
    /**
     * Distancia en kilómetros
     */
    private double kilometros;
    
    /**
     * Duración del viaje en formato texto (ej: "2 hours 30 mins")
     */
    private String duracionTexto;
}
