/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.tools;

import dev.theknife.app.dataaccess.CSVManager;
import dev.theknife.app.util.Logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Strumento di utilità per aggiungere ID progressivi ai file CSV di test.
 * <p>
 * Questa classe è utilizzata per migrare file CSV esistenti aggiungendo un campo ID
 * progressivo come prima colonna. Gestisce sia file che già hanno un ID (mantenendolo)
 * sia file che non lo hanno (aggiungendone uno nuovo).
 * </p>
 * <p>
 * <b>Funzionalità:</b>
 * <ul>
 *   <li>Aggiunge colonna ID se mancante</li>
 *   <li>Mantiene ID esistenti se già presenti</li>
 *   <li>Normalizza il formato CSV con quote per preservare virgole nei campi</li>
 *   <li>Aggiorna l'header del CSV con il nuovo formato</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class TestCSVIdAdder {
    // CAMPI
    private static final Logger logger = Logger.getLogger(TestCSVIdAdder.class);
    private static final String TARGET_PATH = "C:\\Users\\oitti\\Documents\\Uni\\Lab\\TheKnife\\app\\src\\test\\resources\\data\\michelin_my_maps.csv";
    private static final int EXPECTED_FIELDS_WITHOUT_ID = 15; // Name..RestaurateurEmail(optional)
    private static final int EXPECTED_FIELDS_WITH_ID = 16;    // Id + Name..RestaurateurEmail(optional)
    private static final String NEW_HEADER = "Id,Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description,RestaurateurEmail";
    
    // COSTRUTTORI
    private TestCSVIdAdder() {
    }

    // METODI
    /**
     * Metodo principale per eseguire la migrazione del file CSV.
     * <p>
     * Legge il file CSV specificato, aggiunge ID progressivi dove mancanti,
     * normalizza il formato e riscrive il file aggiornato.
     * </p>
     *
     * @param args Argomenti della riga di comando. Il primo argomento (opzionale)
     *             specifica il percorso del file CSV da processare. Se non fornito,
     *             viene utilizzato il percorso predefinito.
     * @throws Exception Se si verifica un errore durante la lettura o scrittura del file.
     */
    public static void main(String[] args) throws Exception {
        String path = args != null && args.length > 0 ? args[0] : TARGET_PATH;
        boolean sequentialMode = args != null && args.length > 1
                && ( "sequential".equalsIgnoreCase(args[1]) || "--sequential".equalsIgnoreCase(args[1]) );
        File file = new File(path);
        if (!file.exists()) {
            logger.error("File not found: " + path);
            System.exit(1);
        }

        List<String> inputLines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                inputLines.add(line);
            }
        }

        if (inputLines.isEmpty()) {
            logger.error("File is empty: " + path);
            System.exit(1);
        }

        String header = inputLines.get(0);
        List<String> outputLines = new ArrayList<>(inputLines.size());
        if (header.startsWith("Id,")) {
            outputLines.add(header);
        } else {
            outputLines.add(NEW_HEADER);
        }

        if (sequentialMode) {
            long nextId = 1;
            for (int i = 1; i < inputLines.size(); i++) {
                String raw = inputLines.get(i);
                if (raw == null || raw.trim().isEmpty()) {
                    continue;
                }
                String[] fields = CSVManager.parseCSVLine(raw);
                if (fields.length == 0) {
                    continue;
                }
                String[] withoutId = padToLength(fields, EXPECTED_FIELDS_WITHOUT_ID);
                String[] normalized = new String[EXPECTED_FIELDS_WITH_ID];
                normalized[0] = String.valueOf(nextId++);
                System.arraycopy(withoutId, 0, normalized, 1, withoutId.length);
                StringBuilder sb = new StringBuilder();
                for (int f = 0; f < normalized.length; f++) {
                    if (f == 0) {
                        sb.append(normalized[f] == null ? "" : normalized[f].trim());
                    } else {
                        sb.append(",");
                        String v = normalized[f];
                        if (v == null) v = "";
                        String cleaned = v.replace("\"", "\"\"");
                        sb.append("\"").append(cleaned).append("\"");
                    }
                }
                outputLines.add(sb.toString());
            }
        } else {
            java.util.HashSet<Long> usedIds = new java.util.HashSet<>();
            long maxId = 0;
            for (int i = 1; i < inputLines.size(); i++) {
                String raw = inputLines.get(i);
                if (raw == null || raw.trim().isEmpty()) {
                    continue;
                }
                String[] fields = CSVManager.parseCSVLine(raw);
                if (fields.length == 0) {
                    continue;
                }
                try {
                    long existingId = Long.parseLong(fields[0].trim());
                    usedIds.add(existingId);
                    if (existingId > maxId) {
                        maxId = existingId;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            long nextId = maxId + 1;

            java.util.HashSet<Long> seenIds = new java.util.HashSet<>();
            for (int i = 1; i < inputLines.size(); i++) {
                String raw = inputLines.get(i);
                if (raw == null || raw.trim().isEmpty()) {
                    continue;
                }
                String[] fields = CSVManager.parseCSVLine(raw);
                if (fields.length == 0) {
                    continue;
                }

                Long numericId = null;
                try {
                    numericId = Long.parseLong(fields[0].trim());
                } catch (NumberFormatException ignored) {}

                String[] normalized;
                if (numericId != null) {
                    normalized = padToLength(fields, EXPECTED_FIELDS_WITH_ID);
                    if (!seenIds.add(numericId)) {
                        while (usedIds.contains(nextId)) {
                            nextId++;
                        }
                        normalized[0] = String.valueOf(nextId);
                        usedIds.add(nextId);
                        seenIds.add(nextId);
                        nextId++;
                    } else {
                        usedIds.add(numericId);
                    }
                } else {
                    String[] withoutId = padToLength(fields, EXPECTED_FIELDS_WITHOUT_ID);
                    normalized = new String[EXPECTED_FIELDS_WITH_ID];
                    while (usedIds.contains(nextId)) {
                        nextId++;
                    }
                    normalized[0] = String.valueOf(nextId);
                    usedIds.add(nextId);
                    seenIds.add(nextId);
                    nextId++;
                    System.arraycopy(withoutId, 0, normalized, 1, withoutId.length);
                }

                StringBuilder sb = new StringBuilder();
                for (int f = 0; f < normalized.length; f++) {
                    if (f == 0) {
                        sb.append(normalized[f] == null ? "" : normalized[f].trim());
                    } else {
                        sb.append(",");
                        String v = normalized[f];
                        if (v == null) v = "";
                        String cleaned = v.replace("\"", "\"\"");
                        sb.append("\"").append(cleaned).append("\"");
                    }
                }
                outputLines.add(sb.toString());
            }
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (String out : outputLines) {
                writer.println(out);
            }
        }

        logger.info("Updated CSV with progressive Ids at: " + path);
    }

    /**
     * Estende un array di stringhe alla lunghezza target riempiendo con stringhe vuote.
     * <p>
     * Se l'array di input è più corto della lunghezza target, i campi mancanti
     * vengono riempiti con stringhe vuote. Se è più lungo, viene troncato.
     * </p>
     *
     * @param input L'array di input da estendere.
     * @param targetLen La lunghezza target desiderata.
     * @return Un nuovo array della lunghezza target con i valori dell'input e campi vuoti dove necessario.
     */
    private static String[] padToLength(String[] input, int targetLen) {
        String[] out = new String[targetLen];
        int copyLen = Math.min(input.length, targetLen);
        for (int i = 0; i < copyLen; i++) {
            out[i] = input[i];
        }
        for (int i = copyLen; i < targetLen; i++) {
            out[i] = "";
        }
        return out;
    }
}
