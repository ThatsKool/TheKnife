/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.config;

import dev.theknife.app.util.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper per la gestione dei dati dell'applicazione.
 * <p>
 * I dati vengono sempre letti e salvati nella directory locale dell'utente:
 * <ul>
 *   <li>Windows: {@code C:\Users\&lt;user&gt;\.theknife\data}</li>
 *   <li>macOS:   {@code ~/Library/Application Support/TheKnife/data}</li>
 *   <li>Linux:   {@code ~/.local/share/theknife/data}</li>
 * </ul>
 * È possibile sovrascrivere con la proprietà di sistema {@code theknife.data.dir}.
 * </p>
 * <p>
 * Al primo avvio, se la directory locale è vuota, i dati iniziali vengono copiati dalla
 * cartella {@code data} del progetto (in sviluppo) o dalla cartella {@code data} accanto al JAR.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public final class ResourceFileHelper {
    // CAMPI
    private static final Logger logger = Logger.getLogger(ResourceFileHelper.class);
    private static final Path TARGET_DIR;

    private static final String[] CSV_FILE_NAMES = {
        "users.csv",
        "michelin_my_maps.csv",
        "reviews.csv",
        "favorites.csv"
    };

    // COSTRUTTORI
    static {
        String override = System.getProperty("theknife.data.dir");
        Path target;
        if (override != null && !override.isBlank()) {
            target = Paths.get(override).normalize().toAbsolutePath();
        } else {
            String os = System.getProperty("os.name", "").toLowerCase();
            String userHome = System.getProperty("user.home");
            if (os.contains("win")) {
                target = Paths.get(userHome, ".theknife", "data");
            } else if (os.contains("mac")) {
                target = Paths.get(userHome, "Library", "Application Support", "TheKnife", "data");
            } else {
                target = Paths.get(userHome, ".local", "share", "theknife", "data");
            }
        }
        TARGET_DIR = target.normalize().toAbsolutePath();
        logger.info("ResourceFileHelper using data directory: " + TARGET_DIR);
        try {
            if (!Files.exists(TARGET_DIR)) {
                Files.createDirectories(TARGET_DIR);
            }
        } catch (IOException e) {
            logger.error("Could not create data directory: " + TARGET_DIR, e);
        }
    }

    /**
     * Determina la directory che contiene la cartella {@code data} di seed.
     * Usata solo per copiare i dati iniziali nella directory utente al primo avvio.
     * Se si è in esecuzione da JAR: directory del JAR; altrimenti root del progetto.
     */
    private static Path resolveSeedBaseDir() {
        try {
            var location = ResourceFileHelper.class.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                Path codeSource = Paths.get(location.toURI()).normalize().toAbsolutePath();
                if (Files.isRegularFile(codeSource) && codeSource.getFileName().toString().toLowerCase().endsWith(".jar")) {
                    Path jarDir = codeSource.getParent();
                    if (jarDir != null) {
                        return jarDir;
                    }
                }
            }
        } catch (URISyntaxException | IllegalArgumentException ignored) {
            // fallback sotto
        }
        Path base = Paths.get(System.getProperty("user.dir", ".")).normalize().toAbsolutePath();
        if ("app".equals(base.getFileName() != null ? base.getFileName().toString() : null)) {
            base = base.getParent() != null ? base.getParent() : base;
        }
        return base;
    }

    /**
     * Copia ricorsivamente il contenuto di {@code source} in {@code target}.
     */
    private static void copyDirectoryRecursively(Path source, Path target) throws IOException {
        if (!Files.isDirectory(source)) {
            return;
        }
        try (var stream = Files.list(source)) {
            for (Path entry : stream.toList()) {
                Path dest = target.resolve(entry.getFileName());
                if (Files.isDirectory(entry)) {
                    Files.createDirectories(dest);
                    copyDirectoryRecursively(entry, dest);
                } else {
                    Files.createDirectories(dest.getParent());
                    Files.copy(entry, dest, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Verifica se la directory utente ha già i CSV (dati già inizializzati).
     */
    private static boolean hasRequiredData(Path dir) {
        for (String name : CSV_FILE_NAMES) {
            if (Files.isRegularFile(dir.resolve(name))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Costruttore privato per impedire l'istanziazione.
     */
    private ResourceFileHelper() {}

    // METODI
    /**
     * Inizializza la directory dati utente: la crea se necessario e, al primo avvio,
     * copia i dati di seed dalla cartella {@code data} del progetto o da quella accanto al JAR.
     * Restituisce i percorsi dei file CSV nella directory locale dell'utente.
     *
     * @return Mappa nome file → percorso assoluto per ogni CSV.
     * @throws IOException Se la directory non può essere creata o un file richiesto non esiste dopo la copia.
     */
    public static Map<String, Path> initializeAllCsvFiles() throws IOException {
        Files.createDirectories(TARGET_DIR);

        Path seedDir = resolveSeedBaseDir().resolve("data");
        if (Files.isDirectory(seedDir) && !hasRequiredData(TARGET_DIR)) {
            logger.info("Primo avvio: copia dati di seed da " + seedDir + " a " + TARGET_DIR);
            copyDirectoryRecursively(seedDir, TARGET_DIR);
        }

        Map<String, Path> csvPaths = new HashMap<>();
        for (String fileName : CSV_FILE_NAMES) {
            Path path = TARGET_DIR.resolve(fileName);
            if (Files.notExists(path)) {
                throw new IOException("File dati non trovato (copiare o creare in " + TARGET_DIR + "): " + fileName);
            }
            csvPaths.put(fileName, path);
        }
        return csvPaths;
    }

    /**
     * Restituisce la directory dati locale dell'utente (lettura e scrittura).
     *
     * @return Il percorso della directory dati.
     */
    public static Path getTargetDirectory() {
        return TARGET_DIR;
    }

    /**
     * Restituisce la directory delle immagini nella cartella dati utente ({@code <user_data>/images}).
     *
     * @return Il percorso della directory immagini.
     */
    public static Path getImagesDirectory() {
        return TARGET_DIR.resolve("images");
    }
}
