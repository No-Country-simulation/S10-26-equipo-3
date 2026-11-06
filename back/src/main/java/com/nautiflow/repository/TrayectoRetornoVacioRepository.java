package com.nautiflow.repository;

import com.nautiflow.model.TrayectoRetornoVacio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de trayectos de retorno vacío.
 */
public interface TrayectoRetornoVacioRepository extends JpaRepository<TrayectoRetornoVacio, Long> {
    /**
     * Lista los trayectos de retorno vacío de un viaje.
     */
    List<TrayectoRetornoVacio> findByViaje_IdViaje(Long idViaje);
}
