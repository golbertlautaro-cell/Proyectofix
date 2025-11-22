package com.tpi.solicitudes.web;

import com.tpi.solicitudes.domain.Cliente;
import com.tpi.solicitudes.service.ClienteService;
import com.tpi.solicitudes.web.dto.ContenedorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Clientes", description = "Gestión de clientes y sus contenedores")
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @Operation(
        summary = "Listar todos los clientes",
        description = "Retorna una página de clientes registrados en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping
    public Page<Cliente> listar(
        @Parameter(description = "Información de paginación") Pageable pageable
    ) {
        log.info("Listando clientes - página: {}, tamaño: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Cliente> result = service.findAll(pageable);
        log.debug("Se encontraron {} clientes", result.getTotalElements());
        return result;
    }

    @Operation(
        summary = "Obtener cliente por ID",
        description = "Retorna un cliente específico según su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @GetMapping("/{id}")
    public Cliente obtener(
        @Parameter(description = "ID del cliente", required = true) @PathVariable Long id
    ) {
        return service.findById(id);
    }

    @Operation(
        summary = "Crear nuevo cliente",
        description = "Registra un nuevo cliente en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Cliente creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente crear(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del cliente a crear",
            required = true,
            content = @Content(schema = @Schema(implementation = Cliente.class))
        )
        @RequestBody @Valid Cliente c
    ) {
        return service.create(c);
    }

    @Operation(
        summary = "Actualizar cliente",
        description = "Actualiza los datos de un cliente existente"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado correctamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @PutMapping("/{id}")
    public Cliente actualizar(
        @Parameter(description = "ID del cliente", required = true) @PathVariable Long id,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos actualizados del cliente",
            required = true
        )
        @RequestBody @Valid Cliente c
    ) {
        return service.update(id, c);
    }

    @Operation(
        summary = "Eliminar cliente",
        description = "Elimina un cliente del sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente eliminado correctamente"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
        @Parameter(description = "ID del cliente", required = true) @PathVariable Long id
    ) {
        service.delete(id);
    }
}
