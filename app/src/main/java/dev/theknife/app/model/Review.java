/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Modello che rappresenta una recensione di un ristorante.
 * <p>
 * Contiene i dati relativi alla valutazione (rating, commento), all'autore (utente)
 * e al soggetto recensito (ristorante). Supporta anche un meccanismo di risposta
 * da parte del ristoratore e una contro-risposta del cliente.
 * </p>
 * <p>
 * La classe è immutabile per garantire thread-safety e consistenza.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public final class Review {
    // CAMPI
    /** Identificativo univoco della recensione (Stringa generata). */
    private final String id;
    
    /** 
     * ID del ristorante recensito (Chiave esterna).
     * @see Restaurant#getId()
     */
    private final Long restaurantId;
    
    /** Nome del ristorante (campo legacy per compatibilità con vecchi dati). */
    private final String restaurantName;
    
    /** 
     * Email dell'utente autore della recensione (Chiave esterna).
     * @see dev.theknife.app.model.User#getEmail()
     */
    private final String userEmail;
    
    /** Nome visualizzato dell'utente (campo legacy). */
    private final String userName;
    
    /** Punteggio assegnato da 1 a 5 stelle. */
    private final int rating;
    
    /** Testo della recensione. */
    private final String comment;
    
    /** Data di pubblicazione della recensione. */
    private final LocalDate reviewDate;
    
    /** 
     * Flag che indica se la recensione è verificata (es. prova d'acquisto).
     */
    private final boolean isVerified;
    
    /** Risposta ufficiale del ristoratore alla recensione. */
    private final String restaurateurResponse;
    
    /** Contro-risposta del cliente alla risposta del ristoratore. */
    private final String clientResponse;
    
    /** Formattatore per la serializzazione delle date (ISO_LOCAL_DATE). */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    
    // COSTRUTTORI
    /**
     * Costruttore principale completo per il caricamento da persistenza.
     *
     * @param id ID univoco recensione
     * @param restaurantId ID numerico ristorante
     * @param restaurantName Nome ristorante (fallback)
     * @param userEmail Email utente
     * @param userName Nome utente (fallback)
     * @param rating Voto (clamped 1-5)
     * @param comment Testo recensione
     * @param reviewDate Data pubblicazione
     * @param isVerified Flag verifica
     * @param restaurateurResponse Risposta del ristoratore
     * @param clientResponse Contro-risposta del cliente
     */
    public Review(String id, Long restaurantId, String restaurantName, String userEmail, String userName, int rating, String comment,
                  LocalDate reviewDate, boolean isVerified, String restaurateurResponse, String clientResponse) {
        this.id = id;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.userEmail = userEmail;
        this.userName = userName;
        this.rating = Math.max(1, Math.min(5, rating));
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.isVerified = isVerified;
        this.restaurateurResponse = restaurateurResponse;
        this.clientResponse = clientResponse;
    }
    
    /**
     * Costruttore per la creazione di NUOVE recensioni da parte dell'utente.
     * <p>Genera automaticamente un ID univoco temporaneo.</p>
     *
     * @param restaurantName Nome ristorante
     * @param userName Nome utente
     * @param userEmail Email utente (per verifica ownership)
     * @param rating Voto (1-5)
     * @param comment Testo recensione
     */
    public Review(String restaurantName, String userName, String userEmail, int rating, String comment) {
        this.id = generateId();
        this.restaurantId = null;
        this.restaurantName = restaurantName;
        this.userEmail = userEmail;
        this.userName = userName;
        this.rating = Math.max(1, Math.min(5, rating)); // Ensure rating is between 1-5
        this.comment = comment;
        this.reviewDate = LocalDate.now();
        this.isVerified = false;
        this.restaurateurResponse = null;
        this.clientResponse = null;
    }

    
    // METODI
    /**
     * Genera un ID univoco basato su timestamp e random.
     * @return Stringa ID (es. "REV_123456789_123")
     */
    private String generateId() {
        return "REV_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    // Getters
    
    /**
     * Restituisce l'ID della recensione.
     * @return ID univoco.
     */
    public String getId() { return id; }
    
    /**
     * Restituisce l'ID del ristorante recensito.
     * @return ID ristorante o null (se legacy).
     */
    public Long getRestaurantId() { return restaurantId; }
    
    /**
     * Restituisce il nome del ristorante.
     * @return Nome ristorante.
     */
    public String getRestaurantName() { return restaurantName; }
    
    /**
     * Restituisce l'email dell'autore.
     * @return Email utente o null.
     */
    public String getUserEmail() { return userEmail; }
    
    /**
     * Restituisce il nome dell'autore.
     * @return Nome utente.
     */
    public String getUserName() { return userName; }
    
    /**
     * Restituisce il voto assegnato.
     * @return Intero tra 1 e 5.
     */
    public int getRating() { return rating; }
    
    /**
     * Restituisce il testo della recensione.
     * @return Commento.
     */
    public String getComment() { return comment; }
    
    /**
     * Restituisce la data della recensione.
     * @return Data di pubblicazione.
     */
    public LocalDate getReviewDate() { return reviewDate; }
    
    /**
     * Indica se la recensione è verificata.
     * @return true se verificata, false altrimenti.
     */
    public boolean isVerified() { return isVerified; }
    
    /**
     * Restituisce la risposta del ristoratore.
     * @return Testo risposta o null se assente.
     */
    public String getRestaurateurResponse() { return restaurateurResponse; }
    
    /**
     * Restituisce la contro-risposta del cliente.
     * @return Testo risposta o null se assente.
     */
    public String getClientResponse() { return clientResponse; }

    /**
     * Crea una copia della recensione con un nuovo ID.
     *
     * @param newId Nuovo ID da assegnare.
     * @return Nuova istanza Review.
     */
    public Review withId(String newId) {
        return new Review(newId, restaurantId, restaurantName, userEmail, userName, rating, comment, reviewDate, isVerified, restaurateurResponse, clientResponse);
    }

    /**
     * Crea una copia della recensione con un nuovo voto e commento.
     *
     * @param newRating Il nuovo voto da assegnare.
     * @param newComment Il nuovo commento.
     * @return Una nuova istanza di Review con i valori aggiornati.
     */
    public Review withRatingAndComment(int newRating, String newComment) {
        return new Review(id, restaurantId, restaurantName, userEmail, userName, newRating, newComment, reviewDate, isVerified, restaurateurResponse, clientResponse);
    }

    /**
     * Crea una copia della recensione con una risposta del ristoratore.
     *
     * @param response La risposta del ristoratore.
     * @return Una nuova istanza di Review con la risposta aggiunta.
     */
    public Review withRestaurateurResponse(String response) {
        return new Review(id, restaurantId, restaurantName, userEmail, userName, rating, comment, reviewDate, isVerified, response, clientResponse);
    }

    /**
     * Crea una copia della recensione con una contro-risposta del cliente.
     *
     * @param response La contro-risposta del cliente.
     * @return Una nuova istanza di Review con la contro-risposta aggiunta.
     */
    public Review withClientResponse(String response) {
        return new Review(id, restaurantId, restaurantName, userEmail, userName, rating, comment, reviewDate, isVerified, restaurateurResponse, response);
    }

    /**
     * Crea una copia della recensione marcata come verificata.
     *
     * @return Una nuova istanza di Review con il flag di verifica impostato a true.
     */
    public Review markVerified() {
        return new Review(id, restaurantId, restaurantName, userEmail, userName, rating, comment, reviewDate, true, restaurateurResponse, clientResponse);
    }
    
    /**
     * Verifica se la recensione ha una risposta del ristoratore.
     *
     * @return true se esiste una risposta del ristoratore, false altrimenti.
     */
    public boolean hasRestaurateurResponse() { return restaurateurResponse != null && !restaurateurResponse.trim().isEmpty(); }
    
    /**
     * Verifica se la recensione ha una contro-risposta del cliente.
     *
     * @return true se esiste una contro-risposta del cliente, false altrimenti.
     */
    public boolean hasClientResponse() { return clientResponse != null && !clientResponse.trim().isEmpty(); }
    
    /**
     * Restituisce il voto come rappresentazione grafica a stelle (es. ★★★★☆).
     * 
     * @return Una stringa di 5 caratteri contenente stelle piene o vuote.
     */
    public String getRatingStars() {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    /**
     * Restituisce la data della recensione formattata come stringa.
     * 
     * @return La data formattata (es. "2023-10-15").
     */
    public String getFormattedDate() {
        return reviewDate.format(DATE_FORMATTER);
    }
    
    /**
     * Restituisce il commento troncato per la visualizzazione.
     *
     * @param maxLength La lunghezza massima del commento da restituire.
     * @return Il commento troncato con "..." se supera la lunghezza massima.
     */
    public String getTruncatedComment(int maxLength) {
        if (comment == null) return "";
        if (comment.length() <= maxLength) return comment;
        int bodyLength = Math.max(0, maxLength - 6);
        return comment.substring(0, bodyLength) + "...";
    }
    
    /**
     * Verifica se i dati della recensione sono validi.
     *
     * @return true se tutti i campi obbligatori sono presenti e validi, false altrimenti.
     */
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
               restaurantName != null && !restaurantName.trim().isEmpty() &&
               userName != null && !userName.trim().isEmpty() &&
               rating >= 1 && rating <= 5 &&
               comment != null && !comment.trim().isEmpty() &&
               reviewDate != null;
    }
    
    /**
     * Restituisce una rappresentazione stringa della recensione in formato CSV.
     * <p>
     * Formato: id,restaurantId,userEmail,restaurantName,userName,rating,comment,reviewDate,isVerified,restaurateurResponse,clientResponse
     * </p>
     *
     * @return Stringa rappresentativa della recensione in formato CSV.
     */
    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s,%s,%s",
            id, 
            restaurantId != null ? restaurantId.toString() : "",
            userEmail != null ? userEmail : "",
            restaurantName != null ? restaurantName : "",
            userName != null ? userName : "",
            rating, 
            comment.replace(",", ";"), // Replace commas in comment to avoid CSV issues
            reviewDate.format(DATE_FORMATTER),
            isVerified,
            restaurateurResponse != null ? restaurateurResponse.replace(",", ";") : "", // Replace commas in response
            clientResponse != null ? clientResponse.replace(",", ";") : "" // Replace commas in client response
        );
    }
    
    /**
     * Confronta questa recensione con un altro oggetto per uguaglianza.
     * <p>
     * Due recensioni sono considerate uguali se hanno lo stesso ID.
     * </p>
     *
     * @param obj L'oggetto da confrontare.
     * @return true se gli oggetti sono uguali, false altrimenti.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Review review = (Review) obj;
        return id != null ? id.equals(review.id) : review.id == null;
    }
    
    /**
     * Restituisce il codice hash di questa recensione.
     * <p>
     * Il codice hash è basato sull'ID della recensione.
     * </p>
     *
     * @return Il codice hash della recensione.
     */
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
