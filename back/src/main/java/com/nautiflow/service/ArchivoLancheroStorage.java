package com.nautiflow.service;

import com.nautiflow.exception.AlmacenamientoNoConfiguradoException;
import com.nautiflow.exception.ArchivoDemasiadoGrandeException;
import com.nautiflow.exception.ErrorAlmacenamientoException;
import com.nautiflow.exception.SolicitudInvalidaException;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.retries.StandardRetryStrategy;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ArchivoLancheroStorage {
    private static final Logger logger = LoggerFactory.getLogger(ArchivoLancheroStorage.class);
    private static final Map<String, String> EXTENSIONES_DOCUMENTO = Map.of(
            "application/pdf", ".pdf",
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final String endpoint;
    private final String accessKeyId;
    private final String secretAccessKey;
    private final String bucket;
    private final String region;
    private final long maxDocumentoBytes;
    private final long maxFotoBytes;
    private final long urlPrefirmadaMinutos;
    private final ImagenWebpConverter imagenWebpConverter;
    private volatile S3Client s3Client;
    private volatile S3Presigner s3Presigner;

    public ArchivoLancheroStorage(
            @Value("${nautiflow.r2.endpoint:}") String endpoint,
            @Value("${nautiflow.r2.access-key-id:}") String accessKeyId,
            @Value("${nautiflow.r2.secret-access-key:}") String secretAccessKey,
            @Value("${nautiflow.r2.bucket:}") String bucket,
            @Value("${nautiflow.r2.region:auto}") String region,
            @Value("${nautiflow.storage.max-document-bytes:15728640}") long maxDocumentoBytes,
            @Value("${nautiflow.storage.max-photo-bytes:10485760}") long maxFotoBytes,
            @Value("${nautiflow.storage.presigned-url-minutes:15}") long urlPrefirmadaMinutos,
            ImagenWebpConverter imagenWebpConverter
    ) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.bucket = bucket;
        this.region = region;
        this.maxDocumentoBytes = maxDocumentoBytes;
        this.maxFotoBytes = maxFotoBytes;
        this.urlPrefirmadaMinutos = urlPrefirmadaMinutos;
        this.imagenWebpConverter = imagenWebpConverter;
        if (maxDocumentoBytes <= 0 || maxFotoBytes <= 0 || urlPrefirmadaMinutos <= 0) {
            throw new IllegalArgumentException("Los límites de almacenamiento y vigencia de URL deben ser positivos.");
        }
    }

    public ArchivoAlmacenado guardar(MultipartFile archivo, TipoArchivo tipo) throws IOException {
        return guardar(archivo, tipo, tipo == null ? "archivo" : tipo.name().toLowerCase());
    }

    public ArchivoAlmacenado guardar(MultipartFile archivo, TipoArchivo tipo, String nombreArchivo)
            throws IOException {
        byte[] contenido = null;
        String contentType = null;
        String clave = null;
        long inicioProcesamiento = System.nanoTime();
        try {
            if (archivo == null || archivo.isEmpty()) {
                throw new SolicitudInvalidaException("Los documentos y fotos enviados no pueden estar vacíos.");
            }
            contenido = archivo.getBytes();
            contentType = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase();
            String extension;
            if (tipo == TipoArchivo.FOTO) {
                if (!contentType.startsWith("image/")) {
                    throw new SolicitudInvalidaException("Las fotos deben ser archivos de imagen.");
                }
                if (contenido.length > maxFotoBytes) {
                    throw new ArchivoDemasiadoGrandeException(
                            "Cada foto puede pesar como máximo " + maxFotoBytes / (1024 * 1024) + " MB."
                    );
                }
                contenido = imagenWebpConverter.convertir(contenido);
                if (contenido.length > maxFotoBytes) {
                    throw new ArchivoDemasiadoGrandeException(
                            "La foto convertida supera el límite de "
                                    + maxFotoBytes / (1024 * 1024) + " MB."
                    );
                }
                contentType = "image/webp";
                extension = ".webp";
            } else {
                extension = EXTENSIONES_DOCUMENTO.get(contentType);
                if (extension == null) {
                    throw new SolicitudInvalidaException("Documento no permitido; envíe PDF, JPEG, PNG o WebP.");
                }
                if (contenido.length > maxDocumentoBytes) {
                    throw new ArchivoDemasiadoGrandeException(
                            "Cada documento puede pesar como máximo " + maxDocumentoBytes / (1024 * 1024) + " MB."
                    );
                }
                validarContenidoDocumento(contenido, contentType);
                if (contentType.startsWith("image/")) {
                    contenido = imagenWebpConverter.convertir(contenido);
                    if (contenido.length > maxDocumentoBytes) {
                        throw new ArchivoDemasiadoGrandeException(
                                "La imagen convertida supera el límite de "
                                        + maxDocumentoBytes / (1024 * 1024) + " MB."
                        );
                    }
                    contentType = "image/webp";
                    extension = ".webp";
                }
            }

            String carpeta = tipo == TipoArchivo.FOTO ? "fotos" : "documentos";
            clave = "nautiflow/" + carpeta + "/" + UUID.randomUUID() + extension;
        } finally {
            logger.info("Procesamiento y conversión del archivo {} (tipo={}): {} ms",
                    nombreArchivo, tipo, milisegundosDesde(inicioProcesamiento));
        }

        long inicioSubida = System.nanoTime();
        try {
            s3().putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(clave)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(contenido)
            );
            return new ArchivoAlmacenado(clave, contentType);
        } catch (SdkException exception) {
            registrarErrorR2("subir archivo", exception);
            try {
                eliminar(List.of(clave));
            } catch (RuntimeException cleanupException) {
                exception.addSuppressed(cleanupException);
                logger.error("No se pudo limpiar el objeto R2 tras fallar su subida.", cleanupException);
            }
            throw new ErrorAlmacenamientoException(
                    mensajeErrorSubida(exception),
                    exception
            );
        } finally {
            logger.info("Subida del archivo {} a Cloudflare R2 (tipo={}): {} ms",
                    nombreArchivo, tipo, milisegundosDesde(inicioSubida));
        }
    }

    public void eliminar(List<String> claves) {
        if (claves.isEmpty()) {
            return;
        }
        SdkException primerError = null;
        for (String clave : claves) {
            try {
                s3().deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(clave).build());
            } catch (SdkException exception) {
                registrarErrorR2("eliminar archivo", exception);
                if (primerError == null) {
                    primerError = exception;
                } else {
                    primerError.addSuppressed(exception);
                }
            }
        }
        if (primerError != null) {
            throw new IllegalStateException(
                    "No se pudieron eliminar uno o más archivos temporales del bucket R2.",
                    primerError
            );
        }
    }

    public String generarUrlPrefirmada(String clave) {
        if (!StringUtils.hasText(clave) || !clave.startsWith("nautiflow/")) {
            throw new SolicitudInvalidaException("La clave del archivo no es válida.");
        }
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(clave)
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(urlPrefirmadaMinutos))
                    .getObjectRequest(getObjectRequest)
                    .build();
            return presigner().presignGetObject(presignRequest).url().toString();
        } catch (SdkException exception) {
            registrarErrorR2("generar URL prefirmada", exception);
            throw new ErrorAlmacenamientoException("No se pudo generar la URL prefirmada.", exception);
        }
    }

    @PreDestroy
    void cerrarCliente() {
        if (s3Client != null) {
            s3Client.close();
        }
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }

    private synchronized S3Client s3() {
        if (s3Client == null) {
            if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(accessKeyId)
                    || !StringUtils.hasText(secretAccessKey) || !StringUtils.hasText(bucket)) {
                throw new AlmacenamientoNoConfiguradoException(
                        "La configuración de Cloudflare R2 está incompleta. Verifica endpoint, bucket, región, "
                                + "R2_ACCESS_KEY y R2_SECRET_KEY en el entorno del proceso."
                );
            }
            try {
                s3Client = S3Client.builder()
                        .endpointOverride(URI.create(endpoint))
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                        ))
                        .serviceConfiguration(S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .chunkedEncodingEnabled(false)
                                .build())
                        .overrideConfiguration(ClientOverrideConfiguration.builder()
                                .retryStrategy(StandardRetryStrategy.builder().maxAttempts(3).build())
                                .build())
                        .build();
            } catch (IllegalArgumentException exception) {
                throw new IllegalStateException("La URL del endpoint de Cloudflare R2 no es válida.", exception);
            }
        }
        return s3Client;
    }

    private void registrarErrorR2(String operacion, SdkException exception) {
        if (exception instanceof S3Exception s3Exception) {
            String codigo = s3Exception.awsErrorDetails() == null
                    ? "desconocido"
                    : s3Exception.awsErrorDetails().errorCode();
            String detalle = s3Exception.awsErrorDetails() == null
                    ? "sin detalle"
                    : s3Exception.awsErrorDetails().errorMessage();
            String requestId = s3Exception.requestId();
            logger.error(
                    "Error Cloudflare R2 al {}: status={}, code={}, detail={}, requestId={}",
                    operacion,
                    s3Exception.statusCode(),
                    codigo,
                    detalle,
                    requestId
            );
            return;
        }

        logger.error(
                "Error de cliente/conexión AWS SDK al {}: tipo={}",
                operacion,
                exception.getClass().getSimpleName()
        );
    }

    private long milisegundosDesde(long inicioNano) {
        return (System.nanoTime() - inicioNano) / 1_000_000;
    }

    private String mensajeErrorSubida(SdkException exception) {
        if (!(exception instanceof S3Exception s3Exception) || s3Exception.awsErrorDetails() == null) {
            return "No se pudo conectar con Cloudflare R2. Verifica endpoint, conectividad y credenciales S3.";
        }

        return switch (s3Exception.awsErrorDetails().errorCode()) {
            case "AccessDenied", "InvalidAccessKeyId" ->
                    "R2 rechazó las credenciales o permisos. Verifica que sean credenciales S3 de R2 con permiso de escritura en deltabucket.";
            case "SignatureDoesNotMatch" ->
                    "R2 rechazó la firma. Verifica R2_ACCESS_KEY y R2_SECRET_KEY; no uses un token API de Cloudflare.";
            case "NoSuchBucket" ->
                    "R2 no encuentra el bucket configurado. Verifica que deltabucket exista en esta cuenta.";
            case "InvalidRequest", "AuthorizationHeaderMalformed" ->
                    "R2 rechazó la configuración de la solicitud. Verifica endpoint, región auto y configuración S3.";
            case "InvalidArgument" ->
                    "R2 rechazó un parámetro de la solicitud. Verifica que R2_ACCESS_KEY sea el Access Key ID S3 de R2 (32 caracteres), no un token de API de Cloudflare.";
            default -> "R2 rechazó la subida (código " + s3Exception.awsErrorDetails().errorCode()
                    + "). Revisa los logs del backend para el request ID.";
        };
    }

    private synchronized S3Presigner presigner() {
        if (s3Presigner == null) {
            s3();
            s3Presigner = S3Presigner.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                    ))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(true)
                            .chunkedEncodingEnabled(false)
                            .build())
                    .build();
        }
        return s3Presigner;
    }

    private void validarContenidoDocumento(byte[] contenido, String contentType) {
        boolean valido = switch (contentType) {
            case "application/pdf" -> contenido.length >= 5
                    && contenido[0] == '%' && contenido[1] == 'P' && contenido[2] == 'D'
                    && contenido[3] == 'F' && contenido[4] == '-';
            case "image/jpeg" -> contenido.length >= 3
                    && (contenido[0] & 0xFF) == 0xFF && (contenido[1] & 0xFF) == 0xD8
                    && (contenido[2] & 0xFF) == 0xFF;
            case "image/png" -> contenido.length >= 8
                    && (contenido[0] & 0xFF) == 0x89 && contenido[1] == 'P'
                    && contenido[2] == 'N' && contenido[3] == 'G';
            case "image/webp" -> contenido.length >= 12
                    && new String(contenido, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF")
                    && new String(contenido, 8, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP");
            default -> false;
        };
        if (!valido) {
            throw new SolicitudInvalidaException("El contenido no coincide con el tipo de archivo declarado.");
        }
    }

    public enum TipoArchivo {
        FOTO,
        DOCUMENTO
    }

    public record ArchivoAlmacenado(String clave, String tipoContenido) {
    }
}
