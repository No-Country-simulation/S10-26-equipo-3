package com.nautiflow.repository;

import com.nautiflow.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {
    List<Documento> findByLanchero_IdLanchero(Long idLanchero);
    List<Documento> findByLanchero_IdLancheroOrderByIdDocumentoAsc(Long idLanchero);
}
