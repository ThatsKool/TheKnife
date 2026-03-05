/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.model.Restaurant;
import dev.theknife.app.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servizio specializzato per le operazioni di lettura, ricerca,
 * filtraggio e paginazione dei ristoranti.
 * <p>
 * Incapsula la logica di query sulla sorgente dati gestita da {@link RestaurantService},
 * mantenendo separati i casi d'uso di lettura dalle operazioni di gestione (creazione,
 * modifica, persistenza).
 * </p>
 */
public class RestaurantQueryService {

    private final Logger logger;
    private final RestaurantService restaurantService;

    /**
     * Crea una nuova istanza del servizio di query.
     *
     * @param restaurantService servizio di gestione ristoranti che espone la sorgente dati in memoria.
     */
    public RestaurantQueryService(RestaurantService restaurantService) {
        this.logger = Logger.getLogger(RestaurantQueryService.class);
        this.restaurantService = restaurantService;
    }

    /**
     * Restituisce una porzione paginata della lista di ristoranti.
     * <p>
     * L'implementazione è identica a quella originaria in {@link RestaurantService}
     * per garantire la totale compatibilità comportamentale.
     * </p>
     *
     * @param offset indice iniziale (0-based).
     * @param limit  numero massimo di elementi da restituire.
     * @return Lista di ristoranti nella pagina richiesta.
     */
    public List<Restaurant> getRestaurantsRange(int offset, int limit) {
        List<Restaurant> restaurants = restaurantService.getAllRestaurantsInternal();

        if (limit <= 0 || offset >= restaurants.size()) {
            return Collections.emptyList();
        }
        int end = Math.min(offset + limit, restaurants.size());
        return new ArrayList<>(restaurants.subList(offset, end));
    }

    /**
     * Restituisce il numero totale di ristoranti disponibili.
     *
     * @return conteggio totale dei ristoranti.
     */
    public int getTotalRestaurantCount() {
        return restaurantService.getAllRestaurantsInternal().size();
    }

    /**
     * Esegue una ricerca full-text con paginazione sui ristoranti.
     * <p>
     * L'algoritmo replica esattamente la precedente implementazione di
     * {@code RestaurantService#searchRestaurantsRange} per non alterare
     * il comportamento osservabile.
     * </p>
     *
     * @param searchTerm termine di ricerca (può essere null o vuoto).
     * @param offset     indice iniziale della pagina (0-based).
     * @param limit      numero massimo di risultati nella pagina.
     * @return Lista di ristoranti corrispondenti ai criteri, limitata alla pagina richiesta.
     */
    public List<Restaurant> searchRestaurantsRange(String searchTerm, int offset, int limit) {
        List<Restaurant> restaurants = restaurantService.getAllRestaurantsInternal();

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getRestaurantsRange(offset, limit);
        }

        String term = searchTerm.toLowerCase().trim();
        List<Restaurant> allMatches = restaurants.stream()
                .filter(r -> r != null && matchesSearchTerm(r, term))
                .collect(Collectors.toList());

        int startIndex = Math.min(offset, allMatches.size());
        int endIndex = Math.min(offset + limit, allMatches.size());

        List<Restaurant> pageResults = startIndex < endIndex
                ? allMatches.subList(startIndex, endIndex)
                : new ArrayList<>();

        logger.debug("Search found " + allMatches.size() + " total matches for '" + searchTerm +
                "', returning page " + (limit == 0 ? 1 : (offset / Math.max(1, limit) + 1)) +
                " with " + pageResults.size() + " results");
        return pageResults;
    }

    /**
     * Verifica se un ristorante corrisponde ai criteri di ricerca.
     * <p>
     * Logica estratta da {@link RestaurantService} per centralizzare il filtraggio
     * relativo alle liste.
     * </p>
     *
     * @param restaurant Il ristorante da verificare.
     * @param term       Il termine di ricerca (già normalizzato in lowercase).
     * @return {@code true} se c'è corrispondenza, {@code false} altrimenti.
     */
    private boolean matchesSearchTerm(Restaurant restaurant, String term) {
        return (restaurant.getName() != null && restaurant.getName().toLowerCase().contains(term))
                || (restaurant.getCuisine() != null && restaurant.getCuisine().toLowerCase().contains(term))
                || (restaurant.getLocation() != null && restaurant.getLocation().toLowerCase().contains(term))
                || (restaurant.getAward() != null && restaurant.getAward().toLowerCase().contains(term));
    }
}

