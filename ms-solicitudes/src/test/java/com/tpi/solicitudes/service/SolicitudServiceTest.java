package com.tpi.solicitudes.service;

import com.tpi.solicitudes.domain.Solicitud;
import com.tpi.solicitudes.domain.EstadoSolicitud;
import com.tpi.solicitudes.repository.SolicitudRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolicitudService Tests")
class SolicitudServiceTest {

    @Mock
    private SolicitudRepository solicitudRepository;

    @InjectMocks
    private SolicitudService solicitudService;

    private Solicitud solicitud;

    @BeforeEach
    void setUp() {
        solicitud = Solicitud.builder()
                .nroSolicitud(1L)
                .idCliente(1L)
                .idContenedor(100L)
                .estado(EstadoSolicitud.BORRADOR)
                .costoEstimado(5000.0)
                .costoFinal(5200.0)
                .tiempoReal(48.0)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Debe obtener solicitud por ID exitosamente")
    void testFindById() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        Solicitud result = solicitudService.findById(1L);

        assertNotNull(result);
        assertEquals(solicitud.getNroSolicitud(), result.getNroSolicitud());
        assertEquals(solicitud.getIdCliente(), result.getIdCliente());
        verify(solicitudRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando solicitud no existe")
    void testFindByIdNotFound() {
        when(solicitudRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            solicitudService.findById(999L);
        });
        verify(solicitudRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe listar todas las solicitudes paginadas")
    void testFindAll() {
        var pageable = PageRequest.of(0, 10);
        var solicitudList = Arrays.asList(solicitud);
        var page = new PageImpl<>(solicitudList, pageable, 1);

        when(solicitudRepository.findAll(pageable)).thenReturn(page);

        Page<Solicitud> result = solicitudService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(solicitud.getNroSolicitud(), result.getContent().get(0).getNroSolicitud());
        verify(solicitudRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Debe crear solicitud exitosamente")
    void testCreateSolicitud() {
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitud);

        Solicitud result = solicitudService.create(solicitud);

        assertNotNull(result);
        assertEquals(solicitud.getNroSolicitud(), result.getNroSolicitud());
        assertEquals(EstadoSolicitud.BORRADOR, result.getEstado());
        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("Debe actualizar estado de solicitud")
    void testUpdateSolicitudStatus() {
        Solicitud solicitudActualizada = solicitud;
        solicitudActualizada.setEstado(EstadoSolicitud.PROGRAMADA);
        
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(solicitudRepository.save(any(Solicitud.class))).thenReturn(solicitudActualizada);

        Solicitud result = solicitudService.update(1L, solicitudActualizada);

        assertNotNull(result);
        assertEquals(EstadoSolicitud.PROGRAMADA, result.getEstado());
        verify(solicitudRepository, times(1)).save(any(Solicitud.class));
    }

    @Test
    @DisplayName("Debe eliminar solicitud exitosamente")
    void testDeleteSolicitud() {
        when(solicitudRepository.existsById(1L)).thenReturn(true);
        
        solicitudService.delete(1L);

        verify(solicitudRepository, times(1)).deleteById(1L);
    }
}
