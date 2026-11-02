package com.nautiflow.repository;

import com.nautiflow.model.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZonaRepository extends JpaRepository<Zona, Long> {
    Optional<Zona> findByNombreIgnoreCase(String nombre);
}
