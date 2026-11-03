package com.nautiflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.exception.SolicitudInvalidaException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Component
public class LancheroSolicitudParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LancheroSolicitudParser.class);

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public LancheroSolicitudParser(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public LancheroRegistroDTO parsear(String solicitudJson) {
        long inicioParseo = System.nanoTime();
        try {
            if (!StringUtils.hasText(solicitudJson)) {
                throw new SolicitudInvalidaException("La parte 'solicitud' no puede estar vacía.");
            }

            LancheroRegistroDTO solicitud;
            long inicioDeserializacion = System.nanoTime();
            try {
                solicitud = objectMapper.readValue(solicitudJson, LancheroRegistroDTO.class);
            } catch (JsonProcessingException exception) {
                throw new SolicitudInvalidaException("La parte 'solicitud' debe contener un JSON válido.", exception);
            } finally {
                LOGGER.info("Deserialización del JSON de registro: {} ms",
                        milisegundosDesde(inicioDeserializacion));
            }
            if (solicitud == null) {
                throw new SolicitudInvalidaException("La parte 'solicitud' debe contener un objeto JSON.");
            }

            long inicioValidacion = System.nanoTime();
            try {
                String mensaje = validator.validate(solicitud).stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining("; "));
                if (!mensaje.isEmpty()) {
                    throw new SolicitudInvalidaException(mensaje);
                }
            } finally {
                LOGGER.info("Validación Bean Validation de solicitud JSON: {} ms",
                        milisegundosDesde(inicioValidacion));
            }

            return solicitud;
        } finally {
            LOGGER.info("Tiempo total parseo y validación de solicitud JSON: {} ms", milisegundosDesde(inicioParseo));
        }
    }

    private long milisegundosDesde(long inicioNano) {
        return (System.nanoTime() - inicioNano) / 1_000_000;
    }
}
