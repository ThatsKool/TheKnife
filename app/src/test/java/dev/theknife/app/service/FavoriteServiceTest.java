/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.config.FileProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per la classe {@link FavoriteService}.
 * <p>
 * Verifica la gestione dei ristoranti preferiti, inclusa l'aggiunta,
 * la rimozione e la persistenza delle associazioni utente-ristorante.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
class FavoriteServiceTest {

    private Path tempDir;
    private FileProvider fileProvider;
    private FavoriteService favoriteService;
    private File favoritesFile;

    @BeforeEach
    void setUp() throws IOException {
        tempDir = Files.createTempDirectory("theknife_test");
        favoritesFile = tempDir.resolve("favorites.csv").toFile();
        
        fileProvider = new FileProvider() {
            @Override
            public Path getCsvPath(String fileName) {
                if (fileName.equals("favorites.csv")) {
                    return favoritesFile.toPath();
                }
                return tempDir.resolve(fileName);
            }

            @Override
            public boolean hasCsvPath(String fileName) {
                return true;
            }
            
            // Implement other methods if needed, or make FileProvider an interface/abstract class mock
            // Assuming FileProvider is a class we can override or it's an interface.
            // Based on previous reads, it seems to be a class.
        };

        favoriteService = new FavoriteService(fileProvider);
    }

    @AfterEach
    void tearDown() throws IOException {
        // Cleanup
        if (favoritesFile.exists()) {
            favoritesFile.delete();
        }
        Files.deleteIfExists(tempDir);
    }

    @Test
    void testRemoveAllFavorites() {
        String user = "testUser";
        String rest1 = "Pizza Place";
        String rest2 = "Burger Joint";

        // 1. Add 2 favorites
        assertTrue(favoriteService.addFavorite(user, rest1));
        assertTrue(favoriteService.addFavorite(user, rest2));

        assertEquals(2, favoriteService.getUserFavorites(user).size());

        // 2. Remove first favorite
        assertTrue(favoriteService.removeFavorite(user, rest1));
        assertEquals(1, favoriteService.getUserFavorites(user).size());
        assertEquals(rest2, favoriteService.getUserFavorites(user).get(0));

        // 3. Remove second favorite (the last one)
        assertTrue(favoriteService.removeFavorite(user, rest2));
        assertEquals(0, favoriteService.getUserFavorites(user).size());
        
        // 4. Verify persistence
        // Reload service to simulate app restart
        FavoriteService newService = new FavoriteService(fileProvider);
        assertEquals(0, newService.getUserFavorites(user).size());
    }
}
