/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.Restaurant;
import dev.theknife.app.model.Review;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.IReviewService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.Logger;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ViewModel per la schermata di dettaglio del ristorante.
 * <p>
 * Implementa il pattern MVVM (Model-View-ViewModel) per separare la logica di presentazione
 * dalla View. Gestisce lo stato reattivo della schermata dei dettagli, inclusi i dati del ristorante
 * e le recensioni associate.
 * </p>
 * <p>
 * <b>Caratteristiche principali:</b>
 * <ul>
 *   <li>Caricamento asincrono dei dati per non bloccare l'UI thread (JavaFX Application Thread).</li>
 *   <li>Binding bidirezionale o unidirezionale tramite Property JavaFX.</li>
 *   <li>Gestione dello stato di "loading" e della visibilità dei controlli (es. pulsante aggiungi recensione).</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.view.RestaurantDetailsView
 */
public class RestaurantDetailsViewModel {
    // CAMPI
    private final IRestaurantService restaurantService;
    private final IReviewService reviewService;
    private final SessionContext sessionContext;
    private final Logger logger;
    
    // Properties esposte alla View
    private final ObservableList<Review> reviews;
    private final StringProperty restaurantName;
    private final StringProperty restaurantAddress;
    private final StringProperty restaurantLocation;
    private final StringProperty restaurantPrice;
    private final StringProperty restaurantCuisine;
    private final StringProperty restaurantPhone;
    private final StringProperty restaurantWebsite;
    private final StringProperty restaurantAward;
    private final StringProperty restaurantDescription;
    private final StringProperty averageRating;
    private final StringProperty reviewCount;
    private final BooleanProperty isLoading;
    private final BooleanProperty hasReviews;
    private final BooleanProperty canAddReview;
    
    // Stato interno
    private Restaurant currentRestaurant;
    private String currentUserName;
    
    // COSTRUTTORI
    /**
     * Costruisce il ViewModel iniettando i servizi e il contesto di sessione.
     *
     * @param restaurantService Servizio per il recupero dei dati del ristorante.
     * @param reviewService Servizio per la gestione delle recensioni.
     * @param sessionContext Contesto di sessione (nessun getInstance).
     */
    public RestaurantDetailsViewModel(IRestaurantService restaurantService, IReviewService reviewService, SessionContext sessionContext) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
        this.sessionContext = sessionContext;
        this.logger = Logger.getLogger(RestaurantDetailsViewModel.class);
        
        // Inizializzazione properties
        this.reviews = FXCollections.observableArrayList();
        this.restaurantName = new SimpleStringProperty("");
        this.restaurantAddress = new SimpleStringProperty("");
        this.restaurantLocation = new SimpleStringProperty("");
        this.restaurantPrice = new SimpleStringProperty("");
        this.restaurantCuisine = new SimpleStringProperty("");
        this.restaurantPhone = new SimpleStringProperty("");
        this.restaurantWebsite = new SimpleStringProperty("");
        this.restaurantAward = new SimpleStringProperty("");
        this.restaurantDescription = new SimpleStringProperty("");
        this.averageRating = new SimpleStringProperty("0.0");
        this.reviewCount = new SimpleStringProperty("0");
        this.isLoading = new SimpleBooleanProperty(false);
        this.hasReviews = new SimpleBooleanProperty(false);
        this.canAddReview = new SimpleBooleanProperty(false);
    }
    
    // METODI
    /**
     * Avvia il caricamento dei dettagli del ristorante e delle recensioni.
     * <p>
     * L'operazione avviene in background tramite {@link CompletableFuture} per mantenere
     * l'interfaccia utente reattiva. Al termine, aggiorna le Property nel thread JavaFX.
     * </p>
     *
     * @param restaurantName Il nome del ristorante da caricare.
     * @param userName Il nome dell'utente corrente (opzionale, se null viene recuperato dalla sessione).
     */
    public void loadRestaurantDetails(String restaurantName, String userName) {
        if (userName == null) {
            this.currentUserName = sessionContext.getCurrentUserName();
        } else {
            this.currentUserName = userName;
        }
        
        isLoading.set(true);
        
        // Esecuzione asincrona
        CompletableFuture.runAsync(() -> {
            try {
                // Recupero del ristorante (Lazy Loading simulato se necessario)
                currentRestaurant = restaurantService.findRestaurantByName(restaurantName);
                
                // Aggiornamento UI nel thread JavaFX
                javafx.application.Platform.runLater(() -> {
                    if (currentRestaurant != null) {
                        updateRestaurantProperties();
                        loadReviews();
                        updateCanAddReview();
                    }
                    isLoading.set(false);
                });
            } catch (Exception e) {
                logger.error("Error loading restaurant details: " + e.getMessage(), e);
                javafx.application.Platform.runLater(() -> {
                    isLoading.set(false);
                });
            }
        });
    }
    
    /**
     * Aggiorna le Property relative ai dati statici del ristorante.
     */
    private void updateRestaurantProperties() {
        if (currentRestaurant == null) return;
        
        restaurantName.set(currentRestaurant.getName());
        restaurantAddress.set(currentRestaurant.getAddress());
        restaurantLocation.set(currentRestaurant.getLocation());
        restaurantPrice.set(currentRestaurant.getPrice());
        restaurantCuisine.set(currentRestaurant.getCuisine());
        restaurantPhone.set(currentRestaurant.getPhoneNumber() != null ? 
                          currentRestaurant.getPhoneNumber() : "Non disponibile");
        restaurantWebsite.set(currentRestaurant.getWebsiteUrl() != null && 
                             !currentRestaurant.getWebsiteUrl().isEmpty() ? 
                             currentRestaurant.getWebsiteUrl() : "Non disponibile");
        restaurantAward.set(currentRestaurant.getAward() != null ? 
                           currentRestaurant.getAward() : "Nessun riconoscimento");
        restaurantDescription.set(currentRestaurant.getDescription());
    }
    
    /**
     * Carica le recensioni per il ristorante corrente e aggiorna la lista osservabile.
     */
    private void loadReviews() {
        if (currentRestaurant == null) return;
        
        List<Review> restaurantReviews = reviewService.getReviewsForRestaurant(currentRestaurant.getName());
        reviews.clear();
        reviews.addAll(restaurantReviews);
        
        updateReviewStatistics();
    }
    
    /**
     * Calcola e aggiorna le statistiche delle recensioni (media, conteggio).
     */
    private void updateReviewStatistics() {
        if (currentRestaurant == null) return;
        
        int count = reviewService.getReviewCount(currentRestaurant.getName());
        double average = reviewService.getAverageRating(currentRestaurant.getName());
        
        reviewCount.set(String.valueOf(count));
        averageRating.set(String.format("%.1f", average));
        hasReviews.set(count > 0);
    }
    
    /**
     * Determina se l'utente corrente può aggiungere una recensione.
     * <p>
     * Regola: Un utente può recensire solo se loggato e se non ha già recensito questo ristorante.
     * </p>
     */
    private void updateCanAddReview() {
        if (currentRestaurant == null || currentUserName == null) {
            canAddReview.set(false);
            return;
        }
        
        boolean hasReviewed = reviewService.hasUserReviewedRestaurant(currentUserName, currentRestaurant.getName());
        canAddReview.set(!hasReviewed);
    }
    
    /**
     * Ricarica le recensioni forzando un aggiornamento dal servizio.
     * Utile dopo l'aggiunta di una nuova recensione.
     */
    public void refreshReviews() {
        if (currentRestaurant != null) {
            reviewService.refreshReviews();
            loadReviews();
            updateCanAddReview();
        }
    }
    
    /**
     * Restituisce la distribuzione dei voti (1-5 stelle) per il grafico.
     *
     * @return Un array di interi dove l'indice i corrisponde a i+1 stelle.
     */
    public int[] getRatingDistribution() {
        if (currentRestaurant == null) return new int[5];
        return reviewService.getRatingDistribution(currentRestaurant.getName());
    }
    
    /**
     * Restituisce una rappresentazione testuale della distribuzione dei voti.
     *
     * @return Stringa formattata con il conteggio per ogni stella.
     */
    public String getRatingDistributionText() {
        if (currentRestaurant == null) return "";
        return reviewService.getRatingDistributionText(currentRestaurant.getName());
    }
    
    /**
     * Restituisce l'oggetto ristorante corrente.
     *
     * @return Il ristorante visualizzato.
     */
    public Restaurant getCurrentRestaurant() {
        return currentRestaurant;
    }
    
    /**
     * Restituisce il nome dell'utente corrente.
     *
     * @return Il nome utente o null se non loggato.
     */
    public String getCurrentUserName() {
        return currentUserName;
    }

    /**
     * Elimina una recensione. Verifica ownership via {@link SessionContext}.
     * Dopo l'eliminazione esegue {@link #refreshReviews()}.
     *
     * @param review La recensione da eliminare.
     * @return {@code true} se eliminata, {@code false} altrimenti.
     */
    public boolean deleteReview(Review review) {
        if (review == null || review.getId() == null) return false;
        var user = sessionContext.getCurrentUser();
        String email = user != null ? user.getEmail() : null;
        boolean ok = reviewService.deleteReview(review.getId(), email);
        if (ok) refreshReviews();
        return ok;
    }

    /**
     * Aggiunge la risposta del ristoratore a una recensione.
     *
     * @param reviewId ID recensione.
     * @param response Testo risposta.
     * @return {@code true} se successo.
     */
    public boolean addRestaurateurResponse(String reviewId, String response) {
        var user = sessionContext.getCurrentUser();
        String email = user != null ? user.getEmail() : null;
        return reviewService.addRestaurateurResponse(reviewId, response, email);
    }

    /**
     * Aggiunge la contro-risposta del cliente.
     *
     * @param reviewId ID recensione.
     * @param response Testo risposta.
     * @return {@code true} se successo.
     */
    public boolean addClientResponse(String reviewId, String response) {
        var user = sessionContext.getCurrentUser();
        String email = user != null ? user.getEmail() : null;
        return reviewService.addClientResponse(reviewId, response, email);
    }
    
    // --- Metodi getter per le Property (Binding JavaFX) ---

    /**
     * Restituisce la lista osservabile delle recensioni.
     * <p>
     * Utilizzata per il binding con i componenti UI (es. ListView) che visualizzano
     * le recensioni del ristorante.
     * </p>
     *
     * @return La lista osservabile delle recensioni.
     */
    public ObservableList<Review> getReviews() {
        return reviews;
    }
    
    /**
     * Restituisce la property del nome del ristorante.
     *
     * @return Property contenente il nome.
     */
    public StringProperty restaurantNameProperty() {
        return restaurantName;
    }
    
    /**
     * Restituisce la property dell'indirizzo.
     *
     * @return Property contenente l'indirizzo.
     */
    public StringProperty restaurantAddressProperty() {
        return restaurantAddress;
    }
    
    /**
     * Restituisce la property della posizione (Città/Zona).
     *
     * @return Property contenente la posizione.
     */
    public StringProperty restaurantLocationProperty() {
        return restaurantLocation;
    }
    
    /**
     * Restituisce la property del prezzo.
     *
     * @return Property contenente il prezzo (es. "$$$").
     */
    public StringProperty restaurantPriceProperty() {
        return restaurantPrice;
    }
    
    /**
     * Restituisce la property della cucina.
     *
     * @return Property contenente il tipo di cucina.
     */
    public StringProperty restaurantCuisineProperty() {
        return restaurantCuisine;
    }
    
    /**
     * Restituisce la property del numero di telefono.
     *
     * @return Property contenente il numero di telefono.
     */
    public StringProperty restaurantPhoneProperty() {
        return restaurantPhone;
    }
    
    /**
     * Restituisce la property del sito web.
     *
     * @return Property contenente l'URL del sito web.
     */
    public StringProperty restaurantWebsiteProperty() {
        return restaurantWebsite;
    }
    
    /**
     * Restituisce la property del premio/riconoscimento.
     *
     * @return Property contenente il premio (es. "1 Stella Michelin").
     */
    public StringProperty restaurantAwardProperty() {
        return restaurantAward;
    }
    
    /**
     * Restituisce la property della descrizione.
     *
     * @return Property contenente la descrizione.
     */
    public StringProperty restaurantDescriptionProperty() {
        return restaurantDescription;
    }
    
    /**
     * Restituisce la property del voto medio.
     *
     * @return Property contenente il voto medio formattato (es. "4.5").
     */
    public StringProperty averageRatingProperty() {
        return averageRating;
    }
    
    /**
     * Restituisce la property del conteggio recensioni.
     *
     * @return Property contenente il numero di recensioni.
     */
    public StringProperty reviewCountProperty() {
        return reviewCount;
    }
    
    /**
     * Restituisce la property di stato caricamento.
     *
     * @return Property booleana, true se in caricamento.
     */
    public BooleanProperty isLoadingProperty() {
        return isLoading;
    }
    
    /**
     * Restituisce la property che indica la presenza di recensioni.
     *
     * @return Property booleana, true se ci sono recensioni.
     */
    public BooleanProperty hasReviewsProperty() {
        return hasReviews;
    }
    
    /**
     * Restituisce la property che indica se l'utente può aggiungere una recensione.
     *
     * @return Property booleana, true se l'utente può recensire.
     */
    public BooleanProperty canAddReviewProperty() {
        return canAddReview;
    }
}
