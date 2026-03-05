/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.container;

import dev.theknife.app.auth.SessionManager;
import dev.theknife.app.config.DefaultFileProvider;
import dev.theknife.app.config.FileProvider;
import dev.theknife.app.service.*;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.Logger;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Container semplice per la Dependency Injection (DI) e la gestione dei servizi.
 * <p>
 * Implementa il pattern Singleton per fornire un unico punto di accesso alle istanze
 * dei servizi in tutta l'applicazione. Favorisce il disaccoppiamento tra i componenti
 * gestendo la creazione e il ciclo di vita delle dipendenze.
 * </p>
 * <p>
 * <b>Funzionalità:</b>
 * <ul>
 *   <li>Registrazione di istanze Singleton.</li>
 *   <li>Registrazione di Factory per la creazione on-demand (Lazy Initialization).</li>
 *   <li>Risoluzione automatica delle dipendenze richieste.</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class DependencyContainer {
    // CAMPI
    private static final DependencyContainer instance = new DependencyContainer();
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();
    private final Logger logger;
    
    // COSTRUTTORI
    /**
     * Costruttore privato per pattern Singleton.
     * <p>
     * Inizializza il logger e prepara il container per la registrazione dei servizi.
     * </p>
     */
    private DependencyContainer() {
        this.logger = Logger.getLogger(DependencyContainer.class);
    }
    
    // METODI
    /**
     * Restituisce l'istanza singleton del container.
     * 
     * @return L'istanza unica di DependencyContainer.
     */
    public static DependencyContainer getInstance() {
        return instance;
    }
    
    /**
     * Registra un'istanza singleton per una specifica classe/interfaccia.
     * 
     * @param clazz La classe o interfaccia chiave per il recupero.
     * @param instance L'istanza concreta da associare.
     * @param <T> Il tipo della classe.
     */
    public <T> void registerSingleton(Class<T> clazz, T instance) {
        singletons.put(clazz, instance);
    }
    
    /**
     * Registra una factory per la creazione di istanze di una classe.
     * <p>
     * Utile per oggetti che richiedono una nuova istanza ogni volta o per
     * inizializzazione pigra (lazy).
     * </p>
     * 
     * @param clazz La classe o interfaccia chiave.
     * @param factory La funzione fornitore (Supplier) che crea l'istanza.
     * @param <T> Il tipo della classe.
     */
    public <T> void registerFactory(Class<T> clazz, Supplier<T> factory) {
        factories.put(clazz, factory);
    }
    
    /**
     * Recupera un'istanza della classe specificata.
     * <p>
     * Cerca prima tra i singleton registrati. Se non trovata, controlla le factory.
     * Se trovata una factory, crea l'istanza, la cachea come singleton (in questa implementazione ibrida)
     * e la restituisce.
     * </p>
     * 
     * @param clazz La classe da recuperare.
     * @return L'istanza richiesta.
     * @param <T> Il tipo della classe.
     * @throws IllegalArgumentException se nessuna registrazione è trovata per la classe.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        // Check if we have a singleton instance
        if (singletons.containsKey(clazz)) {
            return (T) singletons.get(clazz);
        }
        
        // Check if we have a factory
        if (factories.containsKey(clazz)) {
            Supplier<T> factory = (Supplier<T>) factories.get(clazz);
            T instance = factory.get();
            
            // Cache the instance as singleton
            singletons.put(clazz, instance);
            return instance;
        }
        
        throw new IllegalArgumentException("No registration found for class: " + clazz.getName());
    }
    
    /**
     * Verifica se una classe è registrata nel container.
     * 
     * @param clazz La classe da verificare.
     * @return true se registrata, false altrimenti.
     */
    public boolean isRegistered(Class<?> clazz) {
        return singletons.containsKey(clazz) || factories.containsKey(clazz);
    }
    
    /**
     * Rimuove tutte le registrazioni.
     * <p>
     * Utile principalmente per i test per resettare lo stato tra un test e l'altro.
     * </p>
     */
    public void clear() {
        singletons.clear();
        factories.clear();
    }
    
    /**
     * Inizializza tutti i servizi principali e le dipendenze dell'applicazione.
     * <p>
     * Questo metodo deve essere chiamato una sola volta all'avvio dell'applicazione (nel metodo main o start).
     * Configura:
     * <ul>
     *   <li>{@link FileProvider} per la gestione dei percorsi file.</li>
     *   <li>Servizi di business (Restaurant, Review, User, Favorite).</li>
     * </ul>
     * </p>
     */
    public void initializeAllServices() {
        try {
            //Inizializza FileProvider prima di tutto
            FileProvider fileProvider = new DefaultFileProvider();
            registerSingleton(FileProvider.class, fileProvider);
            logger.info("FileProvider initialized");

            // Context di sessione (singola istanza, iniettata; nessun getInstance() altrove)
            SessionContext sessionContext = SessionManager.getInstance();
            registerSingleton(SessionContext.class, sessionContext);
            logger.info("SessionContext initialized");
            
            // Registra i servizi con le loro dipendenze (sia interfacce che classi concrete)
            RestaurantService restaurantService = new RestaurantService(fileProvider);
            RestaurantQueryService restaurantQueryService = new RestaurantQueryService(restaurantService);
            restaurantService.setRestaurantQueryService(restaurantQueryService);

            registerSingleton(IRestaurantService.class, restaurantService);
            registerSingleton(RestaurantService.class, restaurantService);
            registerSingleton(RestaurantQueryService.class, restaurantQueryService);
            
            UserService userService = new UserService(fileProvider);
            registerSingleton(IUserService.class, userService);
            registerSingleton(UserService.class, userService);
            
            IRestaurantService iRestaurantService = restaurantService;
            ReviewService reviewService = new ReviewService(fileProvider, iRestaurantService);
            registerSingleton(IReviewService.class, reviewService);
            registerSingleton(ReviewService.class, reviewService);
            
            FavoriteService favoriteService = new FavoriteService(fileProvider);
            registerSingleton(IFavoriteService.class, favoriteService);
            registerSingleton(FavoriteService.class, favoriteService);
            
            logger.info("All services initialized");
        } catch (IOException e) {
            logger.error("Failed to initialize services: " + e.getMessage(), e);
            throw new RuntimeException("Failed to initialize dependency container", e);
        }
    }
    
}
