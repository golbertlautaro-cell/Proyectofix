package com.tpi.solicitudes.service;

import com.tpi.solicitudes.domain.Cliente;
import com.tpi.solicitudes.repository.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ClienteService {

    private final ClienteRepository repository;

    public ClienteService(ClienteRepository repository) {
        this.repository = repository;
    }

    public List<Cliente> findAll() { // legacy
        return repository.findAll();
    }

    public Page<Cliente> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Cliente findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Cliente no encontrado: " + id));
    }

    public Cliente create(Cliente c) {
        c.setIdCliente(null);
        return repository.save(c);
    }

    public Cliente update(Long id, Cliente c) {
        Cliente actual = findById(id);
        actual.setNombre(c.getNombre());
        actual.setEmail(c.getEmail());
        actual.setTelefono(c.getTelefono());
        return repository.save(actual);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NoSuchElementException("Cliente no encontrado: " + id);
        }
        repository.deleteById(id);
    }
}
