package com.nautiflow.service;

import com.nautiflow.dto.LanchaDestacadaDTO;
import com.nautiflow.dto.PaginaLanchasDestacadasDTO;
import com.nautiflow.model.EstadoSolicitud;
import com.nautiflow.repository.DocumentoRepository;
import com.nautiflow.repository.LanchaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LanchasDestacadasService {
    private static final int CANTIDAD_POR_PAGINA = 10;

    private final LanchaRepository lanchaRepository;
    private final DocumentoRepository documentoRepository;
    private final ArchivoLancheroStorage archivoStorage;

    public LanchasDestacadasService(
            LanchaRepository lanchaRepository,
            DocumentoRepository documentoRepository,
            ArchivoLancheroStorage archivoStorage
    ) {
        this.lanchaRepository = lanchaRepository;
        this.documentoRepository = documentoRepository;
        this.archivoStorage = archivoStorage;
    }

    @Transactional(readOnly = true)
    public PaginaLanchasDestacadasDTO listar() {
        List<LanchaRepository.DestacadaProjection> lanchas =
                lanchaRepository.buscarDestacadas(
                        EstadoSolicitud.APROBADO,
                        PageRequest.of(0, CANTIDAD_POR_PAGINA)
                );

        Map<Long, String> fotosPorLanchero = obtenerFotosPrincipales(lanchas);
        List<LanchaDestacadaDTO> contenido = lanchas.stream()
                .map(lancha -> {
                    String claveFoto = fotosPorLanchero.get(lancha.getIdLanchero());
                    String fotoUrl = claveFoto == null
                            ? null
                            : archivoStorage.generarUrlPrefirmada(claveFoto);
                    return new LanchaDestacadaDTO(
                            lancha.getIdLancha(),
                            lancha.getNombreEmbarcacion(),
                            lancha.getLancheroNombreCompleto(),
                            fotoUrl,
                            lancha.getPromedioCalificacion(),
                            lancha.getTotalResenas(),
                            true
                    );
                })
                .toList();

        long totalElementos = lanchaRepository.countByLanchero_EstadoSolicitud(EstadoSolicitud.APROBADO);
        int totalPaginas = Math.toIntExact(
                (totalElementos + CANTIDAD_POR_PAGINA - 1) / CANTIDAD_POR_PAGINA
        );
        return new PaginaLanchasDestacadasDTO(
                contenido,
                0,
                CANTIDAD_POR_PAGINA,
                totalElementos,
                totalPaginas
        );
    }

    private Map<Long, String> obtenerFotosPrincipales(
            List<LanchaRepository.DestacadaProjection> lanchas
    ) {
        if (lanchas.isEmpty()) {
            return Map.of();
        }

        List<Long> idsLancheros = lanchas.stream()
                .map(LanchaRepository.DestacadaProjection::getIdLanchero)
                .distinct()
                .toList();
        Map<Long, String> fotosPorLanchero = new HashMap<>();
        documentoRepository.buscarFotosLancha(idsLancheros).forEach(foto ->
                fotosPorLanchero.putIfAbsent(foto.getIdLanchero(), foto.getClaveArchivo())
        );
        return fotosPorLanchero;
    }
}
