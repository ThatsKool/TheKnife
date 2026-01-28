/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.config;

import java.io.IOException;

/**
 * Classe di configurazione globale dell'applicazione.
 * <p>
 * Contiene costanti statiche per la configurazione dell'ambiente,
 * credenziali di test e flag di debug.
 * Le configurazioni sensibili sono caricate da file di properties esterno.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public final class AppConfig {
    // CAMPI
    /**
     * Flag per abilitare/disabilitare la modalità di debug.
     * Se true, attiva log dettagliati e funzionalità di sviluppo.
     */
    public static final boolean DEBUG;
    
    /**
     * Email per l'utente cliente di test.
     */
    public static final String TEST_CLIENT_EMAIL;
    
    /**
     * Password per l'utente cliente di test.
     */
    public static final String TEST_CLIENT_PASSWORD;
    
    /**
     * Email per l'utente ristoratore di test.
     */
    public static final String TEST_RESTAURATEUR_EMAIL;
    
    /**
     * Password per l'utente ristoratore di test.
     */
    public static final String TEST_RESTAURATEUR_PASSWORD;
    
    // COSTRUTTORI
    static {
        // Hardcoded configuration for simplicity and team sharing
        DEBUG = true;
        TEST_CLIENT_EMAIL = "client@example.com";
        TEST_CLIENT_PASSWORD = "client123";
        TEST_RESTAURATEUR_EMAIL = "resto@example.com";
        TEST_RESTAURATEUR_PASSWORD = "resto123";
    }
    
    /**
     * Costruttore privato per prevenire l'istanziazione.
     * Questa è una classe di utilità/costanti statiche.
     */
    private AppConfig() {
    }

    
}
