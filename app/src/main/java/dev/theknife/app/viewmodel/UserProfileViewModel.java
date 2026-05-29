/*
 * ViewModel per la vista del profilo utente.
 * Incapsula sessione, aggiornamento posizione e rilevamento automatico.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.User;
import dev.theknife.app.service.IUserService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.CoordinateParser;
import dev.theknife.app.util.GeoValidator;
import dev.theknife.app.util.IpLocationDetector;
import dev.theknife.app.util.Logger;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Consumer;

public class UserProfileViewModel {

    public enum LocationUpdateStatus {
        SUCCESS,
        NOT_LOGGED_IN,
        INVALID_NUMBER,
        INVALID_COORDINATES,
        SAVE_ERROR
    }

    public static final class LocationUpdateResult {
        private final LocationUpdateStatus status;
        private final String message;

        private LocationUpdateResult(LocationUpdateStatus status, String message) {
            this.status = status;
            this.message = message;
        }

        public static LocationUpdateResult success() {
            return new LocationUpdateResult(LocationUpdateStatus.SUCCESS, null);
        }

        public static LocationUpdateResult error(LocationUpdateStatus status, String message) {
            return new LocationUpdateResult(status, message);
        }

        public LocationUpdateStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public boolean isSuccess() {
            return status == LocationUpdateStatus.SUCCESS;
        }
    }

    public enum LocationDetectStatus {
        LOADING,
        SUCCESS,
        NETWORK_ERROR
    }

    public record LocationDetectOutcome(
        LocationDetectStatus status,
        String message,
        Double latitude,
        Double longitude
    ) {
        public static LocationDetectOutcome loading() {
            return new LocationDetectOutcome(
                LocationDetectStatus.LOADING,
                "Rilevamento posizione in corso...",
                null,
                null
            );
        }

        public static LocationDetectOutcome success(double latitude, double longitude) {
            return new LocationDetectOutcome(
                LocationDetectStatus.SUCCESS,
                "Posizione rilevata con successo!",
                latitude,
                longitude
            );
        }

        public static LocationDetectOutcome networkError() {
            return new LocationDetectOutcome(
                LocationDetectStatus.NETWORK_ERROR,
                "Connessione assente! Inserisci le coordinate manualmente.",
                null,
                null
            );
        }
    }

    private final IUserService userService;
    private final SessionContext sessionContext;
    private final Logger logger;

    public UserProfileViewModel(IUserService userService, SessionContext sessionContext) {
        this.userService = userService;
        this.sessionContext = sessionContext;
        this.logger = Logger.getLogger(UserProfileViewModel.class);
    }

    public User getCurrentUser() {
        return sessionContext != null ? sessionContext.getCurrentUser() : null;
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * Parsa, valida e salva la nuova posizione dell'utente corrente.
     */
    public LocationUpdateResult updateLocationFromText(String latitudeText, String longitudeText) {
        if (!isLoggedIn()) {
            return LocationUpdateResult.error(
                LocationUpdateStatus.NOT_LOGGED_IN,
                "Nessun utente loggato"
            );
        }

        double latitude;
        double longitude;
        try {
            latitude = CoordinateParser.parse(latitudeText);
            longitude = CoordinateParser.parse(longitudeText);
            GeoValidator.validateCoordinates(latitude, longitude);
        } catch (NumberFormatException e) {
            return LocationUpdateResult.error(
                LocationUpdateStatus.INVALID_NUMBER,
                "Inserisci valori numerici validi."
            );
        } catch (IllegalArgumentException e) {
            return LocationUpdateResult.error(
                LocationUpdateStatus.INVALID_COORDINATES,
                e.getMessage()
            );
        }

        try {
            updateCurrentUserLocation(latitude, longitude);
            return LocationUpdateResult.success();
        } catch (IOException e) {
            logger.error("Failed to update user location", e);
            return LocationUpdateResult.error(
                LocationUpdateStatus.SAVE_ERROR,
                "Errore di sistema: impossibile salvare le modifiche."
            );
        }
    }

    /**
     * Avvia il rilevamento asincrono della posizione tramite IP pubblico.
     *
     * @param onOutcome callback invocato sul thread JavaFX con l'esito
     */
    public void detectLocationAsync(Consumer<LocationDetectOutcome> onOutcome) {
        if (onOutcome != null) {
            Platform.runLater(() -> onOutcome.accept(LocationDetectOutcome.loading()));
        }

        Thread thread = new Thread(() -> {
            try {
                IpLocationDetector.Coordinates coords = IpLocationDetector.detectFromPublicIp();
                LocationDetectOutcome outcome = LocationDetectOutcome.success(
                    coords.latitude(),
                    coords.longitude()
                );
                if (onOutcome != null) {
                    Platform.runLater(() -> onOutcome.accept(outcome));
                }
            } catch (Exception e) {
                logger.error("Auto-detect location failed", e);
                if (onOutcome != null) {
                    Platform.runLater(() -> onOutcome.accept(LocationDetectOutcome.networkError()));
                }
            }
        }, "UserProfileLocationDetect");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Formatta una coordinata per la visualizzazione nei campi testo.
     */
    public String formatCoordinate(double value) {
        return String.format(Locale.US, "%.4f", value);
    }

    /**
     * Aggiorna la posizione dell'utente corrente e persiste le modifiche.
     */
    public void updateCurrentUserLocation(double lat, double lon) throws IOException {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Nessun utente loggato");
        }

        User updatedUser = new User(
            currentUser.getName(),
            currentUser.getSurname(),
            currentUser.getEmail(),
            currentUser.getPassword(),
            currentUser.getDateOfBirth(),
            lat,
            lon,
            currentUser.getRole()
        );

        userService.updateUser(updatedUser);
        if (sessionContext != null) {
            sessionContext.setCurrentUser(updatedUser);
        }
    }
}
