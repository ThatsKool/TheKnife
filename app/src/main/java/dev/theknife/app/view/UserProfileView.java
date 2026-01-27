/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.view;

import dev.theknife.app.model.User;
import dev.theknife.app.service.IUserService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Vista per la gestione del profilo utente.
 * <p>
 * Visualizza le informazioni dell'account (nome, email, avatar) e permette
 * all'utente di aggiornare la propria posizione geografica (latitudine/longitudine).
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class UserProfileView {
    // CAMPI
    private final IUserService userService;
    private final SessionContext sessionContext;
    private static final String PRIMARY_GREEN = "#2E7D32";
    private static final String BACKGROUND_WHITE = "#FFFFFF";
    private static final String TEXT_DARK = "#212121";
    private static final String TEXT_GRAY = "#757575";
    private static final String BORDER_GRAY = "#E0E0E0";
    private static final String ERROR_RED = "#D32F2F";
 
    private final Logger logger = Logger.getLogger(UserProfileView.class);
    
    // COSTRUTTORI
    /**
     * Costruisce la vista del profilo utente.
     * 
     * @param userService Il servizio per la gestione degli utenti.
     * @param sessionContext Il contesto della sessione corrente.
     */
    public UserProfileView(IUserService userService, SessionContext sessionContext) {
        this.userService = userService;
        this.sessionContext = sessionContext;
    }
    
    // METODI
    /**
     * Crea il contenuto del modale profilo.
     * 
     * @return Il nodo root del pannello profilo.
     */
    public Node createView() {
        User currentUser = sessionContext != null ? sessionContext.getCurrentUser() : null;
        if (currentUser == null) {
            return new Label("Utente non loggato");
        }

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);"
        );
        root.setMaxWidth(500);
        root.setMinWidth(400);

        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLabel = new Label("Il tuo Profilo");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand; -fx-text-fill: " + TEXT_GRAY + ";");
        closeButton.setOnAction(e -> ModalManager.getInstance().close());
        
        header.getChildren().addAll(titleLabel, spacer, closeButton);

        // User Info Section
        HBox userInfo = new HBox(20);
        userInfo.setAlignment(Pos.CENTER_LEFT);
        userInfo.setPadding(new Insets(10, 0, 20, 0));
        
        // Avatar (Circle with Initials)
        String initial = currentUser.getName().substring(0, 1).toUpperCase();
        Circle avatarCircle = new Circle(30);
        avatarCircle.setFill(Color.web(PRIMARY_GREEN));
        Text avatarText = new Text(initial);
        avatarText.setFill(Color.WHITE);
        avatarText.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        
        javafx.scene.layout.StackPane avatar = new javafx.scene.layout.StackPane(avatarCircle, avatarText);
        
        VBox userDetails = new VBox(5);
        Label nameLabel = new Label(currentUser.getName() + " " + currentUser.getSurname());
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.web(TEXT_DARK));
        
        Label emailLabel = new Label(currentUser.getEmail());
        emailLabel.setFont(Font.font("Segoe UI", 14));
        emailLabel.setTextFill(Color.web(TEXT_GRAY));
        
        Label roleLabel = new Label(currentUser.getRole());
        roleLabel.setStyle("-fx-background-color: #E8F5E9; -fx-text-fill: " + PRIMARY_GREEN + "; -fx-padding: 2 8; -fx-background-radius: 10;");
        roleLabel.setFont(Font.font("Segoe UI", 12));
        
        userDetails.getChildren().addAll(nameLabel, emailLabel, roleLabel);
        userInfo.getChildren().addAll(avatar, userDetails);

        // Divider
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();

        // Location Edit Section
        VBox locationSection = new VBox(15);
        locationSection.setPadding(new Insets(10, 0, 0, 0));
        
        Label locTitle = new Label("Aggiorna Posizione");
        locTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        locTitle.setTextFill(Color.web(TEXT_DARK));
        
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        
        TextField latField = createStyledTextField(String.valueOf(currentUser.getLatitude()), "Latitudine");
        TextField lonField = createStyledTextField(String.valueOf(currentUser.getLongitude()), "Longitudine");
        
        grid.add(new Label("Latitudine:"), 0, 0);
        grid.add(latField, 0, 1);
        grid.add(new Label("Longitudine:"), 1, 0);
        grid.add(lonField, 1, 1);
        
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.web(ERROR_RED));
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);

        // Auto-Location Button
        Button locationBtn = new Button("📍 Usa la mia posizione attuale");
        locationBtn.setMaxWidth(Double.MAX_VALUE);
        locationBtn.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;"
        );
        // dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(locationBtn); // Optional if utils available
        locationBtn.setOnAction(e -> autoDetectLocation(latField, lonField, errorLabel));

        Button updateButton = new Button("Aggiorna Posizione");
        updateButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-padding: 10 20;" +
            "-fx-background-radius: 20;" +
            "-fx-cursor: hand;"
        );
        updateButton.setMaxWidth(Double.MAX_VALUE);
        
        // Update Action
        updateButton.setOnAction(e -> {
            try {
                double lat = Double.parseDouble(latField.getText().replace(",", "."));
                double lon = Double.parseDouble(lonField.getText().replace(",", "."));
                
                dev.theknife.app.util.GeoValidator.validateCoordinates(lat, lon);
                
                User updatedUser = new User(
                    currentUser.getName(),
                    currentUser.getSurname(),
                    currentUser.getEmail(),
                    currentUser.getPassword(),
                    currentUser.getDateOfBirth(),
                    lat,
                    lon,
                    currentUser.getRole()
                );
                
                // Save
                userService.updateUser(updatedUser);
                if (sessionContext != null) sessionContext.setCurrentUser(updatedUser);
                
                // Success
                ModalManager.getInstance().close(); // Close profile modal
                ModalManager.getInstance().showInfo("Successo", "Posizione aggiornata correttamente!");
                
            } catch (NumberFormatException ex) {
                errorLabel.setText("Inserisci valori numerici validi.");
                errorLabel.setVisible(true);
            } catch (IllegalArgumentException ex) {
                errorLabel.setText(ex.getMessage());
                errorLabel.setVisible(true);
            } catch (IOException ex) {
                logger.error("Failed to update user location", ex);
                errorLabel.setText("Errore di sistema: impossibile salvare le modifiche.");
                errorLabel.setVisible(true);
            }
        });

        locationSection.getChildren().addAll(locTitle, grid, locationBtn, errorLabel, updateButton);
        
        root.getChildren().addAll(header, userInfo, separator, locationSection);
        
        return root;
    }
    
    /**
     * Crea un campo di testo stilizzato con valori iniziali e prompt.
     *
     * @param initialValue Il valore iniziale del campo.
     * @param prompt Il testo di prompt da mostrare quando il campo è vuoto.
     * @return Un TextField configurato con lo stile appropriato.
     */
    private TextField createStyledTextField(String initialValue, String prompt) {
        TextField field = new TextField(initialValue);
        field.setPromptText(prompt);
        field.setPrefHeight(38);
        field.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 8px 12px;" +
            "-fx-font-size: 14px;"
        );
        return field;
    }

    /**
     * Rileva automaticamente la posizione (lat/lon) basandosi sull'IP pubblico.
     * Utilizza il servizio gratuito ip-api.com.
     */
    private void autoDetectLocation(TextField latitudeField, TextField longitudeField, Label statusLabel) {
        statusLabel.setText("Rilevamento posizione in corso...");
        statusLabel.setStyle("-fx-text-fill: #1976D2;"); // Info Blue
        statusLabel.setVisible(true);
        
        // Esegui in background per non bloccare la UI
        new Thread(() -> {
            HttpURLConnection con = null;
            try {
                // Servizio di geolocalizzazione IP gratuito (no API key richiesta per uso limitato)
                URL url = new URL("http://ip-api.com/json/?fields=lat,lon,status");
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                con.setReadTimeout(5000);
                
                if (con.getResponseCode() != 200) {
                    throw new IOException("HTTP Error: " + con.getResponseCode());
                }
                
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }
                
                String json = response.toString();
                
                // Parsing manuale JSON per evitare dipendenze esterne
                double lat = extractJsonValue(json, "lat");
                double lon = extractJsonValue(json, "lon");
                
                // Aggiorna UI nel thread JavaFX
                javafx.application.Platform.runLater(() -> {
                    latitudeField.setText(String.format(java.util.Locale.US, "%.4f", lat));
                    longitudeField.setText(String.format(java.util.Locale.US, "%.4f", lon));
                    statusLabel.setText("Posizione rilevata con successo!");
                    statusLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";"); // Success Green
                });
                
            } catch (Exception e) {
                logger.error("Auto-detect location failed", e);
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Connessione assente! Inserisci le coordinate manualmente.");
                    statusLabel.setStyle("-fx-text-fill: " + ERROR_RED + ";"); // Error Red
                });
            } finally {
                if (con != null) con.disconnect();
            }
        }).start();
    }
    
    /**
     * Estrae un valore numerico da una stringa JSON semplice.
     * <p>
     * Esegue un parsing manuale del JSON per evitare dipendenze esterne.
     * </p>
     *
     * @param json La stringa JSON da analizzare.
     * @param key La chiave del valore da estrarre.
     * @return Il valore numerico estratto, o 0.0 se non trovato o non valido.
     */
    private double extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int startIdx = json.indexOf(searchKey);
            if (startIdx == -1) return 0.0;
            
            startIdx += searchKey.length();
            int endIdx = json.indexOf(",", startIdx);
            if (endIdx == -1) endIdx = json.indexOf("}", startIdx);
            
            if (endIdx > startIdx) {
                String valueStr = json.substring(startIdx, endIdx).trim();
                return Double.parseDouble(valueStr);
            }
        } catch (Exception e) {
            // Ignore parse errors
        }
        return 0.0;
    }
}
