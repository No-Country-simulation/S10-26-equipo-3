package com.nautiflow.repository;

import com.nautiflow.model.Lanchero;
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
    long countByEstadoSolicitud(String estadoSolicitud);
    long countByEstadoViaje(String estadoViaje);
    Page<Lanchero> findByEstadoSolicitud(String estadoSolicitud, Pageable pageable);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lanchero l where l.idLanchero = :id")
    java.util.Optional<Lanchero> findLockedById(@Param("id") Long id);
    @EntityGraph(attributePaths = {"lancha", "lancha.zonas", "documentos"})
    java.util.Optional<Lanchero> findByIdLanchero(Long idLanchero);
}
