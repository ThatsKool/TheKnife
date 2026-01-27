/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.util;

import dev.theknife.app.config.ResourceFileHelper;
import javafx.scene.image.Image;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Gestore delle immagini per le diverse tipologie di cucina.
 * <p>
 * Questa classe implementa il pattern Singleton per gestire centralmente il caricamento
 * e la cache delle risorse grafiche. Mappa specifiche tipologie di cucina a immagini
 * macro-categoria (es. "Sushi", "Ramen" -> "japanese.jpg").
 * </p>
 * <p>
 * <b>Funzionalità principali:</b>
 * <ul>
 *   <li>Caricamento lazy delle immagini dal classpath.</li>
 *   <li>Caching in memoria per evitare letture I/O ripetute.</li>
 *   <li>Logica di fallback su immagine di default.</li>
 *   <li>Mapping intelligente di stringhe parziali (es. "Tuscan" -> "italian.jpg").</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class CuisineImageManager {
    // CAMPI
    private static final Logger logger = Logger.getLogger(CuisineImageManager.class.getName());
    private static CuisineImageManager instance;
    
    /** Cache delle immagini caricate per evitare riletture dal disco/classpath. */
    private final Map<String, Image> imageCache;
    
    // COSTRUTTORI
    /**
     * Costruttore privato per pattern Singleton.
     */
    private CuisineImageManager() {
        imageCache = new HashMap<>();
    }
    
    // METODI
    /**
     * Restituisce l'istanza unica del manager.
     * 
     * @return L'istanza singleton di {@link CuisineImageManager}.
     */
    public static synchronized CuisineImageManager getInstance() {
        if (instance == null) {
            instance = new CuisineImageManager();
        }
        return instance;
    }

    /**
     * Recupera l'immagine associata a una specifica cucina.
     * <p>
     * Se l'immagine è già in cache, viene restituita immediatamente.
     * Altrimenti, viene determinato il nome del file tramite {@link #getMacroGroupImageName(String)},
     * caricata dal disco e cacheata.
     * </p>
     * 
     * @param cuisine La stringa che descrive la cucina (es. "Tuscan", "Sushi").
     * @return L'oggetto {@link Image} corrispondente o l'immagine di default se non trovata.
     */
    public Image getImageForCuisine(String cuisine) {
        String imageName = getMacroGroupImageName(cuisine);
        
        if (imageCache.containsKey(imageName)) {
            return imageCache.get(imageName);
        }
        
        Image image = loadImage(imageName);
        if (image != null) {
            imageCache.put(imageName, image);
            return image;
        }
        
        // Fallback to default
        if (imageCache.containsKey("default")) {
            return imageCache.get("default");
        }
        
        Image defaultImage = loadImage("default.jpg");
        if (defaultImage != null) {
            imageCache.put("default", defaultImage);
            return defaultImage;
        }
        
        return null; // Should ideally return a placeholder generated in code if even default is missing
    }

    /**
     * Carica un'immagine dalla directory {@code data/images/cuisines/} dato il nome del file.
     *
     * @param filename Il nome del file immagine (es. "italian.jpg").
     * @return L'oggetto {@link Image} caricato o {@code null} se il caricamento fallisce.
     */
    private Image loadImage(String filename) {
        Path base = ResourceFileHelper.getImagesDirectory().resolve("cuisines");
        Path path = base.resolve(filename);
        try {
            if (Files.isRegularFile(path)) {
                return new Image(path.toUri().toString());
            }
        } catch (Exception e) {
            logger.warning("Failed to load image: " + path + " - " + e.getMessage());
        }
        return null;
    }

    /**
     * Determina il nome del file immagine macro-categoria per una data cucina.
     * <p>
     * Esegue una serie di controlli case-insensitive per mappare cucine specifiche
     * a categorie generali (es. "Napolitan Pizza" -> "pizza.jpg").
     * </p>
     * 
     * @param cuisine La stringa della cucina.
     * @return Il nome del file immagine associato (incluso estensione .jpg).
     */
    public String getMacroGroupImageName(String cuisine) {
        if (cuisine == null || cuisine.trim().isEmpty()) {
            return "default.jpg";
        }

        String c = cuisine.toLowerCase();

        // 1. Italian
        if (c.contains("italian") || c.contains("tuscan") || c.contains("roman") || 
            c.contains("sicilian") || c.contains("piedmontese") || c.contains("lombardian") || 
            c.contains("venetian") || c.contains("sardinian") || c.contains("apulian") || 
            c.contains("calabrian") || c.contains("emilian") || c.contains("ligurian") || 
            c.contains("mantuan") || c.contains("umbrian") || c.contains("south tyrolean") ||
            c.contains("abruzzo") || c.contains("basilicata") || c.contains("lazio") || 
            c.contains("romagna") || c.contains("valtellina") || c.contains("aosta valley") || 
            c.contains("marches")) {
            return "italian.jpg";
        }

        // 2. Pizza
        if (c.contains("pizza")) {
            return "pizza.jpg";
        }

        // 3. French
        if (c.contains("french") || c.contains("alsatian") || c.contains("breton") || 
            c.contains("burgundian") || c.contains("corsican") || c.contains("lyonnaise") || 
            c.contains("provençal") || c.contains("savoyard") || c.contains("franche-comté")) {
            return "french.jpg";
        }

        // 4. Spanish & Portuguese
        if (c.contains("spanish") || c.contains("portuguese") || c.contains("basque") || 
            c.contains("catalan") || c.contains("galician") || c.contains("andalusian") || 
            c.contains("asturian") || c.contains("castilian")) {
            return "spanish.jpg";
        }

        // 5. Greek & Mediterranean
        if (c.contains("greek") || c.contains("mediterranean")) {
            return "greek.jpg";
        }

        // 6. British & Irish
        if (c.contains("british") || c.contains("english") || c.contains("scottish") || 
            c.contains("irish") || c.contains("fish and chips")) {
            return "british.jpg";
        }

        // 7. German, Austrian, Swiss
        if (c.contains("german") || c.contains("austrian") || c.contains("swiss") || 
            c.contains("bavarian") || c.contains("swabian") || c.contains("alpine") || 
            c.contains("fondue")) {
            return "german.jpg";
        }

        // 8. Scandinavian
        if (c.contains("scandinavian") || c.contains("danish") || c.contains("swedish") || 
            c.contains("norwegian") || c.contains("finnish") || c.contains("belgian") || 
            c.contains("flemish") || c.contains("smørrebrød")) {
            return "scandinavian.jpg";
        }

        // 9. Eastern European
        if (c.contains("eastern european") || c.contains("polish") || c.contains("russian") || 
            c.contains("hungarian") || c.contains("czech") || c.contains("croatian") || 
            c.contains("balkan")) {
            return "eastern_european.jpg";
        }
        
        // 10. European (Generic)
        if (c.contains("european")) {
            return "european.jpg";
        }

        // 11. American & BBQ
        if (c.contains("american") || c.contains("californian") || c.contains("cajun") || 
            c.contains("creole") || c.contains("southern") || c.contains("barbecue") || 
            c.contains("bbq")) {
            return "american.jpg";
        }

        // 12. Mexican & Tex-Mex
        if (c.contains("mexican") || c.contains("tex-mex")) {
            return "mexican.jpg";
        }

        // 13. Latin American
        if (c.contains("south american") || c.contains("latin american") || c.contains("argentinian") || 
            c.contains("brazilian") || c.contains("colombian") || c.contains("cuban") || 
            c.contains("peruvian") || c.contains("venezuelan") || c.contains("puerto rican") || 
            c.contains("jamaican") || c.contains("caribbean")) {
            return "latin_american.jpg";
        }

        // 14. Japanese & Sushi
        if (c.contains("japanese") || c.contains("sushi") || c.contains("ramen") || 
            c.contains("teppanyaki") || c.contains("tempura") || c.contains("udon") || 
            c.contains("soba") || c.contains("yakitori") || c.contains("tonkatsu") || 
            c.contains("izakaya") || c.contains("kyoto") || c.contains("oden") || 
            c.contains("okonomiyaki") || c.contains("unagi") || c.contains("fugu") || 
            c.contains("shabu-shabu") || c.contains("sukiyaki") || c.contains("kushiage") || 
            c.contains("yoshoku") || c.contains("shojin") || c.contains("obanzai") || 
            c.contains("onigiri") || c.contains("japan")) {
            return "japanese.jpg";
        }

        // 15. Chinese
        if (c.contains("chinese") || c.contains("cantonese") || c.contains("sichuan") || 
            c.contains("dim sum") || c.contains("beijing") || c.contains("shanghai") || 
            c.contains("hunanese") || c.contains("fujian") || c.contains("chiu chow") || 
            c.contains("dongbei") || c.contains("hainanese") || c.contains("hakkanese") || 
            c.contains("hang zhou") || c.contains("huaiyang") || c.contains("hubei") || 
            c.contains("hui") || c.contains("jiangzhe") || c.contains("macanese") || 
            c.contains("ningbo") || c.contains("shaanxi") || c.contains("shandong") || 
            c.contains("shun tak") || c.contains("taizhou") || c.contains("teochew") || 
            c.contains("xibei") || c.contains("xinjiang") || c.contains("yunnanese") || 
            c.contains("zhejiang") || c.contains("hotpot") || c.contains("peking")) {
            return "chinese.jpg";
        }

        // 16. Korean
        if (c.contains("korean") || c.contains("bulgogi") || c.contains("gejang") || 
            c.contains("gomtang") || c.contains("jokbal") || c.contains("kalguksu") || 
            c.contains("mandu") || c.contains("naengmyeon") || c.contains("seolleongtang") || 
            c.contains("sujebi") || c.contains("yukhoe") || c.contains("chueotang") || 
            c.contains("doganitang") || c.contains("dubu") || c.contains("dwaeji")) {
            return "korean.jpg";
        }

        // 17. Thai
        if (c.contains("thai") || c.contains("isan")) {
            return "thai.jpg";
        }

        // 18. Vietnamese
        if (c.contains("vietnamese")) {
            return "vietnamese.jpg";
        }

        // 19. Indian & South Asian
        if (c.contains("indian") || c.contains("curry") || c.contains("pakistani") || 
            c.contains("sri lankan") || c.contains("nepali") || c.contains("tibetan")) {
            return "indian.jpg";
        }

        // 20. Asian (Generic & SE)
        if (c.contains("asian") || c.contains("malaysian") || c.contains("singaporean") || 
            c.contains("indonesian") || c.contains("filipino") || c.contains("cambodian") || 
            c.contains("lao") || c.contains("burmese") || c.contains("taiwanese") || 
            c.contains("peranakan") || c.contains("balinese")) {
            return "asian.jpg";
        }

        // 21. Middle Eastern
        if (c.contains("middle eastern") || c.contains("lebanese") || c.contains("israeli") || 
            c.contains("turkish") || c.contains("persian") || c.contains("emirati") || 
            c.contains("armenian") || c.contains("afghan")) {
            return "middle_eastern.jpg";
        }

        // 22. African
        if (c.contains("african") || c.contains("moroccan") || c.contains("ethiopian") || 
            c.contains("egyptian")) {
            return "african.jpg";
        }

        // 23. Steakhouse & Grill
        if (c.contains("steakhouse") || c.contains("meat") || c.contains("grill") || 
            c.contains("beef") || c.contains("pork") || c.contains("lamb") || 
            c.contains("chicken") || c.contains("duck")) {
            return "steakhouse.jpg";
        }

        // 24. Seafood
        if (c.contains("seafood") || c.contains("shellfish") || c.contains("crab") || 
            c.contains("oyster")) {
            return "seafood.jpg";
        }

        // 25. Vegetarian & Vegan
        if (c.contains("vegetarian") || c.contains("vegan") || c.contains("organic")) {
            return "vegetarian.jpg";
        }

        // 26. Street Food
        if (c.contains("street food") || c.contains("noodle") || c.contains("congee") || 
            c.contains("dumpling") || c.contains("small eats") || c.contains("deli") || 
            c.contains("bakery") || c.contains("rice")) {
            return "street_food.jpg";
        }

        // 27. Fusion & Creative
        if (c.contains("fusion") || c.contains("creative") || c.contains("innovative") || 
            c.contains("contemporary") || c.contains("modern") || c.contains("international") || 
            c.contains("world") || c.contains("gastropub") || c.contains("farm") || 
            c.contains("seasonal") || c.contains("sharing")) {
            return "fusion.jpg";
        }

        // 28. Traditional
        if (c.contains("traditional") || c.contains("classic") || c.contains("home") || 
            c.contains("country") || c.contains("regional") || c.contains("cheese")) {
            return "traditional.jpg";
        }

        return "default.jpg";
    }
}
