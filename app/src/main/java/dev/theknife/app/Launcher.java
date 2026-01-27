/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app;

/**
 * Classe launcher per avviare l'applicazione JavaFX.
 * <p>
 * Questa classe gestisce l'avvio dell'applicazione JavaFX da JAR,
 * assicurandosi che JavaFX sia caricato correttamente su Java 11+.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 2.0
 * @since 1.0
 */
public class Launcher {
    
    // COSTRUTTORI
    /**
     * Costruttore privato per prevenire l'istanziazione.
     * <p>
     * La classe espone solo metodi statici di utilità.
     * </p>
     */
    private Launcher() {
    }

    // METODI
    
    /**
     * Avvia l'applicazione JavaFX.
     * <p>
     * Verifica che la versione di Java sia 17 o superiore e che JavaFX sia disponibile.
     * </p>
     *
     * @param args Argomenti della riga di comando.
     */
    public static void main(String[] args) {
        // Verifica la versione di Java (richiede Java 17+)
        String javaVersion = System.getProperty("java.version");
        int majorVersion = getJavaMajorVersion(javaVersion);
        
        if (majorVersion < 17) {
            System.err.println("ERRORE: Java 17 o superiore richiesto!");
            System.err.println("Versione Java attuale: " + javaVersion);
            System.err.println("Versione minima richiesta: Java 17");
            System.err.println("Scarica Java 17+ da: https://adoptium.net/");
            System.exit(1);
        }
        
        // Verifica che JavaFX sia disponibile
        try {
            Class.forName("javafx.application.Application");
        } catch (ClassNotFoundException e) {
            System.err.println("ERRORE: JavaFX non trovato!");
            System.err.println("Assicurati che il JAR includa tutte le dipendenze JavaFX.");
            System.err.println("Esegui: java --module-path <path-to-javafx> --add-modules javafx.controls,javafx.fxml -jar TheKnife.jar");
            System.exit(1);
        }
        
        // Avvia l'applicazione
        App.main(args);
    }
    
    /**
     * Estrae la versione major di Java dalla stringa di versione.
     * <p>
     * Gestisce diversi formati di versione:
     * - Java 8: "1.8.0_xxx"
     * - Java 9+: "9", "10", "11", "17", "21", ecc.
     * - Java 9+ con formato completo: "17.0.1"
     * </p>
     *
     * @param versionString La stringa di versione Java.
     * @return La versione major di Java.
     */
    private static int getJavaMajorVersion(String versionString) {
        if (versionString == null || versionString.isEmpty()) {
            return 0;
        }
        
        // Rimuovi prefissi come "1." per versioni vecchie
        if (versionString.startsWith("1.")) {
            // Java 8 o precedente: "1.8.0_xxx" -> 8
            String[] parts = versionString.split("\\.");
            if (parts.length > 1) {
                try {
                    return Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        } else {
            // Java 9+: "17.0.1" o "21" -> 17 o 21
            String[] parts = versionString.split("\\.");
            try {
                return Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        
        return 0;
    }
}
