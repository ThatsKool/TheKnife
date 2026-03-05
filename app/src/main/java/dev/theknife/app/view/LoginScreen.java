/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.view;

import dev.theknife.app.App;
import dev.theknife.app.config.AppConfig;
import dev.theknife.app.service.IUserService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.util.AnimationUtils;
import dev.theknife.app.util.Logger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.function.Supplier;
import java.util.concurrent.CompletableFuture;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import dev.theknife.app.viewmodel.LoginViewModel;

/**
 * View per la schermata di accesso (Login).
 * Gestisce validazione, sessione e navigazione verso la Home.
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class LoginScreen {
    // CAMPI
    private static final String PRIMARY_GREEN = "#2E7D32";
    private static final String LIGHT_GREEN = "#4CAF50";
    private static final String BACKGROUND_WHITE = "#FFFFFF";
    private static final String TEXT_DARK = "#212121";
    private static final String TEXT_GRAY = "#757575";
    private static final String BORDER_GRAY = "#E0E0E0";
    private static final String BACKGROUND_LIGHT = "#F5F5F5";

    private Scene scene;
    private TextField emailField;
    private PasswordField passwordField;
    private Label statusLabel;
    private ProgressIndicator loadingIndicator;
    private Button loginBtn;
    private TextField visiblePasswordField;
    private Button togglePasswordBtn;
    private Button backBtn;
    private Stage primaryStage;
    private Supplier<Scene> homeSceneSupplier;
    private Runnable onHomeSceneRefresh;
    private final IUserService userService;
    private final SessionContext sessionContext;

    private final LoginViewModel viewModel;

    // COSTRUTTORI
    /**
     * Costruttore semplificato per la schermata di login.
     * 
     * @param primaryStage Lo stage principale.
     * @param homeScene La scena home a cui accedere dopo il login.
     */
    public LoginScreen(Stage primaryStage, Scene homeScene) {
        this(primaryStage, () -> homeScene, null, null, null);
    }

    /**
     * Costruttore completo con iniezione delle dipendenze.
     * 
     * @param primaryStage Lo stage principale.
     * @param homeSceneSupplier Supplier per ottenere la scena home (lazy loading).
     * @param onHomeSceneRefresh Callback da eseguire al refresh della home.
     * @param userService Servizio gestione utenti.
     * @param sessionContext Contesto sessione.
     */
    public LoginScreen(Stage primaryStage, Supplier<Scene> homeSceneSupplier, Runnable onHomeSceneRefresh,
                       IUserService userService, SessionContext sessionContext) {
        this.primaryStage = primaryStage;
        this.homeSceneSupplier = homeSceneSupplier;
        this.onHomeSceneRefresh = onHomeSceneRefresh;
        this.userService = userService;
        this.sessionContext = sessionContext;
        this.viewModel = userService != null ? new LoginViewModel(userService) : null;

        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50, 40, 50, 40));
        root.setStyle("-fx-background-color: #C8E6C9;");

        Label titleLabel = new Label("Accedi");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(20);
        grid.setPadding(new Insets(30, 0, 20, 0));
        javafx.scene.layout.ColumnConstraints column1 = new javafx.scene.layout.ColumnConstraints();
        column1.setPercentWidth(100);
        column1.setHalignment(javafx.geometry.HPos.CENTER);
        grid.getColumnConstraints().add(column1);

        Label emailLabel = new Label("Email");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        emailLabel.setTextFill(Color.web(PRIMARY_GREEN));
        emailField = new TextField();
        emailField.setPrefWidth(320);
        emailField.setPrefHeight(42);
        emailField.setPromptText("tua.email@esempio.com");
        emailField.setMaxWidth(320);
        emailField.setStyle("-fx-background-color: " + BACKGROUND_WHITE + ";-fx-border-color: " + BORDER_GRAY + ";-fx-border-width: 1px;-fx-border-radius: 8px;-fx-background-radius: 8px;-fx-padding: 10px 15px;-fx-font-size: 14px;");
        grid.add(emailLabel, 0, 0);
        grid.add(emailField, 0, 1);

        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        passwordLabel.setTextFill(Color.web(PRIMARY_GREEN));
        String fieldStyle = "-fx-background-color: " + BACKGROUND_WHITE + ";-fx-border-color: " + BORDER_GRAY + ";-fx-border-width: 1px;-fx-border-radius: 8px;-fx-background-radius: 8px;-fx-padding: 10px 40px 10px 15px;-fx-font-size: 14px;";
        passwordField = new PasswordField();
        passwordField.setPrefWidth(320);
        passwordField.setPrefHeight(42);
        passwordField.setMaxWidth(320);
        passwordField.setStyle(fieldStyle);
        visiblePasswordField = new TextField();
        visiblePasswordField.setPrefWidth(320);
        visiblePasswordField.setPrefHeight(42);
        visiblePasswordField.setMaxWidth(320);
        visiblePasswordField.setStyle(fieldStyle);
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        togglePasswordBtn = new Button("👁");
        togglePasswordBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: " + TEXT_GRAY + "; -fx-font-size: 16px;");
        togglePasswordBtn.setPadding(new Insets(0, 10, 0, 0));
        togglePasswordBtn.setOnAction(e -> {
            boolean isVisible = visiblePasswordField.isVisible();
            if (isVisible) {
                visiblePasswordField.setVisible(false);
                visiblePasswordField.setManaged(false);
                passwordField.setVisible(true);
                passwordField.setManaged(true);
                togglePasswordBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: " + TEXT_GRAY + "; -fx-font-size: 16px;");
            } else {
                passwordField.setVisible(false);
                passwordField.setManaged(false);
                visiblePasswordField.setVisible(true);
                visiblePasswordField.setManaged(true);
                togglePasswordBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-text-fill: " + PRIMARY_GREEN + "; -fx-font-size: 16px;");
            }
            if (visiblePasswordField.isVisible()) {
                visiblePasswordField.positionCaret(visiblePasswordField.getText().length());
            } else {
                passwordField.positionCaret(passwordField.getText().length());
            }
        });

        StackPane passwordContainer = new StackPane();
        passwordContainer.setMaxWidth(320);
        passwordContainer.setAlignment(Pos.CENTER_LEFT);
        passwordContainer.getChildren().addAll(passwordField, visiblePasswordField, togglePasswordBtn);
        StackPane.setAlignment(togglePasswordBtn, Pos.CENTER_RIGHT);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordContainer, 0, 3);

        statusLabel = new Label("");
        statusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        statusLabel.setStyle("-fx-text-fill: #D32F2F;");
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(20, 20);
        loadingIndicator.setVisible(false);
        loadingIndicator.setManaged(false);
        HBox statusBox = new HBox(10, loadingIndicator, statusLabel);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setPadding(new Insets(5, 0, 0, 0));

        VBox buttonBox = new VBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        loginBtn = new Button("Accedi");
        loginBtn.setPrefSize(320, 48);
        loginBtn.setMaxWidth(320);
        loginBtn.setStyle("-fx-background-color: " + PRIMARY_GREEN + ";-fx-text-fill: white;-fx-font-weight: 600;-fx-font-size: 16px;-fx-background-radius: 24px;-fx-cursor: hand;");
        AnimationUtils.applyButtonHoverAnimation(loginBtn);
        loginBtn.setOnAction(e -> handleLogin());
        backBtn = new Button("Torna alla Home");
        backBtn.setPrefSize(320, 42);
        backBtn.setMaxWidth(320);
        backBtn.setStyle("-fx-background-color: white;-fx-text-fill: " + TEXT_DARK + ";-fx-font-weight: 500;-fx-font-size: 14px;-fx-background-radius: 21px;-fx-border-color: " + BORDER_GRAY + ";-fx-border-width: 1px;-fx-border-radius: 21px;-fx-cursor: hand;");
        AnimationUtils.applyButtonHoverAnimation(backBtn);
        backBtn.setOnAction(e -> primaryStage.setScene(homeSceneSupplier.get()));
        buttonBox.getChildren().add(loginBtn);
        buttonBox.getChildren().add(backBtn);

        if (AppConfig.DEBUG) {
            HBox debugBox = new HBox(10);
            debugBox.setAlignment(Pos.CENTER);
            debugBox.setPadding(new Insets(10, 0, 0, 0));
            Button clientAutoBtn = new Button("Usa Cliente Test");
            clientAutoBtn.setPrefSize(155, 36);
            clientAutoBtn.setStyle("-fx-background-color: " + BACKGROUND_LIGHT + ";-fx-text-fill: " + TEXT_DARK + ";-fx-font-weight: 500;-fx-font-size: 12px;-fx-background-radius: 18px;-fx-cursor: hand;");
            clientAutoBtn.setOnAction(ev -> autofillTestClient());
            Button restoAutoBtn = new Button("Usa Ristoratore Test");
            restoAutoBtn.setPrefSize(155, 36);
            restoAutoBtn.setStyle("-fx-background-color: " + BACKGROUND_LIGHT + ";-fx-text-fill: " + TEXT_DARK + ";-fx-font-weight: 500;-fx-font-size: 12px;-fx-background-radius: 18px;-fx-cursor: hand;");
            restoAutoBtn.setOnAction(ev -> autofillTestRestaurateur());
            debugBox.getChildren().addAll(clientAutoBtn, restoAutoBtn);
            buttonBox.getChildren().add(debugBox);
        }

        root.getChildren().addAll(titleLabel, grid, statusBox, buttonBox);
        AnimationUtils.slideInFromBottom(root, 500);
        scene = App.createSceneWithModal(root, 450, 550);
    }

    // METODI
    /**
     * Gestisce il processo di login dell'utente.
     * <p>
     * Valida i campi email e password, verifica le credenziali tramite il servizio,
     * imposta la sessione corrente e naviga alla Home in caso di successo.
     * </p>
     */
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Inserisci email e password!");
            if (email.isEmpty()) AnimationUtils.shake(emailField);
            if (password.isEmpty()) AnimationUtils.shake(passwordField);
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            statusLabel.setText("Inserisci un indirizzo email valido!");
            statusLabel.setStyle("-fx-text-fill: #D32F2F;");
            AnimationUtils.shake(emailField);
            return;
        }
        setLoadingState(true);
        statusLabel.setText("Verifica credenziali in corso...");
        statusLabel.setStyle("-fx-text-fill: " + TEXT_GRAY + ";");

        CompletableFuture.supplyAsync(() -> {
            try {
                return viewModel != null ? viewModel.login(email, password) : null;
            } catch (Exception ex) {
                Logger.getLogger(LoginScreen.class).error("Login error", ex);
                throw new RuntimeException(ex);
            }
        }).thenAcceptAsync(loggedInUser -> {
            setLoadingState(false);
            if (loggedInUser != null && sessionContext != null) {
                sessionContext.setCurrentUser(loggedInUser);
                statusLabel.setText("Accesso riuscito! Benvenuto, " + loggedInUser.getName() + "!");
                statusLabel.setStyle("-fx-text-fill: #2E7D32;");
                clearFields();
                pauseAndSwitch();
            } else {
                statusLabel.setText("Email o password non validi! Riprova.");
                statusLabel.setStyle("-fx-text-fill: #D32F2F;");
                AnimationUtils.shake(loginBtn);
            }
        }, javafx.application.Platform::runLater)
        .exceptionally(ex -> {
            javafx.application.Platform.runLater(() -> {
                setLoadingState(false);
                statusLabel.setText("Errore durante il login. Riprova.");
                statusLabel.setStyle("-fx-text-fill: #D32F2F;");
            });
            return null;
        });
    }

    /**
     * Imposta lo stato di caricamento dell'interfaccia durante l'autenticazione.
     *
     * @param loading true per mostrare l'indicatore di caricamento e disabilitare i controlli, false altrimenti.
     */
    private void setLoadingState(boolean loading) {
        loadingIndicator.setVisible(loading);
        loadingIndicator.setManaged(loading);
        loginBtn.setDisable(loading);
        emailField.setDisable(loading);
        passwordField.setDisable(loading);
        visiblePasswordField.setDisable(loading);
        togglePasswordBtn.setDisable(loading);
        backBtn.setDisable(loading);
    }

    /**
     * Attende un secondo e poi naviga alla scena Home.
     * <p>
     * Utilizzato dopo un login riuscito per dare tempo all'utente di vedere il messaggio di successo.
     * </p>
     */
    private void pauseAndSwitch() {
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            if (onHomeSceneRefresh != null) onHomeSceneRefresh.run();
            primaryStage.setScene(homeSceneSupplier.get());
        });
        pause.play();
    }

    /**
     * Ripulisce i campi del form di login.
     */
    private void clearFields() {
        emailField.clear();
        passwordField.clear();
    }

    /**
     * Compila automaticamente i campi con le credenziali del cliente di test (solo DEBUG).
     */
    private void autofillTestClient() {
        emailField.setText(AppConfig.TEST_CLIENT_EMAIL);
        passwordField.setText(AppConfig.TEST_CLIENT_PASSWORD);
        statusLabel.setText("");
    }

    /**
     * Compila automaticamente i campi con le credenziali del ristoratore di test (solo DEBUG).
     */
    private void autofillTestRestaurateur() {
        emailField.setText(AppConfig.TEST_RESTAURATEUR_EMAIL);
        passwordField.setText(AppConfig.TEST_RESTAURATEUR_PASSWORD);
        statusLabel.setText("");
    }

    /**
     * Restituisce la Scene costruita per la schermata di login.
     *
     * @return La scena di login.
     */
    public Scene getScene() {
        return scene;
    }
}
