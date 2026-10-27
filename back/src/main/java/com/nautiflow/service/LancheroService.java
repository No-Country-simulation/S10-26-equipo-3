package com.nautiflow.service;

import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.model.Lanchero;
import com.nautiflow.repository.LancheroRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class LancheroService {
    private final LancheroRepository lancheroRepository;

    public LancheroService(LancheroRepository lancheroRepository) {
        this.lancheroRepository = lancheroRepository;
    }

    @Transactional
    public Lanchero registrarLanchero(LancheroRegistroDTO dto) {
        if (lancheroRepository.existsByDocumentoIdentidad(dto.documentoIdentidad())) {
            throw new IllegalArgumentException("Ya existe un lanchero con ese documento de identidad.");
        }
        if (lancheroRepository.existsByCorreo(dto.correo())) {
            throw new IllegalArgumentException("Ya existe un lanchero con ese correo electrónico.");
        }

        Lanchero lanchero = new Lanchero();
        lanchero.setNombre(dto.nombre());
        lanchero.setApellido(dto.apellido());
        lanchero.setDocumentoIdentidad(dto.documentoIdentidad());
        lanchero.setTelefono(dto.telefono());
        lanchero.setCorreo(dto.correo());

        return lancheroRepository.save(lanchero);
    }
}
