package com.nautiflow.service;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.SendFailedException;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;

import javax.net.ssl.SSLHandshakeException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegistroEmailSmtpErrorClassifierTest {
    @Test
    void clasificaAutenticacionIncluidaEnLaCadenaDeCausas() {
        assertEquals("SMTP_AUTHENTICATION", RegistroEmailSmtpErrorClassifier.clasificar(
                new MailSendException("mail failed", new MailAuthenticationException(
                        "authentication failed", new AuthenticationFailedException("535")))
        ));
    }

    @Test
    void clasificaErrorTls() {
        assertEquals("SMTP_TLS", RegistroEmailSmtpErrorClassifier.clasificar(
                new MailSendException("mail failed", new SSLHandshakeException("handshake failed"))
        ));
    }

    @Test
    void clasificaTimeout() {
        assertEquals("SMTP_TIMEOUT", RegistroEmailSmtpErrorClassifier.clasificar(
                new IllegalStateException("send failed", new MailSendException(
                        "mail failed", new SocketTimeoutException("timed out")))
        ));
    }

    @Test
    void clasificaFalloDeConexion() {
        assertEquals("SMTP_CONNECTION", RegistroEmailSmtpErrorClassifier.clasificar(
                new MailSendException("mail failed", new ConnectException("connection refused"))
        ));
    }

    @Test
    void clasificaRechazoDelServidorSmtp() {
        assertEquals("SMTP_REJECTED", RegistroEmailSmtpErrorClassifier.clasificar(
                new MailSendException("mail failed", new SendFailedException("message rejected"))
        ));
    }

    @Test
    void clasificaOtrosErroresSinExponerMensajes() {
        RuntimeException error = new IllegalStateException("smtp unavailable");

        assertEquals("SMTP_OTHER", RegistroEmailSmtpErrorClassifier.clasificar(error));
        assertEquals("IllegalStateException", RegistroEmailSmtpErrorClassifier.tiposDeExcepcion(error));
    }

    @Test
    void diagnosticoIncluyeLaCausaYRedactaLasCredenciales() {
        RuntimeException error = new IllegalStateException(
                "send failed", new MailSendException("authentication password=secret-pass")
        );

        String diagnostico = RegistroEmailSmtpErrorClassifier.diagnostico(
                error, "user@example.com", "secret-pass"
        );

        org.junit.jupiter.api.Assertions.assertTrue(diagnostico.contains("IllegalStateException: send failed"));
        org.junit.jupiter.api.Assertions.assertTrue(diagnostico.contains("MailSendException: authentication"));
        org.junit.jupiter.api.Assertions.assertTrue(diagnostico.contains("password=[REDACTED]"));
        org.junit.jupiter.api.Assertions.assertFalse(diagnostico.contains("secret-pass"));
        org.junit.jupiter.api.Assertions.assertFalse(diagnostico.contains("user@example.com"));
    }
}
