package com.nautiflow.repository;

import com.nautiflow.model.TrayectoRetornoVacio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrayectoRetornoVacioRepository extends JpaRepository<TrayectoRetornoVacio, Long> {
    List<TrayectoRetornoVacio> findByViaje_IdViaje(Long idViaje);
}
