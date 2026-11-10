package com.nautiflow.repository;

import com.nautiflow.model.TrayectoRetornoVacio;
import com.nautiflow.model.EstadoTramoVacio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de trayectos de retorno vacío.
 */
public interface TrayectoRetornoVacioRepository extends JpaRepository<TrayectoRetornoVacio, Long> {
    @EntityGraph(attributePaths = {"viaje", "viaje.lancha", "viaje.lancha.lanchero",
            "viaje.zonaOrigen", "viaje.zonaDestino", "zonaOrigen", "zonaDestino"})
    List<TrayectoRetornoVacio> findByEstadoOrderByFechaHoraRetornoDescIdTramoDesc(EstadoTramoVacio estado);

    @EntityGraph(attributePaths = {"viaje", "viaje.lancha", "viaje.lancha.lanchero",
            "viaje.zonaOrigen", "viaje.zonaDestino", "zonaOrigen", "zonaDestino"})
    List<TrayectoRetornoVacio> findByEstadoAndFechaHoraRetornoGreaterThanEqualOrderByFechaHoraRetornoDescIdTramoDesc(
            EstadoTramoVacio estado,
            OffsetDateTime fechaDesde
    );

    @EntityGraph(attributePaths = {"viaje", "viaje.lancha", "viaje.lancha.lanchero",
            "viaje.zonaOrigen", "viaje.zonaDestino", "zonaOrigen", "zonaDestino"})
    List<TrayectoRetornoVacio> findByEstadoAndFechaHoraRetornoLessThanOrderByFechaHoraRetornoDescIdTramoDesc(
            EstadoTramoVacio estado,
            OffsetDateTime fechaHastaExclusiva
    );

    @EntityGraph(attributePaths = {"viaje", "viaje.lancha", "viaje.lancha.lanchero",
            "viaje.zonaOrigen", "viaje.zonaDestino", "zonaOrigen", "zonaDestino"})
    List<TrayectoRetornoVacio> findByEstadoAndFechaHoraRetornoGreaterThanEqualAndFechaHoraRetornoLessThanOrderByFechaHoraRetornoDescIdTramoDesc(
            EstadoTramoVacio estado,
            OffsetDateTime fechaDesde,
            OffsetDateTime fechaHastaExclusiva
    );

    default List<TrayectoRetornoVacio> buscar(
            OffsetDateTime fechaDesde,
            OffsetDateTime fechaHastaExclusiva,
            EstadoTramoVacio estado
    ) {
        if (fechaDesde != null && fechaHastaExclusiva != null) {
            return findByEstadoAndFechaHoraRetornoGreaterThanEqualAndFechaHoraRetornoLessThanOrderByFechaHoraRetornoDescIdTramoDesc(
                    estado,
                    fechaDesde,
                    fechaHastaExclusiva
            );
        }
        if (fechaDesde != null) {
            return findByEstadoAndFechaHoraRetornoGreaterThanEqualOrderByFechaHoraRetornoDescIdTramoDesc(
                    estado,
                    fechaDesde
            );
        }
        if (fechaHastaExclusiva != null) {
            return findByEstadoAndFechaHoraRetornoLessThanOrderByFechaHoraRetornoDescIdTramoDesc(
                    estado,
                    fechaHastaExclusiva
            );
        }
        return findByEstadoOrderByFechaHoraRetornoDescIdTramoDesc(estado);
    }

    @EntityGraph(attributePaths = {"viaje", "viaje.lancha", "viaje.lancha.lanchero",
            "viaje.zonaOrigen", "viaje.zonaDestino", "zonaOrigen", "zonaDestino"})
    Optional<TrayectoRetornoVacio> findWithDetailsByIdTramo(Long idTramo);

    @Query("select tramo.viaje.lancha.lanchero.idLanchero from TrayectoRetornoVacio tramo "
            + "where tramo.idTramo = :idTramo")
    Optional<Long> findLancheroIdByIdTramo(@Param("idTramo") Long idTramo);

    boolean existsByViaje_IdViajeAndEstado(Long idViaje, EstadoTramoVacio estado);

    boolean existsByViaje_Lancha_Lanchero_IdLancheroAndEstado(
            Long idLanchero,
            EstadoTramoVacio estado
    );
}
