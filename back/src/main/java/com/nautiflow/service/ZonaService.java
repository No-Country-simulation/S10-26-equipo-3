package com.nautiflow.service;

import com.nautiflow.dto.ZonaDTO;
import com.nautiflow.repository.ZonaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ZonaService {
    private final ZonaRepository zonaRepository;

    public ZonaService(ZonaRepository zonaRepository) {
        this.zonaRepository = zonaRepository;
    }

    @Transactional(readOnly = true)
    public List<ZonaDTO> listar() {
        return zonaRepository.findAll(Sort.by(Sort.Direction.ASC, "nombre")).stream()
                .map(zona -> new ZonaDTO(zona.getIdZona(), zona.getNombre()))
                .toList();
    }
}
