package org.nhernandez.webapp.sistemaventas.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import javax.imageio.ImageIO;

/**
 * Escala logos al area maxima manteniendo proporcion (letterboxing con fondo blanco).
 */
public final class LogoImageUtil {

    public static final int ANCHO_MAXIMO = 400;
    public static final int ALTO_MAXIMO = 400;

    private LogoImageUtil() {
    }

    public static void normalizarYGuardar(InputStream entrada, Path destino, String extension) throws IOException {
        BufferedImage original = ImageIO.read(entrada);
        if (original == null) {
            throw new IOException("No se pudo leer la imagen");
        }

        double escala = Math.min(1.0,
                Math.min((double) ANCHO_MAXIMO / original.getWidth(), (double) ALTO_MAXIMO / original.getHeight()));
        int ancho = Math.max(1, (int) Math.round(original.getWidth() * escala));
        int alto = Math.max(1, (int) Math.round(original.getHeight() * escala));

        int tipo = "png".equalsIgnoreCase(extension) ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage salida = new BufferedImage(ancho, alto, tipo);
        Graphics2D graficos = salida.createGraphics();
        try {
            graficos.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graficos.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (tipo != BufferedImage.TYPE_INT_ARGB) {
                graficos.setColor(Color.WHITE);
                graficos.fillRect(0, 0, ancho, alto);
            }
            graficos.drawImage(original, 0, 0, ancho, alto, null);
        } finally {
            graficos.dispose();
        }

        String formatoEscritura = "jpg".equalsIgnoreCase(extension) ? "jpeg" : extension.toLowerCase();
        if (!ImageIO.write(salida, formatoEscritura, destino.toFile())) {
            throw new IOException("Formato de imagen no soportado: " + extension);
        }
    }

    public static boolean puedeNormalizar(String contentType) {
        if (contentType == null) {
            return false;
        }
        return switch (contentType.toLowerCase()) {
            case "image/png", "image/jpeg", "image/jpg" -> true;
            default -> false;
        };
    }
}
