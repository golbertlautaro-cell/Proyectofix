package com.tpi.solicitudes.service;

import com.tpi.solicitudes.config.PricingProperties;
import com.tpi.solicitudes.domain.EstadoTramo;
import com.tpi.solicitudes.domain.Ruta;
import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.domain.Tramo;
import com.tpi.solicitudes.repository.RutaRepository;
import com.tpi.solicitudes.repository.SolicitudRepository;
import com.tpi.solicitudes.repository.TramoRepository;
import com.tpi.solicitudes.client.LogisticaClient;
import com.tpi.solicitudes.client.GoogleMapsClient;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests para TramoService
 * 
 * Patrón: AAA (Arrange-Act-Assert)
 * Coverage: CRUD operations, validaciones, excepciones
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TramoService Tests")
@SuppressWarnings("null")
class TramoServiceTest {

    @Mock
    private TramoRepository tramoRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @Mock
    private RutaRepository rutaRepository;

    @Mock
    private LogisticaClient logisticaClient;

    @Mock
    private GoogleMapsClient googleMapsClient;

    @Mock
    private GoogleMapsService googleMapsService;

    @Mock(lenient = true)
    private PricingProperties pricingProperties;

    @InjectMocks
    private TramoService tramoService;

    private Ruta rutaTest;
    private Tramo tramoTest;

    @BeforeEach
    void setUp() {
        // Arrange: Preparar datos de prueba
        rutaTest = new Ruta();
        rutaTest.setIdRuta(1L);

        tramoTest = new Tramo();
        tramoTest.setIdTramo(1L);
        tramoTest.setOrigen("Buenos Aires");
        tramoTest.setDestino("Córdoba");
        tramoTest.setDominioCamion("ABC-123");
        tramoTest.setEstado(EstadoTramo.PENDIENTE);
        tramoTest.setRuta(rutaTest);

        when(pricingProperties.getTarifaBasePromedio()).thenReturn(50.0);
        when(pricingProperties.getConsumoPromedioGeneral()).thenReturn(25.0);
        when(pricingProperties.getPrecioLitroCombustible()).thenReturn(1.5);
        when(pricingProperties.getCostoDiarioDeposito()).thenReturn(120.0);
        when(pricingProperties.getDiasDepositoEstimadoDefault()).thenReturn(0.5);
    }

    /**
     * Test 1: Obtener tramo por ID exitosamente
     */
    @Test
    @DisplayName("Debe obtener un tramo existente por ID")
    void testObtenerTramo_Success() {
        // Arrange
        when(tramoRepository.findById(1L)).thenReturn(Optional.of(tramoTest));

        // Act
        Tramo result = tramoService.obtener(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdTramo());
        assertEquals("Buenos Aires", result.getOrigen());
        assertEquals("Córdoba", result.getDestino());
        verify(tramoRepository, times(1)).findById(1L);
    }

    /**
     * Test 2: Obtener tramo inexistente lanza excepción
     */
    @Test
    @DisplayName("Debe lanzar NoSuchElementException al obtener tramo inexistente")
    void testObtenerTramo_NotFound() {
        // Arrange
        when(tramoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            tramoService.obtener(999L);
        });
        verify(tramoRepository, times(1)).findById(999L);
    }

    /**
     * Test 3: Crear tramo exitosamente
     */
    @Test
    @DisplayName("Debe crear un nuevo tramo")
    void testCrearTramo_Success() {
        // Arrange
        Tramo nuevoTramo = new Tramo();
        nuevoTramo.setOrigen("Buenos Aires");
        nuevoTramo.setDestino("Mendoza");

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(new Solicitud()));
        when(tramoRepository.save(any(Tramo.class))).thenReturn(tramoTest);

        // Act
        Tramo result = tramoService.crear(1L, nuevoTramo);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getIdTramo());
        assertEquals("Buenos Aires", result.getOrigen());
        verify(tramoRepository, times(1)).save(any(Tramo.class));
    }

    /**
     * Test 4: Crear tramo con solicitud inexistente lanza excepción
     */
    @Test
    @DisplayName("Debe lanzar excepción al crear tramo con solicitud inexistente")
    void testCrearTramo_SolicitudNotFound() {
        // Arrange
        Tramo nuevoTramo = new Tramo();
        nuevoTramo.setOrigen("Buenos Aires");

        when(solicitudRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            tramoService.crear(999L, nuevoTramo);
        });
        verify(tramoRepository, never()).save(any(Tramo.class));
    }

    /**
     * Test 5: Actualizar tramo exitosamente
     */
    @Test
    @DisplayName("Debe actualizar un tramo existente")
    void testActualizarTramo_Success() {
        // Arrange
        Tramo tramoActualizado = new Tramo();
        tramoActualizado.setOrigen("La Plata");
        tramoActualizado.setDestino("Rosario");
        tramoActualizado.setDominioCamion("XYZ-789");
        tramoActualizado.setEstado(EstadoTramo.INICIADO);

        when(tramoRepository.findById(1L)).thenReturn(Optional.of(tramoTest));
        when(tramoRepository.save(any(Tramo.class))).thenReturn(tramoTest);

        // Act
        Tramo result = tramoService.actualizar(1L, tramoActualizado);

        // Assert
        assertNotNull(result);
        assertEquals("La Plata", result.getOrigen());
        assertEquals("Rosario", result.getDestino());
        verify(tramoRepository, times(1)).save(any(Tramo.class));
    }

    /**
     * Test 6: Actualizar tramo inexistente lanza excepción
     */
    @Test
    @DisplayName("Debe lanzar excepción al actualizar tramo inexistente")
    void testActualizarTramo_NotFound() {
        // Arrange
        when(tramoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            tramoService.actualizar(999L, tramoTest);
        });
    }

    /**
     * Test 7: Eliminar tramo exitosamente
     */
    @Test
    @DisplayName("Debe eliminar un tramo existente")
    void testEliminarTramo_Success() {
        // Arrange
        when(tramoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(tramoRepository).deleteById(1L);

        // Act
        tramoService.eliminar(1L);

        // Assert
        verify(tramoRepository, times(1)).deleteById(1L);
    }

    /**
     * Test 8: Eliminar tramo inexistente lanza excepción
     */
    @Test
    @DisplayName("Debe lanzar excepción al eliminar tramo inexistente")
    void testEliminarTramo_NotFound() {
        // Arrange
        when(tramoRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> {
            tramoService.eliminar(999L);
        });
        verify(tramoRepository, never()).deleteById(999L);
    }

    /**
     * Test 9: Listar todos los tramos con paginación
     */
    @Test
    @DisplayName("Debe listar todos los tramos paginados")
    void testFindAll_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<Tramo> tramoList = Arrays.asList(tramoTest);
        Page<Tramo> pageExpected = new PageImpl<>(tramoList, pageable, 1);

        when(tramoRepository.findAll(pageable)).thenReturn(pageExpected);

        // Act
        Page<Tramo> result = tramoService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals("Buenos Aires", result.getContent().get(0).getOrigen());
        verify(tramoRepository, times(1)).findAll(pageable);
    }

    /**
     * Test 10: Listar tramos por solicitud
     */
    @Test
    @DisplayName("Debe listar tramos de una solicitud específica")
    void testListarPorSolicitud_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<Tramo> tramoList = Arrays.asList(tramoTest);
        Page<Tramo> pageExpected = new PageImpl<>(tramoList, pageable, 1);

        when(tramoRepository.findPageBySolicitudNroSolicitud(1L, pageable)).thenReturn(pageExpected);

        // Act
        Page<Tramo> result = tramoService.listarPorSolicitud(1L, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(tramoRepository, times(1)).findPageBySolicitudNroSolicitud(1L, pageable);
    }

    /**
     * Test 11: BONUS - Filtrar tramos por estado
     */
    @Test
    @DisplayName("Debe filtrar tramos por estado")
    void testListarConFiltroEstado_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<Tramo> tramoList = Arrays.asList(tramoTest);
        Page<Tramo> pageExpected = new PageImpl<>(tramoList, pageable, 1);

        when(tramoRepository.findByEstadoAndFechaHoraInicioRealBetween(
            eq(EstadoTramo.PENDIENTE.toString()),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(pageable)
        )).thenReturn(pageExpected);

        // Act
        Page<Tramo> result = tramoService.listar(pageable, EstadoTramo.PENDIENTE.toString(), null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tramoRepository).findByEstadoAndFechaHoraInicioRealBetween(
            eq(EstadoTramo.PENDIENTE.toString()),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(pageable)
        );
    }

    /**
     * Test 12: BONUS - Filtrar por dominio de camión
     */
    @Test
    @DisplayName("Debe filtrar tramos por dominio de camión")
    void testListarConFiltroDominioCamion_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        List<Tramo> tramoList = Arrays.asList(tramoTest);
        Page<Tramo> pageExpected = new PageImpl<>(tramoList, pageable, 1);

        when(tramoRepository.findByDominioCamionAndFechaHoraInicioRealBetween(
            eq("ABC-123"),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(pageable)
        )).thenReturn(pageExpected);

        // Act
        Page<Tramo> result = tramoService.listar(pageable, null, "ABC-123", null, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(tramoRepository).findByDominioCamionAndFechaHoraInicioRealBetween(
            eq("ABC-123"),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(pageable)
        );
    }
}