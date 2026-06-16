package org.nhernandez.webapp.sistemaventas.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CfdiSecretCipher {

    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    private CfdiSecretCipher() {
    }

    public static String cifrar(String textoPlano, String claveMaestra) {
        if (textoPlano == null || textoPlano.isBlank()) {
            return null;
        }
        validarClave(claveMaestra);
        try {
            byte[] iv = new byte[IV_BYTES];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.ENCRYPT_MODE, derivarClave(claveMaestra), new GCMParameterSpec(TAG_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            byte[] paquete = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, paquete, 0, iv.length);
            System.arraycopy(cifrado, 0, paquete, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(paquete);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar la contraseña de Facturama", e);
        }
    }

    public static String descifrar(String textoCifrado, String claveMaestra) {
        if (textoCifrado == null || textoCifrado.isBlank()) {
            return null;
        }
        validarClave(claveMaestra);
        try {
            byte[] paquete = Base64.getDecoder().decode(textoCifrado);
            if (paquete.length <= IV_BYTES) {
                throw new IllegalArgumentException("Token cifrado invalido");
            }
            byte[] iv = new byte[IV_BYTES];
            byte[] cifrado = new byte[paquete.length - IV_BYTES];
            System.arraycopy(paquete, 0, iv, 0, IV_BYTES);
            System.arraycopy(paquete, IV_BYTES, cifrado, 0, cifrado.length);
            Cipher cipher = Cipher.getInstance(ALGORITMO);
            cipher.init(Cipher.DECRYPT_MODE, derivarClave(claveMaestra), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo descifrar la contraseña de Facturama", e);
        }
    }

    private static void validarClave(String claveMaestra) {
        if (claveMaestra == null || claveMaestra.isBlank()) {
            throw new IllegalStateException(
                    "Configure CFDI_ENCRYPTION_KEY en el servidor para guardar credenciales Facturama por negocio.");
        }
    }

    private static SecretKey derivarClave(String claveMaestra) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(claveMaestra.trim().getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(hash, "AES");
    }
}
