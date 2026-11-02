package com.nautiflow.service;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArchivoLancheroStorageTest {
    @Test
    void convierteYRedimensionaLaFotoAWebp() throws IOException {
        ImagenWebpConverter converter = new ImagenWebpConverter();
        byte[] webp = converter.convertir(crearImagenPng(2400, 1200));

        assertTrue(webp.length > 12);
        assertEquals("RIFF", new String(webp, 0, 4, java.nio.charset.StandardCharsets.US_ASCII));
        assertEquals("WEBP", new String(webp, 8, 4, java.nio.charset.StandardCharsets.US_ASCII));
        BufferedImage resultado = ImageIO.read(new ByteArrayInputStream(webp));
        try {
            assertEquals(1920, resultado.getWidth());
            assertEquals(960, resultado.getHeight());
        } finally {
            resultado.flush();
        }
    }

    private byte[] crearImagenPng(int ancho, int alto) throws IOException {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "png", salida);
            return salida.toByteArray();
        } finally {
            imagen.flush();
        }
    }
}
