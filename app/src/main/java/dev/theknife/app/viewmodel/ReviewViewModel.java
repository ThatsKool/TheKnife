/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.Review;
import dev.theknife.app.service.IReviewService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.Logger;
import javafx.beans.property.*;
import java.util.concurrent.CompletableFuture;

/**
 * ViewModel per la schermata di gestione delle recensioni (creazione e modifica).
 * <p>
 * Gestisce la logica di validazione dell'input, il calcolo della lunghezza del commento,
 * e l'interazione con il {@link IReviewService} per il salvataggio o l'aggiornamento.
 * </p>
 * <p>
 * <b>Funzionalità principali:</b>
 * <ul>
 *   <li>Gestione stato del form (rating, commento, validità).</li>
 *   <li>Validazione in tempo reale (lunghezza commento, rating obbligatorio).</li>
 *   <li>Invio asincrono della recensione per non bloccare la UI.</li>
 *   <li>Supporto per la modalità "Modifica" di recensioni esistenti.</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.view.ReviewView
 * @see dev.theknife.app.model.Review
 */
public class ReviewViewModel {
    // CAMPI
    private final IReviewService reviewService;
    private final SessionContext sessionContext;
    private final Logger logger;
    private final IntegerProperty rating;
    private final StringProperty comment;
    private final BooleanProperty isValid;
    private final BooleanProperty isSubmitting;
    private final StringProperty errorMessage;
    private final StringProperty successMessage;
    
    private String restaurantName;
    private String userName;
    private Review existingReview; // Per la modalità modifica
    
    // COSTRUTTORI
    /**
     * Costruisce il ViewModel iniettando il servizio delle recensioni e il contesto di sessione.
     *
     * @param reviewService Il servizio per la gestione delle recensioni.
     * @param sessionContext Il contesto di sessione (nessun getInstance).
     */
    public ReviewViewModel(IReviewService reviewService, SessionContext sessionContext) {
        this.reviewService = reviewService;
        this.sessionContext = sessionContext;
        this.logger = Logger.getLogger(ReviewViewModel.class);
        
        // Inizializza le property
        this.rating = new SimpleIntegerProperty(0);
        this.comment = new SimpleStringProperty("");
        this.isValid = new SimpleBooleanProperty(false);
        this.isSubmitting = new SimpleBooleanProperty(false);
        this.errorMessage = new SimpleStringProperty("");
        this.successMessage = new SimpleStringProperty("");
        
        // Imposta la validazione
        setupValidation();
    }
    
    // METODI
    /**
     * Configura la logica di validazione reattiva.
     */
    private void setupValidation() {
        // Valida quando il voto o il commento cambiano
        rating.addListener((obs, oldVal, newVal) -> validateForm());
        comment.addListener((obs, oldVal, newVal) -> validateForm());
    }
    
    /**
     * Inizializza il ViewModel con le informazioni del ristorante e dell'utente.
     *
     * @param restaurantName Il nome del ristorante.
     * @param userName Il nome dell'utente.
     */
    public void initialize(String restaurantName, String userName) {
        this.existingReview = null;
        initializeForNewReview(restaurantName, userName);
    }
    
    /**
     * Inizializza il ViewModel per la modifica di una recensione esistente.
     *
     * @param review La recensione da modificare.
     */
    public void initializeForEdit(Review review) {
        if (!sessionContext.isLoggedIn() || sessionContext.getCurrentUser() == null) {
            errorMessage.set("Devi essere loggato per modificare una recensione. Effettua il login.");
            isValid.set(false);
            return;
        }
        
        var currentUser = sessionContext.getCurrentUser();
        boolean isOwner = currentUser.getEmail() != null && review.getUserEmail() != null
                && review.getUserEmail().equalsIgnoreCase(currentUser.getEmail());
        if (!isOwner) {
            errorMessage.set("Puoi modificare solo le tue recensioni.");
            isValid.set(false);
            return;
        }
        
        this.existingReview = review;
        this.restaurantName = review.getRestaurantName();
        this.userName = review.getUserName() != null ? review.getUserName() : currentUser.getName();
        
        // Carica i dati della recensione esistente
        rating.set(review.getRating());
        comment.set(review.getComment());
        errorMessage.set("");
        successMessage.set("");
        isSubmitting.set(false);
        
        validateForm();
    }
    
    /**
     * Inizializza il ViewModel per una nuova recensione.
     *
     * @param restaurantName Il nome del ristorante.
     * @param userName Il nome dell'utente.
     */
    private void initializeForNewReview(String restaurantName, String userName) {
        if (!sessionContext.isLoggedIn() || userName == null) {
            errorMessage.set("Devi essere loggato per aggiungere una recensione. Accedi o registrati.");
            isValid.set(false);
            return;
        }
        
        this.restaurantName = restaurantName;
        this.userName = userName;
        
        // Resetta lo stato precedente
        rating.set(0);
        comment.set("");
        errorMessage.set("");
        successMessage.set("");
        isSubmitting.set(false);
        
        // Verifica se l'utente ha già recensito questo ristorante (solo email)
        String userEmail = sessionContext.getCurrentUser() != null ? sessionContext.getCurrentUser().getEmail() : null;
        if (reviewService.hasUserReviewedRestaurant(userEmail, restaurantName)) {
            errorMessage.set("Hai già recensito questo ristorante.");
            isValid.set(false);
        } else {
            validateForm();
        }
    }
    
    /**
     * Esegue la validazione del form.
     */
    private void validateForm() {
        boolean valid = rating.get() >= 1 && rating.get() <= 5 && 
                       comment.get() != null && !comment.get().trim().isEmpty();
        isValid.set(valid);
        
        if (!valid) {
            if (rating.get() < 1 || rating.get() > 5) {
                errorMessage.set("Seleziona un voto tra 1 e 5 stelle.");
            } else if (comment.get() == null || comment.get().trim().isEmpty()) {
                errorMessage.set("Inserisci un commento per la tua recensione.");
            }
        } else {
            errorMessage.set("");
        }
    }
    
    /**
     * Imposta il voto (rating).
     *
     * @param newRating Il nuovo voto (1-5).
     */
    public void setRating(int newRating) {
        rating.set(Math.max(1, Math.min(5, newRating)));
    }
    
    /**
     * Invia la recensione (nuova o aggiornamento).
     */
    public void submitReview() {
        if (!sessionContext.isLoggedIn() || userName == null) {
            errorMessage.set("Devi essere loggato per inviare una recensione. Effettua il login.");
            return;
        }
        var user = sessionContext.getCurrentUser();
        if (user != null) {
            String role = user.getRole();
            if (role != null && ("Restaurateur".equalsIgnoreCase(role) || "Ristoratore".equalsIgnoreCase(role))) {
                errorMessage.set("Solo i clienti possono inviare recensioni.");
                return;
            }
        }
        
        if (!isValid.get() || isSubmitting.get()) {
            return;
        }
        
        isSubmitting.set(true);
        errorMessage.set("");
        successMessage.set("");
        
        CompletableFuture.runAsync(() -> {
            try {
                boolean success;
                String userEmail = user != null ? user.getEmail() : null;
                
                if (existingReview != null) {
                    Review updated = existingReview.withRatingAndComment(rating.get(), comment.get().trim());
                    success = reviewService.updateReview(updated, userEmail);
                } else {
                    Review newReview = new Review(restaurantName, userName, userEmail, rating.get(), comment.get().trim());
                    success = reviewService.addReview(newReview);
                }
                
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        if (existingReview != null) {
                            successMessage.set("Recensione aggiornata con successo!");
                        } else {
                            successMessage.set("Recensione inviata con successo!");
                        }
                        // Pulisci il form
                        rating.set(0);
                        comment.set("");
                        isValid.set(false);
                        existingReview = null;
                    } else {
                        errorMessage.set("Impossibile " + (existingReview != null ? "aggiornare" : "inviare") + " la recensione. Riprova.");
                    }
                    isSubmitting.set(false);
                });
            } catch (Exception e) {
                logger.error("Error " + (existingReview != null ? "updating" : "submitting") + " review: " + e.getMessage(), e);
                javafx.application.Platform.runLater(() -> {
                    errorMessage.set("Si è verificato un errore durante " + (existingReview != null ? "l'aggiornamento" : "l'invio") + ": " + e.getMessage());
                    isSubmitting.set(false);
                });
            }
        });
    }
    
    /**
     * Ottiene la rappresentazione a stelle del voto corrente.
     *
     * @return Una stringa di stelle (es. "★★★☆☆").
     */
    public String getRatingStars() {
        int currentRating = rating.get();
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < currentRating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    /**
     * Ottiene la rappresentazione a stelle per un voto specifico.
     *
     * @param ratingValue Il valore del voto.
     * @return Una stringa di stelle.
     */
    public String getRatingStars(int ratingValue) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < ratingValue) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    /**
     * Ottiene il conteggio dei caratteri del commento corrente.
     *
     * @return Il numero di caratteri.
     */
    public int getCommentLength() {
        return comment.get() != null ? comment.get().length() : 0;
    }
    
    /**
     * Ottiene la lunghezza massima consentita per il commento.
     *
     * @return Il limite massimo di caratteri (500).
     */
    public int getMaxCommentLength() {
        return 500; // Limite ragionevole per i commenti
    }
    
    /**
     * Verifica se il commento è troppo lungo.
     *
     * @return true se supera il limite, false altrimenti.
     */
    public boolean isCommentTooLong() {
        return getCommentLength() > getMaxCommentLength();
    }
    
    /**
     * Ottiene lo stato della lunghezza del commento come testo.
     *
     * @return Stringa formattata "corrente/massimo caratteri".
     */
    public String getCommentLengthStatus() {
        int current = getCommentLength();
        int max = getMaxCommentLength();
        return current + "/" + max + " caratteri";
    }
    
    // --- Metodi getter per le Property (Binding JavaFX) ---

    /**
     * Restituisce la property del voto.
     *
     * @return Property contenente il voto (1-5).
     */
    public IntegerProperty ratingProperty() {
        return rating;
    }
    
    /**
     * Restituisce la property del commento.
     *
     * @return Property contenente il testo del commento.
     */
    public StringProperty commentProperty() {
        return comment;
    }
    
    /**
     * Restituisce la property di validità del form.
     *
     * @return Property booleana, true se il form è valido.
     */
    public BooleanProperty isValidProperty() {
        return isValid;
    }
    
    /**
     * Restituisce la property che indica se l'invio è in corso.
     *
     * @return Property booleana, true durante l'invio.
     */
    public BooleanProperty isSubmittingProperty() {
        return isSubmitting;
    }
    
    /**
     * Restituisce la property del messaggio di errore.
     *
     * @return Property contenente il messaggio di errore.
     */
    public StringProperty errorMessageProperty() {
        return errorMessage;
    }
    
    /**
     * Restituisce la property del messaggio di successo.
     *
     * @return Property contenente il messaggio di successo.
     */
    public StringProperty successMessageProperty() {
        return successMessage;
    }
    
    /**
     * Restituisce la property del nome del ristorante.
     *
     * @return Property contenente il nome del ristorante.
     */
    public StringProperty restaurantNameProperty() {
        return new SimpleStringProperty(restaurantName);
    }
    
    /**
     * Restituisce la property del nome utente.
     *
     * @return Property contenente il nome utente.
     */
    public StringProperty userNameProperty() {
        return new SimpleStringProperty(userName);
    }
    
    /**
     * Restituisce la property per lo stato della lunghezza commento.
     *
     * @return Property contenente lo stato formattato.
     */
    public StringProperty commentLengthStatusProperty() {
        return new SimpleStringProperty(getCommentLengthStatus());
    }
    
    /**
     * Restituisce la property che indica se il commento è troppo lungo.
     *
     * @return Property booleana, true se troppo lungo.
     */
    public BooleanProperty isCommentTooLongProperty() {
        return new SimpleBooleanProperty(isCommentTooLong());
    }
    
    // --- Metodi getter standard ---

    /**
     * Restituisce il voto corrente della recensione.
     *
     * @return Il voto (1-5).
     */
    public int getRating() {
        return rating.get();
    }
    
    /**
     * Restituisce il testo del commento corrente.
     *
     * @return Il commento della recensione.
     */
    public String getComment() {
        return comment.get();
    }
    
    /**
     * Verifica se il form è valido.
     *
     * @return true se il form è valido, false altrimenti.
     */
    public boolean isValid() {
        return isValid.get();
    }
    
    /**
     * Verifica se l'invio della recensione è in corso.
     *
     * @return true se l'invio è in corso, false altrimenti.
     */
    public boolean isSubmitting() {
        return isSubmitting.get();
    }
    
    /**
     * Restituisce il messaggio di errore corrente.
     *
     * @return Il messaggio di errore o stringa vuota se non presente.
     */
    public String getErrorMessage() {
        return errorMessage.get();
    }
    
    /**
     * Restituisce il messaggio di successo corrente.
     *
     * @return Il messaggio di successo o stringa vuota se non presente.
     */
    public String getSuccessMessage() {
        return successMessage.get();
    }
    
    /**
     * Restituisce il nome del ristorante per cui si sta creando/modificando la recensione.
     *
     * @return Il nome del ristorante.
     */
    public String getRestaurantName() {
        return restaurantName;
    }
    
    /**
     * Restituisce il nome dell'utente che sta creando/modificando la recensione.
     *
     * @return Il nome dell'utente.
     */
    public String getUserName() {
        return userName;
    }
}
