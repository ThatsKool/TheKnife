/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.view;

import dev.theknife.app.container.DependencyContainer;
import dev.theknife.app.model.Restaurant;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.viewmodel.RestaurantListViewModel;
import dev.theknife.app.config.AppConfig;
import dev.theknife.app.view.ModalManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * View per l'inserimento di un nuovo ristorante.
 * <p>
 * Questa classe fornisce un modulo (form) per permettere ai ristoratori di registrare
 * un nuovo ristorante nel sistema. Include validazione dell'input e feedback visivo.
 * </p>
 * <p>
 * <b>Campi del modulo:</b>
 * <ul>
 *   <li>Nome del ristorante.</li>
 *   <li>Indirizzo completo e geolocalizzazione (latitudine/longitudine).</li>
 *   <li>Tipologia di cucina (es. Italiana, Giapponese).</li>
 *   <li>Fascia di prezzo.</li>
 *   <li>Contatti (telefono, sito web).</li>
 *   <li>Riconoscimenti (premi, stelle verdi).</li>
 *   <li>Servizi offerti.</li>
 * </ul>
 * </p>
 * <p>
 * Utilizza {@link RestaurantListViewModel} per la logica di salvataggio e gestione degli eventi.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.viewmodel.RestaurantListViewModel
 */
public class RestaurantFormView extends VBox {
    // CAMPI
    // The Fork inspired color palette
    private static final String PRIMARY_GREEN = "#2E7D32";
    private static final String LIGHT_GREEN = "#4CAF50";
    private static final String BACKGROUND_WHITE = "#FFFFFF";
    private static final String BACKGROUND_LIGHT = "#F5F5F5";
    private static final String TEXT_DARK = "#212121";
    private static final String TEXT_GRAY = "#757575";
    private static final String BORDER_GRAY = "#E0E0E0";
    
    /**
     * Servizio per la gestione dei ristoranti.
     */
    private final IRestaurantService restaurantService;
    
    /**
     * ViewModel per la logica di business.
     */
    private final RestaurantListViewModel viewModel;
    
    /**
     * Genera lo stile CSS per lo sfondo.
     * 
     * @return Stringa CSS.
     */
    private String getPatternBackgroundStyle() {
        // Use a visible green background color inspired by The Fork
        return "-fx-background-color: #C8E6C9;";
    }
    
    // UI Components
    private final TextField nameField;
    private final TextField addressField;
    private final TextField locationField;
    private final ComboBox<String> priceField;
    private final ComboBox<String> cuisineField;
    private final TextField longitudeField;
    private final TextField latitudeField;
    private final TextField phoneField;
    private final TextField websiteField;
    private final TextField awardField;
    private final TextField greenStarField;
    private final TextField facilitiesField;
    private final TextArea descriptionArea;
    private final Button submitButton;
    private final Button cancelButton;
    private final Label errorLabel;
    private final Label successLabel;
    private Stage primaryStage;
    private Scene backScene;
    private final DependencyContainer container;
    private final SessionContext sessionContext;
    
    // COSTRUTTORI
    /**
     * Costruisce la vista del modulo per l'aggiunta di un ristorante.
     * 
     * @param primaryStage Lo stage principale dell'applicazione.
     * @param backScene La scena a cui tornare (es. lista ristoranti).
     * @param container Il container per l'iniezione delle dipendenze.
     * @param sessionContext Il contesto della sessione utente.
     */
    public RestaurantFormView(Stage primaryStage, Scene backScene,
                              DependencyContainer container, SessionContext sessionContext) {
        this.primaryStage = primaryStage;
        this.backScene = backScene;
        this.container = container;
        this.sessionContext = sessionContext;
        this.restaurantService = container.get(IRestaurantService.class);
        this.viewModel = new RestaurantListViewModel(this.restaurantService, sessionContext);
        
        // Initialize fields
        this.nameField = new TextField();
        this.addressField = new TextField();
        this.locationField = new TextField();
        this.priceField = new ComboBox<>();
        this.cuisineField = new ComboBox<>();
        this.longitudeField = new TextField();
        this.latitudeField = new TextField();
        this.phoneField = new TextField();
        this.websiteField = new TextField();
        this.awardField = new TextField();
        this.greenStarField = new TextField();
        this.facilitiesField = new TextField();
        this.descriptionArea = new TextArea();
        this.submitButton = new Button("Aggiungi Ristorante");
        this.cancelButton = new Button("Annulla");
        this.errorLabel = new Label();
        this.successLabel = new Label();
        
        setupUI();
    }
    
    // METODI
    /**
     * Configura l'interfaccia utente del form.
     * <p>
     * Crea tutti i componenti grafici, i campi di input, i pulsanti e i gestori di eventi.
     * </p>
     */
    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle(getPatternBackgroundStyle());
        setMaxWidth(800);
        setAlignment(Pos.CENTER);
        
        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(35));
        mainContainer.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-background-radius: 16px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 16, 0, 0, 3);" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 16px;"
        );
        
        // Add entrance animation
        dev.theknife.app.util.AnimationUtils.popIn(mainContainer, 400);
        
        // Title
        Label titleLabel = new Label("Aggiungi Nuovo Ristorante");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));
        
        // Form Grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        int row = 0;
        grid.add(new Label("Nome *:"), 0, row);
        grid.add(nameField, 1, row++);
        nameField.setPrefWidth(400);
        
        grid.add(new Label("Indirizzo:"), 0, row);
        grid.add(addressField, 1, row++);
        addressField.setPrefWidth(400);
        
        grid.add(new Label("Città *:"), 0, row);
        grid.add(locationField, 1, row++);
        locationField.setPrefWidth(400);
        
        grid.add(new Label("Prezzo:"), 0, row);
        grid.add(priceField, 1, row++);
        priceField.setPrefWidth(400);
        priceField.setPromptText("Seleziona o cerca prezzo");
        // Populate prices
        javafx.collections.ObservableList<String> prices = javafx.collections.FXCollections.observableArrayList(viewModel.getAvailablePrices());
        makeComboBoxSearchable(priceField, prices);
        
        grid.add(new Label("Cucina *:"), 0, row);
        grid.add(cuisineField, 1, row++);
        cuisineField.setPrefWidth(400);
        cuisineField.setPromptText("Seleziona o cerca cucina");
        // Populate cuisines
        javafx.collections.ObservableList<String> cuisines = javafx.collections.FXCollections.observableArrayList();
        javafx.application.Platform.runLater(() -> {
            try {
                cuisines.addAll(viewModel.getAvailableCuisines());
            } catch (Exception e) {
                dev.theknife.app.util.Logger.getLogger(RestaurantFormView.class).error("Load cuisines failed", e);
            }
        });
        makeComboBoxSearchable(cuisineField, cuisines);
        
        grid.add(new Label("Latitudine *:"), 0, row);
        grid.add(latitudeField, 1, row++);
        latitudeField.setPrefWidth(400);
        latitudeField.setPromptText("es., 40.7128");
        
        grid.add(new Label("Longitudine *:"), 0, row);
        grid.add(longitudeField, 1, row++);
        longitudeField.setPrefWidth(400);
        longitudeField.setPromptText("es., -74.0060");
        
        grid.add(new Label("Telefono:"), 0, row);
        grid.add(phoneField, 1, row++);
        phoneField.setPrefWidth(400);
        
        grid.add(new Label("Sito Web:"), 0, row);
        grid.add(websiteField, 1, row++);
        websiteField.setPrefWidth(400);
        
        grid.add(new Label("Riconoscimento:"), 0, row);
        grid.add(awardField, 1, row++);
        awardField.setPrefWidth(400);
        
        grid.add(new Label("Stella Verde:"), 0, row);
        grid.add(greenStarField, 1, row++);
        greenStarField.setPrefWidth(400);
        
        grid.add(new Label("Servizi:"), 0, row);
        grid.add(facilitiesField, 1, row++);
        facilitiesField.setPrefWidth(400);
        
        grid.add(new Label("Descrizione:"), 0, row);
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setPrefWidth(400);
        descriptionArea.setWrapText(true);
        grid.add(descriptionArea, 1, row++);
        
        // Buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        submitButton.setPrefSize(180, 44);
        submitButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 15px;" +
            "-fx-background-radius: 22px;" +
            "-fx-cursor: hand;"
        );
        
        // Add hover animation
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(submitButton);
        
        submitButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e -> submitButton.setStyle(
            "-fx-background-color: " + LIGHT_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 15px;" +
            "-fx-background-radius: 22px;" +
            "-fx-cursor: hand;"
        ));
        submitButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e -> submitButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 15px;" +
            "-fx-background-radius: 22px;" +
            "-fx-cursor: hand;"
        ));
        
        cancelButton.setPrefSize(140, 44);
        cancelButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + TEXT_DARK + ";" +
            "-fx-font-weight: 500;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 22px;" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 22px;" +
            "-fx-cursor: hand;"
        );
        
        // Add hover animation
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(cancelButton);
        
        cancelButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e -> cancelButton.setStyle(
            "-fx-background-color: " + BACKGROUND_LIGHT + ";" +
            "-fx-text-fill: " + TEXT_DARK + ";" +
            "-fx-font-weight: 500;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 22px;" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 22px;" +
            "-fx-cursor: hand;"
        ));
        cancelButton.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e -> cancelButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + TEXT_DARK + ";" +
            "-fx-font-weight: 500;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 22px;" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 22px;" +
            "-fx-cursor: hand;"
        ));
        buttonBox.getChildren().addAll(submitButton, cancelButton);
        if (AppConfig.DEBUG) {
            Button autoFillBtn = new Button("Compilazione Automatica");
            autoFillBtn.setPrefSize(200, 44);
            autoFillBtn.setStyle(
                "-fx-background-color: " + BACKGROUND_LIGHT + ";" +
                "-fx-text-fill: " + TEXT_DARK + ";" +
                "-fx-font-weight: 500;" +
                "-fx-font-size: 13px;" +
                "-fx-background-radius: 22px;" +
                "-fx-cursor: hand;"
            );
            autoFillBtn.setOnAction(e -> autoFillRandom());
            buttonBox.getChildren().add(autoFillBtn);
        }
        
        // Status labels
        errorLabel.setStyle("-fx-text-fill: #D32F2F; -fx-wrap-text: true; -fx-font-family: 'Segoe UI';");
        successLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + "; -fx-wrap-text: true; -fx-font-family: 'Segoe UI';");
        
        mainContainer.getChildren().addAll(titleLabel, grid, buttonBox, errorLabel, successLabel);
        getChildren().add(mainContainer);
        
        // Event handlers
        submitButton.setOnAction(e -> onSubmit());
        cancelButton.setOnAction(e -> primaryStage.setScene(backScene));
    }

    /**
     * Compila automaticamente il form con dati casuali validi (solo DEBUG).
     */
    private void autoFillRandom() {
        nameField.setText("Ristorante Demo " + (int)(Math.random() * 1000));
        addressField.setText("Via Roma 123");
        locationField.setText("Milano");
        priceField.setValue("$$");
        cuisineField.setValue("Italiana");
        latitudeField.setText("45.4642");
        longitudeField.setText("9.1900");
        phoneField.setText("+39 02 1234 5678");
        websiteField.setText("https://ristorante-demo.esempio.com");
        awardField.setText("1 Stella");
        greenStarField.setText("Stella Verde");
        facilitiesField.setText("WiFi, Parcheggio, Tavoli all'aperto");
        descriptionArea.setText("Un ristorante demo usato per testare il modulo di aggiunta ristorante.");
        errorLabel.setText("");
        successLabel.setText("");
    }
    
    /**
     * Gestisce la sottomissione del form.
     * <p>
     * Valida i campi obbligatori, verifica i permessi dell'utente e salva il nuovo ristorante.
     * </p>
     */
    private void onSubmit() {
        errorLabel.setText("");
        successLabel.setText("");
        
        // Validazione campi obbligatori: raccogliere tutti i campi mancanti/non validi
        java.util.List<String> mancanti = new java.util.ArrayList<>();
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            mancanti.add("Nome del ristorante");
        }
        if (locationField.getText() == null || locationField.getText().trim().isEmpty()) {
            mancanti.add("Città");
        }
        String cucina = cuisineField.getEditor().getText();
        if (cucina == null || cucina.trim().isEmpty()) {
            mancanti.add("Cucina");
        }
        Double lat = parseDoubleOrNull(latitudeField.getText());
        if (lat == null) {
            mancanti.add("Latitudine (obbligatorio, inserire un numero tra -90 e 90)");
        } else if (lat < -90 || lat > 90) {
            mancanti.add("Latitudine (inserire un numero tra -90 e 90)");
        }
        Double lon = parseDoubleOrNull(longitudeField.getText());
        if (lon == null) {
            mancanti.add("Longitudine (obbligatorio, inserire un numero tra -180 e 180)");
        } else if (lon < -180 || lon > 180) {
            mancanti.add("Longitudine (inserire un numero tra -180 e 180)");
        }
        if (!mancanti.isEmpty()) {
            StringBuilder msg = new StringBuilder("Compila correttamente i seguenti campi obbligatori:\n\n");
            for (String m : mancanti) {
                msg.append("• ").append(m).append("\n");
            }
            msg.append("\nI campi contrassegnati con * sono obbligatori.");
            ModalManager.getInstance().showError("Campi obbligatori", msg.toString());
            return;
        }
        
        if (sessionContext == null || !sessionContext.isLoggedIn() || sessionContext.getCurrentUser() == null) {
            errorLabel.setText("Devi aver effettuato l'accesso come ristoratore!");
            return;
        }
        String role = sessionContext.getCurrentUser().getRole();
        if (!"Restaurateur".equalsIgnoreCase(role) && !"Ristoratore".equalsIgnoreCase(role)) {
            errorLabel.setText("Solo i ristoratori possono aggiungere ristoranti!");
            return;
        }
        String restaurateurEmail = sessionContext.getCurrentUser().getEmail();
        if (restaurateurEmail == null || restaurateurEmail.trim().isEmpty()) {
            errorLabel.setText("Errore: Email utente non trovata. Effettua nuovamente l'accesso.");
            return;
        }
        restaurateurEmail = restaurateurEmail.trim().toLowerCase();
        
        // Verify email is valid
        if (!restaurateurEmail.contains("@")) {
            errorLabel.setText("Errore: Email utente non valida. Effettua nuovamente l'accesso.");
            return;
        }
        
        double longitude = parseDoubleSafe(longitudeField.getText());
        double latitude = parseDoubleSafe(latitudeField.getText());

        // Create restaurant object with restaurateur email
        Restaurant restaurant = new Restaurant(
            nameField.getText().trim(),
            addressField.getText().trim(),
            locationField.getText().trim(),
            priceField.getEditor().getText().trim(),
            cuisineField.getEditor().getText().trim(),
            longitude,
            latitude,
            phoneField.getText().trim(),
            "", // url
            websiteField.getText().trim(),
            awardField.getText().trim(),
            greenStarField.getText().trim(),
            facilitiesField.getText().trim(),
            descriptionArea.getText().trim(),
            restaurateurEmail
        );
        
        // Verify restaurant has restaurateurEmail set before saving
        if (restaurant.getRestaurateurEmail() == null || restaurant.getRestaurateurEmail().trim().isEmpty()) {
            errorLabel.setText("Errore: Impossibile collegare il ristorante al tuo account. Riprova.");
            return;
        }
        
        boolean success = restaurantService.addRestaurant(restaurant);
        
        if (success) {
            ModalManager.getInstance().showInfo(
                "Ristorante aggiunto",
                "Il ristorante è stato inserito correttamente.\n\nVerrai reindirizzato a \"I Miei Ristoranti\".",
                () -> {
                    MyRestaurantsView myRestaurantsView = new MyRestaurantsView(primaryStage, backScene, container, sessionContext);
                    primaryStage.setScene(myRestaurantsView.createScene());
                }
            );
        } else {
            errorLabel.setText("Impossibile aggiungere il ristorante. Riprova.");
        }
    }

    /**
     * Analizza una stringa in un valore double in modo sicuro.
     * <p>
     * Restituisce 0.0 se la stringa non può essere parsata, evitando eccezioni.
     * </p>
     *
     * @param value La stringa da parsare.
     * @return Il valore double parsato, o 0.0 se la conversione fallisce.
     */
    private double parseDoubleSafe(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Restituisce il double parsato dalla stringa, o null se vuota/non numerica.
     * Usato per validare campi obbligatori (es. latitudine/longitudine).
     *
     * @param value La stringa da parsare.
     * @return Il valore parsato, o null se value è vuota o non è un numero valido.
     */
    private Double parseDoubleOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * Crea e restituisce la Scene contenente questo form.
     *
     * @return La Scene configurata con il form.
     */
    public Scene createScene() {
        return dev.theknife.app.App.createSceneWithModal(this, 900, 800);
    }

    /**
     * Rende un ComboBox ricercabile filtrando gli elementi in base al testo inserito.
     * <p>
     * Utilizza un FilteredList per filtrare dinamicamente gli elementi mentre l'utente digita.
     * </p>
     *
     * @param comboBox Il ComboBox da rendere ricercabile.
     * @param fullList La lista completa degli elementi disponibili.
     */
    private void makeComboBoxSearchable(ComboBox<String> comboBox, javafx.collections.ObservableList<String> fullList) {
        comboBox.setEditable(true);
        
        javafx.collections.transformation.FilteredList<String> filteredList = new javafx.collections.transformation.FilteredList<>(fullList, p -> true);
        comboBox.setItems(filteredList);
        
        comboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            final TextField editor = comboBox.getEditor();
            final String selected = comboBox.getSelectionModel().getSelectedItem();
            
            // If the text is the same as the selected item, we don't need to filter
            if (selected != null && selected.equals(newVal)) {
                return;
            }
            
            javafx.application.Platform.runLater(() -> {
                filteredList.setPredicate(item -> {
                    if (newVal == null || newVal.isEmpty()) {
                        return true;
                    }
                    String lowerVal = newVal.toLowerCase();
                    String lowerItem = item.toLowerCase();
                    return lowerItem.contains(lowerVal);
                });
                
                // If the dropdown is hidden and we have text, show it
                // Only show if we have results
                if (!filteredList.isEmpty() && !comboBox.isShowing()) {
                     comboBox.show();
                }
            });
        });
    }
}

