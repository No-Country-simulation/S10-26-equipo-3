package com.nautiflow.repository;

import com.nautiflow.model.Lancha;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.model.EstadoOperativoLanchero;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de embarcaciones, con consultas para contar lanchas y buscar las disponibles para una ruta.
 */
public interface LanchaRepository extends JpaRepository<Lancha, Long> {
    interface DestacadaProjection {
        Long getIdLancha();
        Long getIdLanchero();
        String getNombreEmbarcacion();
        String getLancheroNombreCompleto();
        Double getPromedioCalificacion();
        Long getTotalResenas();
    }

    /**
     * Lista las lanchas que cubren una zona determinada.
     */
    List<Lancha> findDistinctByZonas_IdZona(Long idZona);

    /**
     * Busca la lancha asociada a un lanchero (relación uno a uno).
     */
    Optional<Lancha> findByLanchero_IdLanchero(Long idLanchero);

    /**
     * Cuenta las lanchas cuyos lancheros tienen el estado de solicitud indicado.
     */
    long countByLanchero_EstadoSolicitud(EstadoSolicitud estadoSolicitud);

    @Query("""
            select l.idLancha as idLancha,
                   l.lanchero.idLanchero as idLanchero,
                   l.nombre as nombreEmbarcacion,
                   l.lanchero.nombreCompleto as lancheroNombreCompleto,
                   avg(r.estrellas) as promedioCalificacion,
                   count(r.idResena) as totalResenas
            from Lancha l
            left join l.viajes v
            left join v.resena r
            where l.lanchero.estadoSolicitud = :estadoSolicitud
            group by l.idLancha, l.lanchero.idLanchero, l.nombre, l.lanchero.nombreCompleto
            order by case when count(r.idResena) > 0 then 0 else 1 end asc,
                     avg(r.estrellas) desc,
                     count(r.idResena) desc,
                     l.idLancha desc
            """)
    List<DestacadaProjection> buscarDestacadas(
            @Param("estadoSolicitud") EstadoSolicitud estadoSolicitud,
            Pageable pageable
    );

    /**
     * Obtiene el ID del lanchero dueño de una lancha.
     */
    @Query("select l.lanchero.idLanchero from Lancha l where l.idLancha = :id")
    Optional<Long> findLancheroIdByIdLancha(@Param("id") Long idLancha);

    /**
     * Consulta lanchas aprobadas, activas, con capacidad y zonas compatibles y sin viajes o retornos activos en la fecha indicada.
     */
    @EntityGraph(attributePaths = {"lanchero", "zonas"})
    @Query("""
            select distinct l from Lancha l
            where l.lanchero.estadoSolicitud = :estadoSolicitud
              and l.lanchero.estadoViaje = :estadoViaje
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
            @Param("estadoSolicitud") EstadoSolicitud estadoSolicitud,
            @Param("estadoViaje") EstadoOperativoLanchero estadoViaje,
            @Param("fechaHoraConsulta") OffsetDateTime fechaHoraConsulta
    );
}
