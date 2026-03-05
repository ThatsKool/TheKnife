/*
 * ViewModel per la schermata "I Miei Ristoranti".
 * Incapsula l'accesso a IRestaurantService e IReviewService.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.Restaurant;
import dev.theknife.app.model.Review;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.IReviewService;

import java.util.List;

public class MyRestaurantsViewModel {

    private final IRestaurantService restaurantService;
    private final IReviewService reviewService;

    public MyRestaurantsViewModel(IRestaurantService restaurantService,
                                  IReviewService reviewService) {
        this.restaurantService = restaurantService;
        this.reviewService = reviewService;
    }

    public int getTotalRestaurantCount() {
        return restaurantService.getTotalRestaurantCount();
    }

    public List<Restaurant> getRestaurantsRange(int offset, int limit) {
        return restaurantService.getRestaurantsRange(offset, limit);
    }

    public double getAverageRating(String restaurantName) {
        return reviewService.getAverageRating(restaurantName);
    }

    public int getReviewCount(String restaurantName) {
        return reviewService.getReviewCount(restaurantName);
    }

    public boolean deleteReview(Review review, String userEmail) {
        return reviewService.deleteReview(review.getId(), userEmail);
    }
}

