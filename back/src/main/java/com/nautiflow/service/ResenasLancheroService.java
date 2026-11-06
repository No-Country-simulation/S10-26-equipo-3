package com.nautiflow.service;

import com.nautiflow.dto.PaginaResenasPublicasDTO;
import com.nautiflow.dto.ResenaPublicaDTO;
import com.nautiflow.repository.LancheroRepository;
import com.nautiflow.repository.ResenaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResenasLancheroService {
    private final LancheroRepository lancheroRepository;
    private final ResenaRepository resenaRepository;

    public ResenasLancheroService(LancheroRepository lancheroRepository, ResenaRepository resenaRepository) {
        this.lancheroRepository = lancheroRepository;
        this.resenaRepository = resenaRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResenasPublicasDTO listar(Long idLanchero, int page, int size) {
        PaginacionValidator.validar(page, size);
        if (!lancheroRepository.existsById(idLanchero)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lanchero no encontrado.");
        }

        Page<ResenaPublicaDTO> resultados = resenaRepository
                .findPublicasByLancheroId(idLanchero, PageRequest.of(page, size));
        return new PaginaResenasPublicasDTO(
                resultados.getContent(),
                resultados.getNumber(),
                resultados.getSize(),
                resultados.getTotalElements(),
                resultados.getTotalPages()
        );
    }
}
