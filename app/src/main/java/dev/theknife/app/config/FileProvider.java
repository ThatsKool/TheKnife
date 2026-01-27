/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.config;

import java.nio.file.Path;

/**
 * Interfaccia per la fornitura dei percorsi dei file di dati.
 * <p>
 * Definisce il contratto per accedere ai file CSV utilizzati dall'applicazione,
 * astraendo la logica di localizzazione delle risorse.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public interface FileProvider {
    // METODI
    /**
     * Recupera il percorso di un file CSV specifico.
     * 
     * @param fileName Il nome del file CSV (es. "users.csv").
     * @return Il percorso {@link Path} al file.
     * @throws IllegalArgumentException Se il file non viene trovato o non è gestito.
     */
    Path getCsvPath(String fileName);
    
    /**
     * Verifica l'esistenza del percorso per un dato file CSV.
     * 
     * @param fileName Il nome del file CSV.
     * @return true se il percorso esiste ed è configurato, false altrimenti.
     */
    boolean hasCsvPath(String fileName);
}

