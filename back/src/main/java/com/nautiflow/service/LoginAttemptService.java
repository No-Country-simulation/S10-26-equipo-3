package com.nautiflow.service;

import com.nautiflow.exception.DemasiadosIntentosLoginException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Controla los intentos fallidos de inicio de sesión por dirección IP para frenar la fuerza bruta.
 * Mantiene los contadores en memoria y aplica un bloqueo temporal al superar el máximo configurado.
 */
@Service
public class LoginAttemptService {
    private static final long LIMPIEZA_MILISEGUNDOS = 300_000L;

    private final ConcurrentMap<String, RegistroIntentos> registros = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int maximoIntentos;
    private final Duration ventana;
    private final Duration bloqueo;

    /**
     * Crea el controlador de intentos con los límites configurados.
     *
     * @param clock reloj usado para calcular ventanas y bloqueos.
     * @param maximoIntentos cantidad de fallos permitidos antes de bloquear.
     * @param minutosVentana minutos durante los cuales se cuentan los fallos.
     * @param minutosBloqueo minutos que dura el bloqueo tras superar el máximo.
     */
    public LoginAttemptService(
            Clock clock,
            @Value("${nautiflow.security.login.max-intentos:5}") int maximoIntentos,
            @Value("${nautiflow.security.login.minutos-ventana:15}") long minutosVentana,
            @Value("${nautiflow.security.login.minutos-bloqueo:15}") long minutosBloqueo
    ) {
        if (maximoIntentos < 1) {
            throw new IllegalArgumentException("El máximo de intentos de login debe ser mayor que cero.");
        }
        if (minutosVentana < 1) {
            throw new IllegalArgumentException("La ventana de intentos de login debe ser mayor que cero.");
        }
        if (minutosBloqueo < 1) {
            throw new IllegalArgumentException("El bloqueo de login debe ser mayor que cero.");
        }
        this.clock = clock;
        this.maximoIntentos = maximoIntentos;
        this.ventana = Duration.ofMinutes(minutosVentana);
        this.bloqueo = Duration.ofMinutes(minutosBloqueo);
    }

    /**
     * Verifica si la dirección IP puede intentar iniciar sesión; si está bloqueada, lanza una excepción con el tiempo restante.
     *
     * @param direccionIp dirección IP del cliente.
     * @throws DemasiadosIntentosLoginException si la IP superó el máximo de fallos y sigue bloqueada.
     */
    public void validarPermitido(String direccionIp) {
        String clave = normalizarClave(direccionIp);
        Instant ahora = Instant.now(clock);
        RegistroIntentos registro = registros.computeIfPresent(
                clave,
                (ignorada, actual) -> actual.estaExpirado(ahora, ventana) ? null : actual
        );
        if (registro != null && registro.bloqueadoHasta != null && ahora.isBefore(registro.bloqueadoHasta)) {
            long segundosRestantes = Duration.between(ahora, registro.bloqueadoHasta).toSeconds();
            throw new DemasiadosIntentosLoginException(segundosRestantes);
        }
    }

    /**
     * Registra un intento fallido y bloquea la IP cuando alcanza el máximo dentro de la ventana.
     *
     * @param direccionIp dirección IP del cliente.
     */
    public void registrarFallo(String direccionIp) {
        String clave = normalizarClave(direccionIp);
        Instant ahora = Instant.now(clock);
        registros.compute(clave, (ignorada, actual) -> {
            RegistroIntentos registro = actual;
            if (registro == null || registro.estaExpirado(ahora, ventana)) {
                registro = new RegistroIntentos();
            }
            registro.fallos++;
            registro.ultimoFallo = ahora;
            if (registro.fallos >= maximoIntentos) {
                registro.bloqueadoHasta = ahora.plus(bloqueo);
            }
            return registro;
        });
    }

    /**
     * Limpia los intentos de una IP después de un inicio de sesión correcto.
     *
     * @param direccionIp dirección IP del cliente.
     */
    public void registrarExito(String direccionIp) {
        registros.remove(normalizarClave(direccionIp));
    }

    /**
     * Elimina periódicamente los registros vencidos para evitar que el mapa crezca sin límite.
     */
    @Scheduled(fixedDelay = LIMPIEZA_MILISEGUNDOS)
    public void limpiarRegistrosExpirados() {
        Instant ahora = Instant.now(clock);
        registros.entrySet().removeIf(entrada -> entrada.getValue().estaExpirado(ahora, ventana));
    }

    /**
     * Normaliza la IP usada como clave y usa un valor fijo cuando no está disponible.
     *
     * @param direccionIp dirección IP recibida.
     * @return clave normalizada para el mapa de intentos.
     */
    private String normalizarClave(String direccionIp) {
        if (direccionIp == null || direccionIp.isBlank()) {
            return "desconocida";
        }
        return direccionIp.strip();
    }

    /**
     * Contador de fallos y bloqueo asociado a una dirección IP.
     */
    private static final class RegistroIntentos {
        private volatile int fallos;
        private volatile Instant ultimoFallo;
        private volatile Instant bloqueadoHasta;

        /**
         * Indica si el registro ya no cuenta para la ventana actual ni mantiene un bloqueo vigente.
         *
         * @param ahora instante actual.
         * @param ventana duración de la ventana de conteo.
         * @return true si el registro puede descartarse.
         */
        private boolean estaExpirado(Instant ahora, Duration ventana) {
            Instant bloqueo = bloqueadoHasta;
            if (bloqueo != null) {
                return !ahora.isBefore(bloqueo);
            }
            Instant ultimo = ultimoFallo;
            return ultimo == null || ahora.isAfter(ultimo.plus(ventana));
        }
    }
}
