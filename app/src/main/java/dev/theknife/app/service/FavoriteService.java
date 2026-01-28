/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.config.FileProvider;
import dev.theknife.app.dataaccess.CSVManager;
import dev.theknife.app.model.FavoriteRestaurant;
import dev.theknife.app.util.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementazione del servizio di gestione dei ristoranti preferiti.
 * <p>
 * Questa classe gestisce la persistenza delle preferenze utente su file CSV.
 * Mantiene indici in memoria per ottimizzare le query frequenti (es. "quali sono i preferiti di User X?"
 * o "chi ha aggiunto Restaurant Y ai preferiti?").
 * </p>
 * <p>
 * La consistenza dei dati è garantita sincronizzando le operazioni di scrittura
 * sia sulla cache in memoria che sul disco.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see IFavoriteService
 * @see CSVManager
 */
public class FavoriteService implements IFavoriteService {
    
    // CAMPI
    /** Header del file CSV (persistenza: email utente, ID ristorante). */
    private static final String HEADER = "UserEmail,RestaurantId";
    
    /** Gestore della persistenza CSV. */
    private final CSVManager<FavoriteRestaurant> favoriteManager;
    
    /** Logger di sistema. */
    private final Logger logger;
    
    /** Lista completa dei preferiti (Master copy in memoria). */
    private final List<FavoriteRestaurant> allFavorites;
    
    /** Indice per lookup per utente. Key: userEmail. */
    private final Map<String, List<FavoriteRestaurant>> favoritesByUser = new HashMap<>();
    
    /** Indice per lookup per ristorante. Key: restaurantId. */
    private final Map<Long, List<FavoriteRestaurant>> favoritesByRestaurant = new HashMap<>();
    
    // COSTRUTTORI
    /**
     * Costruisce il servizio e carica i dati iniziali.
     *
     * @param fileProvider Il provider per l'accesso al file system.
     */
    public FavoriteService(FileProvider fileProvider) {
        this.logger = Logger.getLogger(FavoriteService.class);
        this.favoriteManager = new CSVManager<>(
            "favorites.csv",
            HEADER,
            this::parseFavoriteFromCsv,
            FavoriteRestaurant::toString,
            fileProvider
        );
        this.allFavorites = loadAndIndex();
    }
    
    // METODI
    /**
     * {@inheritDoc}
     * <p>
     * Esegue controlli di validazione sui parametri e verifica se la relazione esiste già
     * per evitare duplicati. In caso di successo, aggiorna sia la memoria che il disco.
     * </p>
     */
    @Override
    public boolean addFavorite(String userEmail, Long restaurantId) {
        if (userEmail == null || userEmail.trim().isEmpty() || restaurantId == null) {
            logger.warn("Attempted to add favorite with null or empty userEmail or null restaurantId");
            return false;
        }
        
        try {
            if (isFavorite(userEmail.trim(), restaurantId)) {
                logger.info("Restaurant already in favorites: " + restaurantId);
                return true;
            }
            
            FavoriteRestaurant favorite = new FavoriteRestaurant(userEmail.trim(), restaurantId);
            allFavorites.add(favorite);
            indexFavorite(favorite);
            favoriteManager.save(favorite);
            favoriteManager.saveToDisk();
            
            logger.info("Added favorite restaurantId " + restaurantId + " for user: " + userEmail);
            return true;
        } catch (IOException e) {
            logger.error("Error adding favorite: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean removeFavorite(String userEmail, Long restaurantId) {
        if (userEmail == null || restaurantId == null) {
            return false;
        }
        
        try {
            FavoriteRestaurant favoriteToRemove = allFavorites.stream()
                    .filter(fav -> fav.getUserEmail() != null && fav.getRestaurantId() != null
                            && fav.getUserEmail().equals(userEmail.trim()) && fav.getRestaurantId().equals(restaurantId))
                    .findFirst()
                    .orElse(null);
            
            if (favoriteToRemove == null) {
                logger.warn("Favorite not found for removal: restaurantId " + restaurantId + " for user: " + userEmail);
                return false;
            }
            boolean removed = favoriteManager.removeIf(fav ->
                fav.getUserEmail() != null && fav.getRestaurantId() != null
                && userEmail.trim().equals(fav.getUserEmail()) && restaurantId.equals(fav.getRestaurantId()));
            if (removed) {
                removeFromIndexes(favoriteToRemove);
                favoriteManager.saveToDisk();
                logger.info("Removed favorite restaurantId " + restaurantId + " for user: " + userEmail);
                return true;
            }
            return false;
        } catch (IOException e) {
            logger.error("Error removing favorite: " + e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean isFavorite(String userEmail, Long restaurantId) {
        if (userEmail == null || restaurantId == null) return false;
        return favoritesByUser.getOrDefault(userEmail.trim(), List.of()).stream()
                .anyMatch(fav -> fav.getRestaurantId() != null && fav.getRestaurantId().equals(restaurantId));
    }
    
    @Override
    public List<Long> getUserFavoriteIds(String userEmail) {
        if (userEmail == null) return List.of();
        return favoritesByUser.getOrDefault(userEmail.trim(), List.of()).stream()
                .map(FavoriteRestaurant::getRestaurantId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<FavoriteRestaurant> getUserFavoriteRestaurants(String userEmail) {
        if (userEmail == null) return List.of();
        return new ArrayList<>(favoritesByUser.getOrDefault(userEmail.trim(), List.of()));
    }
    
    /**
     * Carica tutti i preferiti dal CSV e costruisce gli indici in memoria.
     *
     * @return La lista completa dei preferiti caricati.
     */
    private List<FavoriteRestaurant> loadAndIndex() {
        List<FavoriteRestaurant> favorites;
        try {
            favorites = new ArrayList<>(favoriteManager.loadAll());
            logger.info("Loaded " + favorites.size() + " favorites from CSV");
        } catch (IOException e) {
            logger.error("Error loading favorites: " + e.getMessage(), e);
            favorites = new ArrayList<>();
        }
        favoritesByUser.clear();
        favoritesByRestaurant.clear();
        for (FavoriteRestaurant fav : favorites) {
            indexFavorite(fav);
        }
        return favorites;
    }
    
    /**
     * Analizza una riga CSV per creare un oggetto {@link FavoriteRestaurant}.
     *
     * @param csvLine La riga CSV da parsare.
     * @return L'oggetto parsato o {@code null} se la riga non è valida.
     */
    private FavoriteRestaurant parseFavoriteFromCsv(String csvLine) {
        try {
            String[] parts = CSVManager.parseCSVLine(csvLine);
            if (parts.length < 2) {
                logger.warn("Invalid CSV line (expected 2 fields, got " + parts.length + "): " + csvLine);
                return null;
            }
            String userEmail = parts[0].trim();
            String second = parts[1].trim();
            if (userEmail.isEmpty()) return null;
            if (!second.matches("\\d+")) {
                logger.warn("Skipping legacy favorites line (expected RestaurantId numeric): " + csvLine);
                return null;
            }
            Long restaurantId = Long.parseLong(second);
            return new FavoriteRestaurant(userEmail, restaurantId);
        } catch (Exception e) {
            logger.error("Error parsing favorite CSV line: " + csvLine, e);
            return null;
        }
    }
    
    private void indexFavorite(FavoriteRestaurant favorite) {
        if (favorite.getUserEmail() != null) {
            favoritesByUser.computeIfAbsent(favorite.getUserEmail(), k -> new ArrayList<>()).add(favorite);
        }
        if (favorite.getRestaurantId() != null) {
            favoritesByRestaurant.computeIfAbsent(favorite.getRestaurantId(), k -> new ArrayList<>()).add(favorite);
        }
    }

    private void removeFromIndexes(FavoriteRestaurant favorite) {
        if (favorite.getUserEmail() != null) {
            favoritesByUser.getOrDefault(favorite.getUserEmail(), new ArrayList<>()).remove(favorite);
        }
        if (favorite.getRestaurantId() != null) {
            favoritesByRestaurant.getOrDefault(favorite.getRestaurantId(), new ArrayList<>()).remove(favorite);
        }
        allFavorites.remove(favorite);
    }

    /**
     * Ricarica completamente lo stato dei preferiti dal disco.
     * <p>
     * Utile se il file sottostante viene modificato da processi esterni o per reset.
     * </p>
     */
    public void refreshFavorites() {
        allFavorites.clear();
        favoritesByUser.clear();
        favoritesByRestaurant.clear();
        allFavorites.addAll(loadAndIndex());
    }
}

