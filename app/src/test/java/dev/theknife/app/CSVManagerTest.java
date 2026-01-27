/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import dev.theknife.app.config.DefaultFileProvider;
import dev.theknife.app.config.FileProvider;
import dev.theknife.app.service.UserService;
import dev.theknife.app.service.RestaurantService;
import dev.theknife.app.model.User;
import dev.theknife.app.model.Restaurant;
import dev.theknife.app.dataaccess.CSVManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test suite per la classe {@link dev.theknife.app.dataaccess.CSVManager}.
 * <p>
 * Verifica le operazioni di persistenza su file CSV, inclusa la creazione
 * degli header, il salvataggio e il caricamento di entità.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class CSVManagerTest {
    @TempDir
    Path tempDir;

    private FileProvider fileProvider;

    @BeforeEach
    void setUp() throws IOException {
        Map<String, Path> csvPaths = new HashMap<>();
        csvPaths.put("users.csv", tempDir.resolve("users.csv"));
        csvPaths.put("michelin_my_maps.csv", tempDir.resolve("michelin_my_maps.csv"));
        csvPaths.put("reviews.csv", tempDir.resolve("reviews.csv"));
        csvPaths.put("favorites.csv", tempDir.resolve("favorites.csv"));
        fileProvider = new DefaultFileProvider(csvPaths);
    }

    @Test
    void testCreateCSVHeader() throws IOException {
        UserService userService = new UserService(fileProvider);
        RestaurantService restaurantService = new RestaurantService(fileProvider);

        Path usersPath = tempDir.resolve("users.csv");
        assertTrue(Files.exists(usersPath));
        List<String> userLines = Files.readAllLines(usersPath);
        // Debug mode might add default users, so we check for at least the header
        assertTrue(userLines.size() >= 1);
        assertEquals("Name,Surname,Email,Password,DateOfBirth,Latitude,Longitude,Role", userLines.get(0));

        Path restaurantsPath = tempDir.resolve("michelin_my_maps.csv");
        assertTrue(Files.exists(restaurantsPath));
        List<String> restaurantLines = Files.readAllLines(restaurantsPath);
        // Restaurant service might not add debug items by default, but let's be safe
        assertTrue(restaurantLines.size() >= 1);
        assertEquals("Id,Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description,RestaurateurEmail",
            restaurantLines.get(0));
    }

    @Test
    void testSaveAndLoadUser() throws IOException {
        UserService userService = new UserService(fileProvider);

        String password = "hashedPassword";
        User user = new User("John", "Doe", "john.doe@example.com",
                password, LocalDate.of(1990, 1, 15), 40.7128, -74.0060, "Client");

        userService.saveUser(user);

        List<User> users = userService.loadUsers();
        // Check if our user exists in the list
        boolean found = users.stream().anyMatch(u -> 
            u.getEmail().equals("john.doe@example.com") && 
            u.getName().equals("John") &&
            u.getSurname().equals("Doe")
        );
        assertTrue(found, "Saved user should be found in loaded users");
    }

    @Test
    void testSaveAndLoadRestaurant() throws IOException {
        RestaurantService restaurantService = new RestaurantService(fileProvider);

        Restaurant restaurant = new Restaurant(
            "Test Restaurant",
            "123 Test St",
            "Test City",
            "$$",
            "Test Cuisine",
            0.0,
            0.0,
            "+1234567890",
            "",
            "",
            "1 Star",
            "0",
            "WiFi, Parking",
            "A test restaurant",
            "resto@example.com"
        );

        boolean added = restaurantService.addRestaurant(restaurant);
        assertTrue(added);

        List<Restaurant> restaurants = restaurantService.getRestaurantsRange(0, 100);
        Restaurant foundRestaurant = restaurants.stream()
            .filter(r -> r.getName().equals("Test Restaurant"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(foundRestaurant, "Should find the saved restaurant");
        assertEquals("Test Restaurant", foundRestaurant.getName());
        assertEquals("123 Test St", foundRestaurant.getAddress());
        assertEquals("Test City", foundRestaurant.getLocation());
        assertEquals("$$", foundRestaurant.getPrice());
        assertEquals("Test Cuisine", foundRestaurant.getCuisine());
    }

    @Test
    void testSaveUserWithNullDateOfBirth() throws IOException {
        UserService userService = new UserService(fileProvider);

        User user = new User("Jane", "Smith", "jane.smith@example.com",
                "hashedPassword", null, 35.0, -80.0, "Restaurateur");

        userService.saveUser(user);

        List<User> users = userService.loadUsers();
        User foundUser = users.stream()
            .filter(u -> u.getEmail().equals("jane.smith@example.com"))
            .findFirst()
            .orElse(null);
            
        assertNotNull(foundUser, "Should find the saved user");
        assertEquals("Jane", foundUser.getName());
        assertNull(foundUser.getDateOfBirth());
    }

    @Test
    void testParseCSVLine() {
        String simpleLine = "value1,value2,value3";
        String[] simpleResult = CSVManager.parseCSVLine(simpleLine);
        assertEquals(3, simpleResult.length);
        assertEquals("value1", simpleResult[0]);
        assertEquals("value2", simpleResult[1]);
        assertEquals("value3", simpleResult[2]);

        String complexLine = "\"John, Jr.\",\"Doe, Sr.\",hashedPassword,1990-01-15,40.7128,-74.006,Client";
        String[] complexResult = CSVManager.parseCSVLine(complexLine);
        assertEquals(7, complexResult.length);
        assertEquals("John, Jr.", complexResult[0]);
        assertEquals("Doe, Sr.", complexResult[1]);
        assertEquals("hashedPassword", complexResult[2]);

        String escapedQuotesLine = "\"He said \"\"Hello\"\"\",plain,\",\",end";
        String[] escapedQuotesResult = CSVManager.parseCSVLine(escapedQuotesLine);
        assertEquals(4, escapedQuotesResult.length);
        assertEquals("He said \"Hello\"", escapedQuotesResult[0]);
        assertEquals("plain", escapedQuotesResult[1]);
        assertEquals(",", escapedQuotesResult[2]);
        assertEquals("end", escapedQuotesResult[3]);

        String emptyFieldsLine = "a,,\"\",b,";
        String[] emptyFieldsResult = CSVManager.parseCSVLine(emptyFieldsLine);
        assertEquals(5, emptyFieldsResult.length);
        assertEquals("a", emptyFieldsResult[0]);
        assertEquals("", emptyFieldsResult[1]);
        assertEquals("", emptyFieldsResult[2]);
        assertEquals("b", emptyFieldsResult[3]);
        assertEquals("", emptyFieldsResult[4]);
    }
}
