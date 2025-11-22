package com.tpi.logistica.repository;

import com.tpi.logistica.domain.Camion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CamionRepository extends JpaRepository<Camion, String> {
    Page<Camion> findByCapacidadPesoBetween(Double min, Double max, Pageable pageable);
    Page<Camion> findByCapacidadVolumenBetween(Double min, Double max, Pageable pageable);
    Page<Camion> findByCapacidadPesoBetweenAndCapacidadVolumenBetween(Double minPeso, Double maxPeso, Double minVol, Double maxVol, Pageable pageable);
}
