/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.util;

/**
 * Utilità di logging semplice e leggera.
 * <p>
 * Fornisce funzionalità di logging di base (INFO, ERROR, DEBUG, WARN) senza dipendenze esterne pesanti.
 * Supporta la configurazione del livello di debug tramite property di sistema.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class Logger {
    // CAMPI
    private final String className;
    
    // COSTRUTTORI
    /**
     * Costruttore privato per creare un'istanza di Logger.
     * <p>
     * L'istanziazione avviene tramite il metodo statico {@link #getLogger(Class)}.
     * </p>
     *
     * @param className Il nome della classe per cui creare il logger.
     */
    private Logger(String className) {
        this.className = className;
    }
    
    // METODI
    /**
     * Crea un logger per una specifica classe.
     * 
     * @param clazz La classe per cui si vuole loggare.
     * @return Un'istanza di Logger configurata con il nome della classe.
     */
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getSimpleName());
    }
    
    /**
     * Logga un messaggio informativo (INFO).
     * 
     * @param message Il messaggio da loggare.
     */
    public void info(String message) {
        System.out.println("[INFO] [" + className + "] " + message);
    }
    
    /**
     * Logga un messaggio di errore (ERROR).
     * 
     * @param message Il messaggio di errore.
     */
    public void error(String message) {
        System.err.println("[ERROR] [" + className + "] " + message);
    }
    
    /**
     * Logga un messaggio di errore con la relativa eccezione (ERROR).
     * 
     * @param message Il messaggio di errore.
     * @param throwable L'eccezione catturata.
     */
    public void error(String message, Throwable throwable) {
        System.err.println("[ERROR] [" + className + "] " + message);
        java.io.StringWriter sw = new java.io.StringWriter();
        throwable.printStackTrace(new java.io.PrintWriter(sw));
        System.err.println(sw.toString());
    }
    
    /**
     * Logga un messaggio di debug (DEBUG).
     * <p>
     * Il messaggio viene stampato solo se la property di sistema "app.debug" è true.
     * </p>
     * 
     * @param message Il messaggio di debug.
     */
    public void debug(String message) {
        // Only log debug in development - can be controlled via system property
        if (Boolean.getBoolean("app.debug")) {
            System.out.println("[DEBUG] [" + className + "] " + message);
        }
    }
    
    /**
     * Logga un messaggio di avviso (WARN).
     * 
     * @param message Il messaggio di avviso.
     */
    public void warn(String message) {
        System.out.println("[WARN] [" + className + "] " + message);
    }
}

