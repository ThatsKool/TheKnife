/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.model.FavoriteRestaurant;
import java.util.List;

/**
 * Interfaccia per la gestione dei ristoranti preferiti dagli utenti.
 * <p>
 * Definisce le operazioni CRUD (Create, Read, Delete) per la relazione molti-a-molti
 * tra Utenti e Ristoranti (rappresentata logicamente, anche se non vi è un DB relazionale).
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.model.FavoriteRestaurant
 * @see dev.theknife.app.service.FavoriteService
 */
public interface IFavoriteService {
    
    // METODI
    /**
     * Aggiunge un ristorante alla lista dei preferiti di un utente.
     * <p>
     * Se l'associazione esiste già, l'operazione dovrebbe essere idempotente (restituire true
     * senza duplicare i dati).
     * </p>
     *
     * @param userName Il nome utente (identificativo univoco dell'utente).
     * @param restaurantName Il nome del ristorante (identificativo del ristorante in questo contesto legacy).
     * @return {@code true} se l'aggiunta ha successo o se la relazione esisteva già;
     *         {@code false} in caso di errori di persistenza o parametri non validi.
     */
    boolean addFavorite(String userName, String restaurantName);
    
    /**
     * Rimuove un ristorante dalla lista dei preferiti di un utente.
     *
     * @param userName Il nome utente.
     * @param restaurantName Il nome del ristorante da rimuovere.
     * @return {@code true} se la rimozione ha successo; {@code false} se la relazione non esisteva
     *         o in caso di errore.
     */
    boolean removeFavorite(String userName, String restaurantName);
    
    /**
     * Verifica se un ristorante è tra i preferiti di un utente.
     * <p>
     * Utilizzato per aggiornare lo stato dell'UI (es. icona cuore pieno/vuoto).
     * </p>
     *
     * @param userName Il nome utente.
     * @param restaurantName Il nome del ristorante.
     * @return {@code true} se è un preferito, {@code false} altrimenti.
     */
    boolean isFavorite(String userName, String restaurantName);
    
    /**
     * Recupera i nomi di tutti i ristoranti preferiti di un utente.
     *
     * @param userName Il nome utente.
     * @return Una lista di stringhe contenente i nomi dei ristoranti preferiti.
     */
    List<String> getUserFavorites(String userName);
    
    /**
     * Recupera le entità {@link FavoriteRestaurant} associate a un utente.
     * <p>
     * Rispetto a {@link #getUserFavorites(String)}, questo metodo restituisce l'oggetto
     * completo che potrebbe contenere metadati aggiuntivi sulla relazione (es. timestamp aggiunta, note).
     * </p>
     *
     * @param userName Il nome utente.
     * @return Una lista di oggetti {@link FavoriteRestaurant}.
     */
    List<FavoriteRestaurant> getUserFavoriteRestaurants(String userName);
}

