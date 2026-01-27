/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.model.User;
import java.io.IOException;
import java.util.List;

/**
 * Interfaccia per la gestione degli utenti e dell'autenticazione.
 * <p>
 * Definisce il contratto per le operazioni relative al ciclo di vita degli utenti,
 * inclusa la registrazione, il login (validazione credenziali) e il recupero dei profili.
 * </p>
 * <p>
 * <b>Principi architetturali:</b>
 * <ul>
 *   <li>Separazione tra definizione (Interfaccia) e implementazione (Service).</li>
 *   <li>Supporto per la migrazione da un sistema legacy (basato su nome) a uno moderno (basato su email).</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.model.User
 * @see dev.theknife.app.service.UserService
 */
public interface IUserService {
    // METODI
    /**
     * Valida le credenziali di accesso di un utente.
     * <p>
     * Verifica che la coppia email/password corrisponda a un utente registrato.
     * La verifica della password deve tenere conto dell'hashing sicuro.
     * </p>
     * 
     * @param email L'email dell'utente (case-insensitive).
     * @param password La password in chiaro fornita dall'utente.
     * @return {@code true} se le credenziali sono valide, {@code false} altrimenti.
     * @throws IOException Se si verificano errori durante l'accesso ai dati (es. lettura file).
     */
    boolean validateCredentials(String email, String password) throws IOException;
    
    /**
     * Cerca un utente tramite il suo indirizzo email.
     * <p>
     * È il metodo primario per il recupero dei profili utente nel nuovo sistema.
     * </p>
     * 
     * @param email L'indirizzo email da cercare.
     * @return L'oggetto {@link User} se trovato, {@code null} se nessun utente corrisponde.
     * @throws IOException Se si verificano errori di I/O.
     */
    User findUserByEmail(String email) throws IOException;
    
    
    /**
     * Verifica se un indirizzo email è già registrato nel sistema.
     * <p>
     * Utile in fase di registrazione per prevenire duplicati.
     * </p>
     * 
     * @param email L'email da verificare.
     * @return {@code true} se l'email esiste già, {@code false} se è disponibile.
     * @throws IOException Se si verificano errori di I/O.
     */
    boolean emailExists(String email) throws IOException;
    
    /**
     * Registra o aggiorna un utente nel sistema.
     * <p>
     * Se l'utente è nuovo, viene aggiunto. Se esiste già (basandosi sull'email),
     * i suoi dati potrebbero essere sovrascritti o aggiornati a seconda dell'implementazione.
     * </p>
     * 
     * @param user L'oggetto {@link User} da salvare.
     * @throws IOException Se si verificano errori di scrittura o persistenza.
     */
    void saveUser(User user) throws IOException;

    /**
     * Aggiorna i dati di un utente esistente.
     * <p>
     * Sostituisce l'utente identificato dall'email con i nuovi dati forniti.
     * </p>
     *
     * @param user Il nuovo oggetto utente (l'email deve corrispondere a un utente esistente).
     * @throws IOException Se l'utente non esiste o se si verificano errori di persistenza.
     */
    void updateUser(User user) throws IOException;
    
    /**
     * Recupera la lista completa di tutti gli utenti registrati.
     * 
     * @return Una lista contenente tutti gli utenti.
     * @throws IOException Se si verificano errori di I/O.
     */
    List<User> loadUsers() throws IOException;
}
