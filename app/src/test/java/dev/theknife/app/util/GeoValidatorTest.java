/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite per la classe {@link dev.theknife.app.util.GeoValidator}.
 * <p>
 * Verifica la validazione delle coordinate geografiche (latitudine e longitudine)
 * secondo lo standard WGS84.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class GeoValidatorTest {

    @Test
    void testValidLatitude() {
        assertTrue(GeoValidator.isValidLatitude(0));
        assertTrue(GeoValidator.isValidLatitude(90));
        assertTrue(GeoValidator.isValidLatitude(-90));
        assertTrue(GeoValidator.isValidLatitude(45.5));
    }

    @Test
    void testInvalidLatitude() {
        assertFalse(GeoValidator.isValidLatitude(90.1));
        assertFalse(GeoValidator.isValidLatitude(-90.1));
        assertFalse(GeoValidator.isValidLatitude(100));
        assertFalse(GeoValidator.isValidLatitude(-100));
    }

    @Test
    void testValidLongitude() {
        assertTrue(GeoValidator.isValidLongitude(0));
        assertTrue(GeoValidator.isValidLongitude(180));
        assertTrue(GeoValidator.isValidLongitude(-180));
        assertTrue(GeoValidator.isValidLongitude(120.5));
    }

    @Test
    void testInvalidLongitude() {
        assertFalse(GeoValidator.isValidLongitude(180.1));
        assertFalse(GeoValidator.isValidLongitude(-180.1));
        assertFalse(GeoValidator.isValidLongitude(200));
        assertFalse(GeoValidator.isValidLongitude(-200));
    }
    
    @Test
    void testValidateCoordinatesSuccess() {
        assertDoesNotThrow(() -> GeoValidator.validateCoordinates(45, 90));
    }
    
    @Test
    void testValidateCoordinatesFailure() {
        assertThrows(IllegalArgumentException.class, () -> GeoValidator.validateCoordinates(91, 0));
        assertThrows(IllegalArgumentException.class, () -> GeoValidator.validateCoordinates(0, 181));
    }
}
