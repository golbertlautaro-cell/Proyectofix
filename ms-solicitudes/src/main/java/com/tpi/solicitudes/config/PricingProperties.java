package com.tpi.solicitudes.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propiedades de configuración para cálculos de costos de los tramos.
 */
@Data
@Component
@ConfigurationProperties(prefix = "pricing")
public class PricingProperties {

    /** Tarifa promedio por kilómetro utilizada para estimaciones iniciales. */
    private double tarifaBasePromedio = 45.0;

    /** Consumo de combustible promedio (litros cada 100 km) para estimaciones. */
    private double consumoPromedioGeneral = 32.0;

    /** Precio promedio por litro de combustible. */
    private double precioLitroCombustible = 1.5;

    /** Costo diario configurado para estadías en depósito. */
    private double costoDiarioDeposito = 150.0;

    /** Valor por defecto cuando no se informan días estimados en depósito. */
    private double diasDepositoEstimadoDefault = 0.5;
}
