/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.Restaurant;
import dev.theknife.app.model.Review;
import dev.theknife.app.model.User;
import dev.theknife.app.service.IFavoriteService;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.IReviewService;
import dev.theknife.app.service.IUserService;
import dev.theknife.app.session.SessionContext;

import java.io.IOException;
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

    public enum FavoriteButtonStyle {
        ADD,
        REMOVE
    }

    public enum AddReviewPermission {
        ALLOWED,
        NOT_LOGGED_IN,
        RESTAURATEUR_BLOCKED
    }

    public enum FavoriteToggleResult {
        SUCCESS,
        NOT_LOGGED_IN,
        RESTAURATEUR_BLOCKED,
        NO_RESTAURANT,
        NO_EMAIL,
        FAILED
    }

    // CAMPI
    private final IRestaurantService restaurantService;
    private final IReviewService reviewService;
    private final IUserService userService;
    private final IFavoriteService favoriteService;
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
    private final StringProperty ratingDistributionText;
    private final BooleanProperty isFavorite;
    private final BooleanProperty favoriteButtonVisible;
    private final BooleanProperty favoriteButtonDisabled;
    private final StringProperty favoriteButtonText;
    private final ObjectProperty<FavoriteButtonStyle> favoriteButtonStyle;
    private final BooleanProperty addReviewButtonDisabled;
    private final StringProperty addReviewButtonTooltip;
    private final StringProperty addReviewButtonText;

    // Stato interno
    private Restaurant currentRestaurant;
    private String currentUserName;
    
    // COSTRUTTORI
    /**
     * Costruisce il ViewModel iniettando i servizi e il contesto di sessione.
     *
     * @param restaurantService Servizio per il recupero dei dati del ristorante.
     * @param reviewService Servizio per la gestione delle recensioni.
     * @param userService Servizio per risolvere email → nome (autore recensioni).
     * @param sessionContext Contesto di sessione (nessun getInstance).
     */
    public RestaurantDetailsViewModel(IRestaurantService restaurantService,
                                      IReviewService reviewService,
                                      IUserService userService,
                                      IFavoriteService favoriteService,
                                      SessionContext sessionContext) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.favoriteService = favoriteService;
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
        this.ratingDistributionText = new SimpleStringProperty("");
        this.isFavorite = new SimpleBooleanProperty(false);
        this.favoriteButtonVisible = new SimpleBooleanProperty(false);
        this.favoriteButtonDisabled = new SimpleBooleanProperty(true);
        this.favoriteButtonText = new SimpleStringProperty("★ Aggiungi ai Preferiti");
        this.favoriteButtonStyle = new SimpleObjectProperty<>(FavoriteButtonStyle.ADD);
        this.addReviewButtonDisabled = new SimpleBooleanProperty(true);
        this.addReviewButtonTooltip = new SimpleStringProperty("");
        this.addReviewButtonText = new SimpleStringProperty("Aggiungi Recensione");
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
                        refreshFavoriteButtonState();
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
        updateRatingDistributionText();
    }

    private void updateRatingDistributionText() {
        ratingDistributionText.set(getRatingDistributionText());
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
        if (currentRestaurant == null || !isLoggedIn() || isRestaurateur()) {
            canAddReview.set(false);
            refreshAddReviewButtonState();
            return;
        }
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            canAddReview.set(false);
            refreshAddReviewButtonState();
            return;
        }
        boolean hasReviewed = reviewService.hasUserReviewedRestaurant(userEmail, currentRestaurant.getName());
        canAddReview.set(!hasReviewed);
        refreshAddReviewButtonState();
    }

    public boolean isLoggedIn() {
        return sessionContext != null && sessionContext.isLoggedIn();
    }

    public boolean isRestaurateur() {
        if (!isLoggedIn() || sessionContext.getCurrentUser() == null) {
            return false;
        }
        String role = sessionContext.getCurrentUser().getRole();
        return "Restaurateur".equalsIgnoreCase(role) || "Ristoratore".equalsIgnoreCase(role);
    }

    public String getCurrentUserEmail() {
        if (sessionContext == null || sessionContext.getCurrentUser() == null) {
            return null;
        }
        return sessionContext.getCurrentUser().getEmail();
    }

    public AddReviewPermission checkAddReviewPermission() {
        if (!isLoggedIn()) {
            return AddReviewPermission.NOT_LOGGED_IN;
        }
        if (isRestaurateur()) {
            return AddReviewPermission.RESTAURATEUR_BLOCKED;
        }
        return AddReviewPermission.ALLOWED;
    }

    public void refreshAddReviewButtonState() {
        AddReviewPermission permission = checkAddReviewPermission();
        if (permission == AddReviewPermission.NOT_LOGGED_IN) {
            addReviewButtonDisabled.set(true);
            addReviewButtonTooltip.set("Accedi per aggiungere una recensione");
            addReviewButtonText.set("Aggiungi Recensione");
            return;
        }
        if (permission == AddReviewPermission.RESTAURATEUR_BLOCKED) {
            addReviewButtonDisabled.set(true);
            addReviewButtonTooltip.set("I Ristoratori non possono aggiungere recensioni");
            addReviewButtonText.set("Recensioni Disabilitate");
            return;
        }
        boolean canAdd = canAddReview.get();
        addReviewButtonDisabled.set(!canAdd);
        addReviewButtonText.set("Aggiungi Recensione");
        if (!canAdd) {
            addReviewButtonTooltip.set("Hai già recensito questo ristorante");
        } else {
            addReviewButtonTooltip.set("");
        }
    }

    public void refreshFavoriteButtonState() {
        boolean loggedIn = isLoggedIn();
        boolean restaurateur = isRestaurateur();
        favoriteButtonVisible.set(loggedIn && !restaurateur);

        Restaurant restaurant = currentRestaurant;
        boolean disabled = !loggedIn || restaurateur || restaurant == null || restaurant.getId() == null;
        favoriteButtonDisabled.set(disabled);

        if (loggedIn && !restaurateur && restaurant != null && restaurant.getId() != null) {
            String userEmail = getCurrentUserEmail();
            boolean favorite = userEmail != null && favoriteService.isFavorite(userEmail, restaurant.getId());
            isFavorite.set(favorite);
            if (favorite) {
                favoriteButtonText.set("★ Rimuovi dai Preferiti");
                favoriteButtonStyle.set(FavoriteButtonStyle.REMOVE);
            } else {
                favoriteButtonText.set("★ Aggiungi ai Preferiti");
                favoriteButtonStyle.set(FavoriteButtonStyle.ADD);
            }
        } else {
            isFavorite.set(false);
        }
    }

    public FavoriteToggleResult toggleFavorite() {
        if (!isLoggedIn()) {
            return FavoriteToggleResult.NOT_LOGGED_IN;
        }
        if (isRestaurateur()) {
            return FavoriteToggleResult.RESTAURATEUR_BLOCKED;
        }
        if (currentRestaurant == null || currentRestaurant.getId() == null) {
            return FavoriteToggleResult.NO_RESTAURANT;
        }
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            return FavoriteToggleResult.NO_EMAIL;
        }

        Long restaurantId = currentRestaurant.getId();
        boolean favorite = favoriteService.isFavorite(userEmail, restaurantId);
        boolean success = favorite
            ? favoriteService.removeFavorite(userEmail, restaurantId)
            : favoriteService.addFavorite(userEmail, restaurantId);

        if (success) {
            refreshFavoriteButtonState();
            return FavoriteToggleResult.SUCCESS;
        }
        return FavoriteToggleResult.FAILED;
    }

    public boolean isReviewOwner(Review review) {
        if (!isLoggedIn() || review == null) {
            return false;
        }
        String currentUserEmail = getCurrentUserEmail();
        return currentUserEmail != null
            && review.getUserEmail() != null
            && currentUserEmail.equalsIgnoreCase(review.getUserEmail());
    }

    public boolean isRestaurateurOwner(Review review) {
        if (!isLoggedIn() || review == null || currentRestaurant == null) {
            return false;
        }
        String currentUserEmail = getCurrentUserEmail();
        String restaurateurEmail = currentRestaurant.getRestaurateurEmail();
        return currentUserEmail != null
            && restaurateurEmail != null
            && restaurateurEmail.trim().equalsIgnoreCase(currentUserEmail.trim());
    }

    public boolean canShowRestaurateurRespondButton(Review review) {
        return isRestaurateurOwner(review) && review != null && !review.hasRestaurateurResponse();
    }

    public boolean canShowClientRespondButton(Review review) {
        return isReviewOwner(review)
            && review != null
            && review.hasRestaurateurResponse()
            && !review.hasClientResponse();
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
            refreshFavoriteButtonState();
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
     * Restituisce il nome da mostrare per l'autore di una recensione.
     * Usa userName se presente, altrimenti risolve l'email tramite UserService (recensioni da CSV).
     *
     * @param review La recensione.
     * @return Nome da visualizzare (nome utente, email o "Utente").
     */
    public String getReviewAuthorDisplayName(Review review) {
        if (review == null) return "Utente";
        if (review.getUserName() != null && !review.getUserName().isBlank()) {
            return review.getUserName();
        }
        if (review.getUserEmail() != null && !review.getUserEmail().isBlank()) {
            try {
                User user = userService.findUserByEmail(review.getUserEmail().trim());
                if (user != null && user.getName() != null && !user.getName().isBlank()) {
                    return user.getName();
                }
            } catch (IOException e) {
                logger.error("Error resolving author name for " + review.getUserEmail() + ": " + e.getMessage());
            }
            return review.getUserEmail();
        }
        return "Utente";
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

    public StringProperty ratingDistributionTextProperty() {
        return ratingDistributionText;
    }

    public BooleanProperty isFavoriteProperty() {
        return isFavorite;
    }

    public BooleanProperty favoriteButtonVisibleProperty() {
        return favoriteButtonVisible;
    }

    public BooleanProperty favoriteButtonDisabledProperty() {
        return favoriteButtonDisabled;
    }

    public StringProperty favoriteButtonTextProperty() {
        return favoriteButtonText;
    }

    public ObjectProperty<FavoriteButtonStyle> favoriteButtonStyleProperty() {
        return favoriteButtonStyle;
    }

    public BooleanProperty addReviewButtonDisabledProperty() {
        return addReviewButtonDisabled;
    }

    public StringProperty addReviewButtonTooltipProperty() {
        return addReviewButtonTooltip;
    }

    public StringProperty addReviewButtonTextProperty() {
        return addReviewButtonText;
    }
}
