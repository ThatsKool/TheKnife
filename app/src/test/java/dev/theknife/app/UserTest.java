/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import dev.theknife.app.model.User;

/**
 * Test suite per la classe modello {@link User}.
 * <p>
 * Verifica la creazione, l'immutabilità e la serializzazione degli utenti,
 * inclusa la gestione di campi opzionali come la data di nascita.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class UserTest {
    private User user;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(1990, 1, 15);
        user = new User("John", "Doe", "john.doe@example.com", "hashedPassword123", testDate, 40.7128, -74.0060, "Client");
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals("John", user.getName());
        assertEquals("Doe", user.getSurname());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("hashedPassword123", user.getPassword());
        assertEquals(testDate, user.getDateOfBirth());
        assertEquals(40.7128, user.getLatitude(), 0.001);
        assertEquals(-74.0060, user.getLongitude(), 0.001);
        assertEquals("Client", user.getRole());
    }

    @Test
    void testUserCreationWithNullDateOfBirth() {
        User userWithNullDate = new User("Jane", "Smith", "jane.smith@example.com", "password456", null, 35.0, -80.0, "Restaurateur");
        assertNull(userWithNullDate.getDateOfBirth());
    }

    @Test
    void testImmutability() {
        User updated = new User("Jane", "Smith", "jane.smith@example.com", "newPassword",
                LocalDate.of(1985, 5, 20), 35.0, -80.0, "Restaurateur");

        assertEquals("John", user.getName());
        assertEquals("Jane", updated.getName());
    }

    @Test
    void testToStringWithDateOfBirth() {
        String expected = "John,Doe,john.doe@example.com,hashedPassword123,1990-01-15,40.7128,-74.006,Client";
        String actual = user.toString();
        assertEquals(expected, actual);
    }

    @Test
    void testToStringWithNullDateOfBirth() {
        User userWithNullDate = new User("Jane", "Smith", "jane.smith@example.com", "password456", null, 35.0, -80.0, "Restaurateur");
        String expected = "Jane,Smith,jane.smith@example.com,password456,,35.0,-80.0,Restaurateur";
        String actual = userWithNullDate.toString();
        assertEquals(expected, actual);
    }

    @Test
    void testToStringFormat() {
        String result = user.toString();
        String[] parts = result.split(",");
        
        assertEquals(8, parts.length);
        assertEquals("John", parts[0]);
        assertEquals("Doe", parts[1]);
        assertEquals("john.doe@example.com", parts[2]);
        assertEquals("hashedPassword123", parts[3]);
        assertEquals("1990-01-15", parts[4]);
        assertEquals("40.7128", parts[5]);
        assertEquals("-74.006", parts[6]);
        assertEquals("Client", parts[7]);
    }
}
