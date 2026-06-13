/*
 * Utility per il parsing di JSON semplici senza dipendenze esterne.
 */
package dev.theknife.app.util;

/**
 * Parser minimale per oggetti JSON piatti (chiave-valore numerico).
 */
public final class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Estrae un valore numerico da una stringa JSON semplice.
     *
     * @param json la stringa JSON
     * @param key  la chiave del valore
     * @return il valore numerico, oppure {@code 0.0} se assente o non valido
     */
    public static double parseDouble(String json, String key) {
        if (json == null || key == null) {
            return 0.0;
        }
        try {
            String searchKey = "\"" + key + "\":";
            int startIdx = json.indexOf(searchKey);
            if (startIdx == -1) {
                return 0.0;
            }

            startIdx += searchKey.length();
            int endIdx = json.indexOf(",", startIdx);
            if (endIdx == -1) {
                endIdx = json.indexOf("}", startIdx);
            }

            if (endIdx > startIdx) {
                String valueStr = json.substring(startIdx, endIdx).trim();
                return Double.parseDouble(valueStr);
            }
        } catch (Exception ignored) {
            // valore non valido
        }
        return 0.0;
    }
}
