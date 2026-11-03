package com.nautiflow.service;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.SendFailedException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;

import javax.net.ssl.SSLException;
import java.nio.charset.StandardCharsets;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

final class RegistroEmailSmtpErrorClassifier {
    private RegistroEmailSmtpErrorClassifier() {
    }

    static String clasificar(Throwable error) {
        List<Throwable> causas = recorrerCausas(error);

        if (contiene(causas, MailAuthenticationException.class, AuthenticationFailedException.class)) {
            return "SMTP_AUTHENTICATION";
        }
        if (contiene(causas, SSLException.class)) {
            return "SMTP_TLS";
        }
        if (contiene(causas, SocketTimeoutException.class, TimeoutException.class)) {
            return "SMTP_TIMEOUT";
        }
        if (contiene(causas, ConnectException.class, NoRouteToHostException.class,
                UnknownHostException.class, SocketException.class)) {
            return "SMTP_CONNECTION";
        }
        if (causas.stream().anyMatch(RegistroEmailSmtpErrorClassifier::esRechazoSmtp)) {
            return "SMTP_REJECTED";
        }
        return "SMTP_OTHER";
    }

    static String tiposDeExcepcion(Throwable error) {
        Set<String> tipos = new LinkedHashSet<>();
        for (Throwable causa : recorrerCausas(error)) {
            tipos.add(causa.getClass().getSimpleName());
        }
        return String.join(" -> ", tipos);
    }

    static String diagnostico(Throwable error, String... credenciales) {
        List<String> detalles = new ArrayList<>();
        for (Throwable causa : recorrerCausas(error)) {
            StringBuilder detalle = new StringBuilder(causa.getClass().getSimpleName());
            String mensaje = causa.getMessage();
            if (mensaje != null && !mensaje.isBlank()) {
                detalle.append(": ").append(sanitizar(mensaje, credenciales));
            }
            StackTraceElement[] stackTrace = causa.getStackTrace();
            int limite = Math.min(stackTrace.length, 3);
            for (int indice = 0; indice < limite; indice++) {
                detalle.append(" at ").append(stackTrace[indice]);
            }
            detalles.add(detalle.toString());
        }
        return String.join(" | caused by: ", detalles);
    }

    private static String sanitizar(String mensaje, String... credenciales) {
        String seguro = mensaje;
        for (String credencial : credenciales) {
            if (credencial == null || credencial.isBlank()) {
                continue;
            }
            seguro = seguro.replace(credencial, "[REDACTED]");
            String credencialBase64 = Base64.getEncoder()
                    .encodeToString(credencial.getBytes(StandardCharsets.UTF_8));
            seguro = seguro.replace(credencialBase64, "[REDACTED]");
        }
        return seguro.replaceAll(
                "(?i)(password|passwd|token|authorization|credential|secret)(\\s*[=:]\\s*)[^\\s,;]+",
                "$1$2[REDACTED]"
        );
    }

    private static boolean esRechazoSmtp(Throwable error) {
        String tipo = error.getClass().getSimpleName();
        return error instanceof SendFailedException
                || tipo.endsWith("SMTPAddressFailedException")
                || tipo.endsWith("SMTPSendFailedException")
                || tipo.endsWith("SMTPSenderFailedException");
    }

    @SafeVarargs
    private static boolean contiene(List<Throwable> causas, Class<? extends Throwable>... tipos) {
        return causas.stream().anyMatch(causa -> {
            for (Class<? extends Throwable> tipo : tipos) {
                if (tipo.isInstance(causa)) {
                    return true;
                }
            }
            return false;
        });
    }

    private static List<Throwable> recorrerCausas(Throwable error) {
        if (error == null) {
            return List.of();
        }

        List<Throwable> causas = new ArrayList<>();
        Set<Throwable> visitadas = Collections.newSetFromMap(new IdentityHashMap<>());
        Deque<Throwable> pendientes = new ArrayDeque<>();
        pendientes.add(error);

        while (!pendientes.isEmpty()) {
            Throwable actual = pendientes.removeFirst();
            if (!visitadas.add(actual)) {
                continue;
            }
            causas.add(actual);

            if (actual.getCause() != null) {
                pendientes.addLast(actual.getCause());
            }
            if (actual instanceof MessagingException messagingException
                    && messagingException.getNextException() != null) {
                pendientes.addLast(messagingException.getNextException());
            }
            if (actual instanceof MailSendException mailSendException) {
                mailSendException.getFailedMessages().values().forEach(pendientes::addLast);
            }
        }

        return causas;
    }
}
