/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementazione predefinita di {@link FileProvider}.
 * <p>
 * Gestisce la mappatura tra i nomi dei file logici e i percorsi fisici
 * dei file CSV utilizzati per la persistenza dei dati.
 * Utilizza {@link ResourceFileHelper} per l'inizializzazione e la gestione delle risorse.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see FileProvider
 * @see ResourceFileHelper
 */
public class DefaultFileProvider implements FileProvider {
    // CAMPI
    /**
     * Mappa che associa il nome del file (chiave) al suo percorso su disco (valore).
     */
    private final Map<String, Path> csvPaths;
    
    // COSTRUTTORI
    /**
     * Costruttore predefinito.
     * <p>
     * Inizializza tutti i file CSV necessari copiandoli dalle risorse interne
     * se non esistono già nella directory di lavoro dell'utente.
     * </p>
     * 
     * @throws IOException Se si verifica un errore durante l'inizializzazione dei file.
     */
    public DefaultFileProvider() throws IOException {
        this.csvPaths = new HashMap<>();
        Map<String, Path> paths = ResourceFileHelper.initializeAllCsvFiles();
        this.csvPaths.putAll(paths);
    }
    
    /**
     * Costruttore per test o configurazioni personalizzate.
     * 
     * @param csvPaths Mappa pre-popolata dei percorsi dei file CSV.
     */
    public DefaultFileProvider(Map<String, Path> csvPaths) {
        this.csvPaths = new HashMap<>(csvPaths);
    }
    
    // METODI
    /**
     * Recupera il percorso assoluto di un file CSV dato il suo nome.
     * 
     * @param fileName Il nome del file (es. "restaurants.csv").
     * @return Il percorso {@link Path} al file.
     * @throws IllegalArgumentException Se il file richiesto non è gestito dal provider.
     */
    @Override
    public Path getCsvPath(String fileName) {
        if (!csvPaths.containsKey(fileName)) {
            throw new IllegalArgumentException("CSV file not initialized: " + fileName);
        }
        return csvPaths.get(fileName);
    }
    
    /**
     * Verifica se un determinato file CSV è gestito dal provider.
     * 
     * @param fileName Il nome del file da verificare.
     * @return true se il file è gestito, false altrimenti.
     */
    @Override
    public boolean hasCsvPath(String fileName) {
        return csvPaths.containsKey(fileName);
    }
}

