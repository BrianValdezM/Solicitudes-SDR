package com.safedata.sdr.solicitudes.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PasswordTemporalGenerator {

    private static final String MINUS  = "abcdefghijkmnpqrstuvwxyz";
    private static final String MAYUS  = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String DIGIT  = "23456789";
    private static final String SIMB   = "!@#$%&*?";
    private static final int LONGITUD_EXTRA = 8;

    private PasswordTemporalGenerator() {}

    /** Genera una contraseña que cumple la regex de seguridad del sistema. */
    public static String generar() {
        SecureRandom rnd = new SecureRandom();
        String todos = MINUS + MAYUS + DIGIT + SIMB;

        List<Character> chars = new ArrayList<>();
        chars.add(MINUS.charAt(rnd.nextInt(MINUS.length())));
        chars.add(MAYUS.charAt(rnd.nextInt(MAYUS.length())));
        chars.add(DIGIT.charAt(rnd.nextInt(DIGIT.length())));
        chars.add(SIMB.charAt(rnd.nextInt(SIMB.length())));
        for (int i = 0; i < LONGITUD_EXTRA; i++) {
            chars.add(todos.charAt(rnd.nextInt(todos.length())));
        }
        Collections.shuffle(chars, rnd);

        StringBuilder sb = new StringBuilder(chars.size());
        chars.forEach(sb::append);
        return sb.toString();
    }
}