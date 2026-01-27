/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import dev.theknife.app.util.PasswordHasher;

/**
 * Test suite per la classe {@link dev.theknife.app.util.PasswordHasher}.
 * <p>
 * Verifica l'hashing sicuro delle password utilizzando PBKDF2,
 * inclusa la verifica delle password e la gestione di casi limite.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class PasswordHasherTest {

    @Test
    void testHashPassword() {
        String password = "testPassword123";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertTrue(hashedPassword.startsWith("PBKDF2$"));
    }

    @Test
    void testHashPasswordEmptyString() {
        String password = "";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertTrue(hashedPassword.startsWith("PBKDF2$"));
    }

    @Test
    void testHashPasswordSpecialCharacters() {
        String password = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertTrue(hashedPassword.startsWith("PBKDF2$"));
    }

    @Test
    void testHashPasswordConsistency() {
        String password = "samePassword";
        String hash1 = PasswordHasher.hashPassword(password);
        String hash2 = PasswordHasher.hashPassword(password);
        
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testVerifyPasswordCorrect() {
        String password = "testPassword123";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword));
    }

    @Test
    void testVerifyPasswordIncorrect() {
        String password = "testPassword123";
        String wrongPassword = "wrongPassword";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertFalse(PasswordHasher.verifyPassword(wrongPassword, hashedPassword));
    }

    @Test
    void testVerifyPasswordEmptyString() {
        String password = "";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword));
    }

    @Test
    void testVerifyPasswordNullPassword() {
        String password = "testPassword";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertFalse(PasswordHasher.verifyPassword(null, hashedPassword));
    }

    @Test
    void testVerifyPasswordNullHash() {
        String password = "testPassword";
        
        assertFalse(PasswordHasher.verifyPassword(password, null));
    }

    @Test
    void testHashPasswordUnicode() {
        String password = "pässwörd测试";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword));
    }
}
