package com.nautiflow.service;

import com.nautiflow.exception.DemasiadosIntentosLoginException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {
    private static final String IP = "10.0.0.1";
    private static final String OTRA_IP = "10.0.0.2";

    private MutableClock clock;
    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        loginAttemptService = new LoginAttemptService(clock, 3, 15, 15);
    }

    @Test
    void bloqueaLaIpCuandoAlcanzaElMaximoDeFallos() {
        loginAttemptService.registrarFallo(IP);
        loginAttemptService.registrarFallo(IP);

        assertDoesNotThrow(() -> loginAttemptService.validarPermitido(IP));

        loginAttemptService.registrarFallo(IP);

        DemasiadosIntentosLoginException exception = assertThrows(
                DemasiadosIntentosLoginException.class,
                () -> loginAttemptService.validarPermitido(IP)
        );
        assertEquals(900, exception.getSegundosRestantes());
    }

    @Test
    void elExitoReiniciaElContadorDeFallos() {
        loginAttemptService.registrarFallo(IP);
        loginAttemptService.registrarFallo(IP);
        loginAttemptService.registrarExito(IP);

        loginAttemptService.registrarFallo(IP);
        loginAttemptService.registrarFallo(IP);

        assertDoesNotThrow(() -> loginAttemptService.validarPermitido(IP));

        loginAttemptService.registrarFallo(IP);

        assertThrows(DemasiadosIntentosLoginException.class,
                () -> loginAttemptService.validarPermitido(IP));
    }

    @Test
    void elBloqueoSeLevantaAlPasarElTiempoConfigurado() {
        loginAttemptService.registrarFallo(IP);
        loginAttemptService.registrarFallo(IP);
        loginAttemptService.registrarFallo(IP);

        assertThrows(DemasiadosIntentosLoginException.class,
                () -> loginAttemptService.validarPermitido(IP));

        clock.avanzar(Duration.ofMinutes(16));

        assertDoesNotThrow(() -> loginAttemptService.validarPermitido(IP));
    }

    @Test
    void losFallosFueraDeLaVentanaNoCuentan() {
        loginAttemptService.registrarFallo(IP);
        loginAttemptService.registrarFallo(IP);

        clock.avanzar(Duration.ofMinutes(16));

        loginAttemptService.registrarFallo(IP);

        assertDoesNotThrow(() -> loginAttemptService.validarPermitido(IP));
    }

    @Test
    void cadaIpTieneSuPropioContador() {
        loginAttemptService.registrarFallo(IP);
        loginAttemptService.registrarFallo(IP);
        loginAttemptService.registrarFallo(IP);

        assertThrows(DemasiadosIntentosLoginException.class,
                () -> loginAttemptService.validarPermitido(IP));
        assertDoesNotThrow(() -> loginAttemptService.validarPermitido(OTRA_IP));
    }

    @Test
    void laLimpiezaEliminaLosRegistrosVencidos() {
        loginAttemptService.registrarFallo(IP);
        loginAttemptService.registrarFallo(IP);

        clock.avanzar(Duration.ofMinutes(16));
        loginAttemptService.limpiarRegistrosExpirados();

        loginAttemptService.registrarFallo(IP);

        assertDoesNotThrow(() -> loginAttemptService.validarPermitido(IP));
    }

    /**
     * Reloj mutable para simular el paso del tiempo en las pruebas.
     */
    private static final class MutableClock extends Clock {
        private Instant instante;
        private final ZoneId zona;

        private MutableClock(Instant instante) {
            this(instante, ZoneOffset.UTC);
        }

        private MutableClock(Instant instante, ZoneId zona) {
            this.instante = instante;
            this.zona = zona;
        }

        /**
         * Avanza el reloj la duración indicada.
         *
         * @param duracion tiempo a sumar.
         */
        private void avanzar(Duration duracion) {
            instante = instante.plus(duracion);
        }

        @Override
        public ZoneId getZone() {
            return zona;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instante, zone);
        }

        @Override
        public Instant instant() {
            return instante;
        }
    }
}
