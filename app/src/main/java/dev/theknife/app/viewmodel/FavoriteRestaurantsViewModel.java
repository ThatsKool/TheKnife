/*
 * ViewModel per la schermata dei ristoranti preferiti.
 * Incapsula l'accesso a IFavoriteService, IRestaurantService e IReviewService
 * così che la View non debba parlare direttamente con i servizi.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.FavoriteRestaurant;
import dev.theknife.app.model.Restaurant;
import dev.theknife.app.service.IFavoriteService;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.IReviewService;

import java.util.List;

public class FavoriteRestaurantsViewModel {

    private final IFavoriteService favoriteService;
    private final IRestaurantService restaurantService;
    private final IReviewService reviewService;

    public FavoriteRestaurantsViewModel(IFavoriteService favoriteService,
                                        IRestaurantService restaurantService,
                                        IReviewService reviewService) {
        this.favoriteService = favoriteService;
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
    }

    public List<FavoriteRestaurant> getUserFavoriteRestaurants(String userEmail) {
        return favoriteService.getUserFavoriteRestaurants(userEmail);
    }

    public Restaurant findRestaurantById(Long restaurantId) {
        return restaurantService.findRestaurantById(restaurantId);
    }

    public int getReviewCount(String restaurantName) {
        return reviewService.getReviewCount(restaurantName);
    }

    public double getAverageRating(String restaurantName) {
        return reviewService.getAverageRating(restaurantName);
    }

    public boolean removeFavorite(String userEmail, Long restaurantId) {
        return favoriteService.removeFavorite(userEmail, restaurantId);
    }
}

