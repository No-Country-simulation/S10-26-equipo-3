package com.nautiflow.repository;

import com.nautiflow.dto.ResenaPublicaDTO;
import com.nautiflow.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repositorio de reseñas, con consultas por viaje y una consulta paginada con las relaciones necesarias para el panel.
 */
public interface ResenaRepository extends JpaRepository<Resena, Long> {
    /**
     * Calcula el promedio de estrellas de todas las reseñas del lanchero.
     */
    @Query("""
            select avg(r.estrellas)
            from Resena r
            where r.viaje.lancha.lanchero.idLanchero = :idLanchero
            """)
    Double calcularPromedioPorLanchero(@Param("idLanchero") Long idLanchero);

    /**
     * Busca la reseña de un viaje.
     */
    Optional<Resena> findByViaje_IdViaje(Long idViaje);
    /**
     * Indica si un viaje ya tiene reseña.
     */
    boolean existsByViaje_IdViaje(Long idViaje);

    @Query(
            value = """
                    select new com.nautiflow.dto.ResenaPublicaDTO(
                        r.idResena, r.estrellas, r.comentario, r.fecha
                    )
                    from Resena r
                    where r.viaje.lancha.lanchero.idLanchero = :idLanchero
                    order by r.fecha desc, r.idResena desc
                    """,
            countQuery = """
                    select count(r)
                    from Resena r
                    where r.viaje.lancha.lanchero.idLanchero = :idLanchero
                    """
    )
    Page<ResenaPublicaDTO> findPublicasByLancheroId(
            @Param("idLanchero") Long idLanchero,
            Pageable pageable
    );

    /**
     * Lista las reseñas con el viaje, la lancha, el lanchero y las zonas cargadas.
     */
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
