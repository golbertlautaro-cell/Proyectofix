package com.tpi.logistica.service;

import com.tpi.logistica.domain.Camion;
import com.tpi.logistica.repository.CamionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class CamionService {

    private final CamionRepository camionRepository;

    public CamionService(CamionRepository camionRepository) {
        this.camionRepository = camionRepository;
    }

    // CRUD
    @Transactional(readOnly = true)
    public Page<Camion> listar(Pageable pageable) {
        return camionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Camion> listar(Pageable pageable, Double minCapacidadPeso, Double maxCapacidadPeso,
                                Double minCapacidadVolumen, Double maxCapacidadVolumen) {
        boolean hasPeso = minCapacidadPeso != null || maxCapacidadPeso != null;
        boolean hasVol = minCapacidadVolumen != null || maxCapacidadVolumen != null;

        Double minP = (minCapacidadPeso != null) ? minCapacidadPeso : 0.0;
        Double maxP = (maxCapacidadPeso != null) ? maxCapacidadPeso : Double.MAX_VALUE;
        Double minV = (minCapacidadVolumen != null) ? minCapacidadVolumen : 0.0;
        Double maxV = (maxCapacidadVolumen != null) ? maxCapacidadVolumen : Double.MAX_VALUE;

        if (hasPeso && hasVol) {
            return camionRepository.findByCapacidadPesoBetweenAndCapacidadVolumenBetween(minP, maxP, minV, maxV, pageable);
        } else if (hasPeso) {
            return camionRepository.findByCapacidadPesoBetween(minP, maxP, pageable);
        } else if (hasVol) {
            return camionRepository.findByCapacidadVolumenBetween(minV, maxV, pageable);
        } else {
            return camionRepository.findAll(pageable);
        }
    }

    @Transactional(readOnly = true)
    public Camion obtener(String dominio) {
        return camionRepository.findById(dominio)
                .orElseThrow(() -> new NoSuchElementException("Camión no encontrado: " + dominio));
    }

    @Transactional
    public Camion crear(Camion c) {
        // dominio es PK y viene en el body; si existe, se sobreescribiría: preferimos fallo por constraint
        return camionRepository.save(c);
    }

    @Transactional
    public Camion actualizar(String dominio, Camion c) {
        Camion actual = obtener(dominio);
        actual.setCapacidadPeso(c.getCapacidadPeso());
        actual.setCapacidadVolumen(c.getCapacidadVolumen());
        actual.setConsumoPromedio(c.getConsumoPromedio());
        actual.setCostoBaseKm(c.getCostoBaseKm());
        actual.setDisponibilidad(c.getDisponibilidad());
        return camionRepository.save(actual);
    }

    @Transactional
    public void eliminar(String dominio) {
        if (!camionRepository.existsById(dominio)) {
            throw new NoSuchElementException("Camión no encontrado: " + dominio);
        }
        camionRepository.deleteById(dominio);
    }

    /**
     * RF11 - Validar capacidad del camión para transportar un contenedor.
     * Regla: retorna true si el camión existe y tanto el peso como el volumen del contenedor
     * no exceden la capacidad del camión (<=). En cualquier otro caso, retorna false.
     */
    @Transactional(readOnly = true)
    public boolean validarCapacidad(String dominio, Double pesoContenedor, Double volumenContenedor) {
        if (dominio == null || dominio.isBlank()) return false;
        if (pesoContenedor == null || volumenContenedor == null) return false;
        if (pesoContenedor < 0 || volumenContenedor < 0) return false;

        Camion camion = camionRepository.findById(dominio).orElse(null);
        if (camion == null) return false;

        Double capPeso = camion.getCapacidadPeso();
        Double capVol = camion.getCapacidadVolumen();
        if (capPeso == null || capVol == null) return false;

        return pesoContenedor <= capPeso && volumenContenedor <= capVol;
    }

    /**
     * Obtener resumen de estado de camiones (libres y ocupados).
     * Basado en el campo disponibilidad.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadoCamiones() {
        List<Camion> todos = camionRepository.findAll();
        
        long libres = todos.stream()
                .filter(c -> c.getDisponibilidad() != null && c.getDisponibilidad())
                .count();
        
        long ocupados = todos.stream()
                .filter(c -> c.getDisponibilidad() != null && !c.getDisponibilidad())
                .count();
        
        long sinEstado = todos.stream()
                .filter(c -> c.getDisponibilidad() == null)
                .count();
        
        Map<String, Object> estado = new HashMap<>();
        estado.put("total", todos.size());
        estado.put("libres", libres);
        estado.put("ocupados", ocupados);
        estado.put("sinEstado", sinEstado);
        
        return estado;
    }
}
