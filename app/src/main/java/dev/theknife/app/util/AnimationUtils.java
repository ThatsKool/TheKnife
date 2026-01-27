/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.util;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Classe di utilità per animazioni comuni dell'interfaccia utente.
 * <p>
 * Fornisce transizioni leggere per migliorare l'esperienza utente senza un uso eccessivo
 * di risorse. Tutte le animazioni utilizzano JavaFX Animation API e sono ottimizzate
 * per prestazioni fluide.
 * </p>
 * <p>
 * <b>Tipi di animazioni supportate:</b>
 * <ul>
 *   <li>Fade in/out (dissolvenza)</li>
 *   <li>Pop-in (effetto scala)</li>
 *   <li>Hover effects (effetti al passaggio del mouse)</li>
 *   <li>Shake (scuotimento per feedback di errore)</li>
 *   <li>Slide in (scorrimento da diverse direzioni)</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class AnimationUtils {

    // CAMPI
    private static final int DEFAULT_DURATION = 300;

    // COSTRUTTORI
    /**
     * Costruttore privato per prevenire l'istanziazione.
     * <p>
     * La classe espone solo metodi statici di utilità.
     * </p>
     */
    private AnimationUtils() {
    }

    // METODI
    /**
     * Applica un effetto di dissolvenza in entrata (fade in) a un nodo.
     * <p>
     * Il nodo passa da opacità 0.0 a 1.0 nel tempo specificato.
     * </p>
     *
     * @param node Il nodo JavaFX da animare.
     * @param durationMs Durata dell'animazione in millisecondi.
     */
    public static void fadeIn(Node node, int durationMs) {
        if (node == null) return;
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.play();
    }

    /**
     * Applica un effetto di dissolvenza in entrata con durata predefinita (300ms).
     *
     * @param node Il nodo JavaFX da animare.
     */
    public static void fadeIn(Node node) {
        fadeIn(node, DEFAULT_DURATION);
    }

    /**
     * Applica un effetto pop-in (scala da 0.9 a 1.0) con durata predefinita.
     * <p>
     * Utile per dialoghi e card che devono apparire con un effetto di ingrandimento.
     * </p>
     *
     * @param node Il nodo JavaFX da animare.
     */
    public static void popIn(Node node) {
        popIn(node, DEFAULT_DURATION);
    }

    /**
     * Applica un effetto pop-in con durata personalizzata.
     * <p>
     * Combina scala e dissolvenza per creare un effetto di apparizione dinamico.
     * </p>
     *
     * @param node Il nodo JavaFX da animare.
     * @param durationMs Durata dell'animazione in millisecondi.
     */
    public static void popIn(Node node, int durationMs) {
        if (node == null) return;
        node.setScaleX(0.9);
        node.setScaleY(0.9);
        node.setOpacity(0);

        ScaleTransition st = new ScaleTransition(Duration.millis(durationMs), node);
        st.setFromX(0.9);
        st.setFromY(0.9);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);

        st.play();
        ft.play();
    }

    /**
     * Aggiunge un effetto hover sottile (ingrandimento) a un nodo.
     * <p>
     * Quando il mouse entra nel nodo, viene scalato a 1.02x. Quando esce, torna a 1.0x.
     * </p>
     *
     * @param node Il nodo JavaFX a cui aggiungere l'effetto hover.
     */
    public static void addHoverEffect(Node node) {
        if (node == null) return;

        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), node);
        scaleUp.setToX(1.02);
        scaleUp.setToY(1.02);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), node);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setInterpolator(Interpolator.EASE_OUT);

        node.setOnMouseEntered(e -> scaleUp.playFromStart());
        node.setOnMouseExited(e -> scaleDown.playFromStart());
    }

    /**
     * Scuote un nodo orizzontalmente per fornire feedback visivo di errore.
     * <p>
     * Utile per indicare input non validi o azioni fallite.
     * </p>
     *
     * @param node Il nodo JavaFX da scuotere.
     */
    public static void shake(Node node) {
        if (node == null) return;
        TranslateTransition tt = new TranslateTransition(Duration.millis(100), node);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }

    /**
     * Fa scorrere un nodo dal basso verso l'alto con un ritardo opzionale.
     * <p>
     * Combina movimento verticale e dissolvenza per un effetto di entrata fluido.
     * </p>
     *
     * @param node Il nodo JavaFX da animare.
     * @param durationMs Durata dell'animazione in millisecondi.
     * @param delayMs Ritardo prima dell'inizio dell'animazione in millisecondi.
     */
    public static void slideInFromBottom(Node node, int durationMs, int delayMs) {
        if (node == null) return;
        node.setOpacity(0);
        node.setTranslateY(50);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromY(50);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.setDelay(Duration.millis(delayMs));
        
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.setDelay(Duration.millis(delayMs));
        
        tt.play();
        ft.play();
    }

    /**
     * Fa scorrere un nodo dal basso verso l'alto senza ritardo.
     *
     * @param node Il nodo JavaFX da animare.
     * @param durationMs Durata dell'animazione in millisecondi.
     */
    public static void slideInFromBottom(Node node, int durationMs) {
        slideInFromBottom(node, durationMs, 0);
    }

    /**
     * Fa scorrere un nodo da sinistra verso destra con un ritardo opzionale.
     *
     * @param node Il nodo JavaFX da animare.
     * @param durationMs Durata dell'animazione in millisecondi.
     * @param delayMs Ritardo prima dell'inizio dell'animazione in millisecondi.
     */
    public static void slideInFromLeft(Node node, int durationMs, int delayMs) {
        if (node == null) return;
        node.setOpacity(0);
        node.setTranslateX(-50);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromX(-50);
        tt.setToX(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        tt.setDelay(Duration.millis(delayMs));
        
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        ft.setDelay(Duration.millis(delayMs));
        
        tt.play();
        ft.play();
    }

    /**
     * Fa scorrere un nodo da sinistra verso destra senza ritardo.
     *
     * @param node Il nodo JavaFX da animare.
     * @param durationMs Durata dell'animazione in millisecondi.
     */
    public static void slideInFromLeft(Node node, int durationMs) {
        slideInFromLeft(node, durationMs, 0);
    }

    /**
     * Fa scorrere un nodo da destra verso sinistra.
     *
     * @param node Il nodo JavaFX da animare.
     * @param durationMs Durata dell'animazione in millisecondi.
     */
    public static void slideInFromRight(Node node, int durationMs) {
        if (node == null) return;
        node.setOpacity(0);
        node.setTranslateX(50);
        
        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromX(50);
        tt.setToX(0);
        tt.setInterpolator(Interpolator.EASE_OUT);
        
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);
        
        tt.play();
        ft.play();
    }

    /**
     * Applica un'animazione hover standardizzata a un pulsante.
     * <p>
     * Sostituisce le modifiche manuali di stile tramite setOnMouseEntered/Exited per la scala.
     * Nota: Questo metodo gestisce solo la scala. Le modifiche di colore devono essere gestite
     * tramite CSS o logica specifica se complesse.
     * </p>
     *
     * @param button Il pulsante JavaFX a cui applicare l'animazione hover.
     */
    public static void applyButtonHoverAnimation(javafx.scene.control.Button button) {
        if (button == null) return;
        
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setInterpolator(Interpolator.EASE_OUT);
        
        // Chain with existing event handlers if any? 
        // For simplicity, we assume this is the primary hover effect or we append to it.
        // However, JavaFX setOnMouseEntered replaces previous handler.
        // To be safe, we should wrap the existing handler if possible, but for this project
        // we will assume we are setting the main effect.
        // Better approach: use addEventHandler to not overwrite existing logic.
        
        button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e -> scaleUp.playFromStart());
        button.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e -> scaleDown.playFromStart());
    }
}
