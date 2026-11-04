package com.nautiflow.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenRevocationServiceTest {
    private final JwtTokenRevocationService service = new JwtTokenRevocationService();

    @Test
    void revocaSoloElTokenIndicadoHastaSuExpiracion() {
        String token = "token-de-prueba";

        assertTrue(service.revocar(token, Instant.now().plusSeconds(60)));
        assertTrue(service.estaRevocado(token));
        assertFalse(service.estaRevocado("otro-token"));
        assertFalse(service.revocar(token, Instant.now().plusSeconds(60)));
    }

    @Test
    void noRegistraTokensYaExpirados() {
        assertFalse(service.revocar("token-expirado", Instant.now().minusSeconds(1)));
        assertFalse(service.estaRevocado("token-expirado"));
    }

    @Test
    void eliminaLasRevocacionesUnaVezExpiradas() {
        String token = "token-por-expirar";
        assertTrue(service.revocar(token, Instant.now().plusSeconds(60)));

        service.limpiarExpirados(Instant.now().plusSeconds(61));

        assertFalse(service.estaRevocado(token));
    }
}
