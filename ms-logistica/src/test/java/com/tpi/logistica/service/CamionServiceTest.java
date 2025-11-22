package com.tpi.logistica.service;

import com.tpi.logistica.domain.Camion;
import com.tpi.logistica.repository.CamionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CamionServiceTest {

    @Mock
    private CamionRepository camionRepository;

    @InjectMocks
    private CamionService camionService;

    private Camion camionValido;

    @BeforeEach
    void setUp() {
        camionValido = Camion.builder()
                .dominio("ABC123")
                .capacidadPeso(15000.0)
                .capacidadVolumen(30.0)
                .consumoPromedio(12.5)
                .costoBaseKm(50.0)
                .disponibilidad(true)
                .build();
    }

    // ========== TESTS PARA validarCapacidad (RF11) ==========

    @Test
    void validarCapacidad_CamionExistenteYCapacidadSuficiente_RetornaTrue() {
        // Arrange
        when(camionRepository.findById("ABC123")).thenReturn(Optional.of(camionValido));

        // Act
        boolean resultado = camionService.validarCapacidad("ABC123", 10000.0, 25.0);

        // Assert
        assertTrue(resultado);
        verify(camionRepository).findById("ABC123");
    }

    @Test
    void validarCapacidad_PesoExactoIgualACapacidad_RetornaTrue() {
        // Arrange
        when(camionRepository.findById("ABC123")).thenReturn(Optional.of(camionValido));

        // Act
        boolean resultado = camionService.validarCapacidad("ABC123", 15000.0, 30.0);

        // Assert
        assertTrue(resultado, "Debe aceptar peso y volumen exactamente iguales a la capacidad");
    }

    @Test
    void validarCapacidad_PesoExcedeLaCapacidad_RetornaFalse() {
        // Arrange
        when(camionRepository.findById("ABC123")).thenReturn(Optional.of(camionValido));

        // Act
        boolean resultado = camionService.validarCapacidad("ABC123", 16000.0, 25.0);

        // Assert
        assertFalse(resultado, "Debe rechazar contenedores con peso que excede la capacidad");
    }

    @Test
    void validarCapacidad_VolumenExcedeLaCapacidad_RetornaFalse() {
        // Arrange
        when(camionRepository.findById("ABC123")).thenReturn(Optional.of(camionValido));

        // Act
        boolean resultado = camionService.validarCapacidad("ABC123", 10000.0, 35.0);

        // Assert
        assertFalse(resultado, "Debe rechazar contenedores con volumen que excede la capacidad");
    }

    @Test
    void validarCapacidad_CamionNoExiste_RetornaFalse() {
        // Arrange
        when(camionRepository.findById("NOEXISTE")).thenReturn(Optional.empty());

        // Act
        boolean resultado = camionService.validarCapacidad("NOEXISTE", 10000.0, 25.0);

        // Assert
        assertFalse(resultado, "Debe retornar false si el camión no existe");
    }

    @Test
    void validarCapacidad_DominioNulo_RetornaFalse() {
        // Act
        boolean resultado = camionService.validarCapacidad(null, 10000.0, 25.0);

        // Assert
        assertFalse(resultado, "Debe retornar false si el dominio es nulo");
        verify(camionRepository, never()).findById(any());
    }

    @Test
    void validarCapacidad_DominioVacio_RetornaFalse() {
        // Act
        boolean resultado = camionService.validarCapacidad("", 10000.0, 25.0);

        // Assert
        assertFalse(resultado, "Debe retornar false si el dominio está vacío");
        verify(camionRepository, never()).findById(any());
    }

    @Test
    void validarCapacidad_PesoNulo_RetornaFalse() {
        // Act
        boolean resultado = camionService.validarCapacidad("ABC123", null, 25.0);

        // Assert
        assertFalse(resultado, "Debe retornar false si el peso es nulo");
        verify(camionRepository, never()).findById(any());
    }

    @Test
    void validarCapacidad_VolumenNulo_RetornaFalse() {
        // Act
        boolean resultado = camionService.validarCapacidad("ABC123", 10000.0, null);

        // Assert
        assertFalse(resultado, "Debe retornar false si el volumen es nulo");
        verify(camionRepository, never()).findById(any());
    }

    @Test
    void validarCapacidad_PesoNegativo_RetornaFalse() {
        // Act
        boolean resultado = camionService.validarCapacidad("ABC123", -100.0, 25.0);

        // Assert
        assertFalse(resultado, "Debe retornar false si el peso es negativo");
        verify(camionRepository, never()).findById(any());
    }

    @Test
    void validarCapacidad_VolumenNegativo_RetornaFalse() {
        // Act
        boolean resultado = camionService.validarCapacidad("ABC123", 10000.0, -5.0);

        // Assert
        assertFalse(resultado, "Debe retornar false si el volumen es negativo");
        verify(camionRepository, never()).findById(any());
    }

    @Test
    void validarCapacidad_CamionConCapacidadPesoNula_RetornaFalse() {
        // Arrange
        camionValido.setCapacidadPeso(null);
        when(camionRepository.findById("ABC123")).thenReturn(Optional.of(camionValido));

        // Act
        boolean resultado = camionService.validarCapacidad("ABC123", 10000.0, 25.0);

        // Assert
        assertFalse(resultado, "Debe retornar false si la capacidad de peso del camión es nula");
    }

    @Test
    void validarCapacidad_CamionConCapacidadVolumenNula_RetornaFalse() {
        // Arrange
        camionValido.setCapacidadVolumen(null);
        when(camionRepository.findById("ABC123")).thenReturn(Optional.of(camionValido));

        // Act
        boolean resultado = camionService.validarCapacidad("ABC123", 10000.0, 25.0);

        // Assert
        assertFalse(resultado, "Debe retornar false si la capacidad de volumen del camión es nula");
    }

    // ========== TESTS PARA FILTROS ==========

    @Test
    void listar_SinFiltros_RetornaTodosCamiones() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Camion> camiones = Arrays.asList(camionValido);
        Page<Camion> page = new PageImpl<>(camiones, pageable, camiones.size());
        when(camionRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<Camion> resultado = camionService.listar(pageable, null, null, null, null);

        // Assert
        assertEquals(1, resultado.getTotalElements());
        verify(camionRepository).findAll(pageable);
    }

    @Test
    void listar_ConFiltroPeso_UsaRepositoryConRangoPeso() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Camion> camiones = Arrays.asList(camionValido);
        Page<Camion> page = new PageImpl<>(camiones, pageable, camiones.size());
        when(camionRepository.findByCapacidadPesoBetween(10000.0, 20000.0, pageable)).thenReturn(page);

        // Act
        Page<Camion> resultado = camionService.listar(pageable, 10000.0, 20000.0, null, null);

        // Assert
        assertEquals(1, resultado.getTotalElements());
        verify(camionRepository).findByCapacidadPesoBetween(10000.0, 20000.0, pageable);
    }

    @Test
    void listar_ConFiltroVolumen_UsaRepositoryConRangoVolumen() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Camion> camiones = Arrays.asList(camionValido);
        Page<Camion> page = new PageImpl<>(camiones, pageable, camiones.size());
        when(camionRepository.findByCapacidadVolumenBetween(20.0, 40.0, pageable)).thenReturn(page);

        // Act
        Page<Camion> resultado = camionService.listar(pageable, null, null, 20.0, 40.0);

        // Assert
        assertEquals(1, resultado.getTotalElements());
        verify(camionRepository).findByCapacidadVolumenBetween(20.0, 40.0, pageable);
    }

    @Test
    void listar_ConFiltrosPesoYVolumen_UsaRepositoryConAmbosRangos() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Camion> camiones = Arrays.asList(camionValido);
        Page<Camion> page = new PageImpl<>(camiones, pageable, camiones.size());
        when(camionRepository.findByCapacidadPesoBetweenAndCapacidadVolumenBetween(
                10000.0, 20000.0, 20.0, 40.0, pageable)).thenReturn(page);

        // Act
        Page<Camion> resultado = camionService.listar(pageable, 10000.0, 20000.0, 20.0, 40.0);

        // Assert
        assertEquals(1, resultado.getTotalElements());
        verify(camionRepository).findByCapacidadPesoBetweenAndCapacidadVolumenBetween(
                10000.0, 20000.0, 20.0, 40.0, pageable);
    }

    @Test
    void listar_SoloMinPeso_UsaValorPorDefectoParaMax() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Camion> camiones = Arrays.asList(camionValido);
        Page<Camion> page = new PageImpl<>(camiones, pageable, camiones.size());
        when(camionRepository.findByCapacidadPesoBetween(eq(10000.0), eq(Double.MAX_VALUE), eq(pageable)))
                .thenReturn(page);

        // Act
        Page<Camion> resultado = camionService.listar(pageable, 10000.0, null, null, null);

        // Assert
        assertEquals(1, resultado.getTotalElements());
        verify(camionRepository).findByCapacidadPesoBetween(10000.0, Double.MAX_VALUE, pageable);
    }

    // ========== TESTS PARA obtenerEstadoCamiones ==========

    @Test
    void obtenerEstadoCamiones_ConCamionesLibresYOcupados_RetornaResumenCorrecto() {
        // Arrange
        Camion camionLibre1 = Camion.builder().dominio("ABC123").disponibilidad(true).build();
        Camion camionLibre2 = Camion.builder().dominio("DEF456").disponibilidad(true).build();
        Camion camionOcupado = Camion.builder().dominio("GHI789").disponibilidad(false).build();
        Camion camionSinEstado = Camion.builder().dominio("JKL012").disponibilidad(null).build();
        
        List<Camion> camiones = Arrays.asList(camionLibre1, camionLibre2, camionOcupado, camionSinEstado);
        when(camionRepository.findAll()).thenReturn(camiones);

        // Act
        Map<String, Object> estado = camionService.obtenerEstadoCamiones();

        // Assert
        assertEquals(4, estado.get("total"));
        assertEquals(2L, estado.get("libres"));
        assertEquals(1L, estado.get("ocupados"));
        assertEquals(1L, estado.get("sinEstado"));
    }

    @Test
    void obtenerEstadoCamiones_SinCamiones_RetornaCeros() {
        // Arrange
        when(camionRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        Map<String, Object> estado = camionService.obtenerEstadoCamiones();

        // Assert
        assertEquals(0, estado.get("total"));
        assertEquals(0L, estado.get("libres"));
        assertEquals(0L, estado.get("ocupados"));
        assertEquals(0L, estado.get("sinEstado"));
    }
}
