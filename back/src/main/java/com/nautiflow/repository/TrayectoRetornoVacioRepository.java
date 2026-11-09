package com.nautiflow.repository;

import com.nautiflow.model.TrayectoRetornoVacio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repositorio de trayectos de retorno vacío.
 */
public interface TrayectoRetornoVacioRepository extends JpaRepository<TrayectoRetornoVacio, Long> {
    /**
     * Lista los trayectos de retorno vacío de un viaje.
     */
    List<TrayectoRetornoVacio> findByViaje_IdViaje(Long idViaje);

    boolean existsByViaje_IdViajeAndEstado(Long idViaje, String estado);

    @EntityGraph(attributePaths = {"viaje", "viaje.lancha", "zonaOrigen", "zonaDestino"})
    List<TrayectoRetornoVacio> findByEstadoAndFechaHoraRetornoGreaterThanEqualOrderByFechaHoraRetornoAscIdTramoAsc(
            String estado,
            OffsetDateTime desde
    );

    long countByEstadoAndFechaHoraRetornoGreaterThanEqual(String estado, OffsetDateTime desde);
}
