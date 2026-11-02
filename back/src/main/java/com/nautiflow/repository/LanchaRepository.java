package com.nautiflow.repository;

import com.nautiflow.model.Lancha;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LanchaRepository extends JpaRepository<Lancha, Long> {
    List<Lancha> findDistinctByZonas_IdZona(Long idZona);
    Optional<Lancha> findByLanchero_IdLanchero(Long idLanchero);
    long countByLanchero_EstadoSolicitud(String estadoSolicitud);

    @Query("select l.lanchero.idLanchero from Lancha l where l.idLancha = :id")
    Optional<Long> findLancheroIdByIdLancha(@Param("id") Long idLancha);
}
