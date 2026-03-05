/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.config.FileProvider;
import dev.theknife.app.dataaccess.CSVManager;
import dev.theknife.app.model.Restaurant;
import dev.theknife.app.util.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementazione concreta del servizio di gestione dei Ristoranti.
 * <p>
 * Questa classe gestisce il ciclo di vita dei dati dei ristoranti, offrendo funzionalità di:
 * <ul>
 *   <li>Caricamento e persistenza su file CSV tramite {@link CSVManager}.</li>
 *   <li>Indicizzazione in memoria per accessi rapidi (per ID e per Email ristoratore).</li>
 *   <li>Ricerca full-text e geospaziale.</li>
 *   <li>Paginazione dei risultati per ottimizzare le performance dell'UI.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Nota architetturale:</b> Sebbene l'interfaccia supporti il caricamento pigro (Lazy Loading),
 * questa implementazione adotta una strategia di <i>Eager Loading</i> (caricamento ansioso) all'avvio,
 * mantenendo tutti i dati in memoria (In-Memory Database). Questo è accettabile per dataset
 * di dimensioni moderate (fino a ~100k record) e garantisce tempi di risposta immediati per
 * le ricerche e i filtri.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see IRestaurantService
 * @see CSVManager
 */
public class RestaurantService implements IRestaurantService {
    
    // CAMPI
    /** Header del file CSV per la validazione e la scrittura. */
    private static final String HEADER = "Id,Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description,RestaurateurEmail";
    
    /** Lunghezza massima della descrizione per la visualizzazione nelle card. */
    private static final int MAX_DESCRIPTION_LENGTH = 100;

    /** Gestore della persistenza CSV tipizzato per la classe Restaurant. */
    private final CSVManager<Restaurant> restaurantManager;
    
    /** Logger per il tracciamento delle operazioni e degli errori. */
    private final Logger logger;

    /** Servizio dedicato alle query (listing, ricerca, paginazione). */
    private RestaurantQueryService restaurantQueryService;

    /** Cache principale dei ristoranti (Source of Truth in memoria). */
    private final List<Restaurant> restaurants;
    
    /** Indice secondario per ricerca rapida O(1) tramite ID. */
    private final Map<Long, Restaurant> byId = new HashMap<>();
    
    /** Indice secondario per ricerca rapida tramite Email del ristoratore. */
    private final Map<String, List<Restaurant>> byEmail = new HashMap<>();
    
    /** Contatore per la generazione sequenziale degli ID. */
    private long nextId = 1L;
    
    // COSTRUTTORI
    /**
     * Costruisce una nuova istanza del servizio.
     * <p>
     * Inizializza il {@link CSVManager} e carica immediatamente tutti i dati in memoria,
     * costruendo gli indici di ricerca.
     * </p>
     *
     * @param fileProvider Fornitore delle risorse file (per testabilità e astrazione del filesystem).
     */
    public RestaurantService(FileProvider fileProvider) {
        this.logger = Logger.getLogger(RestaurantService.class);
        this.restaurantManager = new CSVManager<>(
            "michelin_my_maps.csv",
            HEADER,
            this::parseRestaurantFromCSV,
            Restaurant::toString,
            fileProvider
        );
        this.restaurants = loadAllAndIndex();
        logger.info("RestaurantService initialized with " + restaurants.size() + " restaurants.");
        if (!restaurants.isEmpty()) {
            logger.info("First restaurant: " + restaurants.get(0).getName());
        } else {
            logger.error("No restaurants loaded! Checking CSV file...");
            try {
                java.nio.file.Path path = restaurantManager.getCSVPath();
                logger.error("CSV Path: " + path.toAbsolutePath());
                logger.error("File exists: " + java.nio.file.Files.exists(path));
                logger.error("File size: " + java.nio.file.Files.size(path));
            } catch (IOException e) {
                logger.error("Error checking CSV file: " + e.getMessage());
            }
        }
    }

    /**
     * Imposta il servizio di query per delegare le operazioni di listing/ricerca.
     * Deve essere configurato dal container di dipendenze dopo la costruzione.
     *
     * @param restaurantQueryService istanza singleton di {@link RestaurantQueryService}.
     */
    void setRestaurantQueryService(RestaurantQueryService restaurantQueryService) {
        this.restaurantQueryService = restaurantQueryService;
    }

    /**
     * Espone la lista in memoria dei ristoranti alla logica di query.
     * <p>
     * Metodo di supporto a visibilità di package per mantenere l'incapsulamento
     * rispetto agli altri layer.
     * </p>
     *
     * @return lista interna dei ristoranti.
     */
    List<Restaurant> getAllRestaurantsInternal() {
        return restaurants;
    }
    
    // METODI
    /**
     * {@inheritDoc}
     * <p>
     * Implementazione basata su sublist della cache in memoria.
     * Efficiente O(1) se la lista supporta accesso casuale.
     * </p>
     */
    @Override
    public List<Restaurant> getRestaurantsRange(int offset, int limit) {
        return restaurantQueryService.getRestaurantsRange(offset, limit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTotalRestaurantCount() {
        return restaurantQueryService.getTotalRestaurantCount();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue una scansione lineare della lista in memoria applicando il filtro di ricerca.
     * Include logica di paginazione sui risultati filtrati.
     * </p>
     */
    public List<Restaurant> searchRestaurantsRange(String searchTerm, int offset, int limit) {
        return restaurantQueryService.searchRestaurantsRange(searchTerm, offset, limit);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Utilizza la mappa indicizzata {@code byId} per un accesso O(1).
     * </p>
     */
    @Override
    public Restaurant findRestaurantById(Long restaurantId) {
        if (restaurantId == null) {
            return null;
        }
        return byId.get(restaurantId);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue una scansione lineare O(n). Per dataset molto grandi si consiglia
     * di aggiungere una mappa indicizzata per nome.
     * </p>
     */
    public Restaurant findRestaurantByName(String restaurantName) {
        if (restaurantName == null || restaurantName.trim().isEmpty()) {
            return null;
        }

        for (Restaurant restaurant : restaurants) {
            if (restaurant != null && restaurant.getName() != null &&
                restaurant.getName().equals(restaurantName)) {
                return restaurant;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String formatRestaurantDetails(Restaurant restaurant) {
        if (restaurant == null) {
            return "Restaurant not found.";
        }
        
        StringBuilder content = new StringBuilder();
        content.append("Name: ").append(restaurant.getName()).append("\n\n");
        content.append("Address: ").append(restaurant.getAddress()).append("\n\n");
        content.append("Location: ").append(restaurant.getLocation()).append("\n\n");
        content.append("Price: ").append(restaurant.getPrice()).append("\n\n");
        content.append("Cuisine: ").append(restaurant.getCuisine()).append("\n\n");
        content.append("Award: ").append(restaurant.getAward()).append("\n\n");
        
        if (restaurant.getPhoneNumber() != null && !restaurant.getPhoneNumber().isEmpty()) {
            content.append("Phone: ").append(restaurant.getPhoneNumber()).append("\n\n");
        }
        
        if (restaurant.getWebsiteUrl() != null && !restaurant.getWebsiteUrl().isEmpty()) {
            content.append("Website: ").append(restaurant.getWebsiteUrl()).append("\n\n");
        }
        
        content.append("Description: ").append(restaurant.getDescription());
        
        return content.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTruncatedDescription(Restaurant restaurant) {
        if (restaurant == null) {
            return "No description available.";
        }
        String description = restaurant.getDescription();
        if (description == null || description.isEmpty()) {
            return "No description available.";
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            return description.substring(0, MAX_DESCRIPTION_LENGTH - 3) + "...";
        }
        return description;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Gestisce la generazione dell'ID univoco se mancante e aggiorna sia la cache in memoria
     * che il file CSV su disco. Gestisce la concorrenza degli ID tramite un contatore atomico locale.
     * </p>
     */
    @Override
    public boolean addRestaurant(Restaurant restaurant) {
        validateRestaurant(restaurant);

        try {
            Restaurant toStore = restaurant;
            if (restaurant.getId() == null) {
                Long newId = generateUniqueId();
                toStore = restaurant.withId(newId);
                logger.debug("Assigned ID " + newId + " to new restaurant: " + restaurant.getName());
            } else {
                // If an explicit ID is provided, make sure nextId moves past it to avoid duplicates.
                if (byId.containsKey(restaurant.getId())) {
                    Long newId = generateUniqueId();
                    toStore = restaurant.withId(newId);
                    logger.warn("Duplicate restaurant ID detected (" + restaurant.getId() + "). Assigned new ID: " + newId);
                }
                if (restaurant.getId() >= nextId) {
                    nextId = restaurant.getId() + 1;
                }
            }

            restaurants.add(toStore);
            indexRestaurant(toStore);
            restaurantManager.save(toStore);
            restaurantManager.saveToDisk();

            logger.info("Successfully added restaurant with ID " + toStore.getId() +
                       " and restaurateurEmail: " + toStore.getRestaurateurEmail() +
                       " - Name: " + toStore.getName());
            return true;
        } catch (IOException e) {
            logger.error("Error adding restaurant: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Valida i dati del ristorante.
     * 
     * @param restaurant Il ristorante da validare.
     * @throws IllegalArgumentException Se i dati non sono validi.
     */
    private void validateRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
        if (restaurant.getName() == null || restaurant.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant name cannot be empty");
        }
        if (restaurant.getLocation() == null || restaurant.getLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant location cannot be empty");
        }
        if (restaurant.getCuisine() == null || restaurant.getCuisine().trim().isEmpty()) {
            throw new IllegalArgumentException("Restaurant cuisine cannot be empty");
        }
        if (restaurant.getLatitude() < -90 || restaurant.getLatitude() > 90) {
            throw new IllegalArgumentException("Invalid latitude: " + restaurant.getLatitude() + ". Must be between -90 and 90");
        }
        if (restaurant.getLongitude() < -180 || restaurant.getLongitude() > 180) {
            throw new IllegalArgumentException("Invalid longitude: " + restaurant.getLongitude() + ". Must be between -180 and 180");
        }
    }

    /**
     * Genera il prossimo ID univoco disponibile.
     * <p>
     * Incrementa il contatore interno {@code nextId} finché non trova un ID libero,
     * garantendo l'assenza di collisioni con ID esistenti.
     * </p>
     *
     * @return Un Long rappresentante il nuovo ID univoco.
     */
    private Long generateUniqueId() {
        // Ensure we skip any IDs already present (handles pre-existing duplicates)
        while (byId.containsKey(nextId)) {
            nextId++;
        }
        return nextId++;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Utilizza la formula dell'Haversine per calcolare la distanza sulla superficie sferica della Terra.
     * </p>
     *
     * @see #haversine(double, double, double, double)
     */
    public List<Restaurant> findNearby(double userLat, double userLon, double radiusKm) {
        List<Restaurant> result = new ArrayList<>();
        for (Restaurant r : restaurants) {
            double distance = haversine(userLat, userLon, r.getLatitude(), r.getLongitude());
            if (distance <= radiusKm) {
                result.add(r);
            }
        }
        return result;
    }

    /**
     * Analizza una riga CSV e la converte in un oggetto {@link Restaurant}.
     * <p>
     * Supporta molteplici formati per retrocompatibilità:
     * <ul>
     *   <li>Vecchio formato (14 campi): Senza ID e Email.</li>
     *   <li>Formato intermedio (15 campi): Senza ID ma con Email.</li>
     *   <li>Nuovo formato (16 campi): Include ID e Email.</li>
     * </ul>
     * </p>
     *
     * @param csvLine La riga grezza letta dal file CSV.
     * @return L'istanza di {@link Restaurant} parsata, o {@code null} se la riga è invalida o è un header.
     */
    private Restaurant parseRestaurantFromCSV(String csvLine) {
        try {
            String trimmed = csvLine == null ? "" : csvLine.trim();
            if (trimmed.isEmpty()) {
                return null;
            }
            if (trimmed.startsWith("Id,Name,Address,Location,Price,") || trimmed.startsWith("Name,Address,Location,Price,")) {
                return null;
            }
            String[] fields = CSVManager.parseCSVLine(csvLine);
            
            Long id = null;
            int fieldOffset = 0;
            
            // Controlla se il primo campo è un numero (ID) 
            // Controlla anche se il primo campo è vuoto se abbiamo abbastanza colonne (implica new format con null ID)
            boolean isNewFormat = false;
            
            if (fields.length >= 16) {
                isNewFormat = true;
            } else if (fields.length == 15 && fields[0].trim().isEmpty()) {
                isNewFormat = true;
            } else if (fields.length > 0 && !fields[0].trim().isEmpty()) {
                // Controlla se il primo campo è numerico (ID)
                if ("Id".equalsIgnoreCase(fields[0].trim()) || "Name".equalsIgnoreCase(fields[0].trim())) {
                    return null;
                }
                try {
                    Long.parseLong(fields[0].trim());
                    isNewFormat = true;
                } catch (NumberFormatException e) {
                    // Il primo campo non è un numero
                    isNewFormat = false;
                }
            }
            
            if (isNewFormat) {
                fieldOffset = 1; // Salta il campo ID
                if (!fields[0].trim().isEmpty()) {
                    try {
                        id = Long.parseLong(fields[0].trim());
                    } catch (NumberFormatException e) {
                        // Dovrebbe essere catturato sopra o è vuoto
                    }
                }
            } else {
                fieldOffset = 0;
            }
            
            // Supporta sia il vecchio formato (14 campi) che il nuovo formato (15-16 campi)
            int minFields = (fieldOffset == 1) ? 15 : 14;
            
            if (fields.length < minFields) {
                logger.warn("Invalid CSV line (expected at least " + minFields + " fields, got " + fields.length + "): " + csvLine);
                return null;
            }
            
            String name = fields[fieldOffset + 0];
            String address = fields[fieldOffset + 1];
            String location = fields[fieldOffset + 2];
            String price = fields[fieldOffset + 3];
            String cuisine = fields[fieldOffset + 4];
            String longitudeRaw = fields[fieldOffset + 5];
            String latitudeRaw = fields[fieldOffset + 6];
            String phoneNumber = fields[fieldOffset + 7];
            String url = fields[fieldOffset + 8];
            String websiteUrl = fields[fieldOffset + 9];
            String award = fields[fieldOffset + 10];
            String greenStar = fields[fieldOffset + 11];
            String facilitiesAndServices = fields[fieldOffset + 12];
            String description = fields[fieldOffset + 13];
            String restaurateurEmail = fields.length > fieldOffset + 14 ? fields[fieldOffset + 14] : null;
            
            name = name == null ? null : name.replace("`", "").trim();
            address = address == null ? null : address.replace("`", "").trim();
            location = location == null ? null : location.replace("`", "").trim();
            
            if (location != null && (location.contains("$") || location.contains("€") || location.contains("£") || location.contains("¥"))) {
                // Sostituisci url e description con stringa vuota per il logging
                String[] fieldsForLog = fields.clone();
                 fieldsForLog[fieldOffset + 8] = "url";
                 fieldsForLog[fieldOffset + 13] = "description";
                logger.warn("Invalid location value detected (price-like): '" + location + "'. Treating as null. Line: " + csvLine + " Fields: " + String.join(";", fieldsForLog)); 
                location = null;
            }
            
            price = price == null ? null : price.replace("`", "").trim();
            cuisine = cuisine == null ? null : cuisine.replace("`", "").trim();
            
            // Valida la Cucina: non deve essere un numero (errore di parsing comune dove le coordinate si spostano nella cucina)
            if (cuisine != null) {
                try {
                    Double.parseDouble(cuisine);
                    // Se viene parsato come un numero, è probabile che sia una coordinata o un ID. Tratta come invalido.
                    // Sostituisci url e description con stringa vuota per il logging
                    String[] fieldsForLog = fields.clone();
                     fieldsForLog[fieldOffset + 8] = "url";
                     fieldsForLog[fieldOffset + 13] = "description";
                    logger.warn("Invalid cuisine value detected (numeric): '" + cuisine + "'. Treating as null. Line: " + csvLine + " Fields: " + String.join(";", fieldsForLog));
                    cuisine = null;
                } catch (NumberFormatException e) {
                    // Non un numero, candidato cucina valido
                }
            }

            longitudeRaw = longitudeRaw == null ? null : longitudeRaw.replace("`", "").trim();
            latitudeRaw = latitudeRaw == null ? null : latitudeRaw.replace("`", "").trim();
            phoneNumber = phoneNumber == null ? null : phoneNumber.replace("`", "").trim();
            url = url == null ? null : url.replace("`", "").trim();
            websiteUrl = websiteUrl == null ? null : websiteUrl.replace("`", "").trim();
            award = award == null ? null : award.replace("`", "").trim();
            greenStar = greenStar == null ? null : greenStar.replace("`", "").trim();
            facilitiesAndServices = facilitiesAndServices == null ? null : facilitiesAndServices.replace("`", "").trim();
            description = description == null ? null : description.replace("`", "").trim();
            restaurateurEmail = restaurateurEmail == null ? null : restaurateurEmail.replace("`", "").trim();
            
            // Log del parsing per il debug
            if (restaurateurEmail != null && !restaurateurEmail.trim().isEmpty()) {
                logger.debug("Parsed restaurant: " + name + " with restaurateurEmail: " + restaurateurEmail);
            } else {
                logger.debug("Parsed restaurant: " + name + " WITHOUT restaurateurEmail (fields.length: " + fields.length + ", fieldOffset: " + fieldOffset + ")");
            }
            
            double longitude = parseDoubleSafe(longitudeRaw);
            double latitude = parseDoubleSafe(latitudeRaw);

            return new Restaurant(
                id, name, address, location, price, cuisine,
                longitude, latitude, phoneNumber, url,
                websiteUrl, award, greenStar, facilitiesAndServices, description, restaurateurEmail
            );
        } catch (Exception e) {
            logger.error("Error parsing restaurant CSV line: " + csvLine, e);
            return null;
        }
    }

    @Override
    public List<Restaurant> getRestaurantsByRestaurateur(String restaurateurEmail) {
        logger.debug("=== getRestaurantsByRestaurateur called ===");
        logger.debug("Input email: '" + restaurateurEmail + "'");
        
        if (restaurateurEmail == null || restaurateurEmail.trim().isEmpty()) {
            logger.debug("Email is null or empty, returning empty list");
            return new ArrayList<>();
        }
        
        // Normalizza l'email del ristoratore per la comparazione (rimuovi spazi e convertila in minuscolo per il matching case-insensitive)
        String normalizedRestaurateurEmail = restaurateurEmail.trim().toLowerCase();
        logger.debug("Normalized search email: '" + normalizedRestaurateurEmail + "'");
        
        List<Restaurant> owned = byEmail.getOrDefault(normalizedRestaurateurEmail, Collections.emptyList());
        logger.info("Found " + owned.size() + " restaurants for restaurateur email: " + normalizedRestaurateurEmail);
        return new ArrayList<>(owned);
    }

    /**
     * Carica tutti i ristoranti dal CSV e costruisce gli indici in memoria.
     * 
     * @return La lista di tutti i ristoranti caricati.
     */
    private List<Restaurant> loadAllAndIndex() {
        List<Restaurant> data;
        try {
            data = new ArrayList<>(restaurantManager.loadAll());
        } catch (IOException e) {
            logger.error("Error loading restaurants: " + e.getMessage(), e);
            data = new ArrayList<>();
        }
        byId.clear();
        byEmail.clear();
        nextId = 1L;
        
        Map<Long, Restaurant> uniqueById = new HashMap<>();
        List<Restaurant> filtered = new ArrayList<>(data.size());
        
        for (Restaurant r : data) {
            if (r == null) continue;
            Long rid = r.getId();
            if (rid != null) {
                if (uniqueById.containsKey(rid)) {
                    logger.warn("Duplicate restaurant ID detected while loading: " + rid + " (" + r.getName() + "). Skipping duplicate entry.");
                    continue;
                }
                uniqueById.put(rid, r);
            }
            filtered.add(r);
            indexRestaurant(r);
            if (rid != null && rid >= nextId) {
                nextId = rid + 1;
            }
        }
        return filtered;
    }

    /**
     * Inserisce un ristorante negli indici in memoria per ricerche rapide.
     * <p>
     * Aggiorna le mappe:
     * <ul>
     *   <li>{@code byId}: per lookup diretto tramite ID.</li>
     *   <li>{@code byEmail}: per raggruppamento per email del ristoratore.</li>
     * </ul>
     * </p>
     *
     * @param restaurant Il ristorante da indicizzare.
     */
    private void indexRestaurant(Restaurant restaurant) {
        if (restaurant.getId() != null) {
            byId.put(restaurant.getId(), restaurant);
        }
        String email = restaurant.getRestaurateurEmail();
        if (email != null && !email.trim().isEmpty()) {
            String normalized = email.trim().toLowerCase();
            byEmail.computeIfAbsent(normalized, k -> new ArrayList<>()).add(restaurant);
        }
    }

    /**
     * Analizza una stringa in un valore double in modo sicuro.
     * <p>
     * Restituisce 0.0 se la stringa non può essere parsata, evitando eccezioni.
     * </p>
     *
     * @param value La stringa da parsare.
     * @return Il valore double parsato, o 0.0 se la conversione fallisce.
     */
    private static double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Calcola la distanza tra due coordinate geografiche usando la formula dell'Haversine.
     * 
     * @param lat1 Latitudine del primo punto.
     * @param lon1 Longitudine del primo punto.
     * @param lat2 Latitudine del secondo punto.
     * @param lon2 Longitudine del secondo punto.
     * @return La distanza in chilometri.
     */
    private static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
