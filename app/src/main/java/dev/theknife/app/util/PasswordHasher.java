/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility per l'hashing sicuro delle password.
 * <p>
 * Implementa lo standard PBKDF2 (Password-Based Key Derivation Function 2)
 * con HMAC-SHA256, includendo il sale e il numero di iterazioni all'interno
 * della stringa risultante. Mantiene inoltre compatibilità con hash legacy
 * basati su SHA-256 esadecimale.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public final class PasswordHasher {
    // CAMPI
    private static final String SCHEME = "PBKDF2";
    private static final String PBKDF2_ALG = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100_000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_BYTES = 16;

    // COSTRUTTORI
    /**
     * Costruttore privato per impedire l'istanziazione.
     * <p>
     * La classe espone solo metodi statici di utilità.
     * </p>
     */
    private PasswordHasher() {}

    // METODI
    /**
     * Genera un hash sicuro per la password fornita.
     * <p>
     * Viene generato un sale casuale e applicato PBKDF2 con il numero di
     * iterazioni configurato. Il risultato è codificato nel formato:
     * {@code SCHEME$iterazioni$saltBase64$hashBase64}.
     * </p>
     *
     * @param password la password in chiaro da proteggere.
     * @return una stringa contenente schema, iterazioni, sale e hash derivato.
     */
    public static String hashPassword(String password) {
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] derived = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        String saltB64 = Base64.getEncoder().encodeToString(salt);
        String hashB64 = Base64.getEncoder().encodeToString(derived);
        return SCHEME + "$" + ITERATIONS + "$" + saltB64 + "$" + hashB64;
    }

    /**
     * Verifica una password in chiaro rispetto a un hash memorizzato.
     * <p>
     * Supporta sia il nuovo formato PBKDF2 (con schema {@code PBKDF2$...})
     * sia il formato legacy SHA-256 esadecimale a 64 caratteri. Per PBKDF2
     * viene utilizzata una comparazione a tempo costante.
     * </p>
     *
     * @param password       la password in chiaro inserita dall'utente.
     * @param hashedPassword l'hash memorizzato nel database.
     * @return {@code true} se la password corrisponde all'hash, {@code false} altrimenti.
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) return false;
        if (hashedPassword.startsWith(SCHEME + "$")) {
            String[] parts = hashedPassword.split("\\$");
            if (parts.length != 4) return false;
            int iter = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password.toCharArray(), salt, iter, expected.length * 8);
            return slowEquals(expected, actual);
        }
        if (hashedPassword.matches("^[0-9a-fA-F]{64}$")) {
            return sha256Hex(password).equalsIgnoreCase(hashedPassword);
        }
        return false;
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALG);
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException("PBKDF2 failure", e);
        }
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) diff |= a[i] ^ b[i];
        return diff == 0;
    }
}
