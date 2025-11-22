package com.tpi.solicitudes.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "rutas")
@Schema(description = "Ruta de transporte alternativa para una solicitud")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    @Schema(description = "Identificador único de la ruta", example = "1")
    private Long idRuta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "nro_solicitud", nullable = false)
    @Schema(description = "Solicitud a la que pertenece la ruta")
    @JsonIgnore
    private Solicitud solicitud;

    @Column(name = "nombre", nullable = false, length = 150)
    @Schema(description = "Nombre descriptivo de la ruta", example = "Ruta principal por Ruta 9")
    private String nombre;

    @Column(name = "descripcion", length = 500)
    @Schema(description = "Descripción detallada de la ruta", example = "Ruta que utiliza Ruta 9 como eje principal")
    private String descripcion;

    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Schema(description = "Lista de tramos que conforman la ruta")
    private List<Tramo> tramos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    @Schema(description = "Estado de la ruta", example = "PENDIENTE")
    private EstadoRuta estado;

    @Column(name = "distancia_total_km")
    @Schema(description = "Distancia total de la ruta en kilómetros (calculada)", example = "150.5")
    private Double distanciaTotalKm;

    @Column(name = "duracion_estimada_horas")
    @Schema(description = "Duración estimada en horas (calculada)", example = "2.5")
    private Double duracionEstimadaHoras;

    @Column(name = "costo_estimado")
    @Schema(description = "Costo estimado de la ruta (calculado)", example = "1500.00")
    private Double costoEstimado;

    @Column(name = "costo_real")
    @Schema(description = "Costo real de la ruta (calculado al finalizar)", example = "1550.00")
    private Double costoReal;

    @Column(name = "es_ruta_seleccionada")
    @Schema(description = "Indica si esta es la ruta seleccionada para ejecutar", example = "false")
    private Boolean esRutaSeleccionada = false;

    @Column(name = "fecha_creacion")
    @Schema(description = "Fecha de creación de la ruta")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    @Schema(description = "Fecha de última actualización")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = this.fechaCreacion;
        if (this.estado == null) {
            this.estado = EstadoRuta.PENDIENTE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
