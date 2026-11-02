package com.nautiflow.controller;

import com.nautiflow.exception.SolicitudInvalidaException;
import com.nautiflow.dto.RegistroLancheroResponse;
import com.nautiflow.service.LancheroSolicitudParser;
import com.nautiflow.service.LancheroService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lancheros")
public class LancheroController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LancheroController.class);

    private final LancheroService lancheroService;
    private final LancheroSolicitudParser solicitudParser;

    public LancheroController(LancheroService lancheroService, LancheroSolicitudParser solicitudParser) {
        this.lancheroService = lancheroService;
        this.solicitudParser = solicitudParser;
    }

    @PostMapping("/registrar")
    public ResponseEntity<RegistroLancheroResponse> register(MultipartHttpServletRequest request) {
        String solicitudJson = request.getParameter("solicitud");
        Map<String, List<MultipartFile>> archivos = request.getMultiFileMap();
        LOGGER.info(
                "Registro multipart recibido: contentType={}, camposTexto={}, partesArchivo={}",
                request.getContentType(),
                request.getParameterMap().entrySet().stream()
                        .map(entry -> entry.getKey() + "(length="
                                + (entry.getValue().length == 0 ? 0 : entry.getValue()[0].length()) + ")")
                        .toList(),
                archivos.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue().stream()
                                .map(file -> "{contentType=" + file.getContentType() + ", size=" + file.getSize() + "}")
                                .toList())
                        .toList()
        );
        if (!StringUtils.hasText(solicitudJson)) {
            if (archivos.containsKey("solicitud")) {
                throw new SolicitudInvalidaException(
                        "La parte 'solicitud' llegó como archivo; envíala como campo de texto con el JSON."
                );
            }
            throw new SolicitudInvalidaException("Falta el campo de texto 'solicitud' con el JSON del registro.");
        }

        MultipartFile licencia = obtenerArchivo(archivos, "licencia");
        MultipartFile seguro = obtenerArchivo(archivos, "seguro");
        MultipartFile antecedentes = obtenerArchivo(archivos, "antecedentes");
        List<MultipartFile> fotos = archivos.getOrDefault("fotos", List.of());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(lancheroService.registrarLanchero(
                        solicitudParser.parsear(solicitudJson), licencia, seguro, antecedentes, fotos
                ));
    }

    private MultipartFile obtenerArchivo(Map<String, List<MultipartFile>> archivos, String nombre) {
        List<MultipartFile> partes = archivos.get(nombre);
        if (partes == null || partes.isEmpty()) {
            throw new SolicitudInvalidaException("Falta el archivo requerido '" + nombre + "'.");
        }
        return partes.getFirst();
    }
}
