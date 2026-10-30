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
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ArchivoLancheroStorage {
    private static final Logger logger = LoggerFactory.getLogger(ArchivoLancheroStorage.class);
    private static final long MAX_PIXELES = 40_000_000L;
    private static final int MAX_DIMENSION = 1920;
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
            @Value("${nautiflow.storage.presigned-url-minutes:15}") long urlPrefirmadaMinutos
    ) {
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
        this.bucket = bucket;
        this.region = region;
        this.maxDocumentoBytes = maxDocumentoBytes;
        this.maxFotoBytes = maxFotoBytes;
        this.urlPrefirmadaMinutos = urlPrefirmadaMinutos;
        if (maxDocumentoBytes <= 0 || maxFotoBytes <= 0 || urlPrefirmadaMinutos <= 0) {
            throw new IllegalArgumentException("Los límites de almacenamiento y vigencia de URL deben ser positivos.");
        }
    }

    public String guardar(MultipartFile archivo, TipoArchivo tipo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("Los documentos y fotos enviados no pueden estar vacíos.");
        }

        byte[] contenido = archivo.getBytes();
        String contentType = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase();
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
            contenido = convertirAWebp(contenido);
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
        }

        String carpeta = tipo == TipoArchivo.FOTO ? "fotos" : "documentos";
        String clave = "nautiflow/" + carpeta + "/" + UUID.randomUUID() + extension;
        try {
            s3().putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(clave)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(contenido)
            );
            return clave;
        } catch (SdkException exception) {
            registrarErrorR2("subir archivo", exception);
            throw new ErrorAlmacenamientoException(
                    mensajeErrorSubida(exception),
                    exception
            );
        }
    }

    public void eliminar(List<String> claves) {
        if (claves.isEmpty()) {
            return;
        }
        try {
            for (String clave : claves) {
                s3().deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(clave).build());
            }
        } catch (SdkException exception) {
            registrarErrorR2("eliminar archivo", exception);
            throw new IllegalStateException("No se pudieron eliminar archivos temporales del bucket R2.", exception);
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

    private byte[] convertirAWebp(byte[] contenido) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(contenido))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new SolicitudInvalidaException("El archivo enviado no es una imagen válida.");
            }
            ImageReader reader = readers.next();
            BufferedImage imagen;
            try {
                reader.setInput(input, true, true);
                int ancho = reader.getWidth(0);
                int alto = reader.getHeight(0);
                if (ancho <= 0 || alto <= 0 || (long) ancho * alto > MAX_PIXELES) {
                    throw new SolicitudInvalidaException("La foto supera el límite de 40 megapíxeles.");
                }
                imagen = reader.read(0);
            } finally {
                reader.dispose();
            }

            double escala = Math.min(1.0, Math.min((double) MAX_DIMENSION / imagen.getWidth(), (double) MAX_DIMENSION / imagen.getHeight()));
            int anchoDestino = Math.max(1, (int) Math.round(imagen.getWidth() * escala));
            int altoDestino = Math.max(1, (int) Math.round(imagen.getHeight() * escala));
            BufferedImage redimensionada = new BufferedImage(anchoDestino, altoDestino, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = redimensionada.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.drawImage(imagen, 0, 0, anchoDestino, altoDestino, null);
            } finally {
                graphics.dispose();
                imagen.flush();
            }

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
            if (!writers.hasNext()) {
                throw new IllegalStateException("No está disponible el codificador WebP.");
            }
            ImageWriter writer = writers.next();
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                 ImageOutputStream output = ImageIO.createImageOutputStream(bytes)) {
                writer.setOutput(output);
                ImageWriteParam params = writer.getDefaultWriteParam();
                if (params.canWriteCompressed()) {
                    params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    params.setCompressionQuality(0.82f);
                }
                writer.write(null, new IIOImage(redimensionada, null, null), params);
                output.flush();
                return bytes.toByteArray();
            } finally {
                redimensionada.flush();
                writer.dispose();
            }
        }
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
}
