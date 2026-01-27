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

/**
 * Test suite per la classe modello {@link Restaurant}.
 * <p>
 * Verifica la creazione e l'immutabilità degli oggetti Restaurant,
 * garantendo che le modifiche non alterino l'istanza originale.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class RestaurantTest {
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant(
            "Test Restaurant", 
            "123 Test St", 
            "Test City", 
            "$$", 
            "Test Cuisine", 
            10.0, 
            20.0, 
            "+1234567890", 
            "http://test.com", 
            "http://test-website.com", 
            "1 Star", 
            "1", 
            "WiFi, Parking",
            "A test restaurant"
        );
    }

    @Test
    void testRestaurantCreation() {
        assertNotNull(restaurant);
        assertEquals("Test Restaurant", restaurant.getName());
        assertEquals("123 Test St", restaurant.getAddress());
        assertEquals("Test City", restaurant.getLocation());
        assertEquals("$$", restaurant.getPrice());
        assertEquals("Test Cuisine", restaurant.getCuisine());
        assertEquals(10.0, restaurant.getLongitude(), 0.0001);
        assertEquals(20.0, restaurant.getLatitude(), 0.0001);
        assertEquals("+1234567890", restaurant.getPhoneNumber());
        assertEquals("http://test.com", restaurant.getUrl());
        assertEquals("http://test-website.com", restaurant.getWebsiteUrl());
        assertEquals("1 Star", restaurant.getAward());
        assertEquals("1", restaurant.getGreenStar());
        assertEquals("A test restaurant", restaurant.getDescription());
    }

    @Test
    void testImmutability() {
        Restaurant updated = new Restaurant(
            "Updated Restaurant",
            "456 Updated St",
            "Updated City",
            "$$$",
            "Updated Cuisine",
            30.0,
            40.0,
            "+9876543210",
            "http://updated.com",
            "http://updated-website.com",
            "2 Stars",
            "0",
            "WiFi, Parking",
            "An updated test restaurant"
        );

        assertEquals("Test Restaurant", restaurant.getName());
        assertEquals("Updated Restaurant", updated.getName());
        assertEquals(30.0, updated.getLongitude(), 0.0001);
        assertEquals(40.0, updated.getLatitude(), 0.0001);
    }
}
