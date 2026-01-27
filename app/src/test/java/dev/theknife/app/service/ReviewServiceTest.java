/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.model.Review;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import dev.theknife.app.config.DefaultFileProvider;
import dev.theknife.app.config.FileProvider;
import dev.theknife.app.service.RestaurantService;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;

/**
 * Test suite per la classe {@link ReviewService}.
 * <p>
 * Verifica le operazioni CRUD sulle recensioni, inclusa l'aggiunta,
 * il recupero per ristorante e per utente, e il calcolo delle statistiche.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class ReviewServiceTest {
    private ReviewService reviewService;
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        Map<String, Path> csvPaths = new HashMap<>();
        csvPaths.put("reviews.csv", tempDir.resolve("reviews.csv"));
        csvPaths.put("users.csv", tempDir.resolve("users.csv"));
        csvPaths.put("michelin_my_maps.csv", tempDir.resolve("michelin_my_maps.csv"));
        csvPaths.put("favorites.csv", tempDir.resolve("favorites.csv"));
        FileProvider fileProvider = new DefaultFileProvider(csvPaths);
        RestaurantService restaurantService = new RestaurantService(fileProvider);
        reviewService = new ReviewService(fileProvider, restaurantService);
    }
    
    @Test
    void testAddReview() {
        Review review = new Review("Test Restaurant", "Test User", "test@example.com", 4, "Great food!");
        
        boolean result = reviewService.addReview(review);
        assertTrue(result);
        
        // Verify review was added
        List<Review> reviews = reviewService.getReviewsForRestaurant("Test Restaurant");
        assertEquals(1, reviews.size());
        assertEquals(review.getComment(), reviews.get(0).getComment());
    }
    
    @Test
    void testAddInvalidReview() {
        Review invalidReview = new Review("", "Test User", "test@example.com", 4, "Great food!");
        
        boolean result = reviewService.addReview(invalidReview);
        assertFalse(result);
    }
    
    @Test
    void testGetReviewsForRestaurant() {
        // Add multiple reviews for the same restaurant
        Review review1 = new Review("Test Restaurant", "User1", "user1@example.com", 4, "Great food!");
        Review review2 = new Review("Test Restaurant", "User2", "user2@example.com", 5, "Excellent service!");
        Review review3 = new Review("Other Restaurant", "User3", "user3@example.com", 3, "Average food");
        
        reviewService.addReview(review1);
        reviewService.addReview(review2);
        reviewService.addReview(review3);
        
        List<Review> restaurantReviews = reviewService.getReviewsForRestaurant("Test Restaurant");
        assertEquals(2, restaurantReviews.size());
        
        // Reviews should be sorted by date (newest first)
        assertTrue(restaurantReviews.get(0).getReviewDate().isAfter(restaurantReviews.get(1).getReviewDate()) ||
                   restaurantReviews.get(0).getReviewDate().isEqual(restaurantReviews.get(1).getReviewDate()));
    }
    
    @Test
    void testGetReviewsByUser() {
        Review review1 = new Review("Restaurant1", "Test User", "test@example.com", 4, "Great food!");
        Review review2 = new Review("Restaurant2", "Test User", "test@example.com", 5, "Excellent service!");
        Review review3 = new Review("Restaurant3", "Other User", "other@example.com", 3, "Average food");
        
        reviewService.addReview(review1);
        reviewService.addReview(review2);
        reviewService.addReview(review3);
        
        List<Review> userReviews = reviewService.getReviewsByUser("Test User");
        assertEquals(2, userReviews.size());
        
        for (Review review : userReviews) {
            assertEquals("Test User", review.getUserName());
        }
    }
    
    @Test
    void testGetAverageRating() {
        Review review1 = new Review("Test Restaurant", "User1", "user1@example.com", 4, "Great food!");
        Review review2 = new Review("Test Restaurant", "User2", "user2@example.com", 5, "Excellent service!");
        Review review3 = new Review("Test Restaurant", "User3", "user3@example.com", 3, "Average food");
        
        reviewService.addReview(review1);
        reviewService.addReview(review2);
        reviewService.addReview(review3);
        
        double averageRating = reviewService.getAverageRating("Test Restaurant");
        assertEquals(4.0, averageRating, 0.01); // (4 + 5 + 3) / 3 = 4.0
    }
    
    @Test
    void testGetAverageRatingNoReviews() {
        double averageRating = reviewService.getAverageRating("Non-existent Restaurant");
        assertEquals(0.0, averageRating, 0.01);
    }
    
    @Test
    void testGetReviewCount() {
        Review review1 = new Review("Test Restaurant", "User1", "user1@example.com", 4, "Great food!");
        Review review2 = new Review("Test Restaurant", "User2", "user2@example.com", 5, "Excellent service!");
        
        reviewService.addReview(review1);
        reviewService.addReview(review2);
        
        int count = reviewService.getReviewCount("Test Restaurant");
        assertEquals(2, count);
    }
    
    @Test
    void testGetRatingDistribution() {
        Review review1 = new Review("Test Restaurant", "User1", "user1@example.com", 5, "Excellent!");
        Review review2 = new Review("Test Restaurant", "User2", "user2@example.com", 4, "Great!");
        Review review3 = new Review("Test Restaurant", "User3", "user3@example.com", 4, "Good!");
        Review review4 = new Review("Test Restaurant", "User4", "user4@example.com", 3, "Average");
        Review review5 = new Review("Test Restaurant", "User5", "user5@example.com", 2, "Poor");
        
        reviewService.addReview(review1);
        reviewService.addReview(review2);
        reviewService.addReview(review3);
        reviewService.addReview(review4);
        reviewService.addReview(review5);
        
        int[] distribution = reviewService.getRatingDistribution("Test Restaurant");
        
        assertEquals(0, distribution[0]); // 1 star
        assertEquals(1, distribution[1]); // 2 stars
        assertEquals(1, distribution[2]); // 3 stars
        assertEquals(2, distribution[3]); // 4 stars
        assertEquals(1, distribution[4]); // 5 stars
    }
    
    @Test
    void testHasUserReviewedRestaurant() {
        Review review = new Review("Test Restaurant", "Test User", "test@example.com", 4, "Great food!");
        reviewService.addReview(review);
        
        assertTrue(reviewService.hasUserReviewedRestaurant("Test User", "Test Restaurant"));
        assertFalse(reviewService.hasUserReviewedRestaurant("Other User", "Test Restaurant"));
        assertFalse(reviewService.hasUserReviewedRestaurant("Test User", "Other Restaurant"));
    }
    
    @Test
    void testGetRecentReviews() {
        // Add reviews with different dates
        Review oldReview = new Review("Restaurant1", "User1", "user1@example.com", 4, "Old review");
        oldReview = new Review(oldReview.getId(), oldReview.getRestaurantId(), oldReview.getRestaurantName(), 
                oldReview.getUserEmail(), oldReview.getUserName(),
                oldReview.getRating(), oldReview.getComment(), LocalDate.now().minusDays(35), oldReview.isVerified(),
                oldReview.getRestaurateurResponse(), oldReview.getClientResponse());
        
        Review recentReview1 = new Review("Restaurant2", "User2", "user2@example.com", 5, "Recent review 1");
        recentReview1 = new Review(recentReview1.getId(), recentReview1.getRestaurantId(), recentReview1.getRestaurantName(),
                recentReview1.getUserEmail(), recentReview1.getUserName(),
                recentReview1.getRating(), recentReview1.getComment(), LocalDate.now().minusDays(10), recentReview1.isVerified(),
                recentReview1.getRestaurateurResponse(), recentReview1.getClientResponse());
        
        Review recentReview2 = new Review("Restaurant3", "User3", "user3@example.com", 3, "Recent review 2");
        recentReview2 = new Review(recentReview2.getId(), recentReview2.getRestaurantId(), recentReview2.getRestaurantName(),
                recentReview2.getUserEmail(), recentReview2.getUserName(),
                recentReview2.getRating(), recentReview2.getComment(), LocalDate.now().minusDays(5), recentReview2.isVerified(),
                recentReview2.getRestaurateurResponse(), recentReview2.getClientResponse());
        
        reviewService.addReview(oldReview);
        reviewService.addReview(recentReview1);
        reviewService.addReview(recentReview2);
        
        List<Review> recentReviews = reviewService.getRecentReviews(10);
        assertEquals(2, recentReviews.size()); // Only the 2 recent reviews
        
        // Should be sorted by date (newest first)
        assertTrue(recentReviews.get(0).getReviewDate().isAfter(recentReviews.get(1).getReviewDate()) ||
                   recentReviews.get(0).getReviewDate().isEqual(recentReviews.get(1).getReviewDate()));
    }
    
    @Test
    void testGetTopRatedRestaurants() {
        // Add reviews for different restaurants
        Review review1 = new Review("Restaurant A", "User1", "user1@example.com", 5, "Excellent!");
        Review review2 = new Review("Restaurant A", "User2", "user2@example.com", 5, "Amazing!");
        Review review3 = new Review("Restaurant A", "User3", "user3@example.com", 4, "Great!");
        
        Review review4 = new Review("Restaurant B", "User4", "user4@example.com", 4, "Good!");
        Review review5 = new Review("Restaurant B", "User5", "user5@example.com", 4, "Nice!");
        Review review6 = new Review("Restaurant B", "User6", "user6@example.com", 3, "Average");
        
        Review review7 = new Review("Restaurant C", "User7", "user7@example.com", 5, "Perfect!");
        Review review8 = new Review("Restaurant C", "User8", "user8@example.com", 5, "Outstanding!");
        Review review9 = new Review("Restaurant C", "User9", "user9@example.com", 5, "Fantastic!");
        
        reviewService.addReview(review1);
        reviewService.addReview(review2);
        reviewService.addReview(review3);
        reviewService.addReview(review4);
        reviewService.addReview(review5);
        reviewService.addReview(review6);
        reviewService.addReview(review7);
        reviewService.addReview(review8);
        reviewService.addReview(review9);
        
        List<String> topRestaurants = reviewService.getTopRatedRestaurants(3);
        
        assertEquals(3, topRestaurants.size());
        assertEquals("Restaurant C", topRestaurants.get(0)); // 5.0 average
        assertEquals("Restaurant A", topRestaurants.get(1)); // 4.67 average
        assertEquals("Restaurant B", topRestaurants.get(2)); // 3.67 average
    }
}
