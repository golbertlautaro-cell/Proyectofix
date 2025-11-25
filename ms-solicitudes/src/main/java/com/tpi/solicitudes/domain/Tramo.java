package com.tpi.solicitudes.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tramos")
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tramo")
    private Long idTramo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ruta", nullable = false)
    @JsonIgnore
    private Ruta ruta;

    // Origen y destino: ahora pueden ser depósito (referencia) o dirección libre
    @Column(name = "origen", length = 100)
    private String origen;

    @Column(name = "destino", length = 100)
    private String destino;

    // Referencias a depósitos (opcional)
    @Column(name = "origen_deposito_id")
    private Long origenDepositoId;

    @Column(name = "destino_deposito_id")
    private Long destinoDepositoId;

    // Direcciones libres (si no se usa depósito)
    @Column(name = "origen_direccion_libre", length = 200)
    private String origenDireccionLibre;

    @Column(name = "destino_direccion_libre", length = 200)
    private String destinoDireccionLibre;

    @Pattern(regexp = "^([A-Z]{3}[0-9]{3}|[A-Z]{2}[0-9]{3}[A-Z]{2})$", message = "Formato de dominio inválido")
    @Column(name = "dominio_camion", length = 20)
    private String dominioCamion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30)
    private EstadoTramo estado;

    @Column(name = "fecha_hora_inicio_real")
    private LocalDateTime fechaHoraInicioReal;

    @Column(name = "fecha_hora_fin_real")
    private LocalDateTime fechaHoraFinReal;

    @Column(name = "odometro_inicial")
    private Double odometroInicial;

    @Column(name = "odometro_final")
    private Double odometroFinal;

    @Column(name = "costo_real")
    private Double costoReal;

    @Column(name = "tiempo_real")
    private Double tiempoReal;

    @Column(name = "costo_aproximado")
    private Double costoAproximado;

    @Column(name = "fecha_hora_inicio_estimada")
    private LocalDateTime fechaHoraInicioEstimada;

    @Column(name = "fecha_hora_fin_estimada")
    private LocalDateTime fechaHoraFinEstimada;

    @Column(name = "distancia_estimada_km")
    private Double distanciaEstimadaKm;

    @Column(name = "distancia_real_km")
    private Double distanciaRealKm;

    // Nuevos campos para estadía y costo real de estadía
    @Column(name = "tiempo_estadia_horas")
    private Double tiempoEstadiaHoras;

    @Column(name = "costo_estadia_real")
    private Double costoEstadiaReal;

    // Orden del tramo en la ruta (obligatorio)
    @Column(name = "orden", nullable = false)
    private Integer orden;

    // El campo depositoId fue reemplazado por origen/destino deposito refs
}
