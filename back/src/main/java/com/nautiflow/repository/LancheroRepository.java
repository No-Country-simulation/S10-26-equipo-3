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

@Repository
public interface LancheroRepository extends JpaRepository<Lanchero, Long> {
    boolean existsByCorreo(String correo);

    long countByEstadoSolicitud(EstadoSolicitud estadoSolicitud);

    long countByEstadoViaje(EstadoOperativoLanchero estadoViaje);

    Page<Lanchero> findByEstadoSolicitud(EstadoSolicitud estadoSolicitud, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lanchero l where l.idLanchero = :id")
    java.util.Optional<Lanchero> findLockedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {"lancha", "lancha.zonas"})
    java.util.Optional<Lanchero> findByIdLanchero(Long idLanchero);
}
