/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

/**
 * Test suite per la classe modello {@link Review}.
 * <p>
 * Verifica la creazione, validazione e formattazione delle recensioni,
 * inclusi i metodi di utilità per la visualizzazione.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class ReviewTest {
    private Review review;
    
    @BeforeEach
    void setUp() {
        review = new Review("REST_001", null, "Test Restaurant", null, "Test User", 4, 
                           "Great food and service!", LocalDate.now(), false, null, null);
    }
    
    @Test
    void testReviewCreation() {
        assertNotNull(review);
        assertEquals("REST_001", review.getId());
        assertEquals("Test Restaurant", review.getRestaurantName());
        assertEquals("Test User", review.getUserName());
        assertEquals(4, review.getRating());
        assertEquals("Great food and service!", review.getComment());
        assertFalse(review.isVerified());
    }
    
    @Test
    void testReviewCreationWithConstructor() {
        Review newReview = new Review("Test Restaurant", "Test User", "test@example.com", 5, "Excellent!");
        
        assertNotNull(newReview);
        assertNotNull(newReview.getId());
        assertEquals("Test Restaurant", newReview.getRestaurantName());
        assertEquals("Test User", newReview.getUserName());
        assertEquals(5, newReview.getRating());
        assertEquals("Excellent!", newReview.getComment());
        assertFalse(newReview.isVerified());
        assertNotNull(newReview.getReviewDate());
    }
    
    @Test
    void testRatingValidation() {
        // Test rating bounds
        Review review1 = new Review("Test Restaurant", "Test User", "test@example.com", 0, "Test");
        assertEquals(1, review1.getRating()); // Should be clamped to 1
        
        Review review2 = new Review("Test Restaurant", "Test User", "test@example.com", 6, "Test");
        assertEquals(5, review2.getRating()); // Should be clamped to 5
        
        Review review3 = new Review("Test Restaurant", "Test User", "test@example.com", 3, "Test");
        assertEquals(3, review3.getRating()); // Should remain 3
    }
    
    @Test
    void testRatingStars() {
        assertEquals("★★★☆☆", review.withRatingAndComment(3, review.getComment()).getRatingStars());
        assertEquals("★★★★★", review.withRatingAndComment(5, review.getComment()).getRatingStars());
        assertEquals("★☆☆☆☆", review.withRatingAndComment(1, review.getComment()).getRatingStars());
    }
    
    @Test
    void testTruncatedComment() {
        String longComment = "This is a very long comment that should be truncated when displayed in the UI to prevent layout issues and maintain a clean appearance.";
        Review updated = review.withRatingAndComment(review.getRating(), longComment);
        
        String truncated = updated.getTruncatedComment(50);
        assertEquals(47, truncated.length()); // 50 - 3 for "..."
        assertTrue(truncated.endsWith("..."));
        
        String shortComment = "Short comment";
        Review updated2 = review.withRatingAndComment(review.getRating(), shortComment);
        assertEquals(shortComment, updated2.getTruncatedComment(50));
    }
    
    @Test
    void testFormattedDate() {
        // Review now formats LocalDate as ISO
        String formatted = review.getFormattedDate();
        assertNotNull(formatted);
    }
    
    @Test
    void testValidation() {
        // Valid review
        assertTrue(review.isValid());
        
        // Invalid reviews
        Review invalidReview1 = new Review(null, null, "Test Restaurant", null, "Test User", 4, "Test", LocalDate.now(), false, null, null);
        assertFalse(invalidReview1.isValid());
        
        Review invalidReview2 = new Review("ID", null, "", null, "Test User", 4, "Test", LocalDate.now(), false, null, null);
        assertFalse(invalidReview2.isValid());
        
        Review invalidReview3 = new Review("ID", null, "Test Restaurant", null, "", 4, "Test", LocalDate.now(), false, null, null);
        assertFalse(invalidReview3.isValid());
        
        Review invalidReview4 = new Review("ID", null, "Test Restaurant", null, "Test User", 4, "", LocalDate.now(), false, null, null);
        assertFalse(invalidReview4.isValid());
        
        Review invalidReview5 = new Review("ID", null, "Test Restaurant", null, "Test User", 4, "Test", null, false, null, null);
        assertFalse(invalidReview5.isValid());
    }
    
    @Test
    void testToString() {
        String reviewString = review.toString();
        assertTrue(reviewString.contains("REST_001"));
        assertTrue(reviewString.contains("Test Restaurant"));
        assertTrue(reviewString.contains("Test User"));
        assertTrue(reviewString.contains("4"));
        assertTrue(reviewString.contains("Great food and service!"));
    }
    
    @Test
    void testEqualsAndHashCode() {
        Review review1 = new Review("ID1", null, "Restaurant", null, "User", 4, "Comment", LocalDate.now(), false, null, null);
        Review review2 = new Review("ID1", null, "Different Restaurant", null, "Different User", 5, "Different Comment", LocalDate.now(), true, null, null);
        Review review3 = new Review("ID2", null, "Restaurant", null, "User", 4, "Comment", LocalDate.now(), false, null, null);
        
        assertEquals(review1, review2); // Same ID
        assertNotEquals(review1, review3); // Different ID
        assertEquals(review1.hashCode(), review2.hashCode());
        assertNotEquals(review1.hashCode(), review3.hashCode());
    }
}
