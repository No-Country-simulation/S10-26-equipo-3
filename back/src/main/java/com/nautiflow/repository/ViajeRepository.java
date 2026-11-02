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

public interface ViajeRepository extends JpaRepository<Viaje, Long> {
    List<Viaje> findByLancha_IdLancha(Long idLancha);

    @EntityGraph(attributePaths = {"lancha", "lancha.lanchero", "zonaOrigen", "zonaDestino"})
    Optional<Viaje> findByTokenResena(String tokenResena);

    @Query("select v.lancha.lanchero.idLanchero from Viaje v where v.idViaje = :id")
    Optional<Long> findLancheroIdByIdViaje(@Param("id") Long idViaje);

    boolean existsByLancha_Lanchero_IdLancheroAndFechaHoraFinIsNull(Long idLanchero);

    boolean existsByLancha_Lanchero_IdLancheroAndIdViajeNotAndFechaHoraFinIsNull(
            Long idLanchero,
            Long idViaje
    );

    @EntityGraph(attributePaths = {"lancha", "lancha.lanchero", "zonaOrigen", "zonaDestino"})
    Page<Viaje> findByLancha_Lanchero_EstadoViajeAndFechaHoraFinIsNull(
            EstadoOperativoLanchero estadoViaje,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"lancha", "lancha.lanchero", "zonaOrigen", "zonaDestino"})
    @Query("select v from Viaje v")
    Page<Viaje> findAllForSeguimiento(Pageable pageable);

    @EntityGraph(attributePaths = "zonaDestino")
    Optional<Viaje> findFirstByLancha_IdLanchaAndFechaHoraFinIsNotNullOrderByFechaHoraFinDescIdViajeDesc(
            Long idLancha
    );
}
