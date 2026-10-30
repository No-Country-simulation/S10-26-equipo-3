package com.nautiflow.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nautiflow.dto.LancheroRegistroDTO;
import com.nautiflow.exception.SolicitudInvalidaException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

@Component
public class LancheroSolicitudParser {
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public LancheroSolicitudParser(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public LancheroRegistroDTO parsear(String solicitudJson) {
        if (!StringUtils.hasText(solicitudJson)) {
            throw new SolicitudInvalidaException("La parte 'solicitud' no puede estar vacía.");
        }

        LancheroRegistroDTO solicitud;
        try {
            solicitud = objectMapper.readValue(solicitudJson, LancheroRegistroDTO.class);
        } catch (JsonProcessingException exception) {
            throw new SolicitudInvalidaException("La parte 'solicitud' debe contener un JSON válido.", exception);
        }
        if (solicitud == null) {
            throw new SolicitudInvalidaException("La parte 'solicitud' debe contener un objeto JSON.");
        }

        String mensaje = validator.validate(solicitud).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        if (!mensaje.isEmpty()) {
            throw new SolicitudInvalidaException(mensaje);
        }

        return solicitud;
    }
}
