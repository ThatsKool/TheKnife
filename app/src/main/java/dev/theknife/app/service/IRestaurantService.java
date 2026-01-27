/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.model.Restaurant;
import java.util.List;

/**
 * Definisce il contratto per le operazioni di business logic relative ai Ristoranti.
 * <p>
 * Questa interfaccia applica il pattern <b>Service Layer</b> per disaccoppiare la logica
 * di presentazione (Controller) dalla persistenza dei dati. Include funzionalità avanzate
 * come il caricamento pigro (Lazy Loading), la paginazione, la ricerca full-text e
 * query geospaziali.
 * </p>
 * <p>
 * Le implementazioni di questa interfaccia devono garantire l'efficienza nella gestione
 * di grandi dataset, evitando il caricamento in memoria dell'intero database.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.model.Restaurant
 * @see dev.theknife.app.service.RestaurantService
 */
public interface IRestaurantService {
    
    // METODI
    /**
     * Recupera un sottoinsieme paginato di ristoranti utilizzando il caricamento pigro.
     * <p>
     * Questo metodo è fondamentale per ottimizzare le prestazioni dell'interfaccia utente
     * (es. infinite scrolling o paginazione numerata), permettendo di caricare i dati
     * in blocchi (chunk) invece che in un'unica soluzione.
     * </p>
     *
     * @param offset Indice base zero del primo record da caricare (skip).
     * @param limit Numero massimo di record da restituire nel blocco corrente.
     * @return Una lista immutabile o modificabile di ristoranti nel range richiesto.
     *         Ritorna una lista vuota se l'offset supera il numero totale di record.
     */
    List<Restaurant> getRestaurantsRange(int offset, int limit);
    
    /**
     * Restituisce il numero totale di ristoranti presenti nel sistema.
     * <p>
     * Questa operazione deve essere efficiente e non deve richiedere il parsing completo
     * di tutti i record se non strettamente necessario (es. lettura metadata o count ottimizzato).
     * </p>
     *
     * @return Il conteggio totale dei record di tipo Ristorante.
     */
    int getTotalRestaurantCount();
    
    /**
     * Cerca ristoranti che corrispondono a un termine di ricerca, con supporto alla paginazione.
     * <p>
     * La ricerca viene effettuata tipicamente su campi chiave come nome, cucina o descrizione.
     * Il matching dovrebbe essere case-insensitive e gestire input parziali.
     * </p>
     *
     * @param searchTerm Il termine da cercare (può essere null o vuoto, nel qual caso
     *                   il comportamento può variare, es. ritornare tutti i risultati o nessuno).
     * @param offset Indice base zero del primo risultato corrispondente da restituire.
     * @param limit Numero massimo di risultati corrispondenti da restituire.
     * @return Una lista di ristoranti che soddisfano i criteri di ricerca nel range specificato.
     */
    List<Restaurant> searchRestaurantsRange(String searchTerm, int offset, int limit);
    
    /**
     * Cerca un singolo ristorante tramite il suo identificativo univoco (ID).
     *
     * @param restaurantId L'identificativo univoco del ristorante da cercare.
     * @return L'istanza di {@link Restaurant} se trovata, altrimenti {@code null}.
     */
    Restaurant findRestaurantById(Long restaurantId);
    
    /**
     * Cerca un singolo ristorante tramite il suo nome esatto.
     * <p>
     * Nota: Se esistono più ristoranti con lo stesso nome, il comportamento è definito
     * dall'implementazione (es. ritorna il primo trovato o lancia un'eccezione).
     * Si consiglia l'uso di {@link #findRestaurantById(Long)} per univocità garantita.
     * </p>
     *
     * @param restaurantName Il nome del ristorante da cercare.
     * @return L'istanza di {@link Restaurant} se trovata, altrimenti {@code null}.
     */
    Restaurant findRestaurantByName(String restaurantName);
    
    /**
     * Formatta i dettagli di un ristorante in una stringa leggibile.
     * <p>
     * Utile per log, debug o visualizzazione rapida in console/UI testuale.
     * </p>
     *
     * @param restaurant L'istanza di ristorante da formattare.
     * @return Una stringa contenente una rappresentazione formattata dei dettagli principali.
     */
    String formatRestaurantDetails(Restaurant restaurant);
    
    /**
     * Genera una descrizione troncata per la visualizzazione in schede (Card) o liste.
     * <p>
     * Se la descrizione originale supera una certa lunghezza predefinita, viene tagliata
     * e suffissa con "..." per preservare il layout dell'interfaccia grafica.
     * </p>
     *
     * @param restaurant Il ristorante di cui troncare la descrizione.
     * @return La descrizione troncata o l'intera descrizione se breve.
     */
    String getTruncatedDescription(Restaurant restaurant);
    
    /**
     * Aggiunge un nuovo ristorante al sistema.
     * <p>
     * Questo metodo gestisce la persistenza del nuovo oggetto. L'implementazione
     * dovrebbe gestire l'assegnazione automatica dell'ID se non presente.
     * </p>
     *
     * @param restaurant L'oggetto {@link Restaurant} da salvare.
     * @return {@code true} se l'operazione ha successo, {@code false} in caso di errore
     *         (es. validazione fallita, errore I/O).
     */
    boolean addRestaurant(Restaurant restaurant);
    
    /**
     * Recupera tutti i ristoranti di proprietà di uno specifico ristoratore.
     *
     * @param restaurateurEmail L'email del ristoratore, utilizzata come chiave esterna logica.
     * @return Una lista di ristoranti associati all'email fornita. Lista vuota se nessuno trovato.
     */
    List<Restaurant> getRestaurantsByRestaurateur(String restaurateurEmail);

    /**
     * Cerca ristoranti situati entro un determinato raggio geografico rispetto a una posizione.
     * <p>
     * Implementa una query geospaziale (es. formula dell'Haversine) per filtrare i risultati
     * in base alla distanza.
     * </p>
     *
     * @param userLat Latitudine della posizione dell'utente (gradi decimali).
     * @param userLon Longitudine della posizione dell'utente (gradi decimali).
     * @param radiusKm Raggio di ricerca in chilometri.
     * @return Una lista di ristoranti che si trovano all'interno del raggio specificato.
     */
    List<Restaurant> findNearby(double userLat, double userLon, double radiusKm);
}

