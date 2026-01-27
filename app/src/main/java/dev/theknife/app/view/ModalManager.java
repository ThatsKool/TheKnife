/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.view;

import dev.theknife.app.view.components.ModalStackPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.effect.DropShadow;

import java.util.function.Consumer;

/**
 * Manager singleton per la gestione di modali a livello applicativo.
 * <p>
 * Sostituisce gli Alert e Dialog nativi di JavaFX con componenti UI personalizzati
 * iniettati nel ModalStackPane corrente. Fornisce un sistema unificato per mostrare
 * dialoghi di conferma, input, errore e caricamento con uno stile coerente ispirato a The Fork.
 * </p>
 * <p>
 * <b>Funzionalità:</b>
 * <ul>
 *   <li>Dialoghi di conferma con callback</li>
 *   <li>Input dialog con validazione</li>
 *   <li>Messaggi di errore e avviso</li>
 *   <li>Indicatori di caricamento</li>
 *   <li>Gestione automatica dello sfondo semi-trasparente</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see ModalStackPane
 */
public class ModalManager {
    // CAMPI
    private static ModalManager instance;
    private ModalStackPane currentTarget;
    
    // The Fork inspired styling constants
    private static final String PRIMARY_GREEN = "#2E7D32";
    private static final String TEXT_DARK = "#212121";
    private static final String TEXT_GRAY = "#757575";
    private static final String ERROR_RED = "#D32F2F";
    private static final String WARNING_ORANGE = "#F57C00";
    
    // COSTRUTTORI
    /**
     * Costruttore privato per pattern Singleton.
     */
    private ModalManager() {}
    
    // METODI
    /**
     * Restituisce l'istanza singleton del ModalManager.
     *
     * @return L'istanza unica del ModalManager.
     */
    public static synchronized ModalManager getInstance() {
        if (instance == null) {
            instance = new ModalManager();
        }
        return instance;
    }
    
    /**
     * Imposta il pannello target corrente dove verranno visualizzati i modali.
     * <p>
     * Questo metodo deve essere chiamato ogni volta che la Scene attiva cambia.
     * </p>
     *
     * @param target Il ModalStackPane dove mostrare i modali.
     */
    public void setTarget(ModalStackPane target) {
        this.currentTarget = target;
    }
    
    /**
     * Mostra un nodo personalizzato come modale.
     *
     * @param content Il contenuto del modale da visualizzare.
     */
    public void showCustom(Node content) {
        if (currentTarget != null) {
            currentTarget.showModal(content);
        } else {
            System.err.println("ERROR: No ModalStackPane target set for ModalManager!");
        }
    }
    
    /**
     * Chiude il modale corrente.
     */
    public void close() {
        if (currentTarget != null) {
            currentTarget.closeModal();
        }
    }
    
    // --- Standard Modals ---
    
    public void showInfo(String title, String message) {
        showStandardDialog("INFO", title, message, null);
    }
    
    public void showInfo(String title, String message, Runnable onConfirm) {
        showStandardDialog("INFO", title, message, onConfirm);
    }
    
    public void showError(String title, String message) {
        showStandardDialog("ERROR", title, message, null);
    }
    
    public void showWarning(String title, String message) {
        showStandardDialog("WARNING", title, message, null);
    }
    
    public void showConfirmation(String title, String message, Runnable onConfirm) {
        showConfirmationDialog(title, message, onConfirm, null);
    }
    
    public void showInput(String title, String message, String promptText, Consumer<String> onConfirm) {
        showInputDialog(title, message, promptText, onConfirm);
    }

    public void showTextAreaDialog(String title, String message, String promptText, Consumer<String> onConfirm) {
        VBox dialog = createBaseDialogLayout();
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));
        
        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(Color.web(TEXT_DARK));
        msgLabel.setWrapText(true);
        
        TextArea textArea = new TextArea();
        textArea.setPromptText(promptText);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(5);
        textArea.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px;");
        
        Button cancelButton = createStyledButton("Annulla", false);
        cancelButton.setOnAction(e -> close());
        
        Button confirmButton = createStyledButton("Invia", true);
        confirmButton.setOnAction(e -> {
            String text = textArea.getText();
            if (text != null && !text.trim().isEmpty()) {
                close();
                if (onConfirm != null) onConfirm.accept(text);
            }
        });
        
        HBox buttonBox = new HBox(10, cancelButton, confirmButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        dialog.getChildren().addAll(titleLabel, msgLabel, textArea, buttonBox);
        showCustom(dialog);
    }

    public void showLoading(String message) {
        VBox dialog = createBaseDialogLayout();
        dialog.setAlignment(Pos.CENTER);
        
        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(Color.web(TEXT_DARK));
        
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);
        
        dialog.getChildren().addAll(progress, msgLabel);
        showCustom(dialog);
    }
    
    // --- Internal Component Factories ---
    
    /**
     * Mostra un dialogo standard (info, errore, warning).
     * 
     * @param type Il tipo di dialogo (INFO, ERROR, WARNING).
     * @param title Il titolo del dialogo.
     * @param message Il messaggio da mostrare.
     * @param onConfirm Callback da eseguire alla chiusura (opzionale).
     */
    private void showStandardDialog(String type, String title, String message, Runnable onConfirm) {
        VBox dialog = createBaseDialogLayout();
        
        // Header Color
        String headerColor = PRIMARY_GREEN;
        if ("ERROR".equals(type)) headerColor = ERROR_RED;
        else if ("WARNING".equals(type)) headerColor = WARNING_ORANGE;
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(headerColor));
        
        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(Color.web(TEXT_DARK));
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(350);
        
        Button okButton = createStyledButton("OK", true);
        okButton.setOnAction(e -> {
            close();
            if (onConfirm != null) onConfirm.run();
        });
        
        HBox buttonBox = new HBox(okButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        dialog.getChildren().addAll(titleLabel, msgLabel, buttonBox);
        showCustom(dialog);
    }
    
    /**
     * Mostra un dialogo di conferma con opzioni OK/Annulla.
     * 
     * @param title Il titolo del dialogo.
     * @param message La domanda o messaggio di conferma.
     * @param onConfirm Callback per la conferma.
     * @param onCancel Callback per l'annullamento (opzionale).
     */
    private void showConfirmationDialog(String title, String message, Runnable onConfirm, Runnable onCancel) {
        VBox dialog = createBaseDialogLayout();
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));
        
        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(Color.web(TEXT_DARK));
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(350);
        
        Button cancelButton = createStyledButton("Annulla", false);
        cancelButton.setOnAction(e -> {
            close();
            if (onCancel != null) onCancel.run();
        });
        
        Button confirmButton = createStyledButton("Conferma", true);
        confirmButton.setOnAction(e -> {
            close();
            if (onConfirm != null) onConfirm.run();
        });
        
        HBox buttonBox = new HBox(10, cancelButton, confirmButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        dialog.getChildren().addAll(titleLabel, msgLabel, buttonBox);
        showCustom(dialog);
    }
    
    private void showInputDialog(String title, String message, String promptText, Consumer<String> onConfirm) {
        VBox dialog = createBaseDialogLayout();
        
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));
        
        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Segoe UI", 14));
        msgLabel.setTextFill(Color.web(TEXT_DARK));
        msgLabel.setWrapText(true);
        
        TextField inputField = new TextField();
        inputField.setPromptText(promptText);
        inputField.setStyle("-fx-padding: 10; -fx-background-radius: 4; -fx-border-color: #E0E0E0; -fx-border-radius: 4;");
        
        // TextArea for longer input if needed? For now TextField is fine for simple input.
        // If we want multi-line, we can check a flag or just use TextArea by default for "Response".
        // But the prompt says "Review Response", which is likely multi-line.
        // Let's stick to generic Input for now. I'll make a specific ReviewResponse dialog later.
        
        Button cancelButton = createStyledButton("Annulla", false);
        cancelButton.setOnAction(e -> close());
        
        Button confirmButton = createStyledButton("Invia", true);
        confirmButton.setOnAction(e -> {
            String text = inputField.getText();
            if (text != null && !text.trim().isEmpty()) {
                close();
                if (onConfirm != null) onConfirm.accept(text);
            }
        });
        
        HBox buttonBox = new HBox(10, cancelButton, confirmButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        dialog.getChildren().addAll(titleLabel, msgLabel, inputField, buttonBox);
        showCustom(dialog);
    }
    
    /**
     * Crea il layout base per i dialoghi modali.
     * <p>
     * Configura un VBox con sfondo bianco, bordi arrotondati, padding e ombra.
     * </p>
     * 
     * @return Un VBox configurato pronto per ospitare il contenuto del dialogo.
     */
    private VBox createBaseDialogLayout() {
        VBox layout = new VBox(20);
        layout.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-padding: 24;");
        layout.setMaxWidth(400);
        layout.setMinWidth(300);
        
        // Shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        shadow.setRadius(10);
        shadow.setOffsetY(2);
        layout.setEffect(shadow);
        
        return layout;
    }
    
    /**
     * Crea un pulsante stilizzato.
     * 
     * @param text Il testo del pulsante.
     * @param isPrimary Se true, applica lo stile primario (verde), altrimenti secondario.
     * @return Il pulsante configurato.
     */
    private Button createStyledButton(String text, boolean isPrimary) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btn.setPadding(new Insets(8, 16, 8, 16));
        
        if (isPrimary) {
            btn.setStyle(
                "-fx-background-color: " + PRIMARY_GREEN + ";" +
                "-fx-text-fill: white;" +
                "-fx-background-radius: 4;" +
                "-fx-cursor: hand;"
            );
        } else {
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: " + TEXT_GRAY + ";" +
                "-fx-background-radius: 4;" +
                "-fx-cursor: hand;" +
                "-fx-border-color: " + TEXT_GRAY + ";" +
                "-fx-border-radius: 4;"
            );
        }
        return btn;
    }
}
