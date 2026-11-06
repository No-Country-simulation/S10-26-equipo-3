package com.nautiflow.repository;

import com.nautiflow.model.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de zonas navegables.
 */
public interface ZonaRepository extends JpaRepository<Zona, Long> {
    /**
     * Busca una zona por nombre sin distinguir mayúsculas y minúsculas.
     */
    Optional<Zona> findByNombreIgnoreCase(String nombre);
}
