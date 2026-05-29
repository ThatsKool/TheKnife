/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.effect.DropShadow;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.RestaurantQueryService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.view.RestaurantListView;
import dev.theknife.app.viewmodel.RestaurantListViewModel;
import dev.theknife.app.view.FavoriteRestaurantsView;
import dev.theknife.app.view.RestaurantFormView;
import dev.theknife.app.view.MyRestaurantsView;
import dev.theknife.app.view.UserProfileView;
import dev.theknife.app.container.DependencyContainer;
import dev.theknife.app.model.User;
import dev.theknife.app.view.components.ModalStackPane;
import dev.theknife.app.view.ModalManager;
import dev.theknife.app.view.LoginScreen;
import dev.theknife.app.view.RegisterScreen;
import javafx.scene.Parent;

/**
 * Classe principale JavaFX dell'applicazione The Knife.
 * <p>
 * Responsabile dell'inizializzazione del contenitore delle dipendenze, della
 * creazione e gestione delle Scene principali (Home, Login, Registrazione) e
 * della navigazione tra le schermate. Applica linee guida di usabilità con
 * uno stile ispirato a The Fork e gestisce la visibilità dei pulsanti in base
 * allo stato dell'utente e al suo ruolo (Cliente/Ristoratore).
 * </p>
 * <p>
 * Note tecniche:
 * <ul>
 *   <li>Tutte le interazioni con i nodi JavaFX avvengono sul JavaFX Application Thread.</li>
 *   <li>La classe utilizza {@link SessionManager} per mostrare/occultare azioni
 *       sensibili al login e al ruolo.</li>
 *   <li>Il container {@link DependencyContainer} viene inizializzato all'avvio
 *       per assicurare servizi pronti (caricamento CSV, ecc.).</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 */
public class App extends Application {
    // CAMPI
    //Palette di colori ispirata a The Fork
    private static final String PRIMARY_GREEN = "#2E7D32"; // Verde scuro
    private static final String LIGHT_GREEN = "#4CAF50"; // Verde chiaro
    private static final String ACCENT_GREEN = "#66BB6A"; // Verde accentuato
    private static final String BACKGROUND_WHITE = "#FFFFFF"; // Bianco
    private static final String BACKGROUND_LIGHT = "#F5F5F5"; // Grigio chiaro
    private static final String TEXT_DARK = "#212121"; // Grigio scuro
    private static final String TEXT_GRAY = "#757575"; // Grigio scuro
    private static final String BORDER_GRAY = "#E0E0E0"; // Grigio scuro
    
    private static final double APP_WIDTH = 1200;
    private static final double APP_HEIGHT = 700; // Ridotta altezza per adattarsi a schermi più piccoli (es. 1366x768)
    private static final double APP_MIN_WIDTH = 900;
    private static final double APP_MIN_HEIGHT = 600;
    
    private Stage primaryStage;
    private Scene homeScene;
    private Scene registerScene;
    private Scene loginScene;
    private Label userStatusLabel;

    /** Composition root only: container e contesto sessione. */
    private DependencyContainer container;
    private SessionContext sessionContext;

    // METODI
    /**
     * Metodo principale per l'avvio dell'applicazione JavaFX.
     *
     * @param args Argomenti della riga di comando.
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    /**
     * Punto di ingresso dell'applicazione JavaFX.
     * <p>
     * Inizializza il contenitore delle dipendenze, crea le Scene principali e
     * mostra la schermata Home. In caso di errori di inizializzazione, registra
     * il problema e interrompe l'avvio.
     * </p>
     *
     * @param primaryStage Lo stage principale fornito da JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("The Knife - Restaurant Management System");
        
        // Imposta l'icona dell'applicazione
        try {
            java.net.URL iconUrl = getClass().getResource("/images/logo.png");
            if (iconUrl != null) {
                primaryStage.getIcons().add(new javafx.scene.image.Image(iconUrl.toExternalForm()));
            }
        } catch (Exception e) {
            System.err.println("Impossibile caricare l'icona dell'applicazione: " + e.getMessage());
        }
        
        try {
            container = DependencyContainer.getInstance();
            container.initializeAllServices();
            sessionContext = container.get(SessionContext.class);
        } catch (Exception e) {
            dev.theknife.app.util.Logger.getLogger(App.class).error("Impossibile inizializzare l'applicazione", e);
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Errore Critico");
            alert.setHeaderText("Impossibile avviare l'applicazione");
            alert.setContentText("Si è verificato un errore durante l'inizializzazione dei servizi:\n" + e.getMessage() + "\n\nL'applicazione verrà chiusa.");
            alert.showAndWait();
            
            return;
        }

        // Inizializza il listener di ModalManager
        primaryStage.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getRoot() instanceof ModalStackPane) {
                ModalManager.getInstance().setTarget((ModalStackPane) newScene.getRoot());
            }
        });
        
        // Crea le scene
        createHomeScene();
        createRegisterScene();
        createLoginScene();
        
        // Inizia con la scena Home
        primaryStage.setScene(homeScene);
        primaryStage.setMinWidth(APP_MIN_WIDTH);
        primaryStage.setMinHeight(APP_MIN_HEIGHT);
        primaryStage.setWidth(APP_WIDTH);
        primaryStage.setHeight(APP_HEIGHT);
        primaryStage.setResizable(true);
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();
        primaryStage.toFront();
        primaryStage.requestFocus();
        PauseTransition pause = new PauseTransition(Duration.millis(150));
        pause.setOnFinished(e -> primaryStage.setAlwaysOnTop(false));
        pause.play();
    }

    /**
     * Metodo helper per creare una Scene con supporto per ModalStackPane.
     *
     * @param root Il nodo radice della scena.
     * @param width La larghezza della scena.
     * @param height L'altezza della scena.
     * @return Una nuova Scene con ModalStackPane configurato.
     */
    public static Scene createSceneWithModal(Parent root, double width, double height) {
        ModalStackPane modalPane = new ModalStackPane(root);
        return new Scene(modalPane, APP_WIDTH, APP_HEIGHT);
    }
    
    /**
     * Crea il logo dell'applicazione.
     * <p>
     * Tenta di caricare "logo.png" dalle risorse. Se non trovato,
     * utilizza il logo geometrico del coltello come fallback.
     * </p>
     *
     * @return Un StackPane contenente il logo dell'applicazione.
     */
    private StackPane createLogo() {
        StackPane logoContainer = new StackPane();
        logoContainer.setPrefSize(300, 300); // Increased to 300 (balanced)
        
        // 1. Prova a caricare il logo fornito dall'utente da /images/logo.png
        try {
            java.net.URL logoUrl = getClass().getResource("/images/logo.png");
            if (logoUrl != null) {
                javafx.scene.image.ImageView logoImage = new javafx.scene.image.ImageView(new javafx.scene.image.Image(logoUrl.toExternalForm()));
                logoImage.setFitWidth(300); // Aumentata a 300
                logoImage.setFitHeight(300);
                logoImage.setPreserveRatio(true);
                
                // Nessun effetto di ombreggiatura per il logo personalizzato per fondere senza interruzioni
                logoContainer.getChildren().add(logoImage);
                return logoContainer;
            }
        } catch (Exception e) {
            // Log di avviso ma continua con il fallback
            System.err.println("Could not load custom logo: " + e.getMessage());
        }

        // 2. Fallback: Crea il logo del coltello stylizzato usando le forme JavaFX
        // Ridimensiona il logo geometrico poiché il contenitore è ora più grande
        javafx.scene.Group knifeGroup = new javafx.scene.Group();
        
        // Crea la lama del coltello (triangolo)
        Polygon blade = new Polygon();
        blade.getPoints().addAll(
            0.0, 0.0,    // In alto a sinistra
            80.0, 0.0,   // In alto a destra
            60.0, 100.0, // In basso a destra
            20.0, 100.0  // In basso a sinistra
        );
        blade.setFill(Color.web(PRIMARY_GREEN));
        blade.setStroke(Color.web(PRIMARY_GREEN));
        blade.setStrokeWidth(2);
        
        // Aggiunge un effetto di riflesso alla lama
        Polygon bladeShine = new Polygon();
        bladeShine.getPoints().addAll(
            10.0, 5.0,
            50.0, 5.0,
            40.0, 60.0,
            15.0, 60.0
        );
        bladeShine.setFill(Color.web(ACCENT_GREEN));
        bladeShine.setOpacity(0.6);
        
        // Crea la maniglia (rettangolo con effetto angoli arrotondati)
        Rectangle handle = new Rectangle(25, 100, 15, 40);
        handle.setFill(Color.web("#8D6E63")); // Maniglia marrone
        handle.setArcWidth(5);
        handle.setArcHeight(5);
        
        // Crea le linee di presa della maniglia
        Rectangle grip1 = new Rectangle(27, 110, 11, 2);
        grip1.setFill(Color.web("#5D4037"));
        Rectangle grip2 = new Rectangle(27, 120, 11, 2);
        grip2.setFill(Color.web("#5D4037"));
        Rectangle grip3 = new Rectangle(27, 130, 11, 2);
        grip3.setFill(Color.web("#5D4037"));
        
        knifeGroup.getChildren().addAll(blade, bladeShine, handle, grip1, grip2, grip3);
        
        // Ridimensiona il gruppo per adattarsi al contenitore (circa 1.65x per 240px)
        knifeGroup.setScaleX(1.65);
        knifeGroup.setScaleY(1.65);
        
        // Aggiunge un effetto di ombreggiatura per la profondità (solo per il fallback)
        DropShadow shadow = new DropShadow();
        shadow.setRadius(8);
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        knifeGroup.setEffect(shadow);
        
        // Assembla il logo
        logoContainer.getChildren().add(knifeGroup);
        
        return logoContainer;
    }
    
    /**
     * Crea una stringa CSS per uno sfondo con pattern sottile.
     *
     * @return Una stringa CSS per il colore di sfondo verde ispirato a The Fork.
     */
    private String getPatternBackgroundStyle() {
        // Usa un colore di sfondo verde visibile ispirato a The Fork
        return "-fx-background-color: #C8E6C9;";
    }
    
    /**
     * Crea un pulsante circolare del profilo con le iniziali dell'utente.
     *
     * @return Un Button configurato con le iniziali dell'utente corrente.
     */
    private Button createProfileButton() {
        User user = sessionContext.getCurrentUser();
        String initials = "";
        if (user != null) {
            if (user.getName() != null && !user.getName().isEmpty()) initials += user.getName().charAt(0);
            if (user.getSurname() != null && !user.getSurname().isEmpty()) initials += user.getSurname().charAt(0);
        }
        if (initials.isEmpty()) initials = "U";
        
        Button btn = new Button(initials.toUpperCase());
        btn.setPrefSize(40, 40);
        btn.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 50%;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 5, 0, 0, 2);"
        );
        
        btn.setOnAction(e -> {
            dev.theknife.app.viewmodel.UserProfileViewModel profileViewModel =
                new dev.theknife.app.viewmodel.UserProfileViewModel(
                    container.get(dev.theknife.app.service.IUserService.class), sessionContext);
            ModalManager.getInstance().showCustom(new UserProfileView(profileViewModel).createView());
        });
        
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(btn);
        
        Tooltip tooltip = new Tooltip("Il tuo Profilo");
        Tooltip.install(btn, tooltip);
        
        return btn;
    }

    /**
     * Costruisce la Scene della Home e configura layout, branding e pulsanti.
     * <p>
     * La visibilità dei pulsanti principali (Preferiti, I miei ristoranti,
     * Aggiungi ristorante) dipende dallo stato della sessione e dal ruolo
     * dell'utente. Aggiorna dinamicamente le etichette e lo stato quando
     * la scena riceve focus/mouse enter.
     * </p>
     */
    private void createHomeScene() {
        // Crea uno StackPane per permettere il sovrapporre la barra superiore sul contenuto
        StackPane root = new StackPane();
        root.setStyle(getPatternBackgroundStyle());
        
        // Barra superiore con pulsante di logout (Sovrapposta)
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(65, 60, 0, 30)); // Spostata in basso (65) e a sinistra (60)
        topBar.setStyle("-fx-background-color: transparent;");
        topBar.setMaxHeight(120); // Aumentato il limite leggermente per adattarsi al padding
        topBar.setPickOnBounds(false); // Permette il click attraverso le aree trasparenti
        
        // Pulsante di logout - stilizzato come The Fork
        Button logoutBtn = new Button("Esci");
        logoutBtn.setPrefSize(90, 36);
        logoutBtn.setStyle(
            "-fx-background-color: " + TEXT_GRAY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 18px;" +
            "-fx-cursor: hand;"
        );
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(
            "-fx-background-color: " + TEXT_DARK + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 18px;" +
            "-fx-cursor: hand;"
        ));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(
            "-fx-background-color: " + TEXT_GRAY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 18px;" +
            "-fx-cursor: hand;"
        ));
        logoutBtn.setOnAction(e -> {
            sessionContext.logout();
            refreshHomeScene();
            primaryStage.setScene(homeScene);
        });
        
        boolean isLoggedIn = sessionContext.isLoggedIn();
        
        if (isLoggedIn) {
            Button profileButton = createProfileButton();
            HBox.setMargin(profileButton, new Insets(0, 15, 0, 0));
            topBar.getChildren().add(profileButton);
        }
        
        logoutBtn.setVisible(isLoggedIn);
        topBar.getChildren().add(logoutBtn);
        // Rimuove root.setTop(topBar) poiché ora usiamo StackPane
        
        // Centra il contenuto con logo e branding
        VBox centerContent = new VBox(10); // Reduced spacing from 15 to 10
        centerContent.setAlignment(Pos.TOP_CENTER); // Allinea al top per spingere il contenuto in basso manualmente
        centerContent.setPadding(new Insets(10, 50, 20, 50)); // Ridotta la padding superiore a 10 per spingere il contenuto ancora più in alto
        centerContent.setStyle("-fx-background-color: transparent;");
        
        // Logo e nome dell'app
        VBox brandingBox = new VBox(5); // Ridotta la spaziatura da 10 a 5
        brandingBox.setAlignment(Pos.CENTER);
        
        StackPane logo = createLogo();
        
        // Sottotitolo
        Label taglineLabel = new Label("Trova il ristorante perfetto");
        taglineLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 18));
        taglineLabel.setTextFill(Color.web(TEXT_GRAY));
        
        brandingBox.getChildren().addAll(logo, taglineLabel);
        
        // Etichetta dello stato dell'utente
        userStatusLabel = new Label();
        userStatusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        userStatusLabel.setTextFill(Color.web(TEXT_GRAY));
        updateUserStatus();
        
        // Pulsanti principali - stilizzati come The Fork
        VBox mainButtonsBox = new VBox(12);
        mainButtonsBox.setAlignment(Pos.CENTER);
        mainButtonsBox.setPadding(new Insets(10, 0, 0, 0)); // Ridotta la padding superiore da 20 a 10
        
        // Pulsante Esplora Ristoranti - azione principale
        Button browseRestaurantsBtn = new Button("Esplora Ristoranti");
        browseRestaurantsBtn.setPrefSize(280, 52);
        browseRestaurantsBtn.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-cursor: hand;"
        );
        browseRestaurantsBtn.setOnMouseEntered(e -> browseRestaurantsBtn.setStyle(
            "-fx-background-color: " + LIGHT_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-cursor: hand;"
        ));
        browseRestaurantsBtn.setOnMouseExited(e -> browseRestaurantsBtn.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-cursor: hand;"
        ));
        browseRestaurantsBtn.setOnAction(e -> {
            IRestaurantService restaurantService = container.get(IRestaurantService.class);
            RestaurantQueryService restaurantQueryService = container.get(RestaurantQueryService.class);
            RestaurantListViewModel listViewModel = new RestaurantListViewModel(restaurantService, restaurantQueryService, sessionContext);
            RestaurantListView restaurantListView = new RestaurantListView(primaryStage, homeScene, listViewModel, container, sessionContext);
            primaryStage.setScene(restaurantListView.getScene());
        });
        
        // Pulsante I Miei Preferiti (visibile solo quando loggato come Cliente)
        Button favoritesBtn = new Button("★ I Miei Preferiti");
        favoritesBtn.setPrefSize(280, 52);
        favoritesBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 26px;" +
            "-fx-cursor: hand;"
        );
        favoritesBtn.setOnMouseEntered(e -> favoritesBtn.setStyle(
            "-fx-background-color: " + BACKGROUND_LIGHT + ";" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 26px;" +
            "-fx-cursor: hand;"
        ));
        favoritesBtn.setOnMouseExited(e -> favoritesBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 26px;" +
            "-fx-cursor: hand;"
        ));
        favoritesBtn.setOnAction(e -> {
            FavoriteRestaurantsView favoritesView = new FavoriteRestaurantsView(primaryStage, homeScene, container, sessionContext);
            Scene favoritesScene = createSceneWithModal(favoritesView, 1200, 800);
            primaryStage.setScene(favoritesScene);
        });
        boolean isClient = sessionContext.isLoggedIn() && sessionContext.getCurrentUser() != null && 
                        ("Client".equalsIgnoreCase(sessionContext.getCurrentUser().getRole()) || "Cliente".equalsIgnoreCase(sessionContext.getCurrentUser().getRole()));
        favoritesBtn.setVisible(isClient);
        
        // Pulsante I Miei Ristoranti (visibile solo quando loggato come Ristoratore)
        Button myRestaurantsBtn = new Button("🏪 I Miei Ristoranti");
        myRestaurantsBtn.setPrefSize(280, 52);
        myRestaurantsBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 26px;" +
            "-fx-cursor: hand;"
        );
        myRestaurantsBtn.setOnMouseEntered(e -> myRestaurantsBtn.setStyle(
            "-fx-background-color: " + BACKGROUND_LIGHT + ";" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 26px;" +
            "-fx-cursor: hand;"
        ));
        myRestaurantsBtn.setOnMouseExited(e -> myRestaurantsBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 26px;" +
            "-fx-cursor: hand;"
        ));
        myRestaurantsBtn.setOnAction(e -> {
            MyRestaurantsView myRestaurantsView = new MyRestaurantsView(primaryStage, homeScene, container, sessionContext);
            primaryStage.setScene(myRestaurantsView.createScene());
        });
        boolean isRestaurateur = sessionContext.isLoggedIn() && sessionContext.getCurrentUser() != null && 
                                ("Restaurateur".equalsIgnoreCase(sessionContext.getCurrentUser().getRole()) || "Ristoratore".equalsIgnoreCase(sessionContext.getCurrentUser().getRole()));
        myRestaurantsBtn.setVisible(isRestaurateur);
        
        // Pulsante Aggiungi Ristorante (visibile solo quando loggato come Ristoratore)
        Button addRestaurantBtn = new Button("➕ Aggiungi Ristorante");
        addRestaurantBtn.setPrefSize(280, 52);
        addRestaurantBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 26px;" +
            "-fx-cursor: hand;"
        );
        addRestaurantBtn.setOnMouseEntered(e -> addRestaurantBtn.setStyle(
            "-fx-background-color: " + BACKGROUND_LIGHT + ";" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 26px;" +
            "-fx-cursor: hand;"
        ));
        addRestaurantBtn.setOnMouseExited(e -> addRestaurantBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 16px;" +
            "-fx-background-radius: 26px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 26px;" +
            "-fx-cursor: hand;"
        ));
        addRestaurantBtn.setOnAction(e -> {
            RestaurantFormView formView = new RestaurantFormView(primaryStage, homeScene, container, sessionContext);
            primaryStage.setScene(formView.createScene());
        });
        addRestaurantBtn.setVisible(isRestaurateur);
        
        mainButtonsBox.getChildren().addAll(browseRestaurantsBtn, favoritesBtn, myRestaurantsBtn, addRestaurantBtn);
        
        // Pulsanti secondari - Login/Registrati
        HBox authButtonsBox = new HBox(15);
        authButtonsBox.setAlignment(Pos.CENTER);
        authButtonsBox.setPadding(new Insets(10, 0, 0, 0)); // Ridotta la padding superiore da 20 a 10
        
        Button registerBtn = new Button("Registrati");
        registerBtn.setPrefSize(140, 42);
        registerBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 21px;" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 21px;" +
            "-fx-cursor: hand;"
        );
        registerBtn.setOnMouseEntered(e -> registerBtn.setStyle(
            "-fx-background-color: " + BACKGROUND_LIGHT + ";" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 21px;" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 21px;" +
            "-fx-cursor: hand;"
        ));
        registerBtn.setOnMouseExited(e -> registerBtn.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 21px;" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 21px;" +
            "-fx-cursor: hand;"
        ));
        registerBtn.setOnAction(e -> primaryStage.setScene(registerScene));
        
        Button loginBtn = new Button("Accedi");
        loginBtn.setPrefSize(140, 42);
        loginBtn.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 21px;" +
            "-fx-cursor: hand;"
        );
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
            "-fx-background-color: " + LIGHT_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 21px;" +
            "-fx-cursor: hand;"
        ));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 21px;" +
            "-fx-cursor: hand;"
        ));
        loginBtn.setOnAction(e -> primaryStage.setScene(loginScene));
        
        loginBtn.setVisible(!isLoggedIn);
        registerBtn.setVisible(!isLoggedIn);
        
        authButtonsBox.getChildren().addAll(registerBtn, loginBtn);
        
        centerContent.setOnMouseEntered(e -> {
            boolean clientCheck = sessionContext.isLoggedIn() && sessionContext.getCurrentUser() != null && 
                              ("Client".equalsIgnoreCase(sessionContext.getCurrentUser().getRole()) || "Cliente".equalsIgnoreCase(sessionContext.getCurrentUser().getRole()));
            boolean restaurateurCheck = sessionContext.isLoggedIn() && sessionContext.getCurrentUser() != null && 
                                    ("Restaurateur".equalsIgnoreCase(sessionContext.getCurrentUser().getRole()) || "Ristoratore".equalsIgnoreCase(sessionContext.getCurrentUser().getRole()));
            boolean loggedInCheck = sessionContext.isLoggedIn();
            favoritesBtn.setVisible(clientCheck);
            myRestaurantsBtn.setVisible(restaurateurCheck);
            addRestaurantBtn.setVisible(restaurateurCheck);
            logoutBtn.setVisible(loggedInCheck);
            loginBtn.setVisible(!loggedInCheck);
            registerBtn.setVisible(!loggedInCheck);
            updateUserStatus();
        });
        
        centerContent.getChildren().addAll(brandingBox, userStatusLabel, mainButtonsBox, authButtonsBox);
        
        // Aggiunge i componenti allo StackPane (l'ordine è importante: dal basso verso l'alto)
        // 1. Centro del contenuto (così è dietro alla Barra superiore se si sovrappongono, ma la Barra superiore è trasparente)
        // In realtà, i pulsanti della Barra superiore devono essere cliccabili, quindi la Barra superiore deve essere l'ultimo.
        root.getChildren().addAll(centerContent, topBar);
        // Allinea esplicitamente la Barra superiore al top per assicurarsi che non fluttui in centro e blocchi i click
        StackPane.setAlignment(topBar, Pos.TOP_CENTER);
        // Assicurati che la Barra superiore non blocchi i click sul contenuto centrale dove è trasparente
        // Already set above: topBar.setPickOnBounds(false);

        homeScene = createSceneWithModal(root, APP_WIDTH, APP_HEIGHT);
    }
    
    /**
     * Aggiorna la schermata Home per riflettere lo stato corrente dell'utente
     * e la visibilità dei pulsanti.
     */
    public void refreshHomeScene() {
        updateUserStatus();
        // Ricrea la scena Home per aggiornare la visibilità dei pulsanti
        createHomeScene();
    }
    
    /**
     * Aggiorna l'etichetta di stato utente (nome ed email) in base alla sessione.
     */
    private void updateUserStatus() {
        if (userStatusLabel == null) return;
        
        if (sessionContext != null && sessionContext.isLoggedIn()) {
            User user = sessionContext.getCurrentUser();
            String role = user.getRole();
            String displayRole = role;
            
            // Traduce/Normalizza il ruolo per la visualizzazione
            if ("Client".equalsIgnoreCase(role) || "Cliente".equalsIgnoreCase(role)) {
                displayRole = "Cliente";
            } else if ("Restaurateur".equalsIgnoreCase(role) || "Ristoratore".equalsIgnoreCase(role)) {
                displayRole = "Ristoratore";
            }
            
            userStatusLabel.setText("Accesso effettuato come: " + user.getName() + " (" + user.getEmail() + ") - " + displayRole);
        } else {
            userStatusLabel.setText("Utente Ospite - Accedi per aggiungere recensioni");
        }
    }
    
    
    /**
     * Crea la Scena di Registrazione e collega il callback di refresh della Home.
     */
    private void createRegisterScene() {
        RegisterScreen registerScreen = new RegisterScreen(primaryStage, () -> homeScene, this::refreshHomeScene,
            container.get(dev.theknife.app.service.IUserService.class), sessionContext);
        registerScene = registerScreen.getScene();
    }
    
    /**
     * Crea la Scena di Login e collega il callback di refresh della Home.
     */
    private void createLoginScene() {
        LoginScreen loginScreen = new LoginScreen(primaryStage, () -> homeScene, this::refreshHomeScene,
            container.get(dev.theknife.app.service.IUserService.class), sessionContext);
        loginScene = loginScreen.getScene();
    }
}
