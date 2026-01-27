/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.model.User;
import dev.theknife.app.config.FileProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per le funzionalità di aggiornamento del {@link UserService}.
 * <p>
 * Verifica la persistenza e l'aggiornamento dei dati utente, con particolare
 * attenzione alla modifica delle coordinate geografiche.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class UserServiceUpdateTest {

    private Path tempFile;
    private UserService userService;
    private FileProvider testFileProvider;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary file for users
        tempFile = Files.createTempFile("users_test", ".csv");
        
        // Write header
        Files.writeString(tempFile, "Name,Surname,Email,Password,DateOfBirth,Latitude,Longitude,Role\n");

        testFileProvider = new FileProvider() {
            @Override
            public Path getCsvPath(String fileName) {
                if ("users.csv".equals(fileName)) {
                    return tempFile;
                }
                throw new IllegalArgumentException("Unknown file: " + fileName);
            }

            @Override
            public boolean hasCsvPath(String fileName) {
                return "users.csv".equals(fileName);
            }
        };

        userService = new UserService(testFileProvider);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void testUpdateUserLocation() throws IOException {
        // 1. Create and register a user
        String email = "testupdate@example.com";
        User originalUser = new User(
            "Test", "User", email, "password123", 
            LocalDate.of(1990, 1, 1), 0.0, 0.0, "Client"
        );
        userService.saveUser(originalUser);

        // Verify initial state
        User retrieved = userService.findUserByEmail(email);
        assertNotNull(retrieved);
        assertEquals(0.0, retrieved.getLatitude());
        assertEquals(0.0, retrieved.getLongitude());

        // 2. Update user with new location
        double newLat = 45.5;
        double newLon = 9.2;
        User updatedUser = new User(
            originalUser.getName(), originalUser.getSurname(), originalUser.getEmail(),
            originalUser.getPassword(), originalUser.getDateOfBirth(),
            newLat, newLon, originalUser.getRole()
        );

        userService.updateUser(updatedUser);

        // 3. Verify in-memory update
        User afterUpdate = userService.findUserByEmail(email);
        assertEquals(newLat, afterUpdate.getLatitude());
        assertEquals(newLon, afterUpdate.getLongitude());

        // 4. Verify persistence (reload service)
        UserService newService = new UserService(testFileProvider);
        User persistedUser = newService.findUserByEmail(email);
        assertNotNull(persistedUser);
        assertEquals(newLat, persistedUser.getLatitude(), 0.0001);
        assertEquals(newLon, persistedUser.getLongitude(), 0.0001);
    }
    
    @Test
    void testUpdateNonExistentUser() {
        User nonExistentUser = new User(
            "Ghost", "User", "ghost@example.com", "pw", 
            LocalDate.now(), 0, 0, "Client"
        );
        
        assertThrows(IOException.class, () -> userService.updateUser(nonExistentUser));
    }
    
    @Test
    void testUpdateInvalidUser() {
         assertThrows(IllegalArgumentException.class, () -> userService.updateUser(null));
    }
}
