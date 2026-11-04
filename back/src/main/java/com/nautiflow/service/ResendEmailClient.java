package com.nautiflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Locale;

/**
 * Cliente HTTP de la API de Resend. Construye el correo, envía la petición, clasifica los errores y sanea los mensajes antes de registrarlos.
 */
@Component
public class ResendEmailClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResendEmailClient.class);
    private static final URI RESEND_EMAILS_URI = URI.create("https://api.resend.com/emails");
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String remitente;
    private final URI endpoint;
    private final Duration requestTimeout;

    /**
     * Crea el cliente HTTP de Resend con tiempos de conexión y espera acotados.
     */
    @Autowired
    public ResendEmailClient(
            ObjectMapper objectMapper,
            @Value("${nautiflow.mail.resend.api-key:}") String apiKey,
            @Value("${nautiflow.mail.resend.from:}") String remitente
    ) {
        this(
                HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build(),
                objectMapper,
                apiKey,
                remitente,
                RESEND_EMAILS_URI,
                REQUEST_TIMEOUT
        );
    }

    /**
     * Constructor interno que permite inyectar el cliente HTTP, el endpoint y el timeout en las pruebas.
     */
    ResendEmailClient(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            String apiKey,
            String remitente,
            URI endpoint,
            Duration requestTimeout
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.remitente = remitente;
        this.endpoint = endpoint;
        this.requestTimeout = requestTimeout;
    }

    /**
     * Envía un correo sin clave de idempotencia.
     */
    public void enviar(CorreoTransaccional correo) {
        enviar(correo, null);
    }

    /**
     * Envía un correo con una clave de idempotencia para evitar duplicados.
     */
    public void enviar(CorreoTransaccional correo, String claveIdempotencia) {
        enviar(crearCuerpoJson(correo), claveIdempotencia);
    }

    /**
     * Construye y valida el JSON que se enviará a la API de Resend.
     */
    public String crearCuerpoJson(CorreoTransaccional correo) {
        validarConfiguracion();

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("from", remitente);
        ArrayNode destinatarios = payload.putArray("to");
        destinatarios.add(correo.destinatario());
        payload.put("subject", correo.asunto());
        payload.put("text", correo.texto());
        payload.put("html", correo.html());
        final String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (IOException exception) {
            throw new ResendEmailException(
                    ResendEmailException.Category.CONFIGURATION,
                    0,
                    true,
                    "No se pudo construir la petición de correo para Resend.",
                    exception,
                    null,
                    null
            );
        }

        return body;
    }

    /**
     * Envía a Resend un cuerpo JSON ya construido, con la clave de idempotencia indicada.
     */
    public void enviar(String body, String claveIdempotencia) {
        validarApiKey();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(endpoint)
                .timeout(requestTimeout)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
        if (claveIdempotencia != null && !claveIdempotencia.isBlank()) {
            requestBuilder.header("Idempotency-Key", claveIdempotencia);
        }
        HttpRequest request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw errorHttp(response.statusCode(), response.body());
            }
        } catch (HttpConnectTimeoutException exception) {
            throw error(ResendEmailException.Category.CONNECTION, "Tiempo de conexión con Resend agotado.",
                    exception);
        } catch (HttpTimeoutException exception) {
            throw error(ResendEmailException.Category.TIMEOUT, "Tiempo de espera de Resend agotado.",
                    exception);
        } catch (ConnectException exception) {
            throw error(ResendEmailException.Category.CONNECTION, "No se pudo conectar con Resend.", exception);
        } catch (IOException exception) {
            throw error(ResendEmailException.Category.CONNECTION, "Falló la comunicación HTTP con Resend.",
                    exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw error(ResendEmailException.Category.PROVIDER_TEMPORARY,
                    "Se interrumpió la petición HTTP a Resend.", exception);
        }
    }

    /**
     * Verifica que estén configurados el remitente y la API key de Resend.
     */
    private void validarConfiguracion() {
        if (remitente == null || remitente.isBlank()) {
            throw error(ResendEmailException.Category.CONFIGURATION,
                    "Falta configurar RESEND_FROM.", null);
        }
        validarApiKey();
    }

    /**
     * Verifica que la API key de Resend esté configurada.
     */
    private void validarApiKey() {
        if (apiKey == null || apiKey.isBlank()) {
            throw error(ResendEmailException.Category.CONFIGURATION,
                    "Falta configurar RESEND_API_KEY.", null);
        }
    }

    /**
     * Clasifica la respuesta HTTP de Resend en una categoría de error y decide si se puede reintentar.
     */
    private ResendEmailException errorHttp(int status, String body) {
        String providerErrorCode = null;
        String providerMessage = null;
        if (body != null && !body.isBlank()) {
            try {
                var responseJson = objectMapper.readTree(body);
                providerErrorCode = textField(responseJson, "name");
                providerMessage = textField(responseJson, "message");
            } catch (JsonProcessingException exception) {
                LOGGER.warn("Resend devolvió HTTP {} con un cuerpo de error no JSON.", status);
            }
        }
        providerErrorCode = sanitizar(providerErrorCode == null ? "unknown_error" : providerErrorCode);
        providerMessage = sanitizar(providerMessage == null ? "Sin mensaje del proveedor." : providerMessage);
        LOGGER.warn("Resend devolvió HTTP {}: code={}, message={}",
                status, providerErrorCode, providerMessage);

        String detalleSeguro = providerMessage.toLowerCase(Locale.ROOT);
        ResendEmailException.Category categoria;
        boolean reintentable;
        if (status == 401) {
            categoria = ResendEmailException.Category.AUTHENTICATION;
            reintentable = false;
        } else if (status == 403 && esErrorDeRemitenteNoVerificado(detalleSeguro)) {
            categoria = ResendEmailException.Category.SENDER_NOT_VERIFIED;
            reintentable = false;
        } else if (status == 403) {
            categoria = ResendEmailException.Category.AUTHENTICATION;
            reintentable = false;
        } else if ((status == 400 || status == 422)
                && (detalleSeguro.contains("recipient") || detalleSeguro.contains("email address")
                || detalleSeguro.contains("invalid email"))) {
            categoria = ResendEmailException.Category.RECIPIENT_INVALID;
            reintentable = false;
        } else if (status == 429) {
            categoria = ResendEmailException.Category.RATE_LIMIT;
            reintentable = true;
        } else if (status == 408 || status == 409 || status == 425 || status >= 500) {
            categoria = ResendEmailException.Category.PROVIDER_TEMPORARY;
            reintentable = true;
        } else {
            categoria = ResendEmailException.Category.PROVIDER_PERMANENT;
            reintentable = false;
        }

        return new ResendEmailException(
                categoria,
                status,
                reintentable,
                "Resend rechazó la petición (HTTP " + status + ", " + categoria + ").",
                null,
                providerErrorCode,
                providerMessage
        );
    }

    /**
     * Extrae un campo de texto de un JSON de error del proveedor.
     */
    private String textField(com.fasterxml.jackson.databind.JsonNode json, String field) {
        var value = json.path(field);
        return value.isTextual() ? value.asText() : null;
    }

    /**
     * Detecta si el error de Resend corresponde a un remitente o dominio no verificado.
     */
    private boolean esErrorDeRemitenteNoVerificado(String message) {
        return (message.contains("domain") || message.contains("sender"))
                && (message.contains("not verified") || message.contains("isn't verified")
                || message.contains("is not verified"))
                || message.contains("verify a domain");
    }

    /**
     * Oculta la API key, credenciales y correos antes de guardar o registrar un mensaje de error.
     */
    private String sanitizar(String message) {
        String safe = message.replace(apiKey, "[REDACTED]").replaceAll(
                "(?i)(password|passwd|token|authorization|credential|secret)(\\s*[=:]\\s*)[^\\s,;]+",
                "$1$2[REDACTED]"
        ).replaceAll(
                "(?i)[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}",
                "[email]"
        );
        return safe.length() <= 1000 ? safe : safe.substring(0, 1000);
    }

    /**
     * Crea una excepción de Resend con la categoría y la causa indicadas.
     */
    private ResendEmailException error(
            ResendEmailException.Category categoria,
            String mensaje,
            Throwable causa
    ) {
        return new ResendEmailException(categoria, 0, true, mensaje, causa, null, null);
    }
}
