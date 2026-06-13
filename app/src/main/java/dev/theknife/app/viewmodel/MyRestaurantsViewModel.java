/*
 * ViewModel per la schermata "I Miei Ristoranti".
 * Incapsula sessione, caricamento asincrono, filtri e accesso ai servizi.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.Restaurant;
import dev.theknife.app.model.Review;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.IReviewService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.List;

public class MyRestaurantsViewModel {

    private static final int BATCH_SIZE = 100;

    public enum RestaurantsScreenState {
        INITIAL,
        LOGIN_REQUIRED,
        EMAIL_MISSING,
        LOADING,
        LOADED_EMPTY,
        LOADED_WITH_DATA,
        LOAD_ERROR
    }

    private final IRestaurantService restaurantService;
    private final IReviewService reviewService;
    private final SessionContext sessionContext;
    private final Logger logger;

    private final ObjectProperty<RestaurantsScreenState> screenState =
        new SimpleObjectProperty<>(RestaurantsScreenState.INITIAL);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty statusError = new SimpleBooleanProperty(false);
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final DoubleProperty loadProgress = new SimpleDoubleProperty(0);
    private final ObservableList<Restaurant> restaurants = FXCollections.observableArrayList();

    public MyRestaurantsViewModel(IRestaurantService restaurantService,
                                  IReviewService reviewService,
                                  SessionContext sessionContext) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
        this.sessionContext = sessionContext;
        this.logger = Logger.getLogger(MyRestaurantsViewModel.class);
    }

    public ObjectProperty<RestaurantsScreenState> screenStateProperty() {
        return screenState;
    }

    public RestaurantsScreenState getScreenState() {
        return screenState.get();
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage.get();
    }

    public BooleanProperty statusErrorProperty() {
        return statusError;
    }

    public boolean isStatusError() {
        return statusError.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public boolean isLoading() {
        return loading.get();
    }

    public DoubleProperty loadProgressProperty() {
        return loadProgress;
    }

    public ObservableList<Restaurant> getRestaurants() {
        return restaurants;
    }

    public String getCurrentUserEmail() {
        if (sessionContext == null || sessionContext.getCurrentUser() == null) {
            return null;
        }
        return sessionContext.getCurrentUser().getEmail();
    }

    public String getCurrentUserName() {
        return sessionContext != null ? sessionContext.getCurrentUserName() : null;
    }

    public double getAverageRating(String restaurantName) {
        return reviewService.getAverageRating(restaurantName);
    }

    public int getReviewCount(String restaurantName) {
        return reviewService.getReviewCount(restaurantName);
    }

    public boolean deleteReview(Review review) {
        if (review == null || review.getId() == null) {
            return false;
        }
        try {
            return reviewService.deleteReview(review.getId(), getCurrentUserEmail());
        } catch (Exception e) {
            logger.error("Delete review failed", e);
            return false;
        }
    }

    /**
     * Valuta la sessione e avvia il caricamento asincrono dei ristoranti del ristoratore.
     */
    public void loadRestaurants() {
        logger.info("Loading restaurants for current user");
        if (sessionContext == null) {
            return;
        }
        if (!sessionContext.isLoggedIn() || sessionContext.getCurrentUser() == null) {
            screenState.set(RestaurantsScreenState.LOGIN_REQUIRED);
            statusMessage.set("Accedi come ristoratore per vedere i tuoi ristoranti.");
            statusError.set(true);
            loading.set(false);
            restaurants.clear();
            return;
        }
        String userEmail = getCurrentUserEmail();
        if (userEmail == null || userEmail.trim().isEmpty()) {
            screenState.set(RestaurantsScreenState.EMAIL_MISSING);
            statusMessage.set("Errore: Email utente non trovata. Effettua nuovamente l'accesso.");
            statusError.set(true);
            loading.set(false);
            restaurants.clear();
            return;
        }

        String normalizedEmail = userEmail.trim().toLowerCase();
        screenState.set(RestaurantsScreenState.LOADING);
        loading.set(true);
        statusMessage.set("Caricamento dei tuoi ristoranti...");
        statusError.set(false);
        restaurants.clear();
        loadProgress.set(0);

        Task<Void> loadTask = new Task<>() {
            @Override
            protected Void call() {
                int offset = 0;
                int total = restaurantService.getTotalRestaurantCount();
                while (offset < total && !isCancelled()) {
                    List<Restaurant> batch = restaurantService.getRestaurantsRange(offset, BATCH_SIZE);
                    if (batch.isEmpty()) {
                        break;
                    }
                    List<Restaurant> matches = filterByRestaurateurEmail(batch, normalizedEmail);
                    if (!matches.isEmpty()) {
                        Platform.runLater(() -> restaurants.addAll(matches));
                    }
                    offset += BATCH_SIZE;
                    updateProgress(Math.min(offset, total), Math.max(total, 1));
                }
                return null;
            }
        };

        loadProgress.bind(loadTask.progressProperty());

        loadTask.setOnSucceeded(e -> {
            loadProgress.unbind();
            loading.set(false);
            if (restaurants.isEmpty()) {
                screenState.set(RestaurantsScreenState.LOADED_EMPTY);
                statusMessage.set(
                    "Nessun ristorante presente. Aggiungi il tuo primo ristorante con il pulsante 'Aggiungi Ristorante'!");
                statusError.set(true);
            } else {
                screenState.set(RestaurantsScreenState.LOADED_WITH_DATA);
                statusMessage.set("Trovati " + restaurants.size() + " ristorante/i");
                statusError.set(false);
            }
        });

        loadTask.setOnFailed(e -> {
            loadProgress.unbind();
            loading.set(false);
            screenState.set(RestaurantsScreenState.LOAD_ERROR);
            statusMessage.set("Errore nel caricamento dei ristoranti. Riprova.");
            statusError.set(true);
            restaurants.clear();
            Throwable ex = loadTask.getException();
            if (ex != null) {
                logger.error("Failed to load restaurants", ex);
            }
        });

        Thread thread = new Thread(loadTask, "MyRestaurantsLoadTask");
        thread.setDaemon(true);
        thread.start();
    }

    private List<Restaurant> filterByRestaurateurEmail(List<Restaurant> batch, String normalizedEmail) {
        return batch.stream()
            .filter(r -> {
                String email = r.getRestaurateurEmail();
                return email != null && email.trim().toLowerCase().equals(normalizedEmail);
            })
            .toList();
    }
}
