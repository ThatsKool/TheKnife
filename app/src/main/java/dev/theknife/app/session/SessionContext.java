/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.session;

import dev.theknife.app.model.User;

/**
 * Contratto per lo stato di sessione dell'applicazione.
 * <p>
 * Astrae chi è loggato e operazioni di login/logout. L'implementazione deve essere
 * iniettata tramite costruttore; nessuna classe deve usare singleton globali.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public interface SessionContext {
    // METODI
    
    /**
     * Restituisce l'utente attualmente autenticato.
     * 
     * @return L'utente loggato, oppure null se nessun utente è autenticato.
     */
    User getCurrentUser();
    
    /**
     * Verifica se un utente è attualmente loggato.
     * 
     * @return true se c'è una sessione attiva, false altrimenti.
     */
    boolean isLoggedIn();
    
    /**
     * Restituisce il nome dell'utente corrente per scopi di visualizzazione.
     * 
     * @return Il nome dell'utente, o una stringa vuota/default se non loggato.
     */
    String getCurrentUserName();
    
    /**
     * Imposta l'utente corrente (login).
     * 
     * @param user L'utente da loggare.
     */
    void setCurrentUser(User user);
    
    /**
     * Termina la sessione corrente (logout).
     * <p>
     * Rimuove il riferimento all'utente corrente.
     * </p>
     */
    void logout();
}
