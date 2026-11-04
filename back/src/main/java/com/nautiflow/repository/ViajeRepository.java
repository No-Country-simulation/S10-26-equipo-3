package com.nautiflow.repository;

import com.nautiflow.model.Viaje;
import com.nautiflow.model.EstadoOperativoLanchero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de viajes, con consultas para disponibilidad, seguimiento, tokens de reseña y viajes en curso.
 */
public interface ViajeRepository extends JpaRepository<Viaje, Long> {
    /**
     * Lista los viajes de una lancha.
     */
    List<Viaje> findByLancha_IdLancha(Long idLancha);

    /**
     * Busca un viaje por su token de reseña con lancha, lanchero y zonas cargados.
     */
    @EntityGraph(attributePaths = {"lancha", "lancha.lanchero", "zonaOrigen", "zonaDestino"})
    Optional<Viaje> findByTokenResena(String tokenResena);

    /**
     * Obtiene el ID del lanchero que realizó un viaje.
     */
    @Query("select v.lancha.lanchero.idLanchero from Viaje v where v.idViaje = :id")
    Optional<Long> findLancheroIdByIdViaje(@Param("id") Long idViaje);

    /**
     * Indica si el lanchero tiene un viaje sin finalizar.
     */
    boolean existsByLancha_Lanchero_IdLancheroAndFechaHoraFinIsNull(Long idLanchero);

    /**
     * Indica si el lanchero tiene otro viaje sin finalizar distinto del indicado.
     */
    boolean existsByLancha_Lanchero_IdLancheroAndIdViajeNotAndFechaHoraFinIsNull(
            Long idLanchero,
            Long idViaje
    );

    /**
     * Lista viajes sin finalizar de lancheros en el estado operativo indicado.
     */
    @EntityGraph(attributePaths = {"lancha", "lancha.lanchero", "zonaOrigen", "zonaDestino"})
    Page<Viaje> findByLancha_Lanchero_EstadoViajeAndFechaHoraFinIsNull(
            EstadoOperativoLanchero estadoViaje,
            Pageable pageable
    );

    /**
     * Lista todos los viajes con lancha, lanchero y zonas cargados para seguimiento.
     */
    @EntityGraph(attributePaths = {"lancha", "lancha.lanchero", "zonaOrigen", "zonaDestino"})
    @Query("select v from Viaje v")
    Page<Viaje> findAllForSeguimiento(Pageable pageable);

    /**
     * Obtiene el último viaje finalizado de una lancha para mostrar su última ubicación conocida.
     */
    @EntityGraph(attributePaths = "zonaDestino")
    Optional<Viaje> findFirstByLancha_IdLanchaAndFechaHoraFinIsNotNullOrderByFechaHoraFinDescIdViajeDesc(
            Long idLancha
    );
}
