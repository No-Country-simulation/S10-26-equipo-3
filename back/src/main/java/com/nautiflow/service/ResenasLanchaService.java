package com.nautiflow.service;

import com.nautiflow.dto.PaginaResenasPublicasDTO;
import com.nautiflow.dto.ResenaPublicaDTO;
import com.nautiflow.repository.LanchaRepository;
import com.nautiflow.repository.ResenaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResenasLanchaService {
    private final LanchaRepository lanchaRepository;
    private final ResenaRepository resenaRepository;

    public ResenasLanchaService(LanchaRepository lanchaRepository, ResenaRepository resenaRepository) {
        this.lanchaRepository = lanchaRepository;
        this.resenaRepository = resenaRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResenasPublicasDTO listar(Long idLancha, int page, int size) {
        PaginacionValidator.validar(page, size);
        if (!lanchaRepository.existsById(idLancha)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lancha no encontrada.");
        }

        Page<ResenaPublicaDTO> resultados = resenaRepository
                .findPublicasByLanchaId(idLancha, PageRequest.of(page, size));
        return new PaginaResenasPublicasDTO(
                resultados.getContent(),
                resultados.getNumber(),
                resultados.getSize(),
                resultados.getTotalElements(),
                resultados.getTotalPages()
        );
    }
}
