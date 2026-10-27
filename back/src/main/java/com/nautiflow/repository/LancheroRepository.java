package com.nautiflow.repository;

import com.nautiflow.model.Lanchero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LancheroRepository extends JpaRepository<Lanchero, Long> {
    boolean existsByDocumentoIdentidad(String documentoIdentidad);
    boolean existsByCorreo(String correo);
}
