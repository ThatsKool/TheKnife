/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.model;

/**
 * Modello che rappresenta l'associazione di un ristorante ai preferiti di un utente.
 * <p>
 * Implementa una relazione molti-a-molti tra {@link dev.theknife.app.model.User} e
 * {@link Restaurant}. La chiave primaria è composta dalla coppia (email utente, ID ristorante).
 * </p>
 * <p>
 * Mantiene campi legacy per supportare la migrazione da vecchie versioni dei dati
 * basate su nomi invece che ID univoci.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public final class FavoriteRestaurant {
    // CAMPI
    /** 
     * Email dell'utente che ha aggiunto il preferito (Parte 1 Chiave Primaria).
     * @see dev.theknife.app.model.User#getEmail()
     */
    private final String userEmail;
    
    /** 
     * ID del ristorante aggiunto ai preferiti (Parte 2 Chiave Primaria).
     * @see Restaurant#getId()
     */
    private final Long restaurantId;

    /** Nome utente (campo legacy per compatibilità con FavoriteService). */
    private final String userName;
    
    /** Nome ristorante (campo legacy per compatibilità con FavoriteService). */
    private final String restaurantName;
    
    // COSTRUTTORI
    /**
     * Costruttore standard per la creazione di un nuovo preferito.
     *
     * @param userEmail Email univoca dell'utente
     * @param restaurantId ID univoco del ristorante
     */
    public FavoriteRestaurant(String userEmail, Long restaurantId) {
        this.userEmail = userEmail;
        this.restaurantId = restaurantId;
        this.userName = null;
        this.restaurantName = null;
    }
    
    /**
     * Costruttore per compatibilità con FavoriteService (usa nomi invece di ID/email).
     * 
     * @param userName Nome utente
     * @param restaurantName Nome ristorante
     */
    public FavoriteRestaurant(String userName, String restaurantName) {
        this.userName = userName;
        this.restaurantName = restaurantName;
        this.userEmail = (userName != null && userName.contains("@")) ? userName : null;
        this.restaurantId = null;
    }


    // METODI
    
    /**
     * Restituisce l'email dell'utente.
     * @return Email utente.
     */
    public String getUserEmail() {
        return userEmail;
    }

    /**
     * Restituisce l'ID del ristorante.
     * @return ID ristorante.
     */
    public Long getRestaurantId() {
        return restaurantId;
    }

    /**
     * Restituisce il nome utente (legacy, per compatibilità con FavoriteService).
     * @return Nome utente.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Restituisce il nome ristorante (legacy, per compatibilità con FavoriteService).
     * @return Nome ristorante.
     */
    public String getRestaurantName() {
        return restaurantName;
    }

    /**
     * Restituisce una rappresentazione in stringa dell'oggetto.
     * <p>
     * Utilizzato per la serializzazione CSV (header: UserName,RestaurantName).
     * Se sono presenti userEmail e restaurantId li usa; altrimenti usa userName e restaurantName (legacy).
     * </p>
     *
     * @return Stringa nel formato CSV per favorites.csv.
     */
    @Override
    public String toString() {
        String first = userEmail != null ? userEmail : (userName != null ? userName : "");
        String second = restaurantId != null ? String.valueOf(restaurantId) : (restaurantName != null ? restaurantName : "");
        return String.format("%s,%s", first, second);
    }

    /**
     * Verifica l'uguaglianza tra due oggetti FavoriteRestaurant.
     * <p>
     * L'uguaglianza è basata sulla chiave composta (userEmail + restaurantId).
     * </p>
     *
     * @param obj Oggetto da confrontare.
     * @return true se gli oggetti rappresentano lo stesso preferito.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FavoriteRestaurant that = (FavoriteRestaurant) obj;
        if (userEmail == null || restaurantId == null || that.userEmail == null || that.restaurantId == null) {
            return false;
        }
        return userEmail.equals(that.userEmail) && restaurantId.equals(that.restaurantId);
    }

    /**
     * Calcola l'hash code dell'oggetto.
     * <p>
     * Coerente con il metodo equals(), basato sulla chiave composta.
     * </p>
     *
     * @return Valore hash.
     */
    @Override
    public int hashCode() {
        if (userEmail == null || restaurantId == null) {
            return 0;
        }
        int result = userEmail.hashCode();
        result = 31 * result + restaurantId.hashCode();
        return result;
    }
}

