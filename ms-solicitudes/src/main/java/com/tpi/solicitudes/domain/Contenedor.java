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

    // Estado del contenedor. Valores posibles: EN_ORIGEN, EN_TRANSITO, EN_DEPOSITO, ENTREGADO, DISPONIBLE
    @Column(name = "estado", length = 30)
    @Schema(description = "Estado del contenedor. Valores: EN_ORIGEN, EN_TRANSITO, EN_DEPOSITO, ENTREGADO, DISPONIBLE", example = "DISPONIBLE")
    private String estado;

    // Nuevo: peso real actual del contenedor (kg)
    @Column(name = "peso_real")
    @Schema(description = "Peso real actual del contenedor en kilogramos", example = "1200.5")
    private Double pesoReal;

    // Nuevo: volumen real actual del contenedor (m3)
    @Column(name = "volumen_real")
    @Schema(description = "Volumen real actual del contenedor en metros cúbicos", example = "12.3")
    private Double volumenReal;

    // Nuevo: referencia al depósito actual donde se encuentra el contenedor (si aplica)
    @Column(name = "deposito_actual_id")
    @Schema(description = "ID del depósito actual donde se encuentra el contenedor", example = "5")
    private Long depositoActualId;
}
