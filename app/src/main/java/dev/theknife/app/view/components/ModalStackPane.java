/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.view.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * StackPane specializzato che supporta un layer di overlay modale.
 * <p>
 * Questo contenitore ha due livelli:
 * <ol>
 *   <li>Il layer del contenuto principale (sotto)</li>
 *   <li>Il layer di overlay modale (sopra, inizialmente nascosto)</li>
 * </ol>
 * </p>
 * <p>
 * Quando viene mostrato un modale, viene visualizzato sopra il contenuto principale
 * con uno sfondo semi-trasparente che oscura il contenuto sottostante. Questo permette
 * di creare dialoghi modali senza dover gestire Stage separati.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.view.ModalManager
 */
public class ModalStackPane extends StackPane {
    // CAMPI
    private final StackPane modalContainer;
    private final Rectangle overlayBackground;
    
    // COSTRUTTORI
    /**
     * Costruisce un ModalStackPane con il contenuto principale specificato.
     *
     * @param content Il contenuto principale da visualizzare sotto il layer modale.
     */
    public ModalStackPane(Parent content) {
        // 1. Main Content Layer
        this.getChildren().add(content);
        
        // 2. Modal Overlay Layer
        this.modalContainer = new StackPane();
        this.modalContainer.setAlignment(Pos.CENTER);
        this.modalContainer.setVisible(false);
        
        // Create semi-transparent background
        this.overlayBackground = new Rectangle();
        this.overlayBackground.setFill(Color.rgb(0, 0, 0, 0.5));
        // Bind rectangle size to stackpane size
        this.overlayBackground.widthProperty().bind(this.widthProperty());
        this.overlayBackground.heightProperty().bind(this.heightProperty());
        
        // Add background and a container for the actual modal dialog
        this.modalContainer.getChildren().add(overlayBackground);
        
        this.getChildren().add(modalContainer);
    }
    
    // METODI
    /**
     * Mostra un dialogo modale con uno sfondo semi-trasparente.
     *
     * @param modalContent Il contenuto del dialogo modale.
     */
    public void showModal(Node modalContent) {
        // Ensure we don't have old content (except background)
        if (modalContainer.getChildren().size() > 1) {
            modalContainer.getChildren().remove(1, modalContainer.getChildren().size());
        }
        
        modalContainer.getChildren().add(modalContent);
        modalContainer.setVisible(true);
        modalContainer.toFront();
        
        // Add animation
        dev.theknife.app.util.AnimationUtils.fadeIn(overlayBackground);
        dev.theknife.app.util.AnimationUtils.popIn(modalContent);
        
        // Accessibility: request focus on the modal content
        modalContent.requestFocus();
    }
    
    /**
     * Chiude il modale attualmente aperto.
     */
    public void closeModal() {
        modalContainer.setVisible(false);
        if (modalContainer.getChildren().size() > 1) {
            modalContainer.getChildren().remove(1, modalContainer.getChildren().size());
        }
    }
    
    /**
     * Verifica se un modale è attualmente aperto.
     * 
     * @return true se il modale è visibile, false altrimenti.
     */
    public boolean isModalOpen() {
        return modalContainer.isVisible();
    }
}
