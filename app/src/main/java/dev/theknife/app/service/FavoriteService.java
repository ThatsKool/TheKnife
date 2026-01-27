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
    /** Header del file CSV per la validazione. */
    private static final String HEADER = "UserName,RestaurantName";
    
    /** Gestore della persistenza CSV. */
    private final CSVManager<FavoriteRestaurant> favoriteManager;
    
    /** Logger di sistema. */
    private final Logger logger;
    
    /** Lista completa dei preferiti (Master copy in memoria). */
    private final List<FavoriteRestaurant> allFavorites;
    
    /** Indice ottimizzato per lookup rapidi per utente. Key: UserName. */
    private final Map<String, List<FavoriteRestaurant>> favoritesByUser = new HashMap<>();
    
    /** Indice ottimizzato per lookup rapidi per ristorante. Key: RestaurantName. */
    private final Map<String, List<FavoriteRestaurant>> favoritesByRestaurant = new HashMap<>();
    
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
    public boolean addFavorite(String userName, String restaurantName) {
        if (userName == null || restaurantName == null || userName.trim().isEmpty() || restaurantName.trim().isEmpty()) {
            logger.warn("Attempted to add favorite with null or empty values");
            return false;
        }
        
        try {
            // controlla se è gia nei preferiti
            if (isFavorite(userName, restaurantName)) {
                logger.info("Restaurant already in favorites: " + restaurantName);
                return true; // Giò nei preferiti, considerato successo
            }
            
            FavoriteRestaurant favorite = new FavoriteRestaurant(userName.trim(), restaurantName.trim());
            allFavorites.add(favorite);
            indexFavorite(favorite);
            favoriteManager.save(favorite);
            favoriteManager.saveToDisk();
            
            logger.info("Added favorite: " + restaurantName + " for user: " + userName);
            return true;
        } catch (IOException e) {
            logger.error("Error adding favorite: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeFavorite(String userName, String restaurantName) {
        if (userName == null || restaurantName == null) {
            return false;
        }
        
        try {
            // cerca il preferito da rimuovere
            FavoriteRestaurant favoriteToRemove = allFavorites.stream()
                    .filter(fav -> fav.getUserName().equals(userName.trim()) && 
                                 fav.getRestaurantName().equals(restaurantName.trim()))
                    .findFirst()
                    .orElse(null);
            
            if (favoriteToRemove == null) {
                logger.warn("Favorite not found for removal: " + restaurantName + " for user: " + userName);
                return false;
            }
            boolean removed = favoriteManager.removeIf(fav -> 
                userName.trim().equals(fav.getUserName()) && restaurantName.trim().equals(fav.getRestaurantName()));
            if (removed) {
                removeFromIndexes(favoriteToRemove);
                favoriteManager.saveToDisk();
                logger.info("Removed favorite: " + restaurantName + " for user: " + userName);
                return true;
            }
            return false;
        } catch (IOException e) {
            logger.error("Error removing favorite: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Utilizza l'indice {@code favoritesByUser} per una verifica rapida O(N_user_favorites)
     * invece di scansionare tutti i preferiti.
     * </p>
     */
    @Override
    public boolean isFavorite(String userName, String restaurantName) {
        return favoritesByUser.getOrDefault(userName, List.of()).stream()
                .anyMatch(fav -> fav.getRestaurantName().equals(restaurantName));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getUserFavorites(String userName) {
        return favoritesByUser.getOrDefault(userName, List.of())
                .stream()
                .map(FavoriteRestaurant::getRestaurantName)
                .collect(Collectors.toList());
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Restituisce una copia della lista per preservare l'incapsulamento degli indici interni.
     * </p>
     */
    @Override
    public List<FavoriteRestaurant> getUserFavoriteRestaurants(String userName) {
        return new ArrayList<>(favoritesByUser.getOrDefault(userName, List.of()));
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
            
            String userName = parts[0].trim();
            String restaurantName = parts[1].trim();
            
            return new FavoriteRestaurant(userName, restaurantName);
        } catch (Exception e) {
            logger.error("Error parsing favorite CSV line: " + csvLine, e);
            return null;
        }
    }
    
    /**
     * Aggiorna gli indici in memoria aggiungendo un nuovo preferito.
     *
     * @param favorite Il preferito da indicizzare.
     */
    private void indexFavorite(FavoriteRestaurant favorite) {
        if (favorite.getUserName() != null) {
            favoritesByUser.computeIfAbsent(favorite.getUserName(), k -> new ArrayList<>()).add(favorite);
        }
        if (favorite.getRestaurantName() != null) {
            favoritesByRestaurant.computeIfAbsent(favorite.getRestaurantName(), k -> new ArrayList<>()).add(favorite);
        }
    }

    /**
     * Rimuove un preferito dagli indici in memoria.
     *
     * @param favorite Il preferito da rimuovere.
     */
    private void removeFromIndexes(FavoriteRestaurant favorite) {
        favoritesByUser.getOrDefault(favorite.getUserName(), new ArrayList<>()).remove(favorite);
        favoritesByRestaurant.getOrDefault(favorite.getRestaurantName(), new ArrayList<>()).remove(favorite);
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

