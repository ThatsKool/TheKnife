/*
 * Autori :
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.auth;

import dev.theknife.app.model.User;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.Logger;

/**
 * Implementazione di {@link SessionContext}: gestore dello stato di sessione.
 * <p>
 * L'istanza va creata nel composition root e iniettata come {@link SessionContext};
 * non usare {@link #getInstance()} in produzione.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see User
 * @see SessionContext
 */
public class SessionManager implements SessionContext {
    // CAMPI
    private static final SessionManager instance = new SessionManager();
    private final Logger logger;
    
    /**
     * L'utente attualmente autenticato nel sistema.
     * Null se nessun utente è loggato.
     */
    private User currentUser;
    
    // COSTRUTTORI
    /**
     * Costruttore privato per pattern Singleton.
     * <p>
     * Inizializza il logger e prepara il gestore di sessione.
     * </p>
     */
    private SessionManager() {
        this.logger = Logger.getLogger(SessionManager.class);
    }
    
    // METODI
    /**
     * Restituisce l'istanza unica del SessionManager.
     * 
     * @return L'istanza singleton.
     */
    public static SessionManager getInstance() {
        return instance;
    }
    
    /**
     * Imposta l'utente corrente (Login).
     * <p>
     * Registra l'evento di login o logout nei log di sistema.
     * </p>
     * 
     * @param user L'utente da impostare come loggato, o null per effettuare il logout.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            logger.info("User logged in");
        } else {
            logger.info("User logged out");
        }
    }
    
    /**
     * Recupera l'utente attualmente loggato.
     * 
     * @return L'oggetto {@link User} corrente, o null se non autenticato.
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Verifica se esiste una sessione attiva.
     * 
     * @return true se un utente è loggato, false altrimenti.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Recupera il nome dell'utente corrente per scopi di visualizzazione.
     * 
     * @return Il nome dell'utente, o null se non loggato.
     */
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getName() : null;
    }
    
    /**
     * Termina la sessione corrente (Logout).
     * <p>
     * Rimuove il riferimento all'utente corrente e logga l'operazione.
     * </p>
     */
    public void logout() {
        if (currentUser != null) {
            logger.info("Logging out user");
        }
        this.currentUser = null;
    }
}

