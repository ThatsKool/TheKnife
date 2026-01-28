/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.service;

import dev.theknife.app.config.FileProvider;
import dev.theknife.app.dataaccess.CSVManager;
import dev.theknife.app.model.Review;
import dev.theknife.app.util.Logger;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementazione del servizio di gestione delle recensioni.
 * <p>
 * Questa classe gestisce il ciclo di vita delle recensioni, inclusa la persistenza su CSV
 * e l'indicizzazione in memoria per query efficienti. Implementa logiche complesse
 * come il calcolo delle aggregazioni (medie, distribuzioni) direttamente sui dati in memoria.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see IReviewService
 * @see CSVManager
 */
public class ReviewService implements IReviewService {
    
    // CAMPI
    /** Header del file CSV (persistenza: id ristorante e email utente). */
    private static final String HEADER = "Id,RestaurantId,UserEmail,Rating,Comment,ReviewDate,IsVerified,RestaurateurResponse,ClientResponse";

    /** Gestore persistenza CSV. */
    private final CSVManager<Review> reviewManager;

    /** Servizio ristoranti per verifica ownership risposte ristoratore. */
    private final IRestaurantService restaurantService;
    
    /** Logger di sistema. */
    private final Logger logger;
    
    /** Lista master di tutte le recensioni. */
    private final List<Review> allReviews;
    
    /** Indice per lookup O(1) tramite ID recensione. */
    private final Map<String, Review> byId = new HashMap<>();
    
    /** Indice per lookup per ristorante (nome, per legacy). */
    private final Map<String, List<Review>> byRestaurantName = new HashMap<>();
    
    /** Indice per lookup per ristorante (ID). */
    private final Map<Long, List<Review>> byRestaurantId = new HashMap<>();
    
    /** Indice per lookup per utente (nome o email). */
    private final Map<String, List<Review>> byUserName = new HashMap<>();
    
    /** Indice per lookup per utente (email). */
    private final Map<String, List<Review>> byUserEmail = new HashMap<>();
    
    /** Contatore per generazione ID (basato su timestamp). */
    private long idCounter = System.currentTimeMillis();
    
    // COSTRUTTORI
    /**
     * Costruisce il servizio inizializzando il manager CSV e caricando i dati.
     *
     * @param fileProvider Il provider per l'accesso ai file.
     * @param restaurantService Servizio ristoranti per verifica ownership (risposte ristoratore).
     */
    public ReviewService(FileProvider fileProvider, IRestaurantService restaurantService) {
        this.logger = Logger.getLogger(ReviewService.class);
        this.restaurantService = restaurantService;
        this.reviewManager = new CSVManager<>(
            "reviews.csv",
            HEADER,
            this::parseReviewFromCSV,
            Review::toString,
            fileProvider
        );
        this.allReviews = loadAndIndex();
    }
    
    // METODI
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Review> getAllReviews() {
        return new ArrayList<>(allReviews);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Utilizza l'indice {@code byRestaurantName} per recuperare i dati in O(1) (accesso alla mappa)
     * e poi ordina i risultati in O(N log N).
     * </p>
     */
    @Override
    public List<Review> getReviewsForRestaurant(String restaurantName) {
        List<Review> list = byRestaurantName.getOrDefault(restaurantName, new ArrayList<>());
        return list.stream()
                .sorted(Comparator.comparing(Review::getReviewDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Review> getReviewsByUser(String userName) {
        List<Review> list = byUserName.getOrDefault(userName, new ArrayList<>());
        return list.stream()
                .sorted(Comparator.comparing(Review::getReviewDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Genera un nuovo ID, valida l'oggetto, aggiorna gli indici in memoria e persiste su disco.
     * </p>
     */
    @Override
    public boolean addReview(Review review) {
        if (review == null || !review.isValid()) {
            logger.warn("Attempted to add invalid review");
            return false;
        }

        try {
            String newId = generateId();
            Long restaurantId = review.getRestaurantId();
            if (restaurantId == null && review.getRestaurantName() != null) {
                dev.theknife.app.model.Restaurant r = restaurantService.findRestaurantByName(review.getRestaurantName());
                if (r != null) restaurantId = r.getId();
            }
            String userEmail = review.getUserEmail();
            if (userEmail == null && review.getUserName() != null && review.getUserName().contains("@")) {
                userEmail = review.getUserName();
            }
            Review toStore = new Review(newId, restaurantId, review.getRestaurantName(), userEmail, review.getUserName(),
                review.getRating(), review.getComment(), review.getReviewDate(), review.isVerified(),
                review.getRestaurateurResponse(), review.getClientResponse());
            allReviews.add(toStore);
            indexReview(toStore);
            reviewManager.save(toStore);
            reviewManager.saveToDisk();
            logger.info("Added review for restaurant: " + (review.getRestaurantName() != null ? review.getRestaurantName() : restaurantId));
            return true;
        } catch (IOException e) {
            logger.error("Error saving review: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calcola la media iterando sulle recensioni in memoria del ristorante specifico.
     * </p>
     */
    @Override
    public double getAverageRating(String restaurantName) {
        List<Review> restaurantReviews = getReviewsForRestaurant(restaurantName);
        if (restaurantReviews.isEmpty()) {
            return 0.0;
        }

        double sum = restaurantReviews.stream()
                .mapToInt(Review::getRating)
                .sum();

        return sum / restaurantReviews.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getReviewCount(String restaurantName) {
        return getReviewsForRestaurant(restaurantName).size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] getRatingDistribution(String restaurantName) {
        List<Review> restaurantReviews = getReviewsForRestaurant(restaurantName);
        int[] distribution = new int[5]; // 1-5 stars

        for (Review review : restaurantReviews) {
            distribution[review.getRating() - 1]++;
        }

        return distribution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRatingDistributionText(String restaurantName) {
        int[] distribution = getRatingDistribution(restaurantName);
        StringBuilder sb = new StringBuilder();
        // distribution[i] = count for (i+1) stars; display 5★ down to 1★
        for (int i = 4; i >= 0; i--) {
            sb.append(i + 1).append("★: ").append(distribution[i]).append(" reviews\n");
        }
        return sb.toString().trim();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasUserReviewedRestaurant(String userName, String restaurantName) {
        return getReviewsForRestaurant(restaurantName).stream()
                .anyMatch(review -> (review.getUserName() != null && review.getUserName().equals(userName))
                    || (review.getUserEmail() != null && review.getUserEmail().equals(userName)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Review> getRecentReviews(int limit) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

        return allReviews.stream()
                .filter(review -> review.getReviewDate().isAfter(thirtyDaysAgo))
                .sorted(Comparator.comparing(Review::getReviewDate).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Esegue un'aggregazione complessa raggruppando per ristorante e calcolando la media.
     * Include un filtro per escludere ristoranti con meno di 3 recensioni (soglia di significatività).
     * </p>
     */
    @Override
    public List<String> getTopRatedRestaurants(int limit) {
        return allReviews.stream()
                .collect(Collectors.groupingBy(Review::getRestaurantName))
                .entrySet().stream()
                .map(entry -> {
                    String restaurantName = entry.getKey();
                    List<Review> reviews = entry.getValue();
                    double avgRating = reviews.stream()
                            .mapToInt(Review::getRating)
                            .average()
                            .orElse(0.0);
                    return new RestaurantRating(restaurantName, avgRating, reviews.size());
                })
                .filter(rr -> rr.reviewCount >= 3) // Only restaurants with at least 3 reviews
                .sorted(Comparator.comparing((RestaurantRating rr) -> rr.averageRating).reversed())
                .limit(limit)
                .map(rr -> rr.restaurantName)
                .collect(Collectors.toList());
    }

    /**
     * Ricarica e re-indicizza le recensioni (usato nei test o flussi di aggiornamento).
     */
    @Override
    public void refreshReviews() {
        allReviews.clear();
        byId.clear();
        byRestaurantName.clear();
        byRestaurantId.clear();
        byUserName.clear();
        byUserEmail.clear();
        allReviews.addAll(loadAndIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateReview(Review review, String requestorEmail) {
        if (review == null || review.getId() == null || !review.isValid()) {
            logger.warn("Attempted to update invalid review");
            return false;
        }

        try {
            Review existingReview = findReviewById(review.getId());
            if (existingReview == null) {
                logger.warn("Review not found for update: " + review.getId());
                return false;
            }

            if (requestorEmail == null || requestorEmail.trim().isEmpty()) {
                logger.warn("Unauthorized attempt to update review: no requestor email");
                return false;
            }
            if (!requestorEmail.trim().equalsIgnoreCase(existingReview.getUserEmail() != null ? existingReview.getUserEmail().trim() : "")) {
                logger.warn("Unauthorized attempt to update review: " + requestorEmail + " is not the owner of review " + review.getId());
                return false;
            }

            // Update in memory cache and indexes
            replaceInCache(existingReview, review);
            reviewManager.saveToDisk();

            logger.info("Updated review: " + review.getId());
            return true;
        } catch (IOException e) {
            logger.error("Error updating review: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean deleteReview(String reviewId, String requestorEmail) {
        if (reviewId == null || reviewId.trim().isEmpty()) {
            logger.warn("Attempted to delete review with null or empty ID");
            return false;
        }

        try {
            Review review = findReviewById(reviewId);
            if (review == null) {
                logger.warn("Review not found for deletion: " + reviewId);
                return false;
            }

            if (requestorEmail == null || requestorEmail.trim().isEmpty()) {
                logger.warn("Unauthorized attempt to delete review: no requestor email");
                return false;
            }
            if (!requestorEmail.trim().equalsIgnoreCase(review.getUserEmail() != null ? review.getUserEmail().trim() : "")) {
                logger.warn("Unauthorized attempt to delete review: " + requestorEmail + " is not the owner of review " + reviewId);
                return false;
            }

            boolean removed = reviewManager.removeIf(r -> reviewId.equals(r.getId()));
            if (removed) {
                removeFromIndexes(review);
                reviewManager.saveToDisk();
                logger.info("Deleted review: " + reviewId);
                return true;
            }
            return false;
        } catch (IOException e) {
            logger.error("Error deleting review: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Review findReviewById(String reviewId) {
        return byId.get(reviewId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addRestaurateurResponse(String reviewId, String response, String requestorEmail) {
        if (reviewId == null || reviewId.trim().isEmpty() || response == null || response.trim().isEmpty()) {
            logger.warn("Attempted to add restaurateur response with invalid parameters");
            return false;
        }
        if (requestorEmail == null || requestorEmail.trim().isEmpty()) {
            logger.warn("Unauthorized attempt to add restaurateur response: no requestor email");
            return false;
        }

        try {
            Review review = findReviewById(reviewId);
            if (review == null) {
                logger.warn("Review not found for adding restaurateur response: " + reviewId);
                return false;
            }

            dev.theknife.app.model.Restaurant restaurant = null;
            if (review.getRestaurantId() != null) {
                restaurant = restaurantService.findRestaurantById(review.getRestaurantId());
            }
            if (restaurant == null && review.getRestaurantName() != null) {
                restaurant = restaurantService.findRestaurantByName(review.getRestaurantName());
            }
            if (restaurant == null) {
                logger.warn("Restaurant not found for review: " + reviewId);
                return false;
            }
            String ownerEmail = restaurant.getRestaurateurEmail();
            if (ownerEmail == null || !ownerEmail.trim().equalsIgnoreCase(requestorEmail.trim())) {
                logger.warn("Unauthorized attempt: " + requestorEmail + " tried to respond to review for restaurant owned by " + ownerEmail);
                return false;
            }

            // Check if restaurateur has already responded
            if (review.hasRestaurateurResponse()) {
                logger.warn("Restaurateur has already responded to review: " + reviewId);
                return false;
            }

            Review updated = review.withRestaurateurResponse(response.trim());
            replaceInCache(review, updated);
            reviewManager.saveToDisk();

            logger.info("Added restaurateur response to review: " + reviewId);
            return true;
        } catch (IOException e) {
            logger.error("Error adding restaurateur response: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addClientResponse(String reviewId, String response, String requestorEmail) {
        if (reviewId == null || reviewId.trim().isEmpty()) {
            logger.warn("Review ID cannot be empty");
            return false;
        }
        if (response == null || response.trim().isEmpty()) {
            logger.warn("Client response cannot be empty");
            return false;
        }
        if (requestorEmail == null || requestorEmail.trim().isEmpty()) {
            logger.warn("Unauthorized attempt to add client response: no requestor email");
            return false;
        }

        try {
            Review review = findReviewById(reviewId);
            if (review == null) {
                logger.warn("Review not found for adding client response: " + reviewId);
                return false;
            }
            if (!requestorEmail.trim().equalsIgnoreCase(review.getUserEmail() != null ? review.getUserEmail().trim() : "")) {
                logger.warn("Unauthorized attempt: " + requestorEmail + " is not the owner of review " + reviewId);
                return false;
            }

            if (!review.hasRestaurateurResponse()) {
                logger.warn("Cannot add client response: restaurateur has not responded to review: " + reviewId);
                return false;
            }

            // Check if client has already responded
            if (review.hasClientResponse()) {
                logger.warn("Client has already responded to review: " + reviewId);
                return false;
            }

            Review updated = review.withClientResponse(response.trim());
            replaceInCache(review, updated);
            reviewManager.saveToDisk();

            logger.info("Added client response to review: " + reviewId);
            return true;
        } catch (IOException e) {
            logger.error("Error adding client response: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Analizza una riga del file CSV per creare un oggetto {@link Review}.
     * <p>
     * Supporta due formati di CSV per garantire la retrocompatibilità:
     * <ul>
     *   <li><b>Nuovo formato (v2):</b> include campi separati per ID Ristorante ed Email Utente.</li>
     *   <li><b>Vecchio formato (v1):</b> utilizza solo i nomi per le relazioni (deprecato).</li>
     * </ul>
     * Gestisce anche la conversione di eventuali caratteri speciali (es. virgole nei commenti).
     * </p>
     *
     * @param csvLine La riga grezza letta dal file CSV.
     * @return L'oggetto {@link Review} ricostruito, oppure {@code null} se la riga è malformata.
     */
    private static String safeField(String[] fields, int i) {
        return (fields != null && i < fields.length && fields[i] != null) ? fields[i].trim() : "";
    }

    private Review parseReviewFromCSV(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }
        try {
            String[] fields = CSVManager.parseCSVLine(csvLine);
            if (fields == null || fields.length < 7) {
                return null;
            }
            // Nuovo formato: Id, RestaurantId, UserEmail, Rating, Comment, ReviewDate, IsVerified, RestaurateurResponse, ClientResponse (9 campi)
            String f1 = safeField(fields, 1);
            String f2 = safeField(fields, 2);
            boolean newFormat = fields.length >= 9
                    && !f1.isEmpty() && f1.matches("\\d+")
                    && f2.contains("@");

            if (newFormat) {
                String id = safeField(fields, 0);
                if (id.isEmpty()) return null;
                Long restaurantId = Long.parseLong(f1);
                String userEmail = f2;
                String r3 = safeField(fields, 3);
                int rating = Integer.parseInt(r3.isEmpty() ? "0" : r3);
                if (rating < 1 || rating > 5) return null;
                String comment = safeField(fields, 4).replace(";", ",");
                LocalDate reviewDate = LocalDate.parse(safeField(fields, 5), DateTimeFormatter.ISO_LOCAL_DATE);
                boolean isVerified = Boolean.parseBoolean(safeField(fields, 6));
                String restaurateurResponse = (fields.length >= 8 && !safeField(fields, 7).isEmpty()) ? safeField(fields, 7).replace(";", ",") : null;
                String clientResponse = (fields.length >= 9 && !safeField(fields, 8).isEmpty()) ? safeField(fields, 8).replace(";", ",") : null;
                return new Review(id, restaurantId, null, userEmail, null, rating, comment, reviewDate, isVerified, restaurateurResponse, clientResponse);
            }
            // Legacy: Id, RestaurantName, UserName, Rating, Comment, ReviewDate, IsVerified, RestaurateurResponse, ClientResponse
            String id = safeField(fields, 0);
            if (id.isEmpty()) return null;
            String restaurantName = safeField(fields, 1);
            String userName = safeField(fields, 2);
            String r3Legacy = safeField(fields, 3);
            int ratingLegacy = Integer.parseInt(r3Legacy.isEmpty() ? "0" : r3Legacy);
            if (ratingLegacy < 1 || ratingLegacy > 5) return null;
            String commentLegacy = safeField(fields, 4).replace(";", ",");
            LocalDate reviewDateLegacy = LocalDate.parse(safeField(fields, 5), DateTimeFormatter.ISO_LOCAL_DATE);
            boolean isVerifiedLegacy = Boolean.parseBoolean(safeField(fields, 6));
            String restaurateurResponseLegacy = (fields.length >= 8 && !safeField(fields, 7).isEmpty()) ? safeField(fields, 7).replace(";", ",") : null;
            String clientResponseLegacy = (fields.length >= 9 && !safeField(fields, 8).isEmpty()) ? safeField(fields, 8).replace(";", ",") : null;
            String userEmailLegacy = (userName != null && userName.contains("@")) ? userName : null;
            return new Review(id, null, restaurantName, userEmailLegacy, userName, ratingLegacy, commentLegacy, reviewDateLegacy, isVerifiedLegacy, restaurateurResponseLegacy, clientResponseLegacy);
        } catch (Exception e) {
            logger.error("Error parsing review CSV line: " + csvLine, e);
            return null;
        }
    }

    /**
     * Classe di supporto interna per i calcoli di aggregazione delle recensioni.
     * <p>
     * Utilizzata come DTO temporaneo durante il calcolo dei ristoranti con il rating migliore.
     * </p>
     */
    private static class RestaurantRating {
        /** Nome del ristorante. */
        final String restaurantName;
        /** Media delle valutazioni. */
        final double averageRating;
        /** Numero totale di recensioni. */
        final int reviewCount;

        /**
         * Crea un nuovo oggetto di aggregazione.
         *
         * @param restaurantName Nome del ristorante.
         * @param averageRating Media calcolata.
         * @param reviewCount Numero di recensioni.
         */
        RestaurantRating(String restaurantName, double averageRating, int reviewCount) {
            this.restaurantName = restaurantName;
            this.averageRating = averageRating;
            this.reviewCount = reviewCount;
        }
    }

    /**
     * Carica tutte le recensioni dal CSV e ricostruisce gli indici in memoria.
     * <p>
     * Questa operazione è costosa (O(N)) e dovrebbe essere eseguita solo all'avvio
     * o durante operazioni di manutenzione massiva.
     * </p>
     *
     * @return La lista completa delle recensioni caricate.
     */
    private List<Review> loadAndIndex() {
        List<Review> reviews;
        try {
            reviews = new ArrayList<>(reviewManager.loadAll());
        } catch (IOException e) {
            logger.error("Error loading reviews: " + e.getMessage(), e);
            reviews = new ArrayList<>();
        }
        byId.clear();
        byRestaurantName.clear();
        byRestaurantId.clear();
        byUserName.clear();
        byUserEmail.clear();
        for (Review review : reviews) {
            if (review == null) continue;
            indexReview(review);
        }
        logger.info("Loaded " + reviews.size() + " reviews from CSV");
        return reviews;
    }

    /**
     * Inserisce una recensione negli indici di ricerca in memoria.
     * <p>
     * Aggiorna le mappe:
     * <ul>
     *   <li>{@code byId}: per lookup diretto.</li>
     *   <li>{@code byRestaurantName}: per raggruppamento per ristorante.</li>
     *   <li>{@code byUserName}: per raggruppamento per utente.</li>
     * </ul>
     * </p>
     *
     * @param review La recensione da indicizzare.
     */
    private void indexReview(Review review) {
        if (review.getId() != null) {
            byId.put(review.getId(), review);
        }
        if (review.getRestaurantName() != null) {
            byRestaurantName.computeIfAbsent(review.getRestaurantName(), k -> new ArrayList<>()).add(review);
        }
        if (review.getRestaurantId() != null) {
            byRestaurantId.computeIfAbsent(review.getRestaurantId(), k -> new ArrayList<>()).add(review);
        }
        if (review.getUserName() != null) {
            byUserName.computeIfAbsent(review.getUserName(), k -> new ArrayList<>()).add(review);
        }
        if (review.getUserEmail() != null) {
            byUserEmail.computeIfAbsent(review.getUserEmail(), k -> new ArrayList<>()).add(review);
        }
    }

    private void removeFromIndexes(Review review) {
        byId.remove(review.getId());
        if (review.getRestaurantName() != null) {
            List<Review> restList = byRestaurantName.get(review.getRestaurantName());
            if (restList != null) restList.remove(review);
        }
        if (review.getRestaurantId() != null) {
            List<Review> byIdList = byRestaurantId.get(review.getRestaurantId());
            if (byIdList != null) byIdList.remove(review);
        }
        if (review.getUserName() != null) {
            List<Review> userList = byUserName.get(review.getUserName());
            if (userList != null) userList.remove(review);
        }
        if (review.getUserEmail() != null) {
            List<Review> emailList = byUserEmail.get(review.getUserEmail());
            if (emailList != null) emailList.remove(review);
        }
        allReviews.remove(review);
    }

    /**
     * Sostituisce una recensione nella cache in memoria e nel CSVManager.
     * 
     * @param oldReview La vecchia versione della recensione.
     * @param newReview La nuova versione della recensione.
     * @throws IOException Se si verifica un errore durante l'aggiornamento.
     */
    private void replaceInCache(Review oldReview, Review newReview) throws IOException {
        int idx = allReviews.indexOf(oldReview);
        if (idx >= 0) {
            allReviews.set(idx, newReview);
        }
        removeFromIndexes(oldReview);
        indexReview(newReview);
        reviewManager.replace(r -> oldReview.getId().equals(r.getId()), newReview);
    }

    /**
     * Genera un nuovo identificativo univoco per una recensione.
     * <p>
     * Utilizza un prefisso "REV_" combinato con un timestamp incrementale per garantire
     * l'unicità all'interno della sessione e (probabilisticamente) nel tempo.
     * </p>
     *
     * @return Una stringa ID univoca (es. "REV_167888999").
     */
    private String generateId() {
        return "REV_" + (idCounter++);
    }
}
