package org.nhernandez.webapp.sistemaventas.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogoImageUtilTest {

    @TempDir
    Path tempDir;

    @Test
    void normalizarYGuardar_escalaImagenGrande() throws IOException {
        BufferedImage grande = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = grande.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, 800, 600);
        g.dispose();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(grande, "png", bytes);

        Path destino = tempDir.resolve("logo.png");
        LogoImageUtil.normalizarYGuardar(new ByteArrayInputStream(bytes.toByteArray()), destino, "png");

        BufferedImage guardada = ImageIO.read(destino.toFile());
        assertTrue(guardada.getWidth() <= LogoImageUtil.ANCHO_MAXIMO);
        assertTrue(guardada.getHeight() <= LogoImageUtil.ALTO_MAXIMO);
        assertEquals(400, guardada.getWidth());
        assertEquals(300, guardada.getHeight());
    }

    @Test
    void puedeNormalizar_aceptaPngJpeg() {
        assertTrue(LogoImageUtil.puedeNormalizar("image/png"));
        assertTrue(LogoImageUtil.puedeNormalizar("image/jpeg"));
    }
}
