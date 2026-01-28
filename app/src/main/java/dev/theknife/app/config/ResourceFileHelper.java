/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.config;

import dev.theknife.app.util.Logger;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper per la gestione dei file di risorsa CSV dell'applicazione.
 * Copia i CSV dalle risorse JAR a una directory scrivibile.
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public final class ResourceFileHelper {
    // CAMPI
    private static final Logger logger = Logger.getLogger(ResourceFileHelper.class);
    private static final Path TARGET_DIR;
    
    // COSTRUTTORI
    static {
        String os = System.getProperty("os.name", "").toLowerCase();
        String userHome = System.getProperty("user.home");
        Path target;
        if (os.contains("win")) {
            target = Paths.get(userHome, ".theknife", "data");
        } else if (os.contains("mac")) {
            target = Paths.get(userHome, "Library", "Application Support", "TheKnife", "data");
        } else {
            target = Paths.get(userHome, ".local", "share", "theknife", "data");
        }
        TARGET_DIR = target;
        logger.info("ResourceFileHelper using data directory: " + TARGET_DIR.toAbsolutePath());
        try {
            if (!Files.exists(TARGET_DIR)) {
                Files.createDirectories(TARGET_DIR);
            }
        } catch (IOException e) {
            logger.error("Could not create data directory: " + TARGET_DIR.toAbsolutePath(), e);
        }
    }

    private static final Map<String, String> CSV_FILES = new HashMap<>();
    
    static {
        // Percorso nel classpath: data/data/ (cartella data in root progetto)
        CSV_FILES.put("data/data/users.csv", "users.csv");
        CSV_FILES.put("data/data/michelin_my_maps.csv", "michelin_my_maps.csv");
        CSV_FILES.put("data/data/reviews.csv", "reviews.csv");
        CSV_FILES.put("data/data/favorites.csv", "favorites.csv");
    }
    
    /**
     * Costruttore privato per impedire l'istanziazione.
     * <p>
     * La classe espone solo metodi statici di utilità.
     * </p>
     */
    private ResourceFileHelper() {}
    
    // METODI
    /**
     * Inizializza tutti i file CSV copiandoli dalle risorse se necessario.
     * <p>
     * Crea la directory di destinazione e copia tutti i file CSV configurati
     * dalle risorse JAR alla directory scrivibile dell'utente.
     * </p>
     *
     * @return Una mappa che associa il nome del file al suo percorso su disco.
     * @throws IOException Se si verifica un errore durante la copia dei file.
     */
    public static Map<String, Path> initializeAllCsvFiles() throws IOException {
        Files.createDirectories(TARGET_DIR);
        Map<String, Path> csvPaths = new HashMap<>();
        for (Map.Entry<String, String> entry : CSV_FILES.entrySet()) {
            Path path = prepareWritableFile(entry.getKey(), entry.getValue());
            csvPaths.put(entry.getValue(), path);
        }
        return csvPaths;
    }

    /**
     * Prepara un file scrivibile copiandolo dalla risorsa se non esiste già.
     * <p>
     * Se il file di destinazione non esiste, viene copiato dalla risorsa JAR.
     * Se esiste già, viene restituito il percorso esistente senza sovrascriverlo.
     * </p>
     *
     * @param resourcePath Il percorso della risorsa nel classpath.
     * @param targetFileName Il nome del file di destinazione.
     * @return Il percorso del file su disco.
     * @throws IOException Se si verifica un errore durante la copia.
     */
    public static Path prepareWritableFile(String resourcePath, String targetFileName) throws IOException {
        Path targetPath = TARGET_DIR.resolve(targetFileName);
        if (Files.notExists(targetPath)) {
            ClassLoader cl = ResourceFileHelper.class.getClassLoader();
            InputStream in = cl.getResourceAsStream(resourcePath);
            if (in == null) {
                String fallback = resourcePath.replace("data/data/", "data/");
                in = cl.getResourceAsStream(fallback);
            }
            if (in == null) {
                throw new FileNotFoundException("Resource not found in classpath: " + resourcePath + " (nor " + resourcePath.replace("data/data/", "data/") + ")");
            }
            try (InputStream stream = in) {
                Files.copy(stream, targetPath);
                logger.info("Copied resource '" + resourcePath + "' to: " + targetPath);
            }
        } else {
            logger.info("File already exists: " + targetPath);
        }
        return targetPath;
    }

    /**
     * Restituisce la directory di destinazione per i file CSV.
     *
     * @return Il percorso della directory di destinazione.
     */
    public static Path getTargetDirectory() {
        return TARGET_DIR;
    }
}
