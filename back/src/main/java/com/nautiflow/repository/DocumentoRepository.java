package com.nautiflow.repository;

import com.nautiflow.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio de documentos de los lancheros.
 */
public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    interface FotoLanchaProjection {
        Long getIdLanchero();
        String getClaveArchivo();
    }

    /**
     * Lista los documentos de un lanchero.
     */
    List<Documento> findByLanchero_IdLanchero(Long idLanchero);
    /**
     * Lista los documentos de un lanchero ordenados por ID ascendente.
     */
    List<Documento> findByLanchero_IdLancheroOrderByIdDocumentoAsc(Long idLanchero);

    @Query("""
            select d.lanchero.idLanchero as idLanchero, d.claveArchivo as claveArchivo
            from Documento d
            where d.tipoDocumento = 'FOTO_LANCHA'
              and d.lanchero.idLanchero in :idsLancheros
            order by d.lanchero.idLanchero asc, d.idDocumento asc
            """)
    List<FotoLanchaProjection> buscarFotosLancha(
            @Param("idsLancheros") List<Long> idsLancheros
    );
}
