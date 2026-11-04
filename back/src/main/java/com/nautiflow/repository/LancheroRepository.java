package com.nautiflow.repository;

import com.nautiflow.model.Lanchero;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.model.EstadoOperativoLanchero;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;

/**
 * Repositorio de lancheros, con búsquedas por correo, estado, paginación y bloqueo pesimista para decisiones concurrentes.
 */
@Repository
public interface LancheroRepository extends JpaRepository<Lanchero, Long> {
    /**
     * Indica si ya existe un lanchero con ese correo.
     */
    boolean existsByCorreo(String correo);

    /**
     * Cuenta lancheros por estado de solicitud.
     */
    long countByEstadoSolicitud(EstadoSolicitud estadoSolicitud);

    /**
     * Cuenta lancheros por estado operativo.
     */
    long countByEstadoViaje(EstadoOperativoLanchero estadoViaje);

    /**
     * Lista lancheros por estado de solicitud con paginación.
     */
    Page<Lanchero> findByEstadoSolicitud(EstadoSolicitud estadoSolicitud, Pageable pageable);

    /**
     * Busca un lanchero y aplica bloqueo pesimista de escritura para actualizarlo con seguridad.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lanchero l where l.idLanchero = :id")
    java.util.Optional<Lanchero> findLockedById(@Param("id") Long id);

    /**
     * Busca un lanchero con su lancha y zonas cargadas.
     */
    @EntityGraph(attributePaths = {"lancha", "lancha.zonas"})
    java.util.Optional<Lanchero> findByIdLanchero(Long idLanchero);
}
