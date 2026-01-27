/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.config.FileProvider;
import dev.theknife.app.dataaccess.CSVManager;
import dev.theknife.app.model.User;
import dev.theknife.app.util.Logger;
import dev.theknife.app.util.PasswordHasher;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementazione del servizio di gestione degli utenti.
 * <p>
 * Questa classe gestisce la persistenza degli utenti su file CSV e offre funzionalità di autenticazione sicura.
 * Implementa la logica di hashing delle password tramite {@link PasswordHasher} e gestisce la retrocompatibilità
 * con vecchi formati di dati (utenti senza email).
 * </p>
 * <p>
 * <b>Gestione della Sicurezza:</b>
 * Le password non vengono mai salvate in chiaro, ma vengono sottoposte a hashing (PBKDF2 o SHA-256)
 * prima della persistenza.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see IUserService
 * @see dev.theknife.app.model.User
 * @see dev.theknife.app.dataaccess.CSVManager
 */
public class UserService implements IUserService {
    
    // CAMPI
    /** Header del file CSV per la validazione e la scrittura. */
    private static final String HEADER = "Name,Surname,Email,Password,DateOfBirth,Latitude,Longitude,Role";
    
    /** Pattern regex per la validazione dell'email. */
    private static final java.util.regex.Pattern EMAIL_PATTERN = 
        java.util.regex.Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    /** Formattatore per le date di nascita nel CSV (ISO LOCAL DATE: yyyy-MM-dd). */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /** Gestore della persistenza CSV generico. */
    private final CSVManager<User> userManager;
    
    /** Logger di sistema. */
    private final Logger logger;

    /** Lista master di tutti gli utenti caricati in memoria. */
    private final List<User> users;
    
    /** Indice per lookup rapido O(1) tramite email (normalizzata in lowercase). */
    private final Map<String, User> byEmail = new HashMap<>();
    
    // COSTRUTTORI
    /**
     * Costruisce il servizio inizializzando il manager CSV e caricando i dati.
     *
     * @param fileProvider Il provider per l'accesso ai file di configurazione/dati.
     */
    public UserService(FileProvider fileProvider) {
        this.logger = Logger.getLogger(UserService.class);
        this.userManager = new CSVManager<>(
            "users.csv",
            HEADER,
            this::parseUserFromCsv,
            User::toString,
            fileProvider
        );
        this.users = loadAndIndex();
        
        // Ensure debug users exist if in debug mode
        if (dev.theknife.app.config.AppConfig.DEBUG) {
            ensureDebugUsers();
        }
    }
    
    // METODI
    /**
     * Assicura che gli utenti di test esistano quando si esegue in modalità debug.
     * <p>
     * Crea automaticamente un utente cliente e un utente ristoratore di test
     * se non esistono già nel sistema.
     * </p>
     */
    private void ensureDebugUsers() {
        try {
            // Check Test Client
            String clientEmail = dev.theknife.app.config.AppConfig.TEST_CLIENT_EMAIL;
            if (!emailExists(clientEmail)) {
                User client = new User(
                    "Test", 
                    "Client", 
                    clientEmail, 
                    dev.theknife.app.config.AppConfig.TEST_CLIENT_PASSWORD, 
                    LocalDate.of(1990, 1, 1), 
                    41.9028, 
                    12.4964, 
                    "Client"
                );
                saveUser(client);
                logger.info("Created debug client user: " + clientEmail);
            }

            // Check Test Restaurateur
            String restoEmail = dev.theknife.app.config.AppConfig.TEST_RESTAURATEUR_EMAIL;
            if (!emailExists(restoEmail)) {
                User restaurateur = new User(
                    "Test", 
                    "Restaurateur", 
                    restoEmail, 
                    dev.theknife.app.config.AppConfig.TEST_RESTAURATEUR_PASSWORD, 
                    LocalDate.of(1985, 5, 15), 
                    45.4642, 
                    9.1900, 
                    "Restaurateur"
                );
                saveUser(restaurateur);
                logger.info("Created debug restaurateur user: " + restoEmail);
            }
        } catch (Exception e) {
            logger.error("Failed to create debug users: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Utilizza {@link PasswordHasher#verifyPassword(String, String)} per confrontare
     * la password fornita con l'hash memorizzato.
     * </p>
     */
    @Override
    public boolean validateCredentials(String email, String password) throws IOException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        User user = findUserByEmail(email);
        if (user == null) return false;
        return PasswordHasher.verifyPassword(password, user.getPassword());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User findUserByEmail(String email) throws IOException {
        if (email == null) return null;
        return byEmail.get(email.toLowerCase());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean emailExists(String email) throws IOException {
        return findUserByEmail(email) != null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue automaticamente l'hashing della password se questa è in chiaro.
     * Verifica inoltre l'univocità dell'email prima del salvataggio.
     * </p>
     *
     * @throws IOException Se l'email esiste già o se fallisce la scrittura su disco.
     * @throws IllegalArgumentException Se i dati dell'utente non sono validi.
     */
    @Override
    public void saveUser(User user) throws IOException {
        validateUser(user);

        String pw = user.getPassword();
        // Hash password if not already hashed (PBKDF2 prefix check)
        if (pw == null || pw.isEmpty() || !pw.startsWith("PBKDF2$")) {
            String hashed = PasswordHasher.hashPassword(pw == null ? "" : pw);
            user = user.withPassword(hashed);
        }
        
        // Check for duplicate email
        if (user.getEmail() != null && byEmail.containsKey(user.getEmail().toLowerCase())) {
            throw new IOException("Email already exists: " + user.getEmail());
        }
        
        users.add(user);
        byEmail.put(user.getEmail().toLowerCase(), user);
        userManager.save(user);
        userManager.saveToDisk();
        logger.info("Saved user: " + user.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(User user) throws IOException {
        validateUser(user);

        // Ensure we are updating an existing user (email must match)
        if (user.getEmail() == null || !byEmail.containsKey(user.getEmail().toLowerCase())) {
             throw new IOException("User not found: " + user.getEmail());
        }

        // Replace in memory list
        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
             if (users.get(i).getEmail().equalsIgnoreCase(user.getEmail())) {
                 users.set(i, user);
                 found = true;
                 break;
             }
        }
        
        if (!found) {
            // Should not happen if it's in the map, but just in case
             throw new IOException("User data inconsistency: found in map but not in list");
        }

        // Update map
        byEmail.put(user.getEmail().toLowerCase(), user);
        
        // Replace in CSV Manager cache
        userManager.replace(u -> u.getEmail().equalsIgnoreCase(user.getEmail()), user);
        
        // Persist
        userManager.saveToDisk();
        logger.info("Updated user: " + user.getEmail());
    }

    /**
     * Valida i dati dell'utente.
     * 
     * @param user L'utente da validare.
     * @throws IllegalArgumentException Se i dati non sono validi.
     */
    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        if (user.getSurname() == null || user.getSurname().trim().isEmpty()) {
            throw new IllegalArgumentException("User surname cannot be empty");
        }
        if (user.getEmail() == null || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + user.getEmail());
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (user.getRole() == null || (!user.getRole().equalsIgnoreCase("Client") && !user.getRole().equalsIgnoreCase("Restaurateur"))) {
            throw new IllegalArgumentException("Invalid role: " + user.getRole() + ". Must be 'Client' or 'Restaurateur'");
        }
        if (user.getLatitude() < -90 || user.getLatitude() > 90) {
             throw new IllegalArgumentException("Invalid latitude: " + user.getLatitude() + ". Must be between -90 and 90");
        }
        if (user.getLongitude() < -180 || user.getLongitude() > 180) {
             throw new IllegalArgumentException("Invalid longitude: " + user.getLongitude() + ". Must be between -180 and 180");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> loadUsers() throws IOException {
        return new ArrayList<>(users);
    }

    /**
     * Analizza una riga del file CSV per creare un oggetto {@link User}.
     * <p>
     * Gestisce la migrazione da un vecchio formato (7 campi, senza email esplicita)
     * generando un'email fittizia basata sul nome.
     * </p>
     *
     * @param csvLine La riga del file CSV.
     * @return L'oggetto {@link User} o {@code null} se la riga non è valida.
     */
    private User parseUserFromCsv(String csvLine) {
        try {
            String[] parts = CSVManager.parseCSVLine(csvLine);
            
            // Expected format: Name,Surname,Email,Password,DateOfBirth,Latitude,Longitude,Role
            if (parts.length < 8) {
                logger.warn("Invalid CSV line (expected 8 fields, got " + parts.length + "): " + csvLine);
                return null;
            }
            
            String name = parts[0];
            String surname = parts[1];
            String email = parts[2];
            String password = parts[3];
            LocalDate dateOfBirth = parts[4].isEmpty() ? null : LocalDate.parse(parts[4], DATE_FORMATTER);
            double latitude = Double.parseDouble(parts[5]);
            double longitude = Double.parseDouble(parts[6]);
            String role = parts[7];

            // Ensure password is hashed if it looks like plain text (not starting with PBKDF2$ and not a 64-char hex string)
            if (password != null && !password.startsWith("PBKDF2$") && !password.matches("^[0-9a-fA-F]{64}$")) {
                password = PasswordHasher.hashPassword(password);
            }

            return new User(name, surname, email, password, dateOfBirth, latitude, longitude, role);
        } catch (Exception e) {
            logger.error("Error parsing user CSV line: " + csvLine, e);
            return null;
        }
    }

    /**
     * Carica tutti gli utenti dal CSV e costruisce l'indice per email.
     *
     * @return La lista degli utenti caricati.
     */
    private List<User> loadAndIndex() {
        List<User> list;
        try {
            list = new ArrayList<>(userManager.loadAll());
        } catch (IOException e) {
            logger.error("Error loading users: " + e.getMessage(), e);
            list = new ArrayList<>();
        }
        byEmail.clear();
        for (User u : list) {
            if (u != null && u.getEmail() != null) {
                byEmail.put(u.getEmail().toLowerCase(), u);
            }
        }
        logger.debug("Loaded " + list.size() + " users");
        return list;
    }
}
