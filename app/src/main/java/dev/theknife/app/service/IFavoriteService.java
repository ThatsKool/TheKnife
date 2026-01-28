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
     * Persistenza in locale usa email utente e ID ristorante. Se l'associazione esiste già,
     * l'operazione è idempotente (restituisce true senza duplicare).
     * </p>
     *
     * @param userEmail Email univoca dell'utente.
     * @param restaurantId ID univoco del ristorante.
     * @return {@code true} se l'aggiunta ha successo o se la relazione esisteva già;
     *         {@code false} in caso di errori di persistenza o parametri non validi.
     */
    boolean addFavorite(String userEmail, Long restaurantId);
    
    /**
     * Rimuove un ristorante dalla lista dei preferiti di un utente.
     *
     * @param userEmail Email utente.
     * @param restaurantId ID del ristorante da rimuovere.
     * @return {@code true} se la rimozione ha successo; {@code false} se la relazione non esisteva
     *         o in caso di errore.
     */
    boolean removeFavorite(String userEmail, Long restaurantId);
    
    /**
     * Verifica se un ristorante è tra i preferiti di un utente.
     *
     * @param userEmail Email utente.
     * @param restaurantId ID del ristorante.
     * @return {@code true} se è un preferito, {@code false} altrimenti.
     */
    boolean isFavorite(String userEmail, Long restaurantId);
    
    /**
     * Recupera gli ID di tutti i ristoranti preferiti di un utente.
     *
     * @param userEmail Email utente.
     * @return Lista di ID ristoranti preferiti.
     */
    List<Long> getUserFavoriteIds(String userEmail);
    
    /**
     * Recupera le entità {@link FavoriteRestaurant} associate a un utente (email + restaurantId).
     *
     * @param userEmail Email utente.
     * @return Lista di {@link FavoriteRestaurant} con userEmail e restaurantId impostati.
     */
    List<FavoriteRestaurant> getUserFavoriteRestaurants(String userEmail);
}

