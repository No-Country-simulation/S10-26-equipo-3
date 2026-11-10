package com.nautiflow.repository;

import com.nautiflow.model.Viaje;
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
     * Busca un viaje con sus asociaciones administrativas cargadas.
     */
    @EntityGraph(attributePaths = {"lancha", "lancha.lanchero", "zonaOrigen", "zonaDestino"})
    Optional<Viaje> findWithDetailsByIdViaje(Long idViaje);

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
     * Lista los viajes sin finalizar, incluso si el lanchero ya publicó el retorno.
     */
    @EntityGraph(attributePaths = {"lancha", "lancha.lanchero", "zonaOrigen", "zonaDestino"})
    Page<Viaje> findByFechaHoraFinIsNull(Pageable pageable);

    /**
     * Cuenta los viajes que aún no tienen fecha de finalización.
     */
    long countByFechaHoraFinIsNull();

    /**
     * Lista todos los viajes con lancha, lanchero y zonas cargados para seguimiento.
     */
    @EntityGraph(attributePaths = {"lancha", "lancha.lanchero", "zonaOrigen", "zonaDestino"})
    @Query("select v from Viaje v where (:estado = 'TODOS' or (:estado = 'EN_VIAJE' and v.fechaHoraFin is null) or (:estado = 'FINALIZADO' and v.fechaHoraFin is not null))")
    Page<Viaje> findForSeguimiento(@Param("estado") String estado, Pageable pageable);

    /**
     * Obtiene el último viaje finalizado de una lancha para mostrar su última ubicación conocida.
     */
    @EntityGraph(attributePaths = "zonaDestino")
    Optional<Viaje> findFirstByLancha_IdLanchaAndFechaHoraFinIsNotNullOrderByFechaHoraFinDescIdViajeDesc(
            Long idLancha
    );
}
