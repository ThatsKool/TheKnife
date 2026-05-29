/*
 * ViewModel per la schermata dei ristoranti preferiti.
 * Incapsula sessione, caricamento asincrono e accesso ai servizi.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.FavoriteRestaurant;
import dev.theknife.app.model.Restaurant;
import dev.theknife.app.model.Review;
import dev.theknife.app.service.IFavoriteService;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.IReviewService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.util.List;

public class FavoriteRestaurantsViewModel {

    public enum FavoritesScreenState {
        INITIAL,
        RESTAURATEUR_NOT_ALLOWED,
        LOGIN_REQUIRED,
        EMAIL_MISSING,
        LOADING,
        LOADED_EMPTY,
        LOADED_WITH_DATA,
        LOAD_ERROR
    }

    private final IFavoriteService favoriteService;
    private final IRestaurantService restaurantService;
    private final IReviewService reviewService;
    private final SessionContext sessionContext;
    private final Logger logger;

    private final ObjectProperty<FavoritesScreenState> screenState =
        new SimpleObjectProperty<>(FavoritesScreenState.INITIAL);
    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final ObservableList<Restaurant> favoriteRestaurants = FXCollections.observableArrayList();

    public FavoriteRestaurantsViewModel(IFavoriteService favoriteService,
                                        IRestaurantService restaurantService,
                                        IReviewService reviewService,
                                        SessionContext sessionContext) {
        this.favoriteService = favoriteService;
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
        this.sessionContext = sessionContext;
        this.logger = Logger.getLogger(FavoriteRestaurantsViewModel.class);
    }

    public ObjectProperty<FavoritesScreenState> screenStateProperty() {
        return screenState;
    }

    public FavoritesScreenState getScreenState() {
        return screenState.get();
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public boolean isLoading() {
        return loading.get();
    }

    public ObservableList<Restaurant> getFavoriteRestaurants() {
        return favoriteRestaurants;
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

    public int getReviewCount(String restaurantName) {
        return reviewService.getReviewCount(restaurantName);
    }

    public double getAverageRating(String restaurantName) {
        return reviewService.getAverageRating(restaurantName);
    }

    public boolean removeFavoriteForCurrentUser(Long restaurantId) {
        String email = getCurrentUserEmail();
        if (email == null || restaurantId == null) {
            return false;
        }
        return favoriteService.removeFavorite(email, restaurantId);
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
     * Valuta la sessione e avvia il caricamento asincrono dei preferiti, aggiornando le property osservabili.
     */
    public void loadFavorites() {
        if (sessionContext == null) {
            return;
        }
        if (isRestaurateur()) {
            screenState.set(FavoritesScreenState.RESTAURATEUR_NOT_ALLOWED);
            statusMessage.set("I Ristoratori non possono aggiungere ristoranti ai preferiti.");
            loading.set(false);
            favoriteRestaurants.clear();
            return;
        }
        if (!sessionContext.isLoggedIn()) {
            screenState.set(FavoritesScreenState.LOGIN_REQUIRED);
            statusMessage.set("Accedi per vedere i tuoi ristoranti preferiti.");
            loading.set(false);
            favoriteRestaurants.clear();
            return;
        }
        String userEmail = getCurrentUserEmail();
        if (userEmail == null) {
            screenState.set(FavoritesScreenState.EMAIL_MISSING);
            statusMessage.set("Effettua l'accesso per vedere i tuoi preferiti.");
            loading.set(false);
            favoriteRestaurants.clear();
            return;
        }

        screenState.set(FavoritesScreenState.LOADING);
        loading.set(true);
        statusMessage.set("");
        favoriteRestaurants.clear();

        Task<ObservableList<Restaurant>> task = new Task<>() {
            @Override
            protected ObservableList<Restaurant> call() {
                return loadFavoriteRestaurants(userEmail);
            }
        };

        task.setOnSucceeded(e -> {
            loading.set(false);
            ObservableList<Restaurant> result = task.getValue();
            if (result == null || result.isEmpty()) {
                screenState.set(FavoritesScreenState.LOADED_EMPTY);
                statusMessage.set("Nessun ristorante preferito. Aggiungine alcuni dai dettagli del ristorante!");
                favoriteRestaurants.clear();
            } else {
                screenState.set(FavoritesScreenState.LOADED_WITH_DATA);
                favoriteRestaurants.setAll(result);
            }
        });

        task.setOnFailed(e -> {
            loading.set(false);
            screenState.set(FavoritesScreenState.LOAD_ERROR);
            statusMessage.set("Errore nel caricamento dei preferiti.");
            favoriteRestaurants.clear();
            Throwable ex = task.getException();
            if (ex != null) {
                logger.error("Failed to load favorites", ex);
            }
        });

        new Thread(task).start();
    }

    private ObservableList<Restaurant> loadFavoriteRestaurants(String userEmail) {
        ObservableList<Restaurant> restaurants = FXCollections.observableArrayList();
        List<FavoriteRestaurant> favorites = favoriteService.getUserFavoriteRestaurants(userEmail);
        if (favorites != null) {
            for (FavoriteRestaurant fav : favorites) {
                if (fav.getRestaurantId() != null) {
                    Restaurant restaurant = restaurantService.findRestaurantById(fav.getRestaurantId());
                    if (restaurant != null) {
                        restaurants.add(restaurant);
                    }
                }
            }
        }
        return restaurants;
    }

    private boolean isRestaurateur() {
        if (!sessionContext.isLoggedIn() || sessionContext.getCurrentUser() == null) {
            return false;
        }
        String role = sessionContext.getCurrentUser().getRole();
        return "Restaurateur".equalsIgnoreCase(role) || "Ristoratore".equalsIgnoreCase(role);
    }
}
