/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.viewmodel;

import dev.theknife.app.model.Restaurant;
import dev.theknife.app.model.User;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.RestaurantQueryService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.Logger;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

/**
 * ViewModel per la schermata della lista dei ristoranti.
 * <p>
 * Gestisce la logica di presentazione e lo stato per la visualizzazione, il filtraggio e la ricerca
 * dei ristoranti. Implementa il pattern MVVM esponendo Property osservabili per il binding con la View.
 * </p>
 * <p>
 * <b>Funzionalità principali:</b>
 * <ul>
 *   <li>Caricamento asincrono dei ristoranti per mantenere la reattività della UI.</li>
 *   <li>Filtraggio avanzato (cucina, posizione, prezzo, stelle, servizi).</li>
 *   <li>Gestione della paginazione (o caricamento massivo ottimizzato).</li>
 *   <li>Logica di ricerca testuale.</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.view.RestaurantListView
 * @see dev.theknife.app.service.IRestaurantService
 */
public class RestaurantListViewModel {
    // CAMPI
    private final IRestaurantService restaurantService;
    private final RestaurantQueryService restaurantQueryService;
    private final SessionContext sessionContext;
    private final Logger logger;
    private final ObservableList<Restaurant> displayedRestaurants;
    private final StringProperty searchTerm;
    private final StringProperty cuisineFilter;
    private final StringProperty locationFilter;
    private final StringProperty priceFilter;
    private final StringProperty starsFilter;
    private final StringProperty distanceFilter;
    private final BooleanProperty deliveryFilter;
    private final BooleanProperty onlineBookingFilter;
    private final BooleanProperty isLoading;
    private final StringProperty sortingStatus;
    private final IntegerProperty currentPage;
    private final IntegerProperty totalRestaurants;
    
    // Con il refactoring del layer CSV, i ristoranti sono già in cache in memoria.
    // Carichiamo tutto in una volta nella ObservableList e ci affidiamo alla virtualizzazione della ListView di JavaFX per le performance UI.
    private static final int RESTAURANTS_PER_PAGE = 10; // mantenuto per compatibilità con la View
    private String currentSearchTerm = "";
    private String currentCuisineFilter = "";
    private String currentLocationFilter = "";
    private String currentPriceFilter = "";
    private String currentStarsFilter = "";
    private String currentDistanceFilter = "";
    private boolean currentDeliveryFilter = false;
    private boolean currentOnlineBookingFilter = false;
    
    // COSTRUTTORI
    /**
     * Costruisce il ViewModel iniettando il servizio dei ristoranti e il contesto di sessione.
     *
     * @param restaurantService Il servizio per l'accesso ai dati dei ristoranti.
     * @param restaurantQueryService Il servizio per le operazioni di query (ricerca, distanza, ordinamento).
     * @param sessionContext Contesto di sessione (nessun getInstance).
     */
    public RestaurantListViewModel(IRestaurantService restaurantService,
                                   RestaurantQueryService restaurantQueryService,
                                   SessionContext sessionContext) {
        this.restaurantService = restaurantService;
        this.restaurantQueryService = restaurantQueryService;
        this.sessionContext = sessionContext;
        this.logger = Logger.getLogger(RestaurantListViewModel.class);
        this.displayedRestaurants = FXCollections.observableArrayList();
        this.searchTerm = new SimpleStringProperty("");
        this.cuisineFilter = new SimpleStringProperty("");
        this.locationFilter = new SimpleStringProperty("");
        this.priceFilter = new SimpleStringProperty("");
        this.starsFilter = new SimpleStringProperty("");
        this.distanceFilter = new SimpleStringProperty("");
        this.deliveryFilter = new SimpleBooleanProperty(false);
        this.onlineBookingFilter = new SimpleBooleanProperty(false);
        this.isLoading = new SimpleBooleanProperty(false);
        this.sortingStatus = new SimpleStringProperty("");
        this.currentPage = new SimpleIntegerProperty(0);
        this.totalRestaurants = new SimpleIntegerProperty(0);
        
        logger.info("RestaurantListViewModel initialized");
        
        // Inizializzazione
        loadInitialData();
    }
    
    // METODI
    /**
     * Carica i dati iniziali dei ristoranti.
     * <p>
     * Recupera il conteggio totale e avvia il caricamento della lista completa.
     * </p>
     */
    private void loadInitialData() {
        totalRestaurants.set(restaurantService.getTotalRestaurantCount());
        loadAllRestaurants();
    }
    
    /**
     * Aggiorna la lista ricaricando tutti i dati dall'inizio.
     * <p>
     * Resetta i filtri, la paginazione e ricarica i ristoranti dal servizio.
     * Utile per sincronizzare la vista con eventuali modifiche ai dati sottostanti.
     * </p>
     */
    public void refresh() {
        currentPage.set(0);
        displayedRestaurants.clear();
        currentSearchTerm = "";
        searchTerm.set("");
        currentCuisineFilter = "";
        cuisineFilter.set("");
        currentLocationFilter = "";
        locationFilter.set("");
        totalRestaurants.set(restaurantService.getTotalRestaurantCount());
        loadAllRestaurants();
    }
    
    /**
     * Carica tutti i ristoranti immediatamente (i dati sono già in cache nel service layer).
     * <p>
     * Metodo mantenuto per compatibilità con la View che potrebbe richiedere il caricamento
     * della "pagina successiva". In questa implementazione, carica tutto in una volta se non già fatto.
     * </p>
     */
    public void loadNextPage() {
        // Compatibilità all'indietro: ora carichiamo tutto in una volta.
        // Manteniamo il metodo poiché la View se lo aspetta per gli hook di lazy-loading.
        if (!displayedRestaurants.isEmpty() || isLoading.get()) {
            return;
        }
        loadAllRestaurants();
    }

    /**
     * Restituisce il numero di ristoranti per pagina.
     *
     * @return Il numero di ristoranti per pagina (costante).
     */
    public int getRestaurantsPerPage() {
        return RESTAURANTS_PER_PAGE;
    }
    
    /**
     * Ottiene tutte le opzioni di distanza disponibili per il filtro.
     *
     * @return Una lista di stringhe rappresentanti i range di distanza.
     */
    public List<String> getAvailableDistances() {
        return List.of(
            "Tutte le distanze",
            "< 1 km",
            "< 5 km",
            "< 10 km",
            "< 20 km",
            "< 50 km"
        );
    }
    
    /**
     * Ottiene tutte le cucine disponibili dal servizio.
     * <p>
     * Estrae l'elenco univoco delle cucine dai ristoranti caricati, gestendo
     * casi di cucine multiple separate da virgola e filtrando valori non validi.
     * </p>
     *
     * @return Una lista ordinata di stringhe rappresentanti i tipi di cucina.
     */
    public List<String> getAvailableCuisines() {
        // Poiché non abbiamo un metodo diretto nel servizio per ottenere tutte le cucine,
        // le estraiamo da tutti i ristoranti.
        // Idealmente questo dovrebbe essere un metodo del servizio.
        int total = restaurantService.getTotalRestaurantCount();
        List<Restaurant> all = restaurantService.getRestaurantsRange(0, total);
        return all.stream()
                .map(Restaurant::getCuisine)
                .filter(c -> c != null && !c.isEmpty())
                // Gestisce casi in cui più cucine sono separate da virgole
                .flatMap(c -> java.util.Arrays.stream(c.split(",")))
                .map(String::trim)
                .filter(this::isValidCuisine)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Ottiene tutte le posizioni (città/zone) disponibili dal servizio.
     *
     * @return Una lista ordinata di stringhe rappresentanti le posizioni.
     */
    public List<String> getAvailableLocations() {
        int total = restaurantService.getTotalRestaurantCount();
        List<Restaurant> all = restaurantService.getRestaurantsRange(0, total);
        return all.stream()
                .map(Restaurant::getLocation)
                .filter(l -> l != null && !l.isEmpty())
                .map(String::trim)
                .filter(this::isValidLocation)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Valida se una stringa è una posizione valida.
     * <p>
     * Filtra prezzi ($$$), coordinate numeriche e valori vuoti.
     * </p>
     *
     * @param location La stringa della posizione da verificare.
     * @return true se la posizione è valida, false altrimenti.
     */
    private boolean isValidLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return false;
        }
        
        // Scarta se sembra un prezzo (contiene simboli di valuta)
        if (location.contains("€") || location.contains("$") || location.contains("£") || location.contains("¥")) {
            return false;
        }
        
        // Scarta se sembra un numero
        try {
            Double.parseDouble(location);
            return false;
        } catch (NumberFormatException e) {
            // Non è un numero, continua i controlli
        }
        
        // Deve contenere almeno una lettera
        return location.matches(".*[a-zA-Z].*");
    }
    
    /**
     * Valida se una stringa è un tipo di cucina valido.
     * <p>
     * Filtra coordinate, prezzi e dati spazzatura.
     * </p>
     *
     * @param cuisine La stringa della cucina da verificare.
     * @return true se la cucina è valida, false altrimenti.
     */
    private boolean isValidCuisine(String cuisine) {
        if (cuisine == null || cuisine.trim().isEmpty()) {
            return false;
        }
        
        // Scarta se sembra un numero (coordinata)
        try {
            Double.parseDouble(cuisine);
            return false;
        } catch (NumberFormatException e) {
            // Non è un numero, continua i controlli
        }
        
        // Scarta se contiene simboli di valuta (Prezzo finito nella colonna cucina)
        if (cuisine.contains("€") || cuisine.contains("$") || cuisine.contains("£") || cuisine.contains("¥")) {
            return false;
        }
        
        // Deve contenere almeno una lettera
        // Questo filtra stringhe puramente numeriche o simboli
        return cuisine.matches(".*[a-zA-Z].*");
    }
    
    /**
     * Verifica se ci sono altri ristoranti da caricare.
     *
     * @return false, poiché carichiamo tutto immediatamente.
     */
    public boolean hasMoreRestaurants() {
        // Carichiamo tutto immediatamente ora.
        return false;
    }
    
    /**
     * Ottiene i dettagli del ristorante come stringa formattata.
     *
     * @param restaurant Il ristorante di cui formattare i dettagli.
     * @return Una stringa contenente i dettagli formattati.
     */
    public String getRestaurantDetails(Restaurant restaurant) {
        return restaurantService.formatRestaurantDetails(restaurant);
    }
    
    /**
     * Ottiene una descrizione troncata per la visualizzazione nelle card.
     *
     * @param restaurant Il ristorante.
     * @return La descrizione troncata.
     */
    public String getTruncatedDescription(Restaurant restaurant) {
        return restaurantService.getTruncatedDescription(restaurant);
    }
    
    // --- Property per il binding ---

    /**
     * Restituisce la lista osservabile dei ristoranti visualizzati.
     *
     * @return La ObservableList dei ristoranti.
     */
    public ObservableList<Restaurant> getDisplayedRestaurants() {
        return displayedRestaurants;
    }
    
    /**
     * Restituisce la property del termine di ricerca.
     *
     * @return Property contenente la stringa di ricerca.
     */
    public StringProperty searchTermProperty() {
        return searchTerm;
    }
    
    /**
     * Restituisce la property del filtro cucina.
     *
     * @return Property contenente il filtro cucina.
     */
    public StringProperty cuisineFilterProperty() {
        return cuisineFilter;
    }
    
    /**
     * Restituisce la property del filtro posizione.
     *
     * @return Property contenente il filtro posizione.
     */
    public StringProperty locationFilterProperty() {
        return locationFilter;
    }
    
    /**
     * Restituisce la property del filtro prezzo.
     *
     * @return Property contenente il filtro prezzo.
     */
    public StringProperty priceFilterProperty() { return priceFilter; }

    /**
     * Restituisce la property del filtro stelle/premi.
     *
     * @return Property contenente il filtro stelle.
     */
    public StringProperty starsFilterProperty() { return starsFilter; }

    /**
     * Restituisce la property del filtro distanza.
     *
     * @return Property contenente il filtro distanza.
     */
    public StringProperty distanceFilterProperty() { return distanceFilter; }

    /**
     * Restituisce la property del filtro delivery.
     *
     * @return Property booleana per il filtro delivery.
     */
    public BooleanProperty deliveryFilterProperty() { return deliveryFilter; }

    /**
     * Restituisce la property del filtro prenotazione online.
     *
     * @return Property booleana per il filtro prenotazione online.
     */
    public BooleanProperty onlineBookingFilterProperty() { return onlineBookingFilter; }

    /**
     * Restituisce la property che indica se il caricamento è in corso.
     *
     * @return Property booleana, true durante il caricamento.
     */
    public BooleanProperty isLoadingProperty() { return isLoading; }

    /**
     * Restituisce la property dello stato di ordinamento.
     *
     * @return Property contenente lo stato.
     */
    public StringProperty sortingStatusProperty() { return sortingStatus; }

    /**
     * Restituisce la property della pagina corrente.
     *
     * @return Property intera con il numero di pagina.
     */
    public IntegerProperty currentPageProperty() { return currentPage; }

    /**
     * Restituisce la property del numero totale di ristoranti.
     *
     * @return Property intera con il totale.
     */
    public IntegerProperty totalRestaurantsProperty() { return totalRestaurants; }
    
    /**
     * Esegue una ricerca testuale.
     *
     * @param query La stringa da cercare nel nome o descrizione.
     */
    public void performSearch(String query) {
        searchTerm.set(query);
        currentSearchTerm = query;
        currentPage.set(0);
        displayedRestaurants.clear();
        loadAllRestaurants();
    }
    
    /**
     * Applica il filtro per tipo di cucina.
     *
     * @param cuisine La cucina da filtrare.
     */
    public void performCuisineFilter(String cuisine) {
        cuisineFilter.set(cuisine);
        currentCuisineFilter = cuisine;
        currentPage.set(0);
        displayedRestaurants.clear();
        loadAllRestaurants();
    }
    
    /**
     * Applica il filtro per posizione.
     *
     * @param location La posizione da filtrare.
     */
    public void performLocationFilter(String location) {
        locationFilter.set(location);
        currentLocationFilter = location;
        currentPage.set(0);
        displayedRestaurants.clear();
        loadAllRestaurants();
    }
    
    /**
     * Applica il filtro per fascia di prezzo.
     *
     * @param price Il prezzo da filtrare (es. "$$").
     */
    public void performPriceFilter(String price) {
        priceFilter.set(price);
        currentPriceFilter = price;
        currentPage.set(0);
        displayedRestaurants.clear();
        loadAllRestaurants();
    }
    
    /**
     * Applica il filtro per stelle/premi.
     *
     * @param stars Il premio da filtrare.
     */
    public void performStarsFilter(String stars) {
        starsFilter.set(stars);
        currentStarsFilter = stars;
        currentPage.set(0);
        displayedRestaurants.clear();
        loadAllRestaurants();
    }

    /**
     * Applica il filtro per distanza.
     *
     * @param distance Il range di distanza da filtrare.
     */
    public void performDistanceFilter(String distance) {
        distanceFilter.set(distance);
        currentDistanceFilter = distance;
        currentPage.set(0);
        displayedRestaurants.clear();
        loadAllRestaurants();
    }
    
    /**
     * Applica il filtro per delivery.
     *
     * @param enable true per mostrare solo ristoranti con delivery.
     */
    public void performDeliveryFilter(boolean enable) {
        deliveryFilter.set(enable);
        currentDeliveryFilter = enable;
        currentPage.set(0);
        displayedRestaurants.clear();
        loadAllRestaurants();
    }
    
    /**
     * Applica il filtro per prenotazione online.
     *
     * @param enable true per mostrare solo ristoranti con prenotazione online.
     */
    public void performOnlineBookingFilter(boolean enable) {
        onlineBookingFilter.set(enable);
        currentOnlineBookingFilter = enable;
        currentPage.set(0);
        displayedRestaurants.clear();
        loadAllRestaurants();
    }

    /**
     * Esegue il caricamento di tutti i ristoranti applicando i filtri attivi.
     * <p>
     * Il metodo è eseguito in modo asincrono tramite {@link CompletableFuture} per non bloccare
     * l'UI thread. Applica in sequenza filtri per ricerca testuale, cucina, posizione,
     * prezzo, stelle e servizi.
     * </p>
     */
    private void loadAllRestaurants() {
        if (isLoading.get()) return;
        isLoading.set(true);

        CompletableFuture.runAsync(() -> {
            List<Restaurant> initialList;
            int total = restaurantService.getTotalRestaurantCount();
            
            // First apply search term if any
            if (currentSearchTerm != null && !currentSearchTerm.trim().isEmpty()) {
                initialList = restaurantService.searchRestaurantsRange(currentSearchTerm, 0, total);
            } else {
                initialList = restaurantService.getRestaurantsRange(0, total);
            }
            
            java.util.stream.Stream<Restaurant> pipeline = initialList.stream();
            
            // Apply Filters (Cuisine, Location, Price, Stars, Delivery, OnlineBooking) BEFORE distance calculation
            
            // Cuisine Filter
            if (currentCuisineFilter != null && !currentCuisineFilter.isEmpty() && !currentCuisineFilter.equals("Tutte le cucine") && !currentCuisineFilter.equals("All Cuisines")) {
                pipeline = pipeline.filter(r -> r.getCuisine() != null && r.getCuisine().contains(currentCuisineFilter));
            }

            // Location Filter
            if (currentLocationFilter != null && !currentLocationFilter.isEmpty() && !currentLocationFilter.equals("Tutte le posizioni") && !currentLocationFilter.equals("All Locations")) {
                pipeline = pipeline.filter(r -> r.getLocation() != null && r.getLocation().contains(currentLocationFilter));
            }
            
            // Price Filter
            if (currentPriceFilter != null && !currentPriceFilter.isEmpty() && !currentPriceFilter.equals("Tutti i prezzi") && !currentPriceFilter.equals("All Prices")) {
                pipeline = pipeline.filter(r -> matchesPrice(r.getPrice(), currentPriceFilter));
            }
            
            // Stars Filter
            if (currentStarsFilter != null && !currentStarsFilter.isEmpty() && !currentStarsFilter.equals("Tutte le stelle") && !currentStarsFilter.equals("All Stars")) {
                pipeline = pipeline.filter(r -> matchesStars(r.getAward(), currentStarsFilter));
            }
            
            // Delivery Filter
            if (currentDeliveryFilter) {
                pipeline = pipeline.filter(this::hasDelivery);
            }
            
            // Online Booking Filter
            if (currentOnlineBookingFilter) {
                pipeline = pipeline.filter(this::hasOnlineBooking);
            }
            
            List<Restaurant> filtered = pipeline.collect(Collectors.toList());

            boolean isLoggedIn = sessionContext.isLoggedIn() && sessionContext.getCurrentUser() != null;
            List<Restaurant> finalResult;
            if (isLoggedIn) {
                User user = sessionContext.getCurrentUser();
                double userLat = user.getLatitude();
                double userLon = user.getLongitude();

                finalResult = restaurantQueryService.applyDistanceFilterAndSort(
                    filtered,
                    userLat,
                    userLon,
                    currentDistanceFilter
                );

                javafx.application.Platform.runLater(() -> sortingStatus.set("Ordinati per vicinanza"));
            } else {
                finalResult = filtered;
                javafx.application.Platform.runLater(() -> sortingStatus.set(""));
            }
            
            javafx.application.Platform.runLater(() -> {
                displayedRestaurants.setAll(finalResult);
                currentPage.set(1); // legacy value: "loaded"
                isLoading.set(false);
                logger.debug("Displayed restaurants count: " + displayedRestaurants.size());
            });
        });
    }

    /**
     * Verifica se il prezzo di un ristorante corrisponde al filtro.
     * <p>
     * Calcola il livello di prezzo (1-4) basandosi sui simboli di valuta o sulla lunghezza
     * della stringa (fallback euristico).
     * </p>
     *
     * @param restaurantPrice Il prezzo del ristorante (es. "$$$").
     * @param filterPrice Il filtro prezzo selezionato (es. "$$$").
     * @return true se corrispondono, false altrimenti.
     */
    private boolean matchesPrice(String restaurantPrice, String filterPrice) {
        if (restaurantPrice == null) return false;
        
        // Calculate price level of the restaurant (1-4)
        long level = 0;
        String p = restaurantPrice.trim();
        
        // Count currency symbols to determine level
        level = p.chars().filter(ch -> ch == '$' || ch == '€' || ch == '£' || ch == '¥' || ch == '฿').count();
        
        // Fallback: if no standard symbols found but string is not empty, assume length represents level 
        // (for cases like "????" if they exist, or just robust handling)
        if (level == 0 && !p.isEmpty()) {
             // Only if it looks like a symbol string (short length)
             if (p.length() <= 5) {
                 level = p.length();
             }
        }
        
        // Filter price is expected to be $, $$, etc. so its length is the target level
        long filterLevel = filterPrice.length();
        
        return level == filterLevel;
    }
    
    /**
     * Verifica se il premio/stelle di un ristorante corrisponde al filtro.
     *
     * @param award Il premio del ristorante.
     * @param filterStars Il filtro selezionato (es. "1 Stella").
     * @return true se corrispondono, false altrimenti.
     */
    private boolean matchesStars(String award, String filterStars) {
        if (award == null) return false;
        
        String target = filterStars;
        if (filterStars.equals("1 Stella")) target = "1 Star";
        else if (filterStars.equals("2 Stelle")) target = "2 Stars";
        else if (filterStars.equals("3 Stelle")) target = "3 Stars";
        
        return award.equalsIgnoreCase(target);
    }
    
    /**
     * Verifica se un ristorante offre il servizio di delivery/asporto.
     * Controlla sia i servizi dichiarati che la descrizione.
     *
     * @param r Il ristorante da verificare.
     * @return true se offre delivery/asporto.
     */
    private boolean hasDelivery(Restaurant r) {
        if (r.getFacilitiesAndServices() != null) {
            String facilities = r.getFacilitiesAndServices().toLowerCase();
            if (facilities.contains("delivery") || facilities.contains("takeaway")) return true;
        }
        // Also check description as fallback
        if (r.getDescription() != null) {
            String desc = r.getDescription().toLowerCase();
            return desc.contains("delivery") || desc.contains("takeaway");
        }
        return false;
    }
    
    /**
     * Verifica se un ristorante offre la prenotazione online.
     *
     * @param r Il ristorante da verificare.
     * @return true se offre prenotazione online.
     */
    private boolean hasOnlineBooking(Restaurant r) {
        // Check facilities for "booking" or "reservation"
        if (r.getFacilitiesAndServices() != null) {
            String facilities = r.getFacilitiesAndServices().toLowerCase();
            if (facilities.contains("booking") || facilities.contains("reservation")) return true;
        }
        // Also check if website URL is present (proxy for online availability as per user expectation often)
        // However, user specifically asked "if the restaurant has availability of online reservations"
        // Most Michelin restaurants have a website. If I filter by URL presence, it might show 90% of them.
        // Let's stick to explicit facilities or description.
        if (r.getDescription() != null) {
            String desc = r.getDescription().toLowerCase();
            if (desc.contains("booking") || desc.contains("reservation")) return true;
        }
        return false; 
    }
    
    /**
     * Restituisce i livelli di prezzo disponibili per il filtro.
     *
     * @return Una lista di stringhe rappresentanti i livelli di prezzo normalizzati.
     */
    public List<String> getAvailablePrices() {
        // Hardcoded normalized levels
        return java.util.Arrays.asList("$", "$$", "$$$", "$$$$");
    }
    
    /**
     * Restituisce i premi/stelle disponibili per il filtro.
     *
     * @return Una lista di stringhe rappresentanti i premi disponibili.
     */
    public List<String> getAvailableAwards() {
        return java.util.Arrays.asList("1 Stella", "2 Stelle", "3 Stelle", "Bib Gourmand");
    }
}
