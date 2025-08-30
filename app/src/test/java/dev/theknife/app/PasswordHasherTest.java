package dev.theknife.app;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    void testHashPassword() {
        String password = "testPassword123";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertEquals(64, hashedPassword.length()); // SHA-256 produces 64 character hex string
    }

    @Test
    void testHashPasswordEmptyString() {
        String password = "";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertEquals(64, hashedPassword.length());
    }

    @Test
    void testHashPasswordSpecialCharacters() {
        String password = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);
        assertEquals(64, hashedPassword.length());
    }

    @Test
    void testHashPasswordConsistency() {
        String password = "samePassword";
        String hash1 = PasswordHasher.hashPassword(password);
        String hash2 = PasswordHasher.hashPassword(password);
        
        assertEquals(hash1, hash2);
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
        
        assertThrows(NullPointerException.class, () -> {
            PasswordHasher.verifyPassword(null, hashedPassword);
        });
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
        assertEquals(64, hashedPassword.length());
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword));
    }
}
