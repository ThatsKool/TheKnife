package dev.theknife.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class UserTest {
    private User user;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(1990, 1, 15);
        user = new User("John", "Doe", "hashedPassword123", testDate, 40.7128, -74.0060, "Client");
    }

    @Test
    void testUserCreation() {
        assertNotNull(user);
        assertEquals("John", user.getName());
        assertEquals("Doe", user.getSurname());
        assertEquals("hashedPassword123", user.getPassword());
        assertEquals(testDate, user.getDateOfBirth());
        assertEquals(40.7128, user.getLatitude(), 0.001);
        assertEquals(-74.0060, user.getLongitude(), 0.001);
        assertEquals("Client", user.getRole());
    }

    @Test
    void testUserCreationWithNullDateOfBirth() {
        User userWithNullDate = new User("Jane", "Smith", "password456", null, 35.0, -80.0, "Restaurateur");
        assertNull(userWithNullDate.getDateOfBirth());
    }

    @Test
    void testSetters() {
        user.setName("Jane");
        user.setSurname("Smith");
        user.setPassword("newPassword");
        user.setDateOfBirth(LocalDate.of(1985, 5, 20));
        user.setLatitude(35.0);
        user.setLongitude(-80.0);
        user.setRole("Restaurateur");

        assertEquals("Jane", user.getName());
        assertEquals("Smith", user.getSurname());
        assertEquals("newPassword", user.getPassword());
        assertEquals(LocalDate.of(1985, 5, 20), user.getDateOfBirth());
        assertEquals(35.0, user.getLatitude(), 0.001);
        assertEquals(-80.0, user.getLongitude(), 0.001);
        assertEquals("Restaurateur", user.getRole());
    }

    @Test
    void testToStringWithDateOfBirth() {
        String expected = "John,Doe,hashedPassword123,1990-01-15,40.7128,-74.006,Client";
        String actual = user.toString();
        assertEquals(expected, actual);
    }

    @Test
    void testToStringWithNullDateOfBirth() {
        User userWithNullDate = new User("Jane", "Smith", "password456", null, 35.0, -80.0, "Restaurateur");
        String expected = "Jane,Smith,password456,,35.0,-80.0,Restaurateur";
        String actual = userWithNullDate.toString();
        assertEquals(expected, actual);
    }

    @Test
    void testToStringFormat() {
        String result = user.toString();
        String[] parts = result.split(",");
        
        assertEquals(7, parts.length);
        assertEquals("John", parts[0]);
        assertEquals("Doe", parts[1]);
        assertEquals("hashedPassword123", parts[2]);
        assertEquals("1990-01-15", parts[3]);
        assertEquals("40.7128", parts[4]);
        assertEquals("-74.006", parts[5]);
        assertEquals("Client", parts[6]);
    }
}
