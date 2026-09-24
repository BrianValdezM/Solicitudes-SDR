package com.safedata.sdr.solicitudes.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.web.util.UriUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Implementación de TOTP (RFC 6238) sin librerías externas.
 * Compatible con Google Authenticator, Microsoft Authenticator, Authy, etc.
 */
public final class TotpUtil {

    private static final int DIGITOS = 6;
    private static final int PASO_SEGUNDOS = 30;
    private static final String ALGORITMO_HMAC = "HmacSHA1";
    private static final String BASE32_ALFABETO = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private TotpUtil() {}

    /** Genera un secreto aleatorio codificado en Base32 (para guardar en BD). */
    public static String generarSecreto() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20]; // 160 bits
        random.nextBytes(bytes);
        return base32Encode(bytes);
    }

    /** URL otpauth:// para generar el código QR que el usuario escanea. */
    public static String generarUrlOtpAuth(String secretoBase32, String correoUsuario, String emisor) {
        // UriUtils.encode usa RFC 3986: espacio -> %20, dos puntos -> %3A, etc.
        String etiqueta = UriUtils.encode(emisor + ":" + correoUsuario, StandardCharsets.UTF_8);
        String emisorCodificado = UriUtils.encode(emisor, StandardCharsets.UTF_8);

        return "otpauth://totp/" + etiqueta
                + "?secret=" + secretoBase32
                + "&issuer=" + emisorCodificado
                + "&algorithm=SHA1"
                + "&digits=" + DIGITOS
                + "&period=" + PASO_SEGUNDOS;
    }

    /** Valida un código de 6 dígitos permitiendo +-1 paso de tolerancia (desfase de reloj). */
    public static boolean validarCodigo(String secretoBase32, String codigoIngresado) {
        if (codigoIngresado == null || !codigoIngresado.matches("\\d{6}")) return false;
        long tiempoActual = System.currentTimeMillis() / 1000L / PASO_SEGUNDOS;
        for (long i = -1; i <= 1; i++) {
            String codigoEsperado = generarCodigo(secretoBase32, tiempoActual + i);
            if (codigoEsperado.equals(codigoIngresado)) return true;
        }
        return false;
    }

    private static String generarCodigo(String secretoBase32, long contadorTiempo) {
        try {
            byte[] llave = base32Decode(secretoBase32);
            byte[] datos = new byte[8];
            long valor = contadorTiempo;
            for (int i = 7; i >= 0; i--) {
                datos[i] = (byte) (valor & 0xFF);
                valor >>= 8;
            }

            Mac mac = Mac.getInstance(ALGORITMO_HMAC);
            mac.init(new SecretKeySpec(llave, ALGORITMO_HMAC));
            byte[] hash = mac.doFinal(datos);

            int offset = hash[hash.length - 1] & 0x0F;
            int binario = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);

            int codigo = binario % (int) Math.pow(10, DIGITOS);
            return String.format("%0" + DIGITOS + "d", codigo);
        } catch (Exception e) {
            throw new RuntimeException("Error generando código TOTP", e);
        }
    }

    // ---- Base32 (sin librerías externas) ----

    private static String base32Encode(byte[] datos) {
        StringBuilder resultado = new StringBuilder();
        int buffer = 0, bitsEnBuffer = 0;
        for (byte b : datos) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsEnBuffer += 8;
            while (bitsEnBuffer >= 5) {
                int indice = (buffer >> (bitsEnBuffer - 5)) & 0x1F;
                resultado.append(BASE32_ALFABETO.charAt(indice));
                bitsEnBuffer -= 5;
            }
        }
        if (bitsEnBuffer > 0) {
            int indice = (buffer << (5 - bitsEnBuffer)) & 0x1F;
            resultado.append(BASE32_ALFABETO.charAt(indice));
        }
        return resultado.toString();
    }

    private static byte[] base32Decode(String base32) {
        String limpio = base32.trim().toUpperCase().replace("=", "");
        int buffer = 0, bitsEnBuffer = 0;
        java.io.ByteArrayOutputStream salida = new java.io.ByteArrayOutputStream();
        for (char c : limpio.toCharArray()) {
            int indice = BASE32_ALFABETO.indexOf(c);
            if (indice < 0) continue;
            buffer = (buffer << 5) | indice;
            bitsEnBuffer += 5;
            if (bitsEnBuffer >= 8) {
                salida.write((buffer >> (bitsEnBuffer - 8)) & 0xFF);
                bitsEnBuffer -= 8;
            }
        }
        return salida.toByteArray();
    }
}
