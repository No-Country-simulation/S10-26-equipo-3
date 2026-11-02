package com.nautiflow.repository;

import com.nautiflow.model.Lancha;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface LanchaRepository extends JpaRepository<Lancha, Long> {
    List<Lancha> findDistinctByZonas_IdZona(Long idZona);
    Optional<Lancha> findByLanchero_IdLanchero(Long idLanchero);
    long countByLanchero_EstadoSolicitud(String estadoSolicitud);

    @Query("select l.lanchero.idLanchero from Lancha l where l.idLancha = :id")
    Optional<Long> findLancheroIdByIdLancha(@Param("id") Long idLancha);

    @EntityGraph(attributePaths = {"lanchero", "zonas"})
    @Query("""
            select distinct l from Lancha l
            where l.lanchero.estadoSolicitud = 'APROBADO'
              and l.lanchero.estadoViaje = 'ACTIVO'
              and (:capacidad is null or l.capacidad >= :capacidad)
              and (:idZonaOrigen is null or exists (
                    select z from Lancha lOrigen join lOrigen.zonas z
                    where lOrigen = l and z.idZona = :idZonaOrigen
              ))
              and (:idZonaDestino is null or exists (
                    select z from Lancha lDestino join lDestino.zonas z
                    where lDestino = l and z.idZona = :idZonaDestino
              ))
              and not exists (
                    select v from Viaje v
                    where v.lancha = l
                      and v.fechaHoraInicio <= :fechaHoraConsulta
                      and (v.fechaHoraFin is null or v.fechaHoraFin > :fechaHoraConsulta)
              )
              and not exists (
                    select t from TrayectoRetornoVacio t
                    where t.viaje.lancha = l and t.estado = 'ACTIVO'
              )
            """)
    List<Lancha> buscarDisponibles(
            @Param("capacidad") Integer capacidad,
            @Param("idZonaOrigen") Long idZonaOrigen,
            @Param("idZonaDestino") Long idZonaDestino,
            @Param("fechaHoraConsulta") OffsetDateTime fechaHoraConsulta
    );
}
