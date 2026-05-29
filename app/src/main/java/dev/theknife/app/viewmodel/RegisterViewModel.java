/*
 * ViewModel per la schermata di Registrazione.
 * Incapsula validazione, controllo email, hashing e creazione utente.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.User;
import dev.theknife.app.service.IUserService;
import dev.theknife.app.util.GeoValidator;
import dev.theknife.app.util.Logger;
import dev.theknife.app.util.PasswordHasher;

import java.io.IOException;
import java.time.LocalDate;

public class RegisterViewModel {

    public record RegisterFormData(
        String name,
        String surname,
        String email,
        String password,
        LocalDate dateOfBirth,
        String latitudeText,
        String longitudeText,
        String displayRole
    ) {
    }

    public record PasswordCriteria(
        boolean minLength,
        boolean uppercase,
        boolean number,
        boolean specialChar
    ) {
        public boolean isValid() {
            return minLength && uppercase && number && specialChar;
        }
    }

    public enum RegistrationStatus {
        SUCCESS,
        VALIDATION_ERROR,
        EMAIL_EXISTS,
        EMAIL_CHECK_ERROR,
        SAVE_ERROR,
        SERVICE_UNAVAILABLE
    }

    public static final class RegistrationResult {
        private final RegistrationStatus status;
        private final String message;
        private final User user;

        private RegistrationResult(RegistrationStatus status, String message, User user) {
            this.status = status;
            this.message = message;
            this.user = user;
        }

        public static RegistrationResult success(User user) {
            return new RegistrationResult(RegistrationStatus.SUCCESS, null, user);
        }

        public static RegistrationResult error(RegistrationStatus status, String message) {
            return new RegistrationResult(status, message, null);
        }

        public RegistrationStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }

        public boolean isSuccess() {
            return status == RegistrationStatus.SUCCESS;
        }
    }

    private final IUserService userService;
    private final Logger logger;

    public RegisterViewModel(IUserService userService) {
        this.userService = userService;
        this.logger = Logger.getLogger(RegisterViewModel.class);
    }

    public boolean isServiceAvailable() {
        return userService != null;
    }

    public PasswordCriteria evaluatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordCriteria(false, false, false, false);
        }
        boolean minLength = password.length() >= 8;
        boolean uppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean number = password.chars().anyMatch(Character::isDigit);
        boolean specialChar = password.chars().anyMatch(ch -> ch == '!' || ch == '@' || ch == '/');
        return new PasswordCriteria(minLength, uppercase, number, specialChar);
    }

    public boolean isPasswordValid(String password) {
        return evaluatePassword(password).isValid();
    }

    /**
     * Valida i dati, verifica l'email, hasha la password, crea e salva l'utente.
     */
    public RegistrationResult register(RegisterFormData form) {
        if (userService == null) {
            return RegistrationResult.error(
                RegistrationStatus.SERVICE_UNAVAILABLE,
                "Servizio non disponibile. Riprova."
            );
        }

        String validationError = validateForm(form);
        if (validationError != null) {
            return RegistrationResult.error(RegistrationStatus.VALIDATION_ERROR, validationError);
        }

        String email = form.email().trim();
        try {
            if (userService.emailExists(email)) {
                return RegistrationResult.error(
                    RegistrationStatus.EMAIL_EXISTS,
                    "Email già registrata! Usa un'altra email o accedi."
                );
            }
        } catch (IOException e) {
            logger.error("Email check failed", e);
            return RegistrationResult.error(
                RegistrationStatus.EMAIL_CHECK_ERROR,
                "Errore nel controllo della disponibilità dell'email. Riprova."
            );
        }

        if (!isPasswordValid(form.password())) {
            return RegistrationResult.error(
                RegistrationStatus.VALIDATION_ERROR,
                "Password non valida! Deve contenere almeno 8 caratteri, 1 lettera maiuscola, 1 numero e 1 carattere speciale (! @ /)."
            );
        }

        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(form.latitudeText().trim());
            longitude = Double.parseDouble(form.longitudeText().trim());
            GeoValidator.validateCoordinates(latitude, longitude);
        } catch (NumberFormatException e) {
            return RegistrationResult.error(
                RegistrationStatus.VALIDATION_ERROR,
                "Formato coordinate non valido!"
            );
        } catch (IllegalArgumentException e) {
            return RegistrationResult.error(
                RegistrationStatus.VALIDATION_ERROR,
                "Coordinate non valide! Latitudine: da -90 a 90, Longitudine: da -180 a 180"
            );
        }

        String hashedPassword = PasswordHasher.hashPassword(form.password());
        User user = new User(
            form.name().trim(),
            form.surname().trim(),
            email,
            hashedPassword,
            form.dateOfBirth(),
            latitude,
            longitude,
            mapDisplayRoleToInternal(form.displayRole())
        );

        try {
            userService.saveUser(user);
            return RegistrationResult.success(user);
        } catch (IOException e) {
            logger.error("Registration failed", e);
            return RegistrationResult.error(
                RegistrationStatus.SAVE_ERROR,
                "Registrazione fallita! Riprova."
            );
        }
    }

    private String validateForm(RegisterFormData form) {
        if (form.name() == null || form.name().trim().isEmpty()
            || form.surname() == null || form.surname().trim().isEmpty()
            || form.email() == null || form.email().trim().isEmpty()
            || form.password() == null || form.password().isEmpty()
            || form.latitudeText() == null || form.latitudeText().trim().isEmpty()
            || form.longitudeText() == null || form.longitudeText().trim().isEmpty()) {
            return "Inserisci tutti i campi obbligatori!";
        }

        String email = form.email().trim();
        if (!isValidEmailFormat(email)) {
            return "Inserisci un indirizzo email valido!";
        }

        return null;
    }

    private boolean isValidEmailFormat(String email) {
        return email.contains("@") && email.contains(".");
    }

    private String mapDisplayRoleToInternal(String displayRole) {
        if ("Ristoratore".equals(displayRole)) {
            return "Restaurateur";
        }
        return "Client";
    }
}
