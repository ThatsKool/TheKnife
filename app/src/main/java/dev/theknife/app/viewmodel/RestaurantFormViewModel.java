/*
 * ViewModel per il form di creazione ristorante.
 * Incapsula validazione, sessione, costruzione del modello e salvataggio.
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.Restaurant;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.RestaurantQueryService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.GeoValidator;
import dev.theknife.app.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RestaurantFormViewModel {

    public record RestaurantFormData(
        String name,
        String address,
        String location,
        String price,
        String cuisine,
        String latitudeText,
        String longitudeText,
        String phone,
        String website,
        String award,
        String greenStar,
        String facilities,
        String description
    ) {
    }

    public enum SubmitStatus {
        SUCCESS,
        VALIDATION_ERROR,
        NOT_LOGGED_IN,
        NOT_RESTAURATEUR,
        EMAIL_MISSING,
        EMAIL_INVALID,
        RESTAURATEUR_EMAIL_MISSING,
        SAVE_FAILED
    }

    public static final class SubmitResult {
        private final SubmitStatus status;
        private final String message;
        private final Restaurant restaurant;

        private SubmitResult(SubmitStatus status, String message, Restaurant restaurant) {
            this.status = status;
            this.message = message;
            this.restaurant = restaurant;
        }

        public static SubmitResult success(Restaurant restaurant) {
            return new SubmitResult(SubmitStatus.SUCCESS, null, restaurant);
        }

        public static SubmitResult error(SubmitStatus status, String message) {
            return new SubmitResult(status, message, null);
        }

        public SubmitStatus getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Restaurant getRestaurant() {
            return restaurant;
        }

        public boolean isSuccess() {
            return status == SubmitStatus.SUCCESS;
        }
    }

    private final IRestaurantService restaurantService;
    private final RestaurantQueryService restaurantQueryService;
    private final SessionContext sessionContext;
    private final Logger logger;

    public RestaurantFormViewModel(IRestaurantService restaurantService,
                                   RestaurantQueryService restaurantQueryService,
                                   SessionContext sessionContext) {
        this.restaurantService = restaurantService;
        this.restaurantQueryService = restaurantQueryService;
        this.sessionContext = sessionContext;
        this.logger = Logger.getLogger(RestaurantFormViewModel.class);
    }

    public boolean addRestaurant(Restaurant restaurant) {
        return restaurantService.addRestaurant(restaurant);
    }

    /**
     * Valida i dati del form, verifica sessione/permessi e salva il ristorante.
     */
    public SubmitResult submit(RestaurantFormData form) {
        List<String> missingFields = collectMissingRequiredFields(form);
        if (!missingFields.isEmpty()) {
            return SubmitResult.error(SubmitStatus.VALIDATION_ERROR, buildMissingFieldsMessage(missingFields));
        }

        if (sessionContext == null || !sessionContext.isLoggedIn() || sessionContext.getCurrentUser() == null) {
            return SubmitResult.error(
                SubmitStatus.NOT_LOGGED_IN,
                "Devi aver effettuato l'accesso come ristoratore!"
            );
        }

        if (!isRestaurateur()) {
            return SubmitResult.error(
                SubmitStatus.NOT_RESTAURATEUR,
                "Solo i ristoratori possono aggiungere ristoranti!"
            );
        }

        String restaurateurEmail = resolveRestaurateurEmail();
        if (restaurateurEmail == null) {
            return SubmitResult.error(
                SubmitStatus.EMAIL_MISSING,
                "Errore: Email utente non trovata. Effettua nuovamente l'accesso."
            );
        }
        if (!isValidEmailFormat(restaurateurEmail)) {
            return SubmitResult.error(
                SubmitStatus.EMAIL_INVALID,
                "Errore: Email utente non valida. Effettua nuovamente l'accesso."
            );
        }

        double latitude = parseDoubleSafe(form.latitudeText());
        double longitude = parseDoubleSafe(form.longitudeText());

        Restaurant restaurant = buildRestaurant(form, longitude, latitude, restaurateurEmail);

        if (restaurant.getRestaurateurEmail() == null || restaurant.getRestaurateurEmail().trim().isEmpty()) {
            return SubmitResult.error(
                SubmitStatus.RESTAURATEUR_EMAIL_MISSING,
                "Errore: Impossibile collegare il ristorante al tuo account. Riprova."
            );
        }

        boolean success = addRestaurant(restaurant);
        if (success) {
            return SubmitResult.success(restaurant);
        }
        logger.error("Failed to add restaurant: " + restaurant.getName());
        return SubmitResult.error(
            SubmitStatus.SAVE_FAILED,
            "Impossibile aggiungere il ristorante. Riprova."
        );
    }

    public List<String> getAvailablePrices() {
        return Arrays.asList("$", "$$", "$$$", "$$$$");
    }

    public List<String> getAvailableCuisines() {
        int total = restaurantQueryService.getTotalRestaurantCount();
        List<Restaurant> all = restaurantQueryService.getRestaurantsRange(0, total);
        return all.stream()
            .map(Restaurant::getCuisine)
            .filter(c -> c != null && !c.isEmpty())
            .flatMap(c -> Arrays.stream(c.split(",")))
            .map(String::trim)
            .filter(this::isValidCuisine)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }

    private List<String> collectMissingRequiredFields(RestaurantFormData form) {
        List<String> missing = new ArrayList<>();

        if (form.name() == null || form.name().trim().isEmpty()) {
            missing.add("Nome del ristorante");
        }
        if (form.location() == null || form.location().trim().isEmpty()) {
            missing.add("Città");
        }
        if (form.cuisine() == null || form.cuisine().trim().isEmpty()) {
            missing.add("Cucina");
        }

        Double lat = parseDoubleOrNull(form.latitudeText());
        if (lat == null) {
            missing.add("Latitudine (obbligatorio, inserire un numero tra -90 e 90)");
        } else if (!GeoValidator.isValidLatitude(lat)) {
            missing.add("Latitudine (inserire un numero tra -90 e 90)");
        }

        Double lon = parseDoubleOrNull(form.longitudeText());
        if (lon == null) {
            missing.add("Longitudine (obbligatorio, inserire un numero tra -180 e 180)");
        } else if (!GeoValidator.isValidLongitude(lon)) {
            missing.add("Longitudine (inserire un numero tra -180 e 180)");
        }

        return missing;
    }

    private String buildMissingFieldsMessage(List<String> missingFields) {
        StringBuilder message = new StringBuilder("Compila correttamente i seguenti campi obbligatori:\n\n");
        for (String field : missingFields) {
            message.append("• ").append(field).append("\n");
        }
        message.append("\nI campi contrassegnati con * sono obbligatori.");
        return message.toString();
    }

    private boolean isRestaurateur() {
        if (sessionContext == null || !sessionContext.isLoggedIn() || sessionContext.getCurrentUser() == null) {
            return false;
        }
        String role = sessionContext.getCurrentUser().getRole();
        return "Restaurateur".equalsIgnoreCase(role) || "Ristoratore".equalsIgnoreCase(role);
    }

    private String resolveRestaurateurEmail() {
        if (sessionContext == null || sessionContext.getCurrentUser() == null) {
            return null;
        }
        String email = sessionContext.getCurrentUser().getEmail();
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private boolean isValidEmailFormat(String email) {
        return email != null && email.contains("@");
    }

    private Restaurant buildRestaurant(RestaurantFormData form, double longitude, double latitude, String restaurateurEmail) {
        return new Restaurant(
            trimToEmpty(form.name()),
            trimToEmpty(form.address()),
            trimToEmpty(form.location()),
            trimToEmpty(form.price()),
            trimToEmpty(form.cuisine()),
            longitude,
            latitude,
            trimToEmpty(form.phone()),
            "",
            trimToEmpty(form.website()),
            trimToEmpty(form.award()),
            trimToEmpty(form.greenStar()),
            trimToEmpty(form.facilities()),
            trimToEmpty(form.description()),
            restaurateurEmail
        );
    }

    private String trimToEmpty(String value) {
        return value != null ? value.trim() : "";
    }

    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value != null ? value.trim() : "");
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Double parseDoubleOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isValidCuisine(String cuisine) {
        if (cuisine == null || cuisine.trim().isEmpty()) {
            return false;
        }

        try {
            Double.parseDouble(cuisine);
            return false;
        } catch (NumberFormatException e) {
            // not numeric
        }

        if (cuisine.contains("€") || cuisine.contains("$") || cuisine.contains("£") || cuisine.contains("¥")) {
            return false;
        }

        return cuisine.matches(".*[a-zA-Z].*");
    }
}
