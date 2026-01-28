/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.dataaccess;

import dev.theknife.app.config.FileProvider;
import dev.theknife.app.util.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Gestore generico per la persistenza su file CSV (Data Access Layer).
 * <p>
 * Questa classe fornisce metodi CRUD (Create, Read, Update, Delete) ottimizzati per
 * lavorare con file CSV. Utilizza una cache in memoria per migliorare le prestazioni
 * di lettura e sincronizza le scritture su disco in modalità batch.
 * </p>
 * <p>
 * <b>Caratteristiche:</b>
 * <ul>
 *   <li>Caricamento iniziale in memoria (caching) all'istanziazione.</li>
 *   <li>Parsing e serializzazione personalizzabili tramite funzioni lambda.</li>
 *   <li>Operazioni di modifica sincronizzate per garantire thread-safety.</li>
 *   <li>Persistenza differita tramite flag <i>dirty</i> e metodo {@link #saveToDisk()}.</li>
 * </ul>
 * </p>
 *
 * @param <T> Il tipo di oggetto gestito dal CSVManager (es. User, Restaurant).
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class CSVManager<T> {
    // CAMPI
    private final String csvFileName;
    private final String headerLine;
    private final Function<String, T> parser;
    private final Function<T, String> serializer;
    private final FileProvider fileProvider;
    private final Logger logger = Logger.getLogger(CSVManager.class);
    private final List<T> cache = new ArrayList<>();
    private boolean dirty = false;

    // COSTRUTTORI
    /**
     * Costruisce un nuovo gestore CSV tipizzato.
     * <p>
     * Inizializza il file (creando l'header se necessario) e carica in memoria
     * tutti i record esistenti, popolando la cache interna.
     * </p>
     *
     * @param csvFileName nome del file CSV (senza il percorso assoluto).
     * @param headerLine  riga di intestazione da scrivere quando il file viene creato.
     * @param parser      funzione che converte una riga CSV in un oggetto di tipo {@code T}.
     * @param serializer  funzione che converte un oggetto {@code T} in una riga CSV.
     * @param fileProvider componente responsabile della risoluzione del percorso del CSV.
     */
    public CSVManager(String csvFileName, String headerLine,
                     Function<String, T> parser, Function<T, String> serializer,
                     FileProvider fileProvider) {
        this.csvFileName = csvFileName;
        this.headerLine = headerLine;
        this.parser = parser;
        this.serializer = serializer;
        this.fileProvider = fileProvider;
        try {
            createCSVHeader();
            loadInitialCache();
        } catch (IOException e) {
            logger.error("Error initializing CSVManager: " + e.getMessage(), e);
        }
    }
    
    // METODI
    /**
     * Aggiunge un nuovo oggetto alla cache e marca il contenuto come modificato.
     * <p>
     * La scrittura effettiva su disco avviene solo quando viene chiamato
     * esplicitamente {@link #saveToDisk()}.
     * </p>
     *
     * @param object l'oggetto da salvare.
     * @throws IOException se si verificano errori nel processo di persistenza.
     */
    public void save(T object) throws IOException {
        if (object == null) return;
        synchronized (cache) {
            cache.add(object);
            dirty = true;
        }
    }

    /**
     * Sostituisce il primo elemento che soddisfa il predicato di ricerca.
     * <p>
     * Scansiona linearmente la cache e, al primo match, rimpiazza l'elemento
     * con il nuovo valore, marcando la cache come modificata.
     * </p>
     *
     * @param matcher  funzione che restituisce {@code true} per l'elemento da sostituire.
     * @param newValue nuovo valore da inserire in cache.
     * @return {@code true} se un elemento è stato sostituito, {@code false} altrimenti.
     */
    public boolean replace(Function<T, Boolean> matcher, T newValue) {
        synchronized (cache) {
            for (int i = 0; i < cache.size(); i++) {
                if (Boolean.TRUE.equals(matcher.apply(cache.get(i)))) {
                    cache.set(i, newValue);
                    dirty = true;
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Rimuove tutti gli elementi che soddisfano il predicato fornito.
     *
     * @param predicate condizione che identifica gli elementi da rimuovere.
     * @return {@code true} se almeno un elemento è stato rimosso, {@code false} altrimenti.
     */
    public boolean removeIf(Function<T, Boolean> predicate) {
        synchronized (cache) {
            boolean removed = cache.removeIf(item -> Boolean.TRUE.equals(predicate.apply(item)));
            if (removed) dirty = true;
            return removed;
        }
    }

    /**
     * Restituisce una vista non modificabile della cache corrente.
     * <p>
     * La lista restituita è una copia difensiva e non può essere alterata
     * dall'esterno.
     * </p>
     *
     * @return lista non modificabile degli elementi in cache.
     * @throws IOException mantenuto per compatibilità con l'interfaccia, anche se
     *                     nella pratica l'accesso avviene solo in memoria.
     */
    public List<T> loadAll() throws IOException {
        synchronized (cache) {
            return Collections.unmodifiableList(new ArrayList<>(cache));
        }
    }

    /**
     * Crea il file CSV con la riga di intestazione, se non esiste già.
     *
     * @throws IOException se non è possibile creare o scrivere il file.
     */
    public void createCSVHeader() throws IOException {
        Path csvPath = getCSVPath();
        File file = csvPath.toFile();
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                writer.println(headerLine);
            }
        }
    }

    /**
     * Scrive il contenuto della cache sul file CSV.
     * <p>
     * Sovrascrive completamente il file, scrivendo prima l'header e poi
     * tutti i record serializzati.
     * </p>
     *
     * @throws IOException se si verifica un errore di I/O durante la scrittura.
     */
    public void saveToDisk() throws IOException {
        Path csvPath = getCSVPath();
        File file = csvPath.toFile();
        logger.debug("Saving to disk: " + file.getAbsolutePath());
        synchronized (cache) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
                writer.println(headerLine);
                for (T item : cache) {
                    writer.println(serializer.apply(item));
                }
                dirty = false;
                logger.debug("Successfully wrote " + cache.size() + " items to " + file.getName());
            } catch (IOException e) {
                logger.error("Failed to write to " + file.getAbsolutePath() + ": " + e.getMessage(), e);
                throw e;
            }
        }
    }

    /**
     * Restituisce il percorso del file CSV gestito.
     *
     * @return il {@link Path} assoluto del file CSV.
     * @throws IOException se il percorso non è inizializzato o non è valido.
     */
    public Path getCSVPath() throws IOException {
        try {
            return fileProvider.getCsvPath(csvFileName);
        } catch (IllegalArgumentException e) {
            throw new IOException("CSV file not initialized: " + csvFileName, e);
        }
    }

    /**
     * Effettua il parsing robusto di una singola riga CSV.
     * <p>
     * Utilizza Apache Commons CSV e gestisce alcuni casi non standard
     * (es. delimitatori alternativi o virgolette annidate), restituendo
     * sempre un array di stringhe non nullo.
     * </p>
     *
     * @param csvLine la riga CSV da analizzare.
     * @return un array di campi estratti dalla riga; può essere vuoto.
     */
    public static String[] parseCSVLine(String csvLine) {
        if (csvLine == null || csvLine.isEmpty()) {
            return new String[0];
        }
        try {
            String[] result = parseLineInternal(csvLine);
            if (result.length == 1 && csvLine.length() > 50 && csvLine.contains(",")) {
                // fall through
            } else {
                return result;
            }
        } catch (Exception e) {
            // fall through
        }
        try {
            String cleanLine = csvLine.trim();
            while (cleanLine.endsWith(";")) {
                cleanLine = cleanLine.substring(0, cleanLine.length() - 1);
            }
            if (cleanLine.startsWith("\"") && cleanLine.endsWith("\"")) {
                cleanLine = cleanLine.substring(1, cleanLine.length() - 1);
                cleanLine = cleanLine.replace("\";\"", ";");
                cleanLine = cleanLine.replace("\"\"", "\"");
                return parseLineInternal(cleanLine);
            }
        } catch (Exception e) {
            Logger.getLogger(CSVManager.class)
                    .error("Error parsing CSV line (even after unwrap attempt): " + csvLine, e);
        }
        return new String[0];
    }

    private static String[] parseLineInternal(String csvLine) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT
                .builder()
                .setTrim(true)
                .setQuote('"')
                .setIgnoreSurroundingSpaces(true)
                .build();
        try (CSVParser parser = new CSVParser(new StringReader(csvLine), format)) {
            List<String> fields = new ArrayList<>();
            for (CSVRecord record : parser) {
                record.forEach(fields::add);
            }
            return fields.toArray(new String[0]);
        }
    }

    /**
     * Restituisce il numero di record attualmente presenti in cache.
     *
     * @return il numero di elementi nella cache.
     * @throws IOException mantenuto per coerenza con altre API che possono sollevare I/O.
     */
    public int countRecords() throws IOException {
        synchronized (cache) {
            return cache.size();
        }
    }

    private void loadInitialCache() throws IOException {
        Path csvPath = getCSVPath();
        File file = csvPath.toFile();
        if (!file.exists()) return;
        synchronized (cache) {
            cache.clear();
            try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
                String line;
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    T object = parser.apply(line);
                    if (object != null) cache.add(object);
                }
            }
            dirty = false;
        }
    }
}
