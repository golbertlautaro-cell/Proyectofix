package com.tpi.solicitudes.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "solicitudes")
@Schema(description = "Solicitud de transporte")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nro_solicitud")
    @Schema(description = "Número único de solicitud", example = "1")
    private Long nroSolicitud;

    @Column(name = "id_contenedor")
    @Schema(description = "ID del contenedor", example = "1")
    private Long idContenedor;

    @Column(name = "id_cliente")
    @Schema(description = "ID del cliente", example = "1")
    private Long idCliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    @Schema(description = "Estado de la solicitud", example = "BORRADOR")
    private EstadoSolicitud estado;

    @Column(name = "costo_estimado")
    @Schema(description = "Costo estimado del transporte", example = "1500.00")
    private Double costoEstimado;

    @Column(name = "costo_final")
    @Schema(description = "Costo final del transporte", example = "1550.00")
    private Double costoFinal;

    @Column(name = "tiempo_real")
    @Schema(description = "Tiempo real de transporte en horas", example = "2.5")
    private Double tiempoReal;

    @Column(name = "fecha_creacion")
    @Schema(description = "Fecha de creación de la solicitud")
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
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
