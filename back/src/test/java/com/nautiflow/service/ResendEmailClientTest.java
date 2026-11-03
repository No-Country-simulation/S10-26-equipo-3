package com.nautiflow.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResendEmailClientTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AtomicInteger responseStatus = new AtomicInteger(200);
    private final AtomicLong responseDelayMillis = new AtomicLong();
    private final AtomicReference<String> responseBody = new AtomicReference<>("{\"id\":\"email-id\"}");
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> requestMethod = new AtomicReference<>();
    private final AtomicReference<String> requestContentType = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();
    private final AtomicReference<String> idempotencyKey = new AtomicReference<>();
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/emails", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            requestMethod.set(exchange.getRequestMethod());
            requestContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            idempotencyKey.set(exchange.getRequestHeaders().getFirst("Idempotency-Key"));
            try {
                Thread.sleep(responseDelayMillis.get());
                byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(responseStatus.get(), body.length);
                exchange.getResponseBody().write(body);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } finally {
                exchange.close();
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void enviaJsonConBearerYClaveDeIdempotencia() throws Exception {
        client().enviar(new CorreoTransaccional(
                "destinatario@example.com", "Solicitud recibida", "Texto actual", "<p>HTML actual</p>"
        ), "registro-lanchero-12");

        JsonNode payload = objectMapper.readTree(requestBody.get());
        assertEquals("POST", requestMethod.get());
        assertEquals("Bearer test-api-key", authorization.get());
        assertEquals("application/json", requestContentType.get());
        assertEquals("registro-lanchero-12", idempotencyKey.get());
        assertEquals("NautiFlow <notificaciones@yusdev.world>", payload.path("from").asText());
        assertEquals("destinatario@example.com", payload.path("to").get(0).asText());
        assertEquals("Solicitud recibida", payload.path("subject").asText());
        assertEquals("Texto actual", payload.path("text").asText());
        assertEquals("<p>HTML actual</p>", payload.path("html").asText());
    }

    @Test
    void clasificaAutenticacion401() {
        assertHttpError(401, "{\"name\":\"validation_error\",\"message\":\"invalid key\"}",
                ResendEmailException.Category.AUTHENTICATION, false);
    }

    @Test
    void clasifica403DePermisosComoErrorDeAutenticacionPermanente() {
        assertHttpError(403, "{\"name\":\"invalid_permission\",\"message\":\"missing required scopes\"}",
                ResendEmailException.Category.AUTHENTICATION, false);
    }

    @Test
    void clasificaRemitenteNoVerificadoYConservaCodigoYMensajeSeguroDelProveedor() {
        ResendEmailException exception = assertHttpError(
                403,
                "{\"name\":\"validation_error\",\"message\":\"The yusdev.world domain is not verified.\"}",
                ResendEmailException.Category.SENDER_NOT_VERIFIED,
                false
        );
        assertEquals("validation_error", exception.getProviderErrorCode());
        assertEquals("The yusdev.world domain is not verified.", exception.getProviderMessage());
    }

    @Test
    void clasifica403DePruebaPorPermisoYRedactaElCorreoDelMensaje() {
        responseStatus.set(403);
        responseBody.set("""
                {"name":"validation_error","message":"You can only send testing emails to user@example.com."}
                """);

        ResendEmailException exception = assertThrows(
                ResendEmailException.class,
                () -> client().enviar(correo())
        );

        assertEquals(ResendEmailException.Category.AUTHENTICATION, exception.getCategory());
        assertEquals("validation_error", exception.getProviderErrorCode());
        assertEquals("You can only send testing emails to [email].", exception.getProviderMessage());
        assertFalse(exception.getProviderMessage().contains("user@example.com"));
    }

    @Test
    void clasificaDestinatarioInvalidoComoErrorPermanente() {
        assertHttpError(400, "{\"name\":\"validation_error\",\"message\":\"Invalid recipient email address\"}",
                ResendEmailException.Category.RECIPIENT_INVALID, false);
    }

    @Test
    void clasificaLimiteDeSolicitudesComoReintentable() {
        assertHttpError(429, "{\"name\":\"rate_limit_exceeded\"}",
                ResendEmailException.Category.RATE_LIMIT, true);
    }

    @Test
    void clasificaFallo5xxComoTemporal() {
        assertHttpError(503, "{\"name\":\"service_unavailable\"}",
                ResendEmailException.Category.PROVIDER_TEMPORARY, true);
    }

    @Test
    void timeoutPreservaLaCausaOriginal() {
        responseDelayMillis.set(300);
        ResendEmailException exception = assertThrows(
                ResendEmailException.class,
                () -> client(Duration.ofMillis(50)).enviar(correo())
        );

        assertEquals(ResendEmailException.Category.TIMEOUT, exception.getCategory());
        assertTrue(exception.isRetryable());
        assertNotNull(exception.getCause());
    }

    @Test
    void falloDeConexionPreservaLaCausaOriginal() {
        int port = server.getAddress().getPort();
        server.stop(0);
        ResendEmailException exception = assertThrows(
                ResendEmailException.class,
                () -> client(URI.create("http://127.0.0.1:" + port + "/emails")).enviar(correo())
        );

        assertEquals(ResendEmailException.Category.CONNECTION, exception.getCategory());
        assertTrue(exception.isRetryable());
        assertNotNull(exception.getCause());
    }

    @Test
    void configuracionAusenteFallaConMensajeSeguro() {
        ResendEmailClient sinConfiguracion = new ResendEmailClient(
                HttpClient.newHttpClient(), objectMapper, "", "", endpoint(), Duration.ofSeconds(1)
        );
        ResendEmailException exception = assertThrows(
                ResendEmailException.class,
                () -> sinConfiguracion.enviar(correo())
        );

        assertEquals(ResendEmailException.Category.CONFIGURATION, exception.getCategory());
        assertTrue(exception.getMessage().contains("RESEND_API_KEY"));
        assertFalse(exception.getMessage().contains("test-api-key"));
    }

    private ResendEmailException assertHttpError(
            int status,
            String body,
            ResendEmailException.Category category,
            boolean retryable
    ) {
        responseStatus.set(status);
        responseBody.set(body);
        ResendEmailException exception = assertThrows(
                ResendEmailException.class,
                () -> client().enviar(correo())
        );
        assertEquals(category, exception.getCategory());
        assertEquals(status, exception.getHttpStatus());
        assertEquals(retryable, exception.isRetryable());
        assertFalse(exception.getMessage().contains(body));
        return exception;
    }

    private ResendEmailClient client() {
        return client(Duration.ofSeconds(2));
    }

    private ResendEmailClient client(Duration timeout) {
        return new ResendEmailClient(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build(),
                objectMapper,
                "test-api-key",
                "NautiFlow <notificaciones@yusdev.world>",
                endpoint(),
                timeout
        );
    }

    private ResendEmailClient client(URI endpoint) {
        return new ResendEmailClient(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build(),
                objectMapper,
                "test-api-key",
                "NautiFlow <notificaciones@yusdev.world>",
                endpoint,
                Duration.ofSeconds(1)
        );
    }

    private URI endpoint() {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/emails");
    }

    private CorreoTransaccional correo() {
        return new CorreoTransaccional("destinatario@example.com", "Asunto", "Texto", "<p>HTML</p>");
    }
}
