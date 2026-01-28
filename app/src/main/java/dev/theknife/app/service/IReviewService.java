/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.model.Review;
import java.util.List;

/**
 * Interfaccia per la gestione delle recensioni dei ristoranti.
 * <p>
 * Definisce il contratto per le operazioni di business logic relative ai feedback degli utenti,
 * inclusa la gestione delle risposte dei ristoratori e dei clienti, il calcolo delle medie
 * e l'analisi della distribuzione dei punteggi.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.model.Review
 * @see dev.theknife.app.service.ReviewService
 */
public interface IReviewService {
    // METODI
    /**
     * Recupera tutte le recensioni presenti nel sistema.
     * 
     * @return Una lista di tutte le recensioni salvate.
     */
    List<Review> getAllReviews();
    
    /**
     * Recupera le recensioni associate a uno specifico ristorante.
     * <p>
     * I risultati dovrebbero essere ordinati cronologicamente (dalla più recente alla più vecchia).
     * </p>
     * 
     * @param restaurantName Il nome del ristorante.
     * @return Una lista di recensioni per il ristorante specificato.
     */
    List<Review> getReviewsForRestaurant(String restaurantName);
    
    /**
     * Recupera le recensioni scritte da uno specifico utente.
     * 
     * @param userName Il nome utente dell'autore.
     * @return Una lista di recensioni scritte dall'utente.
     */
    List<Review> getReviewsByUser(String userName);
    
    /**
     * Aggiunge una nuova recensione al sistema.
     * <p>
     * Questo metodo gestisce la persistenza e l'assegnazione di un identificativo univoco.
     * </p>
     * 
     * @param review L'oggetto {@link Review} da aggiungere.
     * @return {@code true} se l'aggiunta ha successo, {@code false} in caso di validazione fallita o errore I/O.
     */
    boolean addReview(Review review);
    
    /**
     * Calcola la valutazione media di un ristorante.
     * 
     * @param restaurantName Il nome del ristorante.
     * @return La media aritmetica dei punteggi (0.0 se non ci sono recensioni).
     */
    double getAverageRating(String restaurantName);
    
    /**
     * Restituisce il numero totale di recensioni per un ristorante.
     * 
     * @param restaurantName Il nome del ristorante.
     * @return Il numero di recensioni associate.
     */
    int getReviewCount(String restaurantName);
    
    /**
     * Restituisce la distribuzione dei punteggi (rating) per un ristorante.
     * 
     * @param restaurantName Il nome del ristorante.
     * @return Un array di 5 interi dove l'indice {@code i} rappresenta il numero di recensioni
     *         con punteggio {@code i + 1} stelle.
     */
    int[] getRatingDistribution(String restaurantName);
    
    /**
     * Restituisce una rappresentazione testuale formattata della distribuzione dei rating.
     * <p>
     * Utile per la visualizzazione rapida in UI di riepilogo.
     * </p>
     * 
     * @param restaurantName Il nome del ristorante.
     * @return Una stringa formattata (es. "5★: 10, 4★: 5...").
     */
    String getRatingDistributionText(String restaurantName);
    
    /**
     * Verifica se un utente ha già recensito un determinato ristorante (identificato per email).
     * <p>
     * Spesso utilizzato per limitare a una sola recensione per utente/ristorante.
     * </p>
     * 
     * @param userEmail L'email dell'utente.
     * @param restaurantName Il nome del ristorante.
     * @return {@code true} se l'utente ha già lasciato una recensione, {@code false} altrimenti.
     */
    boolean hasUserReviewedRestaurant(String userEmail, String restaurantName);
    
    /**
     * Recupera le recensioni più recenti (es. degli ultimi 30 giorni).
     * 
     * @param limit Numero massimo di recensioni da restituire.
     * @return Una lista di recensioni recenti ordinate per data decrescente.
     */
    List<Review> getRecentReviews(int limit);
    
    /**
     * Identifica i ristoranti con la valutazione media più alta.
     * <p>
     * Solitamente applica una soglia minima di recensioni per evitare bias su singoli voti.
     * </p>
     * 
     * @param limit Numero massimo di ristoranti da restituire.
     * @return Una lista di nomi di ristoranti con i rating migliori.
     */
    List<String> getTopRatedRestaurants(int limit);
    
    /**
     * Ricarica lo stato delle recensioni dalla sorgente dati persistente.
     */
    void refreshReviews();

    /**
     * Aggiorna una recensione esistente. Verifica che {@code requestorEmail} sia l'autore.
     *
     * @param review L'oggetto recensione con i dati aggiornati (deve avere un ID valido).
     * @param requestorEmail Email dell'utente che richiede l'aggiornamento (deve essere il proprietario).
     * @return {@code true} se l'aggiornamento ha successo, {@code false} altrimenti.
     */
    boolean updateReview(Review review, String requestorEmail);

    /**
     * Elimina una recensione tramite il suo ID. Verifica che {@code requestorEmail} sia l'autore.
     *
     * @param reviewId L'identificativo univoco della recensione.
     * @param requestorEmail Email dell'utente che richiede l'eliminazione (deve essere il proprietario).
     * @return {@code true} se l'eliminazione ha successo, {@code false} altrimenti.
     */
    boolean deleteReview(String reviewId, String requestorEmail);

    /**
     * Cerca una singola recensione tramite il suo ID.
     * 
     * @param reviewId L'identificativo univoco.
     * @return L'istanza di {@link Review} se trovata, altrimenti {@code null}.
     */
    Review findReviewById(String reviewId);

    /**
     * Aggiunge una risposta ufficiale del ristoratore a una recensione.
     * Verifica che {@code requestorEmail} sia il ristoratore proprietario del ristorante recensito.
     *
     * @param reviewId L'ID della recensione a cui rispondere.
     * @param response Il testo della risposta.
     * @param requestorEmail Email del ristoratore che risponde (deve essere proprietario del ristorante).
     * @return {@code true} se l'operazione ha successo.
     */
    boolean addRestaurateurResponse(String reviewId, String response, String requestorEmail);

    /**
     * Aggiunge una contro-risposta del cliente alla risposta del ristoratore.
     * Verifica che {@code requestorEmail} sia l'autore della recensione.
     *
     * @param reviewId L'ID della recensione.
     * @param response Il testo della contro-risposta.
     * @param requestorEmail Email del cliente (deve essere autore della recensione).
     * @return {@code true} se l'operazione ha successo.
     */
    boolean addClientResponse(String reviewId, String response, String requestorEmail);
}
