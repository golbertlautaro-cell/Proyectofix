package com.tpi.logistica.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "camiones")
public class Camion {

    @Id
    @NotBlank
    @Pattern(regexp = "^([A-Z]{3}[0-9]{3}|[A-Z]{2}[0-9]{3}[A-Z]{2})$", message = "Formato de dominio inválido")
    @Column(name = "dominio", length = 20, nullable = false, updatable = false)
    private String dominio;

    @PositiveOrZero
    @Column(name = "capacidad_peso")
    private Double capacidadPeso;

    @PositiveOrZero
    @Column(name = "capacidad_volumen")
    private Double capacidadVolumen;

    @PositiveOrZero
    @Column(name = "consumo_promedio")
    private Double consumoPromedio;

    @PositiveOrZero
    @Column(name = "costo_base_km")
    private Double costoBaseKm;

    @Column(name = "disponibilidad")
    private Boolean disponibilidad;

    @Column(name = "nombre_transportista", length = 100)
    private String nombreTransportista;

    @Column(name = "telefono", length = 20)
    private String telefono;
}
