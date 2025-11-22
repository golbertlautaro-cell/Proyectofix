package com.tpi.logistica.service;

import com.tpi.logistica.domain.Deposito;
import com.tpi.logistica.repository.DepositoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
public class DepositoService {

    private final DepositoRepository depositoRepository;

    public DepositoService(DepositoRepository depositoRepository) {
        this.depositoRepository = depositoRepository;
    }

    @Transactional(readOnly = true)
    public Page<Deposito> listar(Pageable pageable) {
        return depositoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Deposito obtener(Long id) {
        return depositoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Depósito no encontrado: " + id));
    }

    @Transactional
    public Deposito crear(Deposito deposito) {
        deposito.setIdDeposito(null); // Generado por BD
        return depositoRepository.save(deposito);
    }

    @Transactional
    public Deposito actualizar(Long id, Deposito deposito) {
        Deposito actual = obtener(id);
        actual.setNombre(deposito.getNombre());
        actual.setLatitud(deposito.getLatitud());
        actual.setLongitud(deposito.getLongitud());
        actual.setCostoEstadiaDiario(deposito.getCostoEstadiaDiario());
        actual.setDireccion(deposito.getDireccion());
        return depositoRepository.save(actual);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!depositoRepository.existsById(id)) {
            throw new NoSuchElementException("Depósito no encontrado: " + id);
        }
        depositoRepository.deleteById(id);
    }
}
