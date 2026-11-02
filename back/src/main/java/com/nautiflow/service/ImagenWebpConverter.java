package com.nautiflow.service;

import com.nautiflow.exception.SolicitudInvalidaException;
import org.springframework.stereotype.Component;

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
import java.util.Iterator;

@Component
public class ImagenWebpConverter {
    private static final long MAX_PIXELES = 40_000_000L;
    private static final int MAX_DIMENSION = 1920;
    private static final float CALIDAD_WEBP = 0.82f;

    public byte[] convertir(byte[] contenido) throws IOException {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(contenido))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new SolicitudInvalidaException("El archivo enviado no es una imagen válida.");
            }

            BufferedImage imagen = leerImagen(readers.next(), input);
            BufferedImage redimensionada = redimensionar(imagen);
            try {
                return codificarWebp(redimensionada);
            } finally {
                redimensionada.flush();
            }
        }
    }

    private BufferedImage leerImagen(ImageReader reader, ImageInputStream input) throws IOException {
        try {
            reader.setInput(input, true, true);
            int ancho = reader.getWidth(0);
            int alto = reader.getHeight(0);
            if (ancho <= 0 || alto <= 0 || (long) ancho * alto > MAX_PIXELES) {
                throw new SolicitudInvalidaException("La foto supera el límite de 40 megapíxeles.");
            }
            return reader.read(0);
        } finally {
            reader.dispose();
        }
    }

    private BufferedImage redimensionar(BufferedImage imagen) {
        double escala = Math.min(
                1.0,
                Math.min((double) MAX_DIMENSION / imagen.getWidth(), (double) MAX_DIMENSION / imagen.getHeight())
        );
        int anchoDestino = Math.max(1, (int) Math.round(imagen.getWidth() * escala));
        int altoDestino = Math.max(1, (int) Math.round(imagen.getHeight() * escala));
        BufferedImage resultado = new BufferedImage(anchoDestino, altoDestino, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resultado.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.drawImage(imagen, 0, 0, anchoDestino, altoDestino, null);
            return resultado;
        } catch (RuntimeException exception) {
            resultado.flush();
            throw exception;
        } finally {
            graphics.dispose();
            imagen.flush();
        }
    }

    private byte[] codificarWebp(BufferedImage imagen) throws IOException {
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
                String[] compressionTypes = params.getCompressionTypes();
                if (compressionTypes != null && compressionTypes.length > 0) {
                    params.setCompressionType(compressionTypes[0]);
                    params.setCompressionQuality(CALIDAD_WEBP);
                }
            }
            writer.write(null, new IIOImage(imagen, null, null), params);
            output.flush();
            return bytes.toByteArray();
        } finally {
            writer.dispose();
        }
    }
}
