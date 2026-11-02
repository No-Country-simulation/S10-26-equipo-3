package com.nautiflow.repository;

import com.nautiflow.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    Optional<Resena> findByViaje_IdViaje(Long idViaje);
    boolean existsByViaje_IdViaje(Long idViaje);

    @EntityGraph(attributePaths = {
            "viaje",
            "viaje.lancha",
            "viaje.lancha.lanchero",
            "viaje.zonaOrigen",
            "viaje.zonaDestino"
    })
    @Query("select r from Resena r")
    Page<Resena> findAllWithLanchero(Pageable pageable);
}
