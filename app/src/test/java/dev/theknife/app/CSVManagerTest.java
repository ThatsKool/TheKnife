package dev.theknife.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class CSVManagerTest {
    
    @TempDir
    Path tempDir;
    
    private File testCsvFile;
    private String originalCsvPath;

    @BeforeEach
    void setUp() throws IOException {
        // Store original CSV path
        originalCsvPath = CSVManager.getCsvFilePath();
        
        // Create test CSV file in temp directory
        testCsvFile = tempDir.resolve("test_users.csv").toFile();
        CSVManager.setCsvFilePath(testCsvFile.getAbsolutePath());
        
        // Create header
        CSVManager.createCSVHeader();
    }

    @AfterEach
    void tearDown() {
        // Restore original CSV path
        CSVManager.setCsvFilePath(originalCsvPath);
    }

    @Test
    void testCreateCSVHeader() throws IOException {
        File newFile = tempDir.resolve("new_users.csv").toFile();
        CSVManager.setCsvFilePath(newFile.getAbsolutePath());
        
        CSVManager.createCSVHeader();
        
        assertTrue(newFile.exists());
        List<String> lines = java.nio.file.Files.readAllLines(newFile.toPath());
        assertEquals(1, lines.size());
        assertEquals("Name,Surname,Password,DateOfBirth,Latitude,Longitude,Role", lines.get(0));
    }

    @Test
    void testSaveUser() throws IOException {
        User user = new User("John", "Doe", "hashedPassword", 
                           LocalDate.of(1990, 1, 15), 40.7128, -74.0060, "Client");
        
        CSVManager.saveUser(user);
        
        List<String> lines = java.nio.file.Files.readAllLines(testCsvFile.toPath());
        assertEquals(2, lines.size()); // Header + 1 user
        assertEquals("John,Doe,hashedPassword,1990-01-15,40.7128,-74.006,Client", lines.get(1));
    }

    @Test
    void testSaveUserWithNullDateOfBirth() throws IOException {
        User user = new User("Jane", "Smith", "hashedPassword", 
                           null, 35.0, -80.0, "Restaurateur");
        
        CSVManager.saveUser(user);
        
        List<String> lines = java.nio.file.Files.readAllLines(testCsvFile.toPath());
        assertEquals(2, lines.size());
        assertEquals("Jane,Smith,hashedPassword,,35.0,-80.0,Restaurateur", lines.get(1));
    }

    @Test
    void testLoadUsers() throws IOException {
        User user1 = new User("John", "Doe", "hash1", 
                             LocalDate.of(1990, 1, 15), 40.7128, -74.0060, "Client");
        User user2 = new User("Jane", "Smith", "hash2", 
                             null, 35.0, -80.0, "Restaurateur");
        
        CSVManager.saveUser(user1);
        CSVManager.saveUser(user2);
        
        List<User> users = CSVManager.loadUsers();
        
        assertEquals(2, users.size());
        assertEquals("John", users.get(0).getName());
        assertEquals("Jane", users.get(1).getName());
    }

    @Test
    void testLoadUsersEmptyFile() throws IOException {
        List<User> users = CSVManager.loadUsers();
        assertEquals(0, users.size());
    }

    @Test
    void testFindUserByName() throws IOException {
        User user = new User("John", "Doe", "hashedPassword", 
                           LocalDate.of(1990, 1, 15), 40.7128, -74.0060, "Client");
        CSVManager.saveUser(user);
        
        User found = CSVManager.findUserByName("John");
        assertNotNull(found);
        assertEquals("John", found.getName());
        assertEquals("Doe", found.getSurname());
    }

    @Test
    void testFindUserByNameNotFound() throws IOException {
        User found = CSVManager.findUserByName("Nonexistent");
        assertNull(found);
    }

    @Test
    void testValidateCredentials() throws IOException {
        String password = "testPassword";
        String hashedPassword = PasswordHasher.hashPassword(password);
        User user = new User("John", "Doe", hashedPassword, 
                           LocalDate.of(1990, 1, 15), 40.7128, -74.0060, "Client");
        CSVManager.saveUser(user);
        
        assertTrue(CSVManager.validateCredentials("John", hashedPassword));
        assertFalse(CSVManager.validateCredentials("John", "wrongHash"));
        assertFalse(CSVManager.validateCredentials("Jane", hashedPassword));
    }

    @Test
    void testValidateCredentialsWithPasswordHasher() throws IOException {
        String password = "testPassword";
        String hashedPassword = PasswordHasher.hashPassword(password);
        User user = new User("John", "Doe", hashedPassword, 
                           LocalDate.of(1990, 1, 15), 40.7128, -74.0060, "Client");
        CSVManager.saveUser(user);
        
        assertTrue(CSVManager.validateCredentials("John", hashedPassword));
        assertTrue(PasswordHasher.verifyPassword(password, hashedPassword));
    }

    @Test
    void testLoadUsersWithInvalidData() throws IOException {
        // Add invalid line to CSV
        java.nio.file.Files.write(testCsvFile.toPath(), 
            ("Name,Surname,Password,DateOfBirth,Latitude,Longitude,Role\n" +
            "John,Doe,hash,invalid-date,40.7128,-74.006,Client\n").getBytes(),
            java.nio.file.StandardOpenOption.APPEND);
        
        List<User> users = CSVManager.loadUsers();
        assertEquals(0, users.size()); // Invalid line should be skipped
    }

    @Test
    void testLoadUsersWithIncompleteData() throws IOException {
        // Add incomplete line to CSV
        java.nio.file.Files.write(testCsvFile.toPath(), 
            ("Name,Surname,Password,DateOfBirth,Latitude,Longitude,Role\n" +
            "John,Doe,hash\n").getBytes(),
            java.nio.file.StandardOpenOption.APPEND);
        
        List<User> users = CSVManager.loadUsers();
        assertEquals(0, users.size()); // Incomplete line should be skipped
    }
}
