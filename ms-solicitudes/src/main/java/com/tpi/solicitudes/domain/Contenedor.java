package com.tpi.solicitudes.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "contenedores")
@Schema(description = "Contenedor asociado a un cliente")
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contenedor")
    @Schema(description = "Identificador único del contenedor", example = "1")
    private Long idContenedor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    @JsonIgnore
    @Schema(description = "Cliente propietario del contenedor")
    private Cliente cliente;

    @NotBlank
    @Column(nullable = false, length = 100)
    @Schema(description = "Descripción del contenedor", example = "Contenedor de 20 pies")
    private String descripcion;

    @Column(length = 50)
    @Schema(description = "Tipo de contenedor", example = "DRY20")
    private String tipo;

    @Column(name = "capacidad_kg")
    @Schema(description = "Capacidad máxima en kilogramos", example = "25000")
    private Double capacidadKg;

    @Column(name = "estado", length = 30)
    @Schema(description = "Estado del contenedor", example = "DISPONIBLE")
    private String estado;
}
