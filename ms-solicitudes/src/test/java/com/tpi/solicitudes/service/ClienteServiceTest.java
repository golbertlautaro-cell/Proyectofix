package com.tpi.solicitudes.service;

import com.tpi.solicitudes.domain.Cliente;
import com.tpi.solicitudes.repository.ClienteRepository;
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

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService Tests")
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = Cliente.builder()
                .idCliente(1L)
                .nombre("Juan Pérez")
                .apellido("García")
                .email("juan@example.com")
                .telefono("1234567890")
                .direccion("Calle Principal 123")
                .build();
    }

    @Test
    @DisplayName("Debe obtener cliente por ID exitosamente")
    void testFindById() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Cliente result = clienteService.findById(1L);

        assertNotNull(result);
        assertEquals(cliente.getIdCliente(), result.getIdCliente());
        assertEquals(cliente.getNombre(), result.getNombre());
        verify(clienteRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando cliente no existe")
    void testFindByIdNotFound() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> {
            clienteService.findById(999L);
        });
        verify(clienteRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Debe listar todos los clientes paginados")
    void testFindAll() {
        var pageable = PageRequest.of(0, 10);
        var clienteList = Arrays.asList(cliente);
        var page = new PageImpl<>(clienteList, pageable, 1);

        when(clienteRepository.findAll(pageable)).thenReturn(page);

        Page<Cliente> result = clienteService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(cliente.getNombre(), result.getContent().get(0).getNombre());
        verify(clienteRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("Debe crear cliente exitosamente")
    void testCreateCliente() {
        when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

        Cliente result = clienteService.create(cliente);

        assertNotNull(result);
        assertEquals(cliente.getIdCliente(), result.getIdCliente());
        assertEquals(cliente.getNombre(), result.getNombre());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debe actualizar cliente exitosamente")
    void testUpdateCliente() {
        Cliente clienteActualizado = cliente;
        clienteActualizado.setNombre("Juan Actualizado");
        
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        Cliente result = clienteService.update(1L, clienteActualizado);

        assertNotNull(result);
        assertEquals("Juan Actualizado", result.getNombre());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debe eliminar cliente exitosamente")
    void testDeleteCliente() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        
        clienteService.delete(1L);

        verify(clienteRepository, times(1)).deleteById(1L);
    }
}
