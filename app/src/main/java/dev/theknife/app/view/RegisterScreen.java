/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.view;

import dev.theknife.app.App;
import dev.theknife.app.config.AppConfig;
import dev.theknife.app.model.User;
import dev.theknife.app.service.IUserService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.AnimationUtils;
import dev.theknife.app.util.Logger;
import dev.theknife.app.util.PasswordHasher;
import javafx.animation.PauseTransition;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import javafx.util.Duration;

/**
 * Controller JavaFX per la schermata di registrazione utenti.
 * <p>
 * Gestisce la validazione dei campi obbligatori, i criteri della password con
 * feedback visivo, la creazione dell'utente e l'accesso automatico al termine.
 * In modalità DEBUG consente la compilazione automatica di dati plausibili.
 * </p>
 * <p>
 * Integrazioni:
 * <ul>
 *   <li>Persistenza e controllo duplicati tramite {@link IUserService}.</li>
 *   <li>Gestione sessione tramite {@link SessionManager}.</li>
 *   <li>Stile coerente con LoginScreen (palette The Fork).</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class RegisterScreen {
    // CAMPI
    // Paletta di colori ispirata a The Fork
    private static final String PRIMARY_GREEN = "#2E7D32"; // Matched with LoginScreen
    private static final String LIGHT_GREEN = "#4CAF50";
    private static final String BACKGROUND_WHITE = "#FFFFFF";
    private static final String TEXT_DARK = "#212121";
    private static final String TEXT_GRAY = "#757575";
    private static final String BORDER_GRAY = "#E0E0E0";
    private static final String BACKGROUND_LIGHT = "#F5F5F5";
    
    /**
     * Genera lo stile CSS per lo sfondo con pattern leggero.
     *
     * @return Stringa CSS applicabile ai container principali.
     */
    private String getPatternBackgroundStyle() {
        // Usa un colore di sfondo verde visibile ispirato a The Fork
        return "-fx-background-color: #C8E6C9;";
    }
    
    private Scene scene;
    private TextField nameField;
    private TextField surnameField;
    private TextField emailField;
    private PasswordField passwordField;
    private TextField passwordVisibleField;
    private Button togglePasswordButton;
    private boolean isPasswordVisible = false;
    private Label passwordCriteriaLabel;
    private DatePicker dateOfBirthPicker;
    private TextField latitudeField;
    private TextField longitudeField;
    private ComboBox<String> roleComboBox;
    private Label statusLabel;
    private Stage primaryStage;
    private Supplier<Scene> homeSceneSupplier;
    private Runnable onHomeSceneRefresh;
    private final IUserService userService;
    private final SessionContext sessionContext;
    private static final Logger logger = Logger.getLogger(RegisterScreen.class);

    // COSTRUTTORI
    /**
     * Costruttore semplificato per la schermata di registrazione.
     * 
     * @param primaryStage Lo stage principale.
     * @param homeScene La scena home a cui tornare.
     */
    public RegisterScreen(Stage primaryStage, Scene homeScene) {
        this(primaryStage, () -> homeScene, null, null, null);
    }
    
    /**
     * Costruttore con callback di refresh.
     * 
     * @param primaryStage Lo stage principale.
     * @param homeScene La scena home a cui tornare.
     * @param onHomeSceneRefresh Callback da eseguire al ritorno alla home.
     */
    public RegisterScreen(Stage primaryStage, Scene homeScene, Runnable onHomeSceneRefresh) {
        this(primaryStage, () -> homeScene, onHomeSceneRefresh, null, null);
    }
    
    /**
     * Costruttore completo con iniezione delle dipendenze.
     * 
     * @param primaryStage Lo stage principale.
     * @param homeSceneSupplier Supplier per ottenere la scena home.
     * @param onHomeSceneRefresh Callback da eseguire al refresh.
     * @param userService Servizio gestione utenti.
     * @param sessionContext Contesto sessione.
     */
    public RegisterScreen(Stage primaryStage, Supplier<Scene> homeSceneSupplier, Runnable onHomeSceneRefresh,
                          IUserService userService, SessionContext sessionContext) {
        this.primaryStage = primaryStage;
        this.homeSceneSupplier = homeSceneSupplier;
        this.onHomeSceneRefresh = onHomeSceneRefresh;
        this.userService = userService;
        this.sessionContext = sessionContext;
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40, 40, 40, 40));
        root.setStyle(getPatternBackgroundStyle());

        // Titolo
        Label titleLabel = new Label("Registrati");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");

        // Griglia del form - Layout a due colonne
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(20);
        grid.setVgap(18);
        grid.setPadding(new Insets(25, 40, 20, 40));

        // Constraints delle colonne responsive
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        // Permette la dimensione naturale basata sul contenuto
        grid.getColumnConstraints().add(col1);

        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        // Permette la dimensione naturale basata sul contenuto
        grid.getColumnConstraints().add(col2);

        // Colonna 0 - Lato sinistro
        // Nome
        Label nameLabel = new Label("Nome *");
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        nameLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        nameField = new TextField();
        nameField.setPrefWidth(280);
        nameField.setPrefHeight(42);
        nameField.setMaxWidth(280);
        nameField.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 15px;" +
            "-fx-font-size: 14px;" +
            "-fx-prompt-text-fill: " + TEXT_GRAY + ";"
        );
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 0, 1);

        // Email (obbligatorio) - lato sinistro
        Label emailLabel = new Label("Email *");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        emailLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        emailField = new TextField();
        emailField.setPrefWidth(280);
        emailField.setPrefHeight(42);
        emailField.setMaxWidth(280);
        emailField.setPromptText("tua.email@esempio.com");
        emailField.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 15px;" +
            "-fx-font-size: 14px;" +
            "-fx-prompt-text-fill: " + TEXT_GRAY + ";"
        );
        grid.add(emailLabel, 0, 2);
        grid.add(emailField, 0, 3);
        GridPane.setValignment(emailField, VPos.TOP);

        // Data di nascita (opzionale)
        Label dobLabel = new Label("Data di nascita (Opzionale)");
        dobLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        dobLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setPrefWidth(280);
        dateOfBirthPicker.setPrefHeight(42);
        dateOfBirthPicker.setMaxWidth(Double.MAX_VALUE);
        dateOfBirthPicker.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 15px;" +
            "-fx-font-size: 14px;"
        );
        grid.add(dobLabel, 0, 4);
        grid.add(dateOfBirthPicker, 0, 5);

        // Latitude
        Label latLabel = new Label("Latitudine *");
        latLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        latLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        latitudeField = new TextField();
        latitudeField.setPrefWidth(280);
        latitudeField.setPrefHeight(42);
        latitudeField.setMaxWidth(280);
        latitudeField.setPromptText("es. 40.7128");
        latitudeField.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 15px;" +
            "-fx-font-size: 14px;" +
            "-fx-prompt-text-fill: " + TEXT_GRAY + ";"
        );
        grid.add(latLabel, 0, 6);
        grid.add(latitudeField, 0, 7);



        // Colonna 1 - Lato destro
        // Cognome
        Label surnameLabel = new Label("Cognome *");
        surnameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        surnameLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        surnameField = new TextField();
        surnameField.setPrefWidth(280);
        surnameField.setPrefHeight(42);
        surnameField.setMaxWidth(280);
        surnameField.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 15px;" +
            "-fx-font-size: 14px;" +
            "-fx-prompt-text-fill: " + TEXT_GRAY + ";"
        );
        grid.add(surnameLabel, 1, 0);
        grid.add(surnameField, 1, 1);

        // Campo password
        Label passwordLabel = new Label("Password *");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        passwordLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        
        // Contenitore del campo password con pulsante di toggle
        HBox passwordContainer = new HBox(5);
        passwordContainer.setAlignment(Pos.CENTER_LEFT);
        
        passwordField = new PasswordField();
        passwordField.setPrefWidth(240);
        passwordField.setPrefHeight(42);
        passwordField.setMaxWidth(240);
        passwordField.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 15px;" +
            "-fx-font-size: 14px;" +
            "-fx-prompt-text-fill: " + TEXT_GRAY + ";"
        );
        
        // Campo password visibile (nascosto di default)
        passwordVisibleField = new TextField();
        passwordVisibleField.setPrefWidth(240);
        passwordVisibleField.setPrefHeight(42);
        passwordVisibleField.setMaxWidth(240);
        passwordVisibleField.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 15px;" +
            "-fx-font-size: 14px;" +
            "-fx-prompt-text-fill: " + TEXT_GRAY + ";"
        );
        passwordVisibleField.setVisible(false);
        passwordVisibleField.setManaged(false);
        
        // Pulsante per cambiare la visibilità della password con l'icona dell'occhio
        togglePasswordButton = new Button();
        togglePasswordButton.setPrefWidth(46);
        togglePasswordButton.setPrefHeight(42);
        
        SVGPath eyeIcon = new SVGPath();
        eyeIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
        eyeIcon.setFill(Color.WHITE);
        eyeIcon.setScaleX(1.2);
        eyeIcon.setScaleY(1.2);
        
        togglePasswordButton.setGraphic(eyeIcon);
        
        togglePasswordButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;" +
            "-fx-border-radius: 8px;"
        );
        AnimationUtils.applyButtonHoverAnimation(togglePasswordButton);
        
        // Cambia la visibilità della password
        togglePasswordButton.setOnAction(e -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                passwordVisibleField.setText(passwordField.getText());
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                passwordVisibleField.setVisible(true);
                passwordVisibleField.setManaged(true);
            } else {
                passwordField.setText(passwordVisibleField.getText());
                passwordVisibleField.setVisible(false);
                passwordVisibleField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
            }
        });
        
        // Sincronizza il testo tra i campi password
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isPasswordVisible) {
                passwordVisibleField.setText(newVal);
            }
        });
        
        passwordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isPasswordVisible) {
                passwordField.setText(newVal);
            }
        });
        
        passwordContainer.getChildren().addAll(passwordField, passwordVisibleField, togglePasswordButton);
        
        // Etichetta dei criteri della password - formato più visibile, posizionato fuori dalla griglia per essere sempre visibile
        passwordCriteriaLabel = new Label("Criteri password:\n• Almeno 8 caratteri\n• 1 lettera maiuscola\n• 1 numero\n• 1 carattere speciale (! @ /)");
        passwordCriteriaLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            passwordCriteriaLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + "; -fx-padding: 5 0 0 0; -fx-line-spacing: 3px;");
        passwordCriteriaLabel.setMinHeight(120); // Aumenta l'altezza per evitare il troncamento del testo
        passwordCriteriaLabel.setWrapText(false);
        
        VBox passwordBox = new VBox(5);
        passwordBox.getChildren().addAll(passwordContainer, passwordCriteriaLabel);
        
        grid.add(passwordLabel, 1, 2);
        grid.add(passwordBox, 1, 3);
        GridPane.setValignment(passwordBox, VPos.TOP);
        
        // Assicura che i criteri della password siano sempre visibili aggiungendo padding extra
        GridPane.setMargin(passwordBox, new Insets(0, 0, 10, 0));
        
        // Aggiorna i criteri della password in tempo reale
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordCriteria(newVal));
        passwordVisibleField.textProperty().addListener((obs, oldVal, newVal) -> updatePasswordCriteria(newVal));

        // Spazio vuoto per l'allineamento
        Label emptyLabel = new Label("");
        grid.add(emptyLabel, 1, 4);

        // Longitudine
        Label lonLabel = new Label("Longitudine *");
        lonLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        lonLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        longitudeField = new TextField();
        longitudeField.setPrefWidth(280);
        longitudeField.setPrefHeight(42);
        longitudeField.setPromptText("es. -74.0060");
        longitudeField.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 15px;" +
            "-fx-font-size: 14px;" +
            "-fx-prompt-text-fill: " + TEXT_GRAY + ";"
        );
        grid.add(lonLabel, 1, 6);
        grid.add(longitudeField, 1, 7);

        // Pulsante per usare la posizione attuale
        Button locationBtn = new Button("📍 Usa la mia posizione attuale");
        locationBtn.setPrefWidth(280);
        locationBtn.setPrefHeight(36);
        locationBtn.setMaxWidth(320);
        locationBtn.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 8px;" +
            "-fx-cursor: hand;"
        );
        AnimationUtils.applyButtonHoverAnimation(locationBtn);
        locationBtn.setOnAction(e -> autoDetectLocation());
        
        // Aggiunge un pulsante che occupa 2 colonne
        grid.add(locationBtn, 0, 8, 2, 1);
        GridPane.setHalignment(locationBtn, javafx.geometry.HPos.CENTER);

        // Ruolo
        Label roleLabel = new Label("Ruolo *");
        roleLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        roleLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Cliente", "Ristoratore");
        roleComboBox.setValue("Cliente");
        roleComboBox.setPrefWidth(280);
        roleComboBox.setPrefHeight(42);
        roleComboBox.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 8px;" +
            "-fx-background-radius: 8px;" +
            "-fx-padding: 10px 15px;" +
            "-fx-font-size: 14px;"
        );
        grid.add(roleLabel, 0, 9);
        grid.add(roleComboBox, 0, 10);

        // Etichetta di stato
        statusLabel = new Label("");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        statusLabel.setStyle("-fx-text-fill: #D32F2F;");
        statusLabel.setPadding(new Insets(5, 0, 0, 0));

        // Pulsanti - organizzati in righe invece che in colonne
        VBox buttonBox = new VBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        // Prima riga: Pulsante di registrazione
        HBox firstRow = new HBox();
        firstRow.setAlignment(Pos.CENTER);
        firstRow.setSpacing(10);
        
        Button registerBtn = new Button("Registrati");
        registerBtn.setPrefSize(320, 48);
        registerBtn.setMaxWidth(320);
        registerBtn.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 24px;" +
            "-fx-cursor: hand;"
        );
        AnimationUtils.applyButtonHoverAnimation(registerBtn);
        registerBtn.setOnAction(e -> handleRegistration());
        
        firstRow.getChildren().add(registerBtn);

        // Seconda riga: Pulsante di ritorno
        HBox secondRow = new HBox();
        secondRow.setAlignment(Pos.CENTER);
        secondRow.setSpacing(10);
        
        Button backBtn = new Button("Torna alla Home");
        backBtn.setPrefSize(320, 44);
        backBtn.setMaxWidth(320);
        backBtn.setStyle(
            "-fx-background-color: " + TEXT_GRAY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 22px;" +
            "-fx-cursor: hand;"
        );
        AnimationUtils.applyButtonHoverAnimation(backBtn);
        backBtn.setOnAction(e -> {
            if (onHomeSceneRefresh != null) {
                onHomeSceneRefresh.run();
            }
            primaryStage.setScene(homeSceneSupplier.get());
        });
        
        secondRow.getChildren().add(backBtn);
        
        // Terza riga: Pulsante di auto-compilazione (solo in modalità DEBUG)
        HBox thirdRow = new HBox();
        thirdRow.setAlignment(Pos.CENTER);
        thirdRow.setSpacing(10);
        
        if (AppConfig.DEBUG) {
            Button autoFillBtn = new Button("Compilazione Automatica");
            autoFillBtn.setPrefSize(320, 36);
            autoFillBtn.setMaxWidth(320);
            autoFillBtn.setStyle(
                "-fx-background-color: " + BACKGROUND_LIGHT + ";" +
                "-fx-text-fill: " + TEXT_DARK + ";" +
                "-fx-font-weight: 500;" +
                "-fx-font-size: 12px;" +
                "-fx-background-radius: 18px;" +
                "-fx-cursor: hand;"
            );
            autoFillBtn.setOnAction(e -> autoFillRandom());
            thirdRow.getChildren().add(autoFillBtn);
        }
        
        buttonBox.getChildren().add(firstRow);
        buttonBox.getChildren().add(secondRow);
        if (AppConfig.DEBUG) {
            buttonBox.getChildren().add(thirdRow);
        }

        root.getChildren().addAll(titleLabel, grid, statusLabel, buttonBox);
        
        // Wrap in ScrollPane per permettere lo scorrimento quando la finestra non è massimizzata
        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setPadding(new Insets(0));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        // Rendere la barra di scorrimento più visibile con i colori verdi di The Fork usando stili inline
        String scrollBarStyle = 
            ".scroll-bar:vertical { " +
            "    -fx-background-color: transparent; " +
            "    -fx-background-insets: 0; " +
            "} " +
            ".scroll-bar:vertical .track { " +
            "    -fx-background-color: rgba(200, 230, 201, 0.4); " +
            "    -fx-background-radius: 5px; " +
            "} " +
            ".scroll-bar:vertical .thumb { " +
            "    -fx-background-color: " + PRIMARY_GREEN + "; " +
            "    -fx-background-radius: 5px; " +
            "    -fx-background-insets: 2; " +
            "    -fx-min-width: 12px; " +
            "} " +
            ".scroll-bar:vertical .thumb:hover { " +
            "    -fx-background-color: " + LIGHT_GREEN + "; " +
            "} " +
            ".scroll-bar:vertical .increment-button, .scroll-bar:vertical .decrement-button { " +
            "    -fx-background-color: transparent; " +
            "    -fx-padding: 5px; " +
            "} " +
            ".scroll-bar:vertical .increment-arrow, .scroll-bar:vertical .decrement-arrow { " +
            "    -fx-background-color: " + PRIMARY_GREEN + "; " +
            "    -fx-shape: \"M 0 0 L 4 8 L 8 0 Z\"; " +
            "}";
        
        // Applica gli stili usando un foglio di stili temporaneo
        try {
            java.io.File tempFile = java.io.File.createTempFile("scrollbar-style", ".css");
            tempFile.deleteOnExit();
            java.nio.file.Files.write(tempFile.toPath(), scrollBarStyle.getBytes());
            scrollPane.getStylesheets().add(tempFile.toURI().toString());
        } catch (Exception e) {
            // Fallback: usa uno stile inline per la barra di scorrimento
            scrollPane.setStyle(scrollPane.getStyle() + 
                " -fx-scroll-bar-width: 12px; " +
                " -fx-scroll-bar-background-color: rgba(200, 230, 201, 0.4);");
        }
        
        // Aggiunge l'animazione di entrata
        AnimationUtils.slideInFromBottom(scrollPane, 500);
        
        scene = App.createSceneWithModal(scrollPane, 700, 650);
    }

    // METODI
    /**
     * Gestisce il processo di registrazione dell'utente.
     * <p>
     * Valida tutti i campi del form, verifica l'unicità dell'email, crea
     * l'utente e imposta la sessione corrente. Al successo, mostra un messaggio e
     * torna automaticamente alla Home dopo un breve ritardo.
     * </p>
     */
    private void handleRegistration() {
        // Valida gli input
        if (nameField.getText().trim().isEmpty() || 
            surnameField.getText().trim().isEmpty() || 
            emailField.getText().trim().isEmpty() ||
            passwordField.getText().trim().isEmpty() ||
            latitudeField.getText().trim().isEmpty() ||
            longitudeField.getText().trim().isEmpty()) {
            statusLabel.setText("Inserisci tutti i campi obbligatori!");
            AnimationUtils.shake(statusLabel);
            return;
        }
        
        // Valida il formato dell'email
        String email = emailField.getText().trim();
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("Inserisci un indirizzo email valido!");
            AnimationUtils.shake(emailField);
            return;
        }
        
        // Valida il servizio utente
        if (userService == null) {
            statusLabel.setText("Servizio non disponibile. Riprova.");
            return;
        }
        // Valida l'email
        try {
            if (userService.emailExists(email)) {
                statusLabel.setText("Email già registrata! Usa un'altra email o accedi.");
                return;
            }
        } catch (IOException e) {
            statusLabel.setText("Errore nel controllo della disponibilità dell'email. Riprova.");
            logger.error("Email check failed", e);
            return;
        }

        // Valida la password
        String password = isPasswordVisible ? passwordVisibleField.getText() : passwordField.getText();
        if (!isPasswordValid(password)) {
            statusLabel.setText("Password non valida! Deve contenere almeno 8 caratteri, 1 lettera maiuscola, 1 numero e 1 carattere speciale (! @ /).");
            return;
        }
        
        // Valida le coordinate
        try {
            double lat = Double.parseDouble(latitudeField.getText().trim());
            double lon = Double.parseDouble(longitudeField.getText().trim());
            
            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                statusLabel.setText("Coordinate non valide! Latitudine: da -90 a 90, Longitudine: da -180 a 180");
                statusLabel.setStyle("-fx-text-fill: #D32F2F;");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Formato coordinate non valido!");
            statusLabel.setStyle("-fx-text-fill: #D32F2F;");
            return;
        }

        // Hash della password
        String hashedPassword = PasswordHasher.hashPassword(password);

        // Mappa il ruolo visualizzato al ruolo interno
        String displayRole = roleComboBox.getValue();
        String internalRole = "Client";
        if ("Ristoratore".equals(displayRole)) {
            internalRole = "Restaurateur";
        }

        // Crea l'oggetto utente
        User user = new User(
            nameField.getText().trim(),
            surnameField.getText().trim(),
            email,
            hashedPassword,
            dateOfBirthPicker.getValue(),
            Double.parseDouble(latitudeField.getText().trim()),
            Double.parseDouble(longitudeField.getText().trim()),
            internalRole
        );

        try {
            // Salva l'utente
            userService.saveUser(user);
            if (sessionContext != null) sessionContext.setCurrentUser(user);
            statusLabel.setText("Registrazione riuscita! Accesso effettuato.");
            statusLabel.setStyle("-fx-text-fill: #2E7D32;");
            clearFields();
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(ev -> {
                if (onHomeSceneRefresh != null) onHomeSceneRefresh.run();
                primaryStage.setScene(homeSceneSupplier.get());
            });
            pause.play();
        } catch (IOException e) {
            statusLabel.setText("Registrazione fallita! Riprova.");
            statusLabel.setStyle("-fx-text-fill: #D32F2F;");
            logger.error("Registration failed", e);
        }
    }


    /**
     * Valida la password secondo i criteri definiti.
     * <p>
     * - Almeno 8 caratteri
     * - Almeno 1 lettera maiuscola
     * - Almeno 1 numero
     * - Almeno 1 carattere speciale (! @ /)
     * </p>
     */
    /**
     * Valida la password secondo i criteri definiti.
     * <p>
     * - Almeno 8 caratteri
     * - Almeno 1 lettera maiuscola
     * - Almeno 1 numero
     * - Almeno 1 carattere speciale (! @ /)
     * </p>
     * @param password La password da validare.
     * @return {@code true} se rispetta tutti i criteri, altrimenti {@code false}.
     */
    private boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        // Check for at least one uppercase letter
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        // Check for at least one number
        boolean hasNumber = password.chars().anyMatch(Character::isDigit);
        // Check for at least one special character (! @ /)
        boolean hasSpecialChar = password.chars().anyMatch(ch -> ch == '!' || ch == '@' || ch == '/');
        
        return hasUppercase && hasNumber && hasSpecialChar;
    }
    
    /**
     * Aggiorna l'etichetta dei criteri password con feedback visivo.
     */
    /**
     * Aggiorna l'etichetta dei criteri password con feedback visivo.
     *
     * @param password La password corrente nel form.
     */
    private void updatePasswordCriteria(String password) {
        if (password == null || password.isEmpty()) {
            passwordCriteriaLabel.setText("Criteri password:\n• Almeno 8 caratteri\n• 1 lettera maiuscola\n• 1 numero\n• 1 carattere speciale (! @ /)");
            passwordCriteriaLabel.setTextFill(Color.web(PRIMARY_GREEN)); // Changed to green dark
            return;
        }
        
        boolean hasMinLength = password.length() >= 8;
        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasNumber = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialChar = password.chars().anyMatch(ch -> ch == '!' || ch == '@' || ch == '/');
        
        StringBuilder criteria = new StringBuilder("Criteri password:\n");
        if (hasMinLength) {
            criteria.append("✓ Almeno 8 caratteri\n");
        } else {
            criteria.append("✗ Almeno 8 caratteri\n");
        }
        if (hasUppercase) {
            criteria.append("✓ 1 lettera maiuscola\n");
        } else {
            criteria.append("✗ 1 lettera maiuscola\n");
        }
        if (hasNumber) {
            criteria.append("✓ 1 numero\n");
        } else {
            criteria.append("✗ 1 numero\n");
        }
        if (hasSpecialChar) {
            criteria.append("✓ 1 carattere speciale (! @ /)");
        } else {
            criteria.append("✗ 1 carattere speciale (! @ /)");
        }
        
        passwordCriteriaLabel.setText(criteria.toString());
        
        // Always use green dark color for password criteria
        passwordCriteriaLabel.setTextFill(Color.web(PRIMARY_GREEN));
    }
    
    /**
     * Compila automaticamente il form con dati casuali validi (solo DEBUG).
     */
    private void autoFillRandom() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String name = "Nome" + random.nextInt(1000, 10000);
        String surname = "Cognome" + random.nextInt(1000, 10000);
        String email = "utente" + random.nextInt(100000, 1000000) + "@esempio.com";
        // Generate valid password (at least 8 chars with uppercase, number, and special char)
        String password = "Password" + random.nextInt(1000, 10000) + "!";
        int currentYear = LocalDate.now().getYear();
        int minYear = currentYear - 60;
        int maxYear = currentYear - 18;
        int year = random.nextInt(minYear, maxYear + 1);
        int month = random.nextInt(1, 13);
        int day = random.nextInt(1, 29);
        LocalDate dob = LocalDate.of(year, month, day);
        double lat = -90 + random.nextDouble() * 180;
        double lon = -180 + random.nextDouble() * 360;
        String role = random.nextBoolean() ? "Cliente" : "Ristoratore";
        nameField.setText(name);
        surnameField.setText(surname);
        emailField.setText(email);
        if (isPasswordVisible) {
            passwordVisibleField.setText(password);
        } else {
            passwordField.setText(password);
        }
        updatePasswordCriteria(password);
        dateOfBirthPicker.setValue(dob);
        latitudeField.setText(String.format(java.util.Locale.US, "%.4f", lat));
        longitudeField.setText(String.format(java.util.Locale.US, "%.4f", lon));
        roleComboBox.setValue(role);
        statusLabel.setText("");
        statusLabel.setStyle("-fx-text-fill: #D32F2F;");
    }


    /**
     * Ripulisce i campi del form di registrazione e ripristina il ruolo predefinito.
     */
    private void clearFields() {
        nameField.clear();
        surnameField.clear();
        emailField.clear();
        passwordField.clear();
        passwordVisibleField.clear();
        if (isPasswordVisible) {
            isPasswordVisible = false;
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
        }
        dateOfBirthPicker.setValue(null);
        latitudeField.clear();
        longitudeField.clear();
        roleComboBox.setValue("Cliente");
        updatePasswordCriteria("");
    }

    /**
     * Rileva automaticamente la posizione (lat/lon) basandosi sull'IP pubblico.
     * Utilizza il servizio gratuito ip-api.com.
     */
    private void autoDetectLocation() {
        statusLabel.setText("Rilevamento posizione in corso...");
        statusLabel.setStyle("-fx-text-fill: #1976D2;"); // Info Blue
        
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
                    statusLabel.setStyle("-fx-text-fill: #2E7D32;"); // Success Green
                });
                
            } catch (Exception e) {
                logger.error("Auto-detect location failed", e);
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Connessione assente! Inserisci le coordinate manualmente.");
                    statusLabel.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;"); // Error Red + Bold
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

    /**
     * Restituisce la Scene costruita per la schermata di registrazione.
     *
     * @return La scena di registrazione.
     */
    public Scene getScene() {
        return scene;
    }
}
