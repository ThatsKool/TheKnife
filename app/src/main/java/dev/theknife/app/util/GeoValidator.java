/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.util;

/**
 * Utility per la validazione di coordinate geografiche.
 * <p>
 * Fornisce metodi statici per verificare la correttezza di latitudine e longitudine
 * secondo lo standard WGS84. È utilizzata in tutta l'applicazione per garantire
 * l'integrità dei dati geospaziali prima del salvataggio o dell'utilizzo in calcoli.
 * </p>
 * <p>
 * <b>Funzionalità:</b>
 * <ul>
 *   <li>Validazione range latitudine (-90 a +90)</li>
 *   <li>Validazione range longitudine (-180 a +180)</li>
 *   <li>Metodi helper che lanciano eccezioni per validazione rigorosa</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class GeoValidator {
    
    // CAMPI

    // COSTRUTTORI
    /**
     * Costruttore privato per prevenire l'istanziazione.
     * <p>
     * La classe espone solo metodi statici di utilità.
     * </p>
     */
    private GeoValidator() {
    }

    // METODI
    /**
     * Verifica se la latitudine è valida (tra -90 e 90).
     * 
     * @param lat Latitudine da verificare.
     * @return true se valida, false altrimenti.
     */
    public static boolean isValidLatitude(double lat) {
        return lat >= -90 && lat <= 90;
    }
    
    /**
     * Verifica se la longitudine è valida (tra -180 e 180).
     * 
     * @param lon Longitudine da verificare.
     * @return true se valida, false altrimenti.
     */
    public static boolean isValidLongitude(double lon) {
        return lon >= -180 && lon <= 180;
    }
    
    /**
     * Valida le coordinate e lancia un'eccezione se non valide.
     * 
     * @param lat Latitudine.
     * @param lon Longitudine.
     * @throws IllegalArgumentException se le coordinate non sono valide.
     */
    public static void validateCoordinates(double lat, double lon) {
        if (!isValidLatitude(lat)) {
            throw new IllegalArgumentException("La latitudine deve essere tra -90 e 90");
        }
        if (!isValidLongitude(lon)) {
            throw new IllegalArgumentException("La longitudine deve essere tra -180 e 180");
        }
    }
}
