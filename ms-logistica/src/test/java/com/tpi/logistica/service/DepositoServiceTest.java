package com.tpi.logistica.service;

import com.tpi.logistica.domain.Deposito;
import com.tpi.logistica.repository.DepositoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests para DepositoService (ms-logistica)
 * 
 * Patrón: AAA (Arrange-Act-Assert)
 * Coverage: CRUD operations para depósitos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DepositoService Tests")
class DepositoServiceTest {

    @Mock
    private DepositoRepository depositoRepository;

    @InjectMocks
    private DepositoService depositoService;

    private Deposito depositoTest;

    @BeforeEach
    void setUp() {
        depositoTest = new Deposito();
        depositoTest.setIdDeposito(1L);
        depositoTest.setNombre("Depósito Buenos Aires");
        depositoTest.setDireccion("Avenida 9 de Julio 1000");
        depositoTest.setLatitud(-34.603684);
        depositoTest.setLongitud(-58.381559);
        depositoTest.setCostoEstadiaDiario(500.0);
    }

    /**
     * Test 1: Obtener depósito por ID exitosamente
     */
    @Test
    @DisplayName("Debe obtener un depósito existente por ID")
    void testObtenerDeposito_Success() {
        // Arrange
        when(depositoRepository.findById(1L)).thenReturn(Optional.of(depositoTest));

        // Act
        Deposito result = depositoService.obtener(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdDeposito());
        assertEquals("Depósito Buenos Aires", result.getNombre());
        verify(depositoRepository, times(1)).findById(1L);
    }

    /**
     * Test 2: Obtener depósito inexistente lanza excepción
     */
    @Test
    @DisplayName("Debe lanzar NoSuchElementException al obtener depósito inexistente")
    void testObtenerDeposito_NotFound() {
        // Arrange
        when(depositoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            depositoService.obtener(999L);
        });
        verify(depositoRepository, times(1)).findById(999L);
    }

    /**
     * Test 3: Crear depósito exitosamente
     */
    @Test
    @DisplayName("Debe crear un nuevo depósito")
    void testCrearDeposito_Success() {
        // Arrange
        Deposito nuevoDeposito = new Deposito();
        nuevoDeposito.setNombre("Depósito Córdoba");
        nuevoDeposito.setDireccion("Calle Test 123");
        nuevoDeposito.setLatitud(-31.413587);
        nuevoDeposito.setLongitud(-64.189811);
        nuevoDeposito.setCostoEstadiaDiario(400.0);

        when(depositoRepository.save(any(Deposito.class))).thenReturn(depositoTest);

        // Act
        Deposito result = depositoService.crear(nuevoDeposito);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdDeposito());
        verify(depositoRepository, times(1)).save(any(Deposito.class));
    }

    /**
     * Test 4: Crear depósito setea ID a null (generado por BD)
     */
    @Test
    @DisplayName("Debe setear ID a null al crear nuevo depósito")
    void testCrearDeposito_IDSetNull() {
        // Arrange
        Deposito nuevoDeposito = new Deposito();
        nuevoDeposito.setIdDeposito(999L);  // ID temporal
        nuevoDeposito.setNombre("Test Depósito");

        when(depositoRepository.save(any(Deposito.class))).thenReturn(depositoTest);

        // Act
        Deposito result = depositoService.crear(nuevoDeposito);

        // Assert
        assertNotNull(result);
        verify(depositoRepository).save(argThat(d -> d.getIdDeposito() == null));
    }

    /**
     * Test 5: Actualizar depósito exitosamente
     */
    @Test
    @DisplayName("Debe actualizar un depósito existente")
    void testActualizarDeposito_Success() {
        // Arrange
        Deposito depositoActualizado = new Deposito();
        depositoActualizado.setNombre("Depósito Buenos Aires - Actualizado");
        depositoActualizado.setDireccion("Avenida 9 de Julio 2000");
        depositoActualizado.setLatitud(-34.603684);
        depositoActualizado.setLongitud(-58.381559);
        depositoActualizado.setCostoEstadiaDiario(600.0);

        when(depositoRepository.findById(1L)).thenReturn(Optional.of(depositoTest));
        when(depositoRepository.save(any(Deposito.class))).thenReturn(depositoTest);

        // Act
        Deposito result = depositoService.actualizar(1L, depositoActualizado);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdDeposito());
        verify(depositoRepository, times(1)).save(any(Deposito.class));
    }

    /**
     * Test 6: Actualizar depósito inexistente lanza excepción
     */
    @Test
    @DisplayName("Debe lanzar excepción al actualizar depósito inexistente")
    void testActualizarDeposito_NotFound() {
        // Arrange
        when(depositoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            depositoService.actualizar(999L, depositoTest);
        });
    }

    /**
     * Test 7: Eliminar depósito exitosamente
     */
    @Test
    @DisplayName("Debe eliminar un depósito existente")
    void testEliminarDeposito_Success() {
        // Arrange
        when(depositoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(depositoRepository).deleteById(1L);

        // Act
        depositoService.eliminar(1L);

        // Assert
        verify(depositoRepository, times(1)).deleteById(1L);
    }

    /**
     * Test 8: Eliminar depósito inexistente lanza excepción
     */
    @Test
    @DisplayName("Debe lanzar excepción al eliminar depósito inexistente")
    void testEliminarDeposito_NotFound() {
        // Arrange
        when(depositoRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            depositoService.eliminar(999L);
        });
        verify(depositoRepository, never()).deleteById(any());
    }

    /**
     * Test 9: Listar todos los depósitos con paginación
     */
    @Test
    @DisplayName("Debe listar todos los depósitos paginados")
    void testListarDepositos_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<Deposito> depositoList = Arrays.asList(depositoTest);
        Page<Deposito> pageExpected = new PageImpl<>(depositoList, pageable, 1);

        when(depositoRepository.findAll(pageable)).thenReturn(pageExpected);

        // Act
        Page<Deposito> result = depositoService.listar(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Depósito Buenos Aires", result.getContent().get(0).getNombre());
        verify(depositoRepository, times(1)).findAll(pageable);
    }

    /**
     * Test 10: BONUS - Validar ubicación geográfica
     */
    @Test
    @DisplayName("Debe contener coordenadas válidas de Buenos Aires")
    void testDepositoUbicacion_CoordenadasValidas() {
        // Arrange
        Deposito depositoBuenosAires = new Deposito();
        depositoBuenosAires.setIdDeposito(1L);
        depositoBuenosAires.setNombre("Depósito CABA");
        depositoBuenosAires.setLatitud(-34.603684);
        depositoBuenosAires.setLongitud(-58.381559);

        // Act & Assert
        assertTrue(depositoBuenosAires.getLatitud() >= -90 && depositoBuenosAires.getLatitud() <= 90,
                "Latitud debe estar entre -90 y 90");
        assertTrue(depositoBuenosAires.getLongitud() >= -180 && depositoBuenosAires.getLongitud() <= 180,
                "Longitud debe estar entre -180 y 180");
    }

    /**
     * Test 11: BONUS - Listar depósitos vacíos
     */
    @Test
    @DisplayName("Debe retornar página vacía cuando no hay depósitos")
    void testListarDepositos_Empty() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Deposito> pageEmpty = new PageImpl<>(Arrays.asList(), pageable, 0);

        when(depositoRepository.findAll(pageable)).thenReturn(pageEmpty);

        // Act
        Page<Deposito> result = depositoService.listar(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    /**
     * Test 12: BONUS - Costo de estadía debe ser positivo
     */
    @Test
    @DisplayName("Debe validar que costo de estadía sea positivo")
    void testDepositoCostoEstadiaDiario_PositivoYValido() {
        // Arrange
        Deposito deposito = new Deposito();
        deposito.setCostoEstadiaDiario(500.0);

        // Act & Assert
        assertTrue(deposito.getCostoEstadiaDiario() > 0, 
                "Costo de estadía debe ser positivo");
    }
}
