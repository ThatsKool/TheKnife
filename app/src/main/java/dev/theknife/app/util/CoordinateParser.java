/*
 * Utility per il parsing di coordinate geografiche da testo.
 */
package dev.theknife.app.util;

/**
 * Normalizza e converte stringhe numeriche in coordinate (supporta virgola decimale).
 */
public final class CoordinateParser {

    private CoordinateParser() {
    }

    /**
     * Normalizza un valore numerico sostituendo la virgola con il punto decimale.
     */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replace(",", ".");
    }

    /**
     * Converte una stringa in double dopo la normalizzazione.
     *
     * @throws NumberFormatException se il valore non è numerico
     */
    public static double parse(String value) throws NumberFormatException {
        String normalized = normalize(value);
        if (normalized.isEmpty()) {
            throw new NumberFormatException("Valore vuoto");
        }
        return Double.parseDouble(normalized);
    }
}
