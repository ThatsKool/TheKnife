/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import dev.theknife.app.config.DefaultFileProvider;
import dev.theknife.app.config.FileProvider;
import dev.theknife.app.model.Restaurant;

import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.*;

/**
 * Test suite per la classe {@link RestaurantService}.
 * <p>
 * Verifica le operazioni di gestione dei ristoranti, inclusa la creazione,
 * la ricerca e il recupero paginato.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class RestaurantServiceTest {
    
    private RestaurantService restaurantService;
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() throws Exception {
        Map<String, Path> csvPaths = new HashMap<>();
        csvPaths.put("michelin_my_maps.csv", tempDir.resolve("michelin_my_maps.csv"));
        csvPaths.put("users.csv", tempDir.resolve("users.csv"));
        csvPaths.put("reviews.csv", tempDir.resolve("reviews.csv"));
        csvPaths.put("favorites.csv", tempDir.resolve("favorites.csv"));
        FileProvider fileProvider = new DefaultFileProvider(csvPaths);
        restaurantService = new RestaurantService(fileProvider);
        createTestRestaurants();
    }
    
    private List<Restaurant> createTestRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        
        // Add test restaurants
        Restaurant r1 = new Restaurant(
            "Italian Place", 
            "123 Italy St", 
            "Rome, Italy", 
            "$$$", 
            "Italian", 
            12.496366, 
            41.902782, 
            "+391234567", 
            "", 
            "http://italianplace.com", 
            "2 Stars", 
            "1", 
            "WiFi, Parking",
            "Authentic Italian cuisine",
            "resto@example.com"
        );
        
        Restaurant r2 = new Restaurant(
            "French Bistro", 
            "456 Paris Ave", 
            "Paris, France", 
            "$$$$", 
            "French", 
            2.352222, 
            48.856613, 
            "+331234567", 
            "", 
            "http://frenchbistro.com", 
            "3 Stars", 
            "0", 
            "WiFi, Valet",
            "Classic French dishes",
            "resto@example.com"
        );
        
        Restaurant r3 = new Restaurant(
            "Sushi Bar", 
            "789 Tokyo Rd", 
            "Tokyo, Japan", 
            "$$", 
            "Japanese", 
            139.691711, 
            35.689487, 
            "+811234567", 
            "", 
            "http://sushibar.com", 
            "1 Star", 
            "1", 
            "WiFi, Takeout",
            "Fresh sushi and sashimi",
            "resto@example.com"
        );
        
        restaurantService.addRestaurant(r1);
        restaurantService.addRestaurant(r2);
        restaurantService.addRestaurant(r3);
        
        restaurants.add(r1);
        restaurants.add(r2);
        restaurants.add(r3);
        
        return restaurants;
    }
    
    @Test
    void testGetAllRestaurants() {
        List<Restaurant> restaurants = restaurantService.getRestaurantsRange(0, 100);
        assertEquals(3, restaurants.size());
        java.util.Set<String> names = new java.util.HashSet<>();
        for (Restaurant r : restaurants) {
            names.add(r.getName());
        }
        assertTrue(names.contains("Italian Place"));
        assertTrue(names.contains("French Bistro"));
        assertTrue(names.contains("Sushi Bar"));
    }
    
    @Test
    void testGetRestaurantsPage() {
        // Test first page with 2 items per page
        List<Restaurant> page1 = restaurantService.getRestaurantsRange(0, 2);
        assertEquals(2, page1.size());
        java.util.Set<String> page1Names = new java.util.HashSet<>();
        page1Names.add(page1.get(0).getName());
        page1Names.add(page1.get(1).getName());
        assertTrue(page1Names.contains("Italian Place"));
        assertTrue(page1Names.contains("French Bistro") || page1Names.contains("Sushi Bar"));
        
        // Test second page with 2 items per page
        List<Restaurant> page2 = restaurantService.getRestaurantsRange(2, 2);
        assertEquals(1, page2.size());
        assertTrue(java.util.Set.of("Italian Place", "French Bistro", "Sushi Bar").contains(page2.get(0).getName()));
        
        // Test empty page (beyond available data)
        List<Restaurant> emptyPage = restaurantService.getRestaurantsRange(5, 2);
        assertTrue(emptyPage.isEmpty());
    }
    
    @Test
    void testGetTotalRestaurantCount() {
        assertEquals(3, restaurantService.getTotalRestaurantCount());
    }
    
    @Test
    void testSearchRestaurants() {
        // Test search by name
        List<Restaurant> italianResults = restaurantService.searchRestaurantsRange("Italian", 0, 10);
        assertEquals(1, italianResults.size());
        assertEquals("Italian Place", italianResults.get(0).getName());
        
        // Test search by cuisine
        List<Restaurant> frenchResults = restaurantService.searchRestaurantsRange("French", 0, 10);
        assertEquals(1, frenchResults.size());
        assertEquals("French Bistro", frenchResults.get(0).getName());
        
        // Test search by location
        List<Restaurant> tokyoResults = restaurantService.searchRestaurantsRange("Tokyo", 0, 10);
        assertEquals(1, tokyoResults.size());
        assertEquals("Sushi Bar", tokyoResults.get(0).getName());
        
        // Test search by award
        List<Restaurant> starResults = restaurantService.searchRestaurantsRange("3 Stars", 0, 10);
        assertEquals(1, starResults.size());
        assertEquals("French Bistro", starResults.get(0).getName());
        
        // Test empty search
        // Test search with no matches
        List<Restaurant> noResults = restaurantService.searchRestaurantsRange("Mexican", 0, 10);
        assertEquals(0, noResults.size());
    }
}
