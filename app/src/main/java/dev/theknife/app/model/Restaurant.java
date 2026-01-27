/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.model;

/**
 * Rappresenta l'entità Ristorante all'interno del dominio applicativo.
 * <p>
 * Questa classe è un modello immutabile che incapsula tutte le informazioni
 * relative a un ristorante, inclusi dettagli geografici, contatti, prezzi e
 * servizi offerti.
 * </p>
 * <p>
 * Viene utilizzata per mappare i dati provenienti dal file CSV e per popolare
 * le viste dell'interfaccia utente.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public final class Restaurant {
    // CAMPI
    /** Identificativo numerico univoco del ristorante (Chiave Primaria). */
    private final Long id;
    
    /** Nome del ristorante. */
    private final String name;
    
    /** Indirizzo completo del ristorante. */
    private final String address;
    
    /** Città o località del ristorante. Internato per ottimizzare la memoria. */
    private final String location;
    
    /** Fascia di prezzo del ristorante (es. "$", "$$$"). Internato per ottimizzare la memoria. */
    private final String price;
    
    /** Tipologia di cucina offerta (es. "Italian", "Japanese"). Internato per ottimizzare la memoria. */
    private final String cuisine;
    
    /** Longitudine geografica per la localizzazione su mappa. */
    private final double longitude;
    
    /** Latitudine geografica per la localizzazione su mappa. */
    private final double latitude;
    
    /** Numero di telefono del ristorante. */
    private final String phoneNumber;
    
    /** URL della pagina ufficiale del ristorante sul portale di origine. */
    private final String url;
    
    /** URL del sito web proprietario del ristorante. */
    private final String websiteUrl;
    
    /** Riconoscimenti o premi ricevuti (es. "MICHELIN Star"). Internato. */
    private final String award;
    
    /** Indicatore di sostenibilità (es. "Green Star"). Internato. */
    private final String greenStar;
    
    /** Lista dei servizi offerti e strutture disponibili. Internato. */
    private final String facilitiesAndServices;
    
    /** Descrizione testuale del ristorante. */
    private final String description;
    
    /** 
     * Email del ristoratore proprietario del ristorante.
     * <p>Funziona come chiave esterna verso l'entità User (se presente).</p>
     */
    private final String restaurateurEmail;
    
    /**
     * Distanza in km dall'utente (campo calcolato a runtime, non persistito).
     * Null se non calcolato o non rilevante.
     */
    private final Double distanceKm;
    
    // COSTRUTTORI
    /**
     * Costruttore semplificato per la creazione di nuovi ristoranti (senza ID).
     * <p>
     * L'ID verrà generato automaticamente in fase di persistenza.
     * </p>
     *
     * @param name Nome del ristorante
     * @param address Indirizzo completo
     * @param location Città o località
     * @param price Fascia di prezzo
     * @param cuisine Tipo di cucina
     * @param longitude Coordinata longitudine
     * @param latitude Coordinata latitudine
     * @param phoneNumber Numero di telefono
     * @param url URL pagina portale
     * @param websiteUrl Sito web ufficiale
     * @param award Premi ricevuti
     * @param greenStar Riconoscimenti sostenibilità
     * @param facilitiesAndServices Servizi offerti
     * @param description Descrizione
     */
    public Restaurant(String name, String address, String location, String price, String cuisine,
                      double longitude, double latitude, String phoneNumber, String url,
                      String websiteUrl, String award, String greenStar, String facilitiesAndServices, String description) {
        this(null, name, address, location, price, cuisine, longitude, latitude, phoneNumber, url,
                websiteUrl, award, greenStar, facilitiesAndServices, description, null);
    }

    /**
     * Costruttore per nuovi ristoranti associati a un ristoratore specifico.
     *
     * @param name Nome del ristorante
     * @param address Indirizzo completo
     * @param location Città o località
     * @param price Fascia di prezzo
     * @param cuisine Tipo di cucina
     * @param longitude Coordinata longitudine
     * @param latitude Coordinata latitudine
     * @param phoneNumber Numero di telefono
     * @param url URL pagina portale
     * @param websiteUrl Sito web ufficiale
     * @param award Premi ricevuti
     * @param greenStar Riconoscimenti sostenibilità
     * @param facilitiesAndServices Servizi offerti
     * @param description Descrizione
     * @param restaurateurEmail Email del ristoratore proprietario
     */
    public Restaurant(String name, String address, String location, String price, String cuisine,
                      double longitude, double latitude, String phoneNumber, String url,
                      String websiteUrl, String award, String greenStar, String facilitiesAndServices, String description, String restaurateurEmail) {
        this(null, name, address, location, price, cuisine, longitude, latitude, phoneNumber, url,
                websiteUrl, award, greenStar, facilitiesAndServices, description, restaurateurEmail);
    }

    /**
     * Costruttore completo per il caricamento da persistenza (es. CSV).
     * <p>
     * Utilizza {@code String.intern()} per i campi ad alta ripetitività (location, price, cuisine, ecc.)
     * per ridurre l'impronta di memoria dell'applicazione.
     * </p>
     *
     * @param id Identificativo univoco (può essere null per nuovi record)
     * @param name Nome del ristorante
     * @param address Indirizzo completo
     * @param location Città o località
     * @param price Fascia di prezzo
     * @param cuisine Tipo di cucina
     * @param longitude Coordinata longitudine
     * @param latitude Coordinata latitudine
     * @param phoneNumber Numero di telefono
     * @param url URL pagina portale
     * @param websiteUrl Sito web ufficiale
     * @param award Premi ricevuti
     * @param greenStar Riconoscimenti sostenibilità
     * @param facilitiesAndServices Servizi offerti
     * @param description Descrizione
     * @param restaurateurEmail Email del ristoratore proprietario
     */
    public Restaurant(Long id, String name, String address, String location, String price, String cuisine,
                      double longitude, double latitude, String phoneNumber, String url,
                      String websiteUrl, String award, String greenStar, String facilitiesAndServices, String description, String restaurateurEmail) {
        this(id, name, address, location, price, cuisine, longitude, latitude, phoneNumber, url, 
             websiteUrl, award, greenStar, facilitiesAndServices, description, restaurateurEmail, null);
    }
    
    /**
     * Costruttore privato completo interno che include distanceKm.
     */
    private Restaurant(Long id, String name, String address, String location, String price, String cuisine,
                      double longitude, double latitude, String phoneNumber, String url,
                      String websiteUrl, String award, String greenStar, String facilitiesAndServices, String description, String restaurateurEmail, Double distanceKm) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.location = intern(location);
        this.price = intern(price);
        this.cuisine = intern(cuisine);
        this.longitude = longitude;
        this.latitude = latitude;
        this.phoneNumber = phoneNumber;
        this.url = url;
        this.websiteUrl = websiteUrl;
        this.award = intern(award);
        this.greenStar = intern(greenStar);
        this.facilitiesAndServices = intern(facilitiesAndServices);
        this.description = description;
        this.restaurateurEmail = restaurateurEmail;
        this.distanceKm = distanceKm;
    }

    // METODI
    /**
     * Restituisce l'ID del ristorante.
     * @return L'identificativo univoco o null se non ancora persistito.
     */
    public Long getId() { return id; }

    /**
     * Restituisce il nome del ristorante.
     * @return Il nome del ristorante.
     */
    public String getName() { return name; }

    /**
     * Restituisce l'indirizzo del ristorante.
     * @return L'indirizzo completo.
     */
    public String getAddress() { return address; }

    /**
     * Restituisce la località del ristorante.
     * @return La città o zona.
     */
    public String getLocation() { return location; }

    /**
     * Restituisce la fascia di prezzo.
     * @return Una stringa rappresentativa del prezzo (es. "$$$").
     */
    public String getPrice() { return price; }

    /**
     * Restituisce il tipo di cucina.
     * @return Il tipo di cucina (es. "Italian").
     */
    public String getCuisine() { return cuisine; }

    /**
     * Restituisce la longitudine.
     * @return La coordinata di longitudine.
     */
    public double getLongitude() { return longitude; }

    /**
     * Restituisce la latitudine.
     * @return La coordinata di latitudine.
     */
    public double getLatitude() { return latitude; }

    /**
     * Restituisce il numero di telefono.
     * @return Il recapito telefonico.
     */
    public String getPhoneNumber() { return phoneNumber; }

    /**
     * Restituisce l'URL della pagina sul portale.
     * @return L'URL della scheda ristorante.
     */
    public String getUrl() { return url; }

    /**
     * Restituisce l'URL del sito web ufficiale.
     * @return L'URL del sito web.
     */
    public String getWebsiteUrl() { return websiteUrl; }

    /**
     * Restituisce i premi ricevuti.
     * @return Stringa descrittiva dei premi.
     */
    public String getAward() { return award; }

    /**
     * Restituisce i riconoscimenti green.
     * @return Stringa descrittiva dei riconoscimenti sostenibili.
     */
    public String getGreenStar() { return greenStar; }

    /**
     * Restituisce i servizi e le strutture.
     * @return Elenco dei servizi disponibili.
     */
    public String getFacilitiesAndServices() { return facilitiesAndServices; }

    /**
     * Restituisce la descrizione del ristorante.
     * @return Testo descrittivo.
     */
    public String getDescription() { return description; }

    /**
     * Restituisce l'email del ristoratore proprietario.
     * @return L'email del proprietario o null se non assegnato.
     */
    public String getRestaurateurEmail() { return restaurateurEmail; }

    /**
     * Restituisce la distanza in km dall'utente (se calcolata).
     * @return La distanza in km o null.
     */
    public Double getDistanceKm() { return distanceKm; }

    /**
     * Crea una copia immutabile di questo ristorante con un nuovo ID.
     * <p>
     * Utilizzato dal servizio di persistenza durante il salvataggio di nuovi record
     * per assegnare l'ID generato.
     * </p>
     *
     * @param newId Il nuovo ID da assegnare.
     * @return Una nuova istanza di Restaurant con l'ID specificato.
     */
    public Restaurant withId(Long newId) {
        return new Restaurant(newId, name, address, location, price, cuisine, longitude, latitude,
                phoneNumber, url, websiteUrl, award, greenStar, facilitiesAndServices, description, restaurateurEmail, distanceKm);
    }

    /**
     * Restituisce una copia di questo ristorante con la distanza specificata.
     * 
     * @param distanceKm La distanza in km.
     * @return Una nuova istanza con la distanza impostata.
     */
    public Restaurant withDistance(Double distanceKm) {
        return new Restaurant(id, name, address, location, price, cuisine, longitude, latitude,
                phoneNumber, url, websiteUrl, award, greenStar, facilitiesAndServices, description, restaurateurEmail, distanceKm);
    }

    /**
     * Restituisce una rappresentazione stringa del ristorante in formato CSV.
     * <p>
     * Il formato è: Id,Name,Address,Location,Price,Cuisine,Longitude,Latitude,PhoneNumber,Url,WebsiteUrl,Award,GreenStar,FacilitiesAndServices,Description,RestaurateurEmail
     * </p>
     *
     * @return Stringa rappresentativa del ristorante in formato CSV.
     */
    @Override
    public String toString() {
        String idStr = id != null ? id.toString() : "";
        String n = csvEscape(name);
        String a = csvEscape(address);
        String l = csvEscape(location);
        String p = csvEscape(price);
        String c = csvEscape(cuisine);
        String lo = Double.toString(longitude);
        String la = Double.toString(latitude);
        String ph = csvEscape(phoneNumber);
        String u = csvEscape(url);
        String wu = csvEscape(websiteUrl);
        String aw = csvEscape(award);
        String gs = csvEscape(greenStar);
        String fs = csvEscape(facilitiesAndServices);
        String d = csvEscape(description);
        String re = csvEscape(restaurateurEmail);
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                idStr, n, a, l, p, c, lo, la, ph, u, wu, aw, gs, fs, d, re);
    }

    /**
     * Escapa una stringa per l'uso in formato CSV.
     * <p>
     * Sostituisce i doppi apici con apostrofi e racchiude la stringa tra doppi apici.
     * </p>
     *
     * @param s La stringa da escapare.
     * @return La stringa escapata, o stringa vuota se s è null.
     */
    private static String csvEscape(String s) {
        if (s == null) return "";
        String cleaned = s.replace("\"", "'");
        return "\"" + cleaned + "\"";
    }

    /**
     * Interna una stringa per garantire l'uguaglianza referenziale.
     * <p>
     * Se la stringa è null, restituisce null. Altrimenti restituisce la stringa internata.
     * </p>
     *
     * @param value La stringa da internare.
     * @return La stringa internata o null se value è null.
     */
    private static String intern(String value) {
        return value == null ? null : value.intern();
    }
}
