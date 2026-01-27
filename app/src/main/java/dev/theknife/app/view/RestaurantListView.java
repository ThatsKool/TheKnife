/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.view;

import dev.theknife.app.App;
import dev.theknife.app.container.DependencyContainer;
import dev.theknife.app.model.Restaurant;
import dev.theknife.app.service.IReviewService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.viewmodel.RestaurantListViewModel;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

/**
 * View principale per la visualizzazione dell'elenco dei ristoranti.
 * <p>
 * Questa classe gestisce la schermata di ricerca e navigazione dei ristoranti.
 * Utilizza una {@link ListView} ottimizzata con celle personalizzate per visualizzare
 * più card ristorante per riga, garantendo alte prestazioni anche con liste numerose
 * grazie alla virtualizzazione della UI.
 * </p>
 * <p>
 * <b>Funzionalità principali:</b>
 * <ul>
 *   <li>Ricerca testuale in tempo reale con meccanismo di debounce.</li>
 *   <li>Pannello di filtraggio avanzato (cucina, posizione, prezzo, stelle, servizi).</li>
 *   <li>Layout responsive a griglia (3 card per riga).</li>
 *   <li>Integrazione con il {@link RestaurantListViewModel} per la logica di business.</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.viewmodel.RestaurantListViewModel
 * @see dev.theknife.app.view.RestaurantDetailsView
 */
public class RestaurantListView {
    // CAMPI
    // The Fork inspired color palette
    private static final String PRIMARY_GREEN = "#2E7D32";
    private static final String LIGHT_GREEN = "#4CAF50";
    private static final String ACCENT_GREEN = "#66BB6A";
    private static final String BACKGROUND_WHITE = "#FFFFFF";
    private static final String BACKGROUND_LIGHT = "#F5F5F5";
    private static final String TEXT_DARK = "#212121";
    private static final String TEXT_GRAY = "#757575";
    private static final String BORDER_GRAY = "#E0E0E0";
    
    private Scene scene;
    private Stage primaryStage;
    private Scene homeScene;
    private RestaurantListViewModel viewModel;
    private final DependencyContainer container;
    private final SessionContext sessionContext;
    
    // UI Components
    private ListView<javafx.collections.ObservableList<Restaurant>> restaurantListView;
    private TextField searchField;
    private ComboBox<String> cuisineFilter;
    private ComboBox<String> locationFilter;
    private ComboBox<String> priceFilter;
    private ComboBox<String> starsFilter;
    private ComboBox<String> distanceFilter;
    private CheckBox deliveryFilter;
    private CheckBox onlineBookingFilter;

    private Button searchButton;
    private int cardsPerRow = 3;
    private javafx.collections.ObservableList<javafx.collections.ObservableList<Restaurant>> rowList;
    
    /**
     * Genera lo stile CSS per il background con pattern sottile.
     *
     * @return Una stringa contenente le direttive CSS per il background-color.
     */
    private String getPatternBackgroundStyle() {
        // Use a visible green background color inspired by The Fork
        return "-fx-background-color: #C8E6C9;";
    }

    // COSTRUTTORI
    /**
     * Costruisce la vista principale della lista ristoranti.
     * <p>
     * Inizializza la scena, configura i binding e carica i dati iniziali.
     * </p>
     *
     * @param primaryStage Lo stage principale dell'applicazione.
     * @param homeScene La scena home per la navigazione "Indietro".
     * @param viewModel Il ViewModel per la gestione della logica di business.
     * @param container Il container per l'iniezione delle dipendenze.
     * @param sessionContext Il contesto della sessione utente.
     */
    public RestaurantListView(Stage primaryStage, Scene homeScene, RestaurantListViewModel viewModel,
                              DependencyContainer container, SessionContext sessionContext) {
        this.primaryStage = primaryStage;
        this.homeScene = homeScene;
        this.viewModel = viewModel;
        this.container = container;
        this.sessionContext = sessionContext;
        createScene();
        setupBindings();
        refresh();
    }
    
    // METODI
    /**
     * Restituisce la scena associata a questa vista.
     *
     * @return L'oggetto {@link Scene} contenente l'interfaccia grafica.
     */
    public Scene getScene() {
        return scene;
    }
    
    /**
     * Aggiorna i dati della lista ristoranti.
     * <p>
     * Richiede al ViewModel di ricaricare i dati applicando i filtri correnti.
     * </p>
     */
    public void refresh() {
        viewModel.refresh();
    }
    
    /**
     * Esegue una ricerca mirata per un ristorante specifico.
     * <p>
     * Imposta il testo nella barra di ricerca e avvia il filtraggio.
     * Include un piccolo ritardo per attendere il caricamento iniziale dei dati se necessario.
     * </p>
     *
     * @param restaurantName Il nome del ristorante da cercare.
     */
    public void searchForRestaurant(String restaurantName) {
        if (restaurantName != null && !restaurantName.trim().isEmpty()) {
            // Wait for the view model to finish initial loading, then perform search
            // The view model loads data asynchronously, so we need to wait a bit
            PauseTransition pause = new PauseTransition(Duration.millis(500));
            pause.setOnFinished(e -> {
                searchField.setText(restaurantName);
                viewModel.performSearch(restaurantName);
            });
            pause.play();
        }
    }
    
    /**
     * Forza l'aggiornamento della lista visualizzata.
     * <p>
     * Utile per riflettere cambiamenti esterni (es. nuovi ristoranti aggiunti).
     * </p>
     */
    public void refreshList() {
        viewModel.refresh();
    }
    
    /**
     * Crea e configura la struttura della scena.
     * <p>
     * Assembla l'header, la barra di ricerca e la lista dei ristoranti in un layout verticale.
     * </p>
     */
    private void createScene() {
        VBox mainLayout = new VBox(20);
        mainLayout.setAlignment(Pos.TOP_CENTER);
        mainLayout.setPadding(new Insets(0)); // Full width, no outer padding
        mainLayout.setStyle(getPatternBackgroundStyle());
        
        // Barra superiore
        HBox header = createHeader();
        
        // Barra di ricerca
        VBox searchBox = createSearchBox();
        
        // Lista dei ristoranti con le card visualizzate in righe
        createRestaurantList();
        
        // Assicura che la vista della lista occupi tutto lo spazio verticale disponibile
        VBox.setVgrow(restaurantListView, Priority.ALWAYS);
        
        mainLayout.getChildren().addAll(header, searchBox, restaurantListView);

        // Sovrapposizione di caricamento
        VBox loadingOverlay = new VBox(15);
        loadingOverlay.setAlignment(Pos.CENTER);
        loadingOverlay.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7);");
        loadingOverlay.visibleProperty().bind(viewModel.isLoadingProperty());
        loadingOverlay.managedProperty().bind(viewModel.isLoadingProperty());
        
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(60, 60);
        
        Label loadingLabel = new Label("Caricamento in corso...");
        loadingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        loadingLabel.setTextFill(Color.web(PRIMARY_GREEN));
        
        loadingOverlay.getChildren().addAll(loadingIndicator, loadingLabel);
        
        StackPane root = new StackPane(mainLayout, loadingOverlay);
        scene = App.createSceneWithModal(root, 1200, 800);
    }
    
    /**
     * Crea l'header della vista con il pulsante indietro e il titolo.
     *
     * @return Un HBox contenente l'header.
     */
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25, 25, 0, 25)); // Padding per il contenuto della barra superiore
        
        Button backButton = new Button("← Indietro");
        backButton.setPrefSize(120, 40);
        backButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;"
        );
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(backButton);
        backButton.setOnAction(e -> primaryStage.setScene(homeScene));
        
        Label titleLabel = new Label("Elenco Ristoranti");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        header.getChildren().addAll(backButton, titleLabel, spacer);
        
        return header;
    }
    
    /**
     * Crea la barra di ricerca e i filtri.
     *
     * @return Un VBox contenente la barra di ricerca e i filtri.
     */
    private VBox createSearchBox() {
        VBox container = new VBox(15);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(0, 25, 10, 25)); // Padding per la barra di ricerca
        
        // 1. Riga superiore: Campo di ricerca, Pulsante di ricerca, e Pulsante di filtro
        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER);
        
        searchField = new TextField();
        searchField.setPromptText("Cerca ristoranti...");
        searchField.setPrefHeight(44);
        searchField.setPrefWidth(500); // Ridotta la larghezza per essere più proporzionata
        searchField.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 22px;" +
            "-fx-background-radius: 22px;" +
            "-fx-padding: 10px 20px;" +
            "-fx-font-size: 14px;" +
            "-fx-font-family: 'Segoe UI';"
        );
        
        // Debounce per la ricerca in tempo reale
        PauseTransition debounceTimer = new PauseTransition(Duration.millis(300));
        debounceTimer.setOnFinished(event -> {
            String text = searchField.getText().trim();
            // Ricerca se >= 3 caratteri o vuoto (per resettare)
            if (text.length() >= 3) {
                viewModel.performSearch(text);
            } else if (text.isEmpty()) {
                // Se vuoto, resettare la ricerca per mostrare tutto
                viewModel.performSearch("");
            }
            // Se 1 o 2 caratteri, non fare nulla (attesa di più input o utente per cancellare)
        });

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            debounceTimer.playFromStart(); // Resetta il timer ogni volta che si preme un tasto
        });
        
        searchButton = new Button("Cerca");
        searchButton.setPrefSize(100, 44);
        searchButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 22px;" +
            "-fx-cursor: hand;"
        );
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(searchButton);
        
        // Pulsante di filtro
        Button filterToggleButton = new Button("Filtri");
        filterToggleButton.setPrefSize(160, 44);
        filterToggleButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 22px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 22px;" +
            "-fx-cursor: hand;"
        );
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(filterToggleButton);
        
        searchRow.getChildren().addAll(searchField, searchButton, filterToggleButton);
        
        // 2. Riga inferiore: Filtri (FlowPane per la responsività) - Inizialmente nascosto
        FlowPane filterRow = new FlowPane(10, 10);
        filterRow.setAlignment(Pos.CENTER);
        filterRow.setPrefWrapLength(1000); // Permette il wrapping
        filterRow.setVisible(false);
        filterRow.setManaged(false); // Non occupa spazio quando è nascosto
        
        // Azione per il toggle della visibilità
        filterToggleButton.setOnAction(e -> {
            boolean isVisible = filterRow.isVisible();
            filterRow.setVisible(!isVisible);
            filterRow.setManaged(!isVisible);
            if (!isVisible) {
                filterToggleButton.setText("Nascondi Filtri");
                filterToggleButton.setStyle(
                    "-fx-background-color: " + PRIMARY_GREEN + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 14px;" +
                    "-fx-background-radius: 22px;" +
                    "-fx-border-width: 0px;" +
                    "-fx-cursor: hand;"
                );
            } else {
                filterToggleButton.setText("Filtri");
                filterToggleButton.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-text-fill: " + PRIMARY_GREEN + ";" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 14px;" +
                    "-fx-background-radius: 22px;" +
                    "-fx-border-color: " + PRIMARY_GREEN + ";" +
                    "-fx-border-width: 2px;" +
                    "-fx-border-radius: 22px;" +
                    "-fx-cursor: hand;"
                );
            }
        });
        
        // Filtro Cucina
        cuisineFilter = new ComboBox<>();
        cuisineFilter.setPromptText("Cucina");
        cuisineFilter.setPrefWidth(180);
        cuisineFilter.setPrefHeight(38);
        cuisineFilter.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 19px;" +
            "-fx-background-radius: 19px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI';"
        );
        
        // Filtro Posizione
        locationFilter = new ComboBox<>();
        locationFilter.setPromptText("Posizione");
        locationFilter.setPrefWidth(180);
        locationFilter.setPrefHeight(38);
        locationFilter.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 19px;" +
            "-fx-background-radius: 19px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI';"
        );
        
        // Filtro Prezzo
        priceFilter = new ComboBox<>();
        priceFilter.setPromptText("Prezzo");
        priceFilter.setPrefWidth(120);
        priceFilter.setPrefHeight(38);
        priceFilter.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 19px;" +
            "-fx-background-radius: 19px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI';"
        );
        
        // Filtro Stelle
        starsFilter = new ComboBox<>();
        starsFilter.setPromptText("Stelle");
        starsFilter.setPrefWidth(120);
        starsFilter.setPrefHeight(38);
        starsFilter.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 19px;" +
            "-fx-background-radius: 19px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI';"
        );
        
        // Filtro Distanza
        distanceFilter = new ComboBox<>();
        distanceFilter.setPromptText("Distanza");
        distanceFilter.setPrefWidth(120);
        distanceFilter.setPrefHeight(38);
        distanceFilter.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 19px;" +
            "-fx-background-radius: 19px;" +
            "-fx-padding: 8px 15px;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI';"
        );
        
        // Filtro Consegna
        deliveryFilter = new CheckBox("Consegna");
        deliveryFilter.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " + TEXT_DARK + ";" +
            "-fx-font-family: 'Segoe UI';"
        );
        
        // Filtro Prenotazione Online
        onlineBookingFilter = new CheckBox("Prenotazione Online");
        onlineBookingFilter.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " + TEXT_DARK + ";" +
            "-fx-font-family: 'Segoe UI';"
        );
        
        // Popola il filtro Cucina
        javafx.collections.ObservableList<String> cuisines = javafx.collections.FXCollections.observableArrayList();
        cuisines.add("Tutte le cucine");
        
        // Popola il filtro Posizione
        javafx.collections.ObservableList<String> locations = javafx.collections.FXCollections.observableArrayList();
        locations.add("Tutte le posizioni");
        
        // Popola il filtro Prezzo
        javafx.collections.ObservableList<String> prices = javafx.collections.FXCollections.observableArrayList();
        prices.add("Tutti i prezzi");
        
        // Popola il filtro Stelle
        javafx.collections.ObservableList<String> stars = javafx.collections.FXCollections.observableArrayList();
        stars.add("Tutte le stelle");
        
        // Popola il filtro Distanza
        javafx.collections.ObservableList<String> distances = javafx.collections.FXCollections.observableArrayList();
        distances.addAll(viewModel.getAvailableDistances());
        
        PauseTransition pause = new PauseTransition(Duration.millis(200));
        pause.setOnFinished(e -> {
            try {
                java.util.List<String> availableCuisines = viewModel.getAvailableCuisines();
                cuisines.addAll(availableCuisines);
                java.util.List<String> availableLocations = viewModel.getAvailableLocations();
                locations.addAll(availableLocations);
                java.util.List<String> availablePrices = viewModel.getAvailablePrices();
                prices.addAll(availablePrices);
                java.util.List<String> availableAwards = viewModel.getAvailableAwards();
                stars.addAll(availableAwards);
            } catch (Exception ex) {
                dev.theknife.app.util.Logger.getLogger(RestaurantListView.class).error("Failed to load filter options", ex);
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Attenzione");
                alert.setHeaderText("Errore caricamento filtri");
                alert.setContentText("Non è stato possibile caricare alcune opzioni di filtro. Riprova più tardi.");
                alert.show();
            }
        });
        pause.play();
        makeComboBoxSearchable(cuisineFilter, cuisines);
        makeComboBoxSearchable(locationFilter, locations);
        priceFilter.setItems(prices);
        starsFilter.setItems(stars);
        distanceFilter.setItems(distances);
        
        if (sessionContext != null && sessionContext.isLoggedIn()) {
            filterRow.getChildren().addAll(cuisineFilter, locationFilter, priceFilter, starsFilter, distanceFilter, deliveryFilter, onlineBookingFilter);
        } else {
            filterRow.getChildren().addAll(cuisineFilter, locationFilter, priceFilter, starsFilter, deliveryFilter, onlineBookingFilter);
        }
        
        // Etichetta di stato di ordinamento
        Label sortingStatusLabel = new Label();
        sortingStatusLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        sortingStatusLabel.setTextFill(Color.web(PRIMARY_GREEN));
        sortingStatusLabel.textProperty().bind(viewModel.sortingStatusProperty());

        // Etichetta del numero di risultati
        Label resultsCountLabel = new Label();
        resultsCountLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        resultsCountLabel.setTextFill(Color.web(TEXT_GRAY));
        
        // Inizializzazione
        resultsCountLabel.setText(viewModel.getDisplayedRestaurants().size() + " ristoranti caricati");
        
        // Listener per gli aggiornamenti
        viewModel.getDisplayedRestaurants().addListener((javafx.collections.ListChangeListener<Restaurant>) c -> {
            javafx.application.Platform.runLater(() -> {
                int count = viewModel.getDisplayedRestaurants().size();
                String suffix = count == 1 ? " ristorante caricato" : " ristoranti caricati";
                resultsCountLabel.setText(count + suffix);
            });
        });
        
        container.getChildren().addAll(searchRow, sortingStatusLabel, resultsCountLabel, filterRow);
        return container;
    }
    
    /**
     * Inizializza la lista dei ristoranti.
     * <p>
     * Configura la {@link ListView} con una factory di celle personalizzata che
     * raggruppa i ristoranti in righe.
     * </p>
     */
    private void createRestaurantList() {
        // Crea una lista osservabile che raggruppa i ristoranti in righe
        rowList = javafx.collections.FXCollections.observableArrayList();
        
        restaurantListView = new ListView<>();
        restaurantListView.setItems(rowList);
        
        // Disabilita la selezione per evitare interferenze con i click dei pulsanti
        restaurantListView.setSelectionModel(new javafx.scene.control.MultipleSelectionModel<javafx.collections.ObservableList<Restaurant>>() {
            @Override
            public javafx.collections.ObservableList<Integer> getSelectedIndices() {
                return javafx.collections.FXCollections.emptyObservableList();
            }
            @Override
            public javafx.collections.ObservableList<javafx.collections.ObservableList<Restaurant>> getSelectedItems() {
                return javafx.collections.FXCollections.emptyObservableList();
            }
            @Override
            public void selectIndices(int index, int... indices) {}
            @Override
            public void selectAll() {}
            @Override
            public void selectFirst() {}
            @Override
            public void selectLast() {}
            @Override
            public void clearAndSelect(int index) {}
            @Override
            public void select(int index) {}
            @Override
            public void select(javafx.collections.ObservableList<Restaurant> obj) {}
            @Override
            public void clearSelection(int index) {}
            @Override
            public void clearSelection() {}
            @Override
            public boolean isSelected(int index) { return false; }
            @Override
            public boolean isEmpty() { return true; }
            @Override
            public void selectPrevious() {}
            @Override
            public void selectNext() {}
        });
        restaurantListView.setFocusTraversable(false);
        
        // Factory di celle personalizzata per visualizzare più card per riga
        restaurantListView.setCellFactory(param -> new RestaurantRowCell());
        
        // Ascolta i ristoranti visualizzati e li raggruppa in righe
        viewModel.getDisplayedRestaurants().addListener((javafx.collections.ListChangeListener.Change<? extends Restaurant> c) -> {
            updateRowList(rowList);
        });
        
        // Listener per il layout responsivo
        restaurantListView.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                double width = newVal.doubleValue();
                int newCardsPerRow;
                
                // Adatta in base alla larghezza della card
                // Forza 3 card per riga quanto più possibile come richiesto
                // Ma scala per schermi più grandi per ottimizzare l'utilizzo dello spazio
                if (width < 700) {
                    newCardsPerRow = 1;
                } else if (width < 1000) {
                    newCardsPerRow = 2;
                } else if (width < 1400) {
                    newCardsPerRow = 3;
                } else if (width < 1800) {
                    newCardsPerRow = 4;
                } else {
                    newCardsPerRow = 5;
                }
                
                if (newCardsPerRow != cardsPerRow) {
                    cardsPerRow = newCardsPerRow;
                    // Raggruppa di nuovo i ristoranti con la nuova dimensione della riga
                    updateRowList(rowList);
                }
            }
        });
        
        // Inizializzazione
        updateRowList(rowList);
        
        // Stile della vista della lista - Background completamente trasparente
        restaurantListView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        
        // Imposta il placeholder per la lista vuota
        restaurantListView.setPlaceholder(createPlaceholder());
    }
    
    /**
     * Crea un placeholder da mostrare quando la lista è vuota.
     *
     * @return Un nodo contenente il messaggio di placeholder.
     */
    private javafx.scene.Node createPlaceholder() {
        VBox placeholder = new VBox(20);
        placeholder.setAlignment(Pos.CENTER);
        
        Label noResultsLabel = new Label("Nessun ristorante trovato con i criteri selezionati.");
        noResultsLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        noResultsLabel.setTextFill(Color.web(TEXT_GRAY));
        
        // Hide placeholder when loading (handled by overlay)
        placeholder.visibleProperty().bind(viewModel.isLoadingProperty().not());
        
        placeholder.getChildren().add(noResultsLabel);
        return placeholder;
    }
    
    /**
     * Raggruppa i ristoranti in righe di 3-4 card ciascuna.
     *
     * @param rowList La lista osservabile delle righe da aggiornare.
     */
    private void updateRowList(javafx.collections.ObservableList<javafx.collections.ObservableList<Restaurant>> rowList) {
        rowList.clear();
        javafx.collections.ObservableList<Restaurant> allRestaurants = viewModel.getDisplayedRestaurants();
        
        for (int i = 0; i < allRestaurants.size(); i += cardsPerRow) {
            javafx.collections.ObservableList<Restaurant> row = javafx.collections.FXCollections.observableArrayList();
            for (int j = 0; j < cardsPerRow && (i + j) < allRestaurants.size(); j++) {
                row.add(allRestaurants.get(i + j));
            }
            rowList.add(row);
        }
    }
    
    /**
     * Cella personalizzata che visualizza più card ristorante orizzontalmente in una riga.
     */
    private class RestaurantRowCell extends ListCell<javafx.collections.ObservableList<Restaurant>> {
        private HBox rowContainer;
        
        /**
         * Costruttore per la cella che rappresenta una riga di ristoranti.
         * <p>
         * Inizializza il contenitore orizzontale con spaziatura e stile trasparente.
         * </p>
         */
        public RestaurantRowCell() {
            rowContainer = new HBox(20);
            rowContainer.setAlignment(Pos.CENTER);
            rowContainer.setPadding(new Insets(10));
            rowContainer.setStyle("-fx-background-color: transparent;");
            
            // Imposta il background della cella a trasparente per mostrare il background verde principale
            setStyle("-fx-background-color: transparent;");
            
            // Rendere la cella non selezionabile e impedire che catturi eventi del mouse
            setFocusTraversable(false);
        }
        
        /**
         * Aggiorna il contenuto della cella quando cambia l'elemento della lista.
         * <p>
         * Crea le card dei ristoranti per la riga corrente e le aggiunge al contenitore.
         * </p>
         *
         * @param row La lista di ristoranti per questa riga.
         * @param empty true se la cella è vuota, false altrimenti.
         */
        @Override
        protected void updateItem(javafx.collections.ObservableList<Restaurant> row, boolean empty) {
            super.updateItem(row, empty);
            
            if (empty || row == null || row.isEmpty()) {
                setGraphic(null);
                return;
            }
            
            // Pulisci le card precedenti
            rowContainer.getChildren().clear();
            
            // Crea una card per ogni ristorante in questa riga
            for (Restaurant restaurant : row) {
                if (restaurant != null) {
                    VBox card = createRestaurantCard(restaurant);
                    rowContainer.getChildren().add(card);
                }
            }
            
            // Se questa è l'ultima riga e ci sono più ristoranti, aggiungi l'indicatore di caricamento
            int index = getIndex();
            int totalRows = getListView().getItems().size();
            if (index == totalRows - 1 && viewModel.hasMoreRestaurants()) {
                VBox loadingCard = createLoadingCard();
                rowContainer.getChildren().add(loadingCard);
            }
            
            setGraphic(rowContainer);
            
            // Assicura che il background rimanga trasparente all'aggiornamento
            setStyle("-fx-background-color: transparent;");
            
            // Animazione dell'apparizione della riga
            dev.theknife.app.util.AnimationUtils.fadeIn(rowContainer);
        }
    }
    
    /**
     * Crea una card per visualizzare un ristorante.
     *
     * @param restaurant Il ristorante da visualizzare.
     * @return Un VBox contenente la card del ristorante.
     */
    private VBox createRestaurantCard(Restaurant restaurant) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(18));
        
        // Calcolo della larghezza dinamica
        // Associa la larghezza alla larghezza della vista della lista divisa per il numero di card per riga
        if (restaurantListView != null) {
            // Sottrae le margini: 
            // - Padding/scroll della ListView: ~40px
            // - Padding della riga HBox: 20px (10*2)
            // - Spaziatura tra le card: 20px * (cardsPerRow - 1)
            int spacing = 20 * (cardsPerRow - 1);
            int margins = 60 + spacing; 
            
            card.prefWidthProperty().bind(
                restaurantListView.widthProperty()
                    .subtract(margins)
                    .divide(cardsPerRow)
            );
            card.setMinWidth(250); // Larghezza minima per evitare che si rompa
        } else {
            card.setPrefWidth(350);
        }
        
        card.setPrefHeight(400);
        card.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-background-radius: 12px;" +
            // Rimossa l'ombreggiatura per migliorare le prestazioni
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );
        
        // Immagine del ristorante
        javafx.scene.Node imagePlaceholder;
        try {
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
            // Associa la larghezza alla larghezza della card meno il padding (18*2 = 36)
            imageView.fitWidthProperty().bind(card.prefWidthProperty().subtract(36));
            imageView.setFitHeight(150);
            imageView.setPreserveRatio(false); // Riempie l'area
            
            // Carica l'immagine in base alla cucina
            String cuisineForImage = restaurant.getCuisine();
            javafx.scene.image.Image image = dev.theknife.app.util.CuisineImageManager.getInstance().getImageForCuisine(cuisineForImage);
            
            if (image != null) {
                imageView.setImage(image);
                // Clip per i bordi arrotondati - bisogna associare anche la larghezza del clip
                javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(320, 150);
                clip.widthProperty().bind(imageView.fitWidthProperty());
                clip.setArcWidth(10);
                clip.setArcHeight(10);
                imageView.setClip(clip);
                imagePlaceholder = imageView;
            } else {
                // Fallback se l'immagine non è disponibile
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(320, 150);
                rect.widthProperty().bind(card.prefWidthProperty().subtract(36));
                rect.setFill(Color.web("#ecf0f1"));
                rect.setArcWidth(10);
                rect.setArcHeight(10);
                imagePlaceholder = rect;
            }
        } catch (Exception e) {
            // Fallback
            javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(320, 150);
            rect.widthProperty().bind(card.prefWidthProperty().subtract(36));
            rect.setFill(Color.web("#ecf0f1"));
            rect.setArcWidth(10);
            rect.setArcHeight(10);
            imagePlaceholder = rect;
        }
        
        // Nome del ristorante
        String restaurantName = restaurant.getName() != null && !restaurant.getName().isEmpty() 
            ? restaurant.getName() 
            : "Ristorante senza nome";
        Label nameLabel = new Label(restaurantName);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        nameLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(320);
        
        // Badge di riconoscimento
        HBox awardBox = new HBox(5);
        awardBox.setAlignment(Pos.CENTER);
        
        if (restaurant.getAward() != null && !restaurant.getAward().isEmpty() && !restaurant.getAward().equals("0")) {
            String awardText = restaurant.getAward();
            Label awardLabel = new Label();
            
            if (awardText.contains("Selected")) {
                awardLabel.setText("Ristorante Selezionato");
                awardLabel.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 15;");
            } else if (awardText.contains("Star")) {
                String translatedAward = awardText.replace("Stars", "Stelle").replace("Star", "Stella");
                awardLabel.setText(translatedAward);
                awardLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 15;");
            } else if (awardText.contains("Bib")) {
                awardLabel.setText("Bib Gourmand");
                awardLabel.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 15;");
            } else {
                awardLabel.setText(awardText);
                awardLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 15;");
            }
            
            awardBox.getChildren().add(awardLabel);
        }
        
        // Prezzo
        if (restaurant.getPrice() != null && !restaurant.getPrice().isEmpty()) {
            Label priceLabel = new Label(restaurant.getPrice());
            priceLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10;");
            awardBox.getChildren().add(priceLabel);
        }
        
        // Cucina
        String cuisine = restaurant.getCuisine() != null && !restaurant.getCuisine().isEmpty() 
            ? restaurant.getCuisine() 
            : "Cucina non specificata";
        Label cuisineLabel = new Label(cuisine);
        cuisineLabel.setFont(Font.font("Segoe UI", 13));
        cuisineLabel.setStyle("-fx-text-fill: " + TEXT_GRAY + ";");
        cuisineLabel.setWrapText(true);
        cuisineLabel.setMaxWidth(320);
        
        // Posizione
        String location = restaurant.getLocation() != null && !restaurant.getLocation().isEmpty() 
            ? restaurant.getLocation() 
            : "Posizione non specificata";
            
        // Aggiunge la distanza se disponibile
        if (restaurant.getDistanceKm() != null) {
            location += String.format(" • %.1f km", restaurant.getDistanceKm());
        }
        
        Label locationLabel = new Label(location);
        locationLabel.setFont(Font.font("Segoe UI", 12));
        locationLabel.setStyle("-fx-text-fill: " + TEXT_GRAY + ";");
        locationLabel.setWrapText(true);
        locationLabel.setMaxWidth(320);
        
        // Descrizione (troncata)
        String description = viewModel.getTruncatedDescription(restaurant);
        if (description == null || description.isEmpty()) {
            description = "Nessuna descrizione disponibile.";
        }
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Segoe UI", 11));
        descLabel.setStyle("-fx-text-fill: " + TEXT_DARK + ";");
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(320);
        descLabel.setMaxHeight(60);
        
        // Pulsante per vedere i dettagli - singolo click per aprire
        Button detailsButton = new Button("Vedi Dettagli");
        detailsButton.setPrefSize(140, 38);
        detailsButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 19px;" +
            "-fx-cursor: hand;"
        );
        detailsButton.setOnMouseEntered(e -> detailsButton.setStyle(
            "-fx-background-color: " + LIGHT_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 19px;" +
            "-fx-cursor: hand;"
        ));
        detailsButton.setOnMouseExited(e -> detailsButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 13px;" +
            "-fx-background-radius: 19px;" +
            "-fx-cursor: hand;"
        ));
        // Cattura il ristorante in una variabile finale per assicurarsi che venga chiuso correttamente
        final Restaurant restaurantForButton = restaurant;
        
        // Handler per singolo click - usa solo setOnAction per un comportamento singolo click affidabile
        detailsButton.setOnAction(e -> {
            e.consume(); // Consuma l'evento per evitare che la ListView lo gestisca
            if (restaurantForButton != null) {
                showRestaurantDetails(restaurantForButton);
            }
        });
        
        // Rendere il pulsante focusabile e assicurarsi che possa ricevere eventi
        detailsButton.setFocusTraversable(true);
        detailsButton.setMnemonicParsing(false);
        
        card.getChildren().addAll(imagePlaceholder, nameLabel, awardBox, cuisineLabel, locationLabel, descLabel, detailsButton);
        
        // Rendere la card intera trasparente per i click del mouse, ma permettere ai figli (come i pulsanti) di ricevere gli eventi
        card.setPickOnBounds(false);
        
        // Aggiunge l'animazione di hover
        dev.theknife.app.util.AnimationUtils.addHoverEffect(card);
        
        return card;
    }
    
    /**
     * Crea una card di caricamento da mostrare quando altri ristoranti stanno venendo caricati.
     *
     * @return Un VBox contenente la card di caricamento.
     */
    private VBox createLoadingCard() {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(18));
        card.setPrefWidth(350);
        card.setPrefHeight(400);
        card.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-background-radius: 12px;" +
            // Rimossa l'ombreggiatura per migliorare le prestazioni
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );
        
        // Placeholder di caricamento
        javafx.scene.shape.Rectangle loadingPlaceholder = new javafx.scene.shape.Rectangle(320, 150);
        loadingPlaceholder.setFill(Color.web(BACKGROUND_LIGHT));
        loadingPlaceholder.setArcWidth(10);
        loadingPlaceholder.setArcHeight(10);
        
        // Testo di caricamento
        Label loadingLabel = new Label("Caricamento altri ristoranti...");
        loadingLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        loadingLabel.setTextFill(Color.web(TEXT_GRAY));
        
        // Indicatore di progressione
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(30, 30);
        
        card.getChildren().addAll(loadingPlaceholder, loadingLabel, progressIndicator);
        
        return card;
    }
    
    /**
     * Configura i binding tra i componenti UI e il ViewModel.
     * <p>
     * Imposta i listener per i filtri, la ricerca e il caricamento lazy.
     * </p>
     */
    private void setupBindings() {
        // Search functionality
        searchField.setOnAction(e -> {
            String term = searchField.getText();
            viewModel.performSearch(term);
        });
        
        searchButton.setOnAction(e -> {
            String term = searchField.getText();
            viewModel.performSearch(term);
        });
        
        cuisineFilter.setOnAction(e -> {
            String selected = cuisineFilter.getValue();
            if (selected != null) {
                viewModel.performCuisineFilter(selected);
            }
        });
        
        locationFilter.setOnAction(e -> {
            String selected = locationFilter.getValue();
            if (selected != null) {
                viewModel.performLocationFilter(selected);
            }
        });
        
        priceFilter.setOnAction(e -> {
            String selected = priceFilter.getValue();
            if (selected != null) {
                viewModel.performPriceFilter(selected);
            }
        });
        
        starsFilter.setOnAction(e -> {
            String selected = starsFilter.getValue();
            if (selected != null) {
                viewModel.performStarsFilter(selected);
            }
        });
        
        distanceFilter.setOnAction(e -> {
            String selected = distanceFilter.getValue();
            if (selected != null) {
                viewModel.performDistanceFilter(selected);
            }
        });
        
        deliveryFilter.selectedProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.performDeliveryFilter(newVal);
        });
        
        onlineBookingFilter.selectedProperty().addListener((obs, oldVal, newVal) -> {
            viewModel.performOnlineBookingFilter(newVal);
        });
        
        viewModel.searchTermProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(searchField.getText())) {
                searchField.setText(newVal);
            }
        });
        
        // Ascolta gli elementi che vengono aggiunti per attivare il controllo di caricamento lazy
        viewModel.getDisplayedRestaurants().addListener((javafx.collections.ListChangeListener.Change<? extends Restaurant> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    // Quando nuovi elementi vengono aggiunti, controlla se è necessario caricare altro
                    javafx.application.Platform.runLater(() -> {
                        checkAndLoadMore();
                    });
                }
            }
        });
        
        // Ascolta gli eventi di scroll per il caricamento lazy
        restaurantListView.setOnScroll(e -> {
            // Controlla se si sta scorrendo verso il basso (deltaY positivo significa che si sta scorrendo verso il basso)
            if (e.getDeltaY() > 0) {
                checkAndLoadMore();
            }
        });
        
        // Ascolta anche le modifiche alla posizione di scroll tramite il flusso virtuale
        restaurantListView.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                // Accede alla barra di scorrimento per rilevare quando siamo vicini al fondo
                restaurantListView.lookupAll(".scroll-bar").forEach(node -> {
                    if (node instanceof ScrollBar) {
                        ScrollBar scrollBar = (ScrollBar) node;
                        if (scrollBar.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                            scrollBar.valueProperty().addListener((obs2, oldVal, newVal) -> {
                                // Se si è scorsi vicino al fondo (80%), carica altro
                                if (newVal.doubleValue() >= 0.8) {
                                    checkAndLoadMore();
                                }
                            });
                        } else if (scrollBar.getOrientation() == javafx.geometry.Orientation.HORIZONTAL) {
                            // Disabilita la barra di scorrimento orizzontale
                            scrollBar.setVisible(false);
                            scrollBar.setManaged(false);
                            scrollBar.setOpacity(0);
                        }
                    }
                });
            }
        });
    }
    
    /**
     * Verifica se siamo vicini al fondo della lista e carica altri ristoranti se necessario.
     * <p>
     * Questo metodo viene chiamato quando vengono aggiunti elementi o durante lo scroll.
     * </p>
     */
    private void checkAndLoadMore() {
        if (viewModel.isLoadingProperty().get() || !viewModel.hasMoreRestaurants()) {
            return;
        }
        
        // Ottiene il numero totale di righe
        int totalRows = restaurantListView.getItems().size();
        
        if (totalRows == 0) {
            return;
        }
        
        
        int restaurantsPerPage = viewModel.getRestaurantsPerPage();
        int totalRestaurants = viewModel.getDisplayedRestaurants().size();
        if (totalRestaurants > 0 && totalRestaurants % restaurantsPerPage == 0) {
            // Small delay to avoid loading too aggressively
            javafx.application.Platform.runLater(() -> {
                if (!viewModel.isLoadingProperty().get() && viewModel.hasMoreRestaurants()) {
                    viewModel.loadNextPage();
                }
            });
        }
    }
    
    /**
     * Mostra la vista dei dettagli di un ristorante.
     *
     * @param restaurant Il ristorante di cui mostrare i dettagli.
     */
    private void showRestaurantDetails(Restaurant restaurant) {
        // Crea la vista dei dettagli del ristorante
        RestaurantDetailsView detailsView = new RestaurantDetailsView(container, sessionContext);

        // Configura la navigazione
        detailsView.setBackButtonAction(() -> {
            primaryStage.setScene(scene);
        });
        
        detailsView.setAddReviewButtonAction(() -> {
            showAddReviewDialog(restaurant, detailsView);
        });
        
        // Configura l'azione per modificare una recensione
        detailsView.setEditReviewAction((review) -> {
            showEditReviewDialog(restaurant, review, detailsView);
        });
        
        // Configura l'azione per eliminare una recensione
        detailsView.setDeleteReviewAction((review) -> {
            deleteReview(review, detailsView);
        });
        
        String userName = sessionContext != null ? sessionContext.getCurrentUserName() : null;
        detailsView.loadRestaurantDetails(restaurant.getName(), userName);
        
        // Crea la scena e mostra
        Scene detailsScene = App.createSceneWithModal(detailsView, 1000, 700);
        primaryStage.setScene(detailsScene);
    }
    
    /**
     * Mostra il dialogo per aggiungere una nuova recensione.
     *
     * @param restaurant Il ristorante per cui aggiungere la recensione.
     * @param detailsView La vista dei dettagli del ristorante.
     */
    private void showAddReviewDialog(Restaurant restaurant, RestaurantDetailsView detailsView) {
        IReviewService reviewService = container.get(IReviewService.class);
        ReviewView reviewView = new ReviewView(reviewService, sessionContext);
        reviewView.setCancelButtonAction(() -> showRestaurantDetails(restaurant));
        reviewView.getViewModel().successMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                javafx.application.Platform.runLater(() -> showRestaurantDetails(restaurant));
            }
        });
        String userName = sessionContext != null ? sessionContext.getCurrentUserName() : null;
        if (userName == null) {
            ModalManager.getInstance().showWarning("Accesso Richiesto", "Devi effettuare l'accesso per aggiungere una recensione.");
            return;
        }
        reviewView.initialize(restaurant.getName(), userName);
        Scene reviewScene = App.createSceneWithModal(reviewView, 700, 600);
        primaryStage.setScene(reviewScene);
    }
    
    /**
     * Mostra il dialogo per modificare una recensione esistente.
     *
     * @param restaurant Il ristorante associato alla recensione.
     * @param review La recensione da modificare.
     * @param detailsView La vista dei dettagli del ristorante.
     */
    private void showEditReviewDialog(Restaurant restaurant, dev.theknife.app.model.Review review, RestaurantDetailsView detailsView) {
        IReviewService reviewService = container.get(IReviewService.class);
        ReviewView reviewView = new ReviewView(reviewService, sessionContext);
        reviewView.setCancelButtonAction(() -> showRestaurantDetails(restaurant));
        reviewView.getViewModel().successMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                javafx.application.Platform.runLater(() -> showRestaurantDetails(restaurant));
            }
        });
        reviewView.initializeForEdit(review);
        Scene reviewScene = App.createSceneWithModal(reviewView, 700, 600);
        primaryStage.setScene(reviewScene);
    }
    
    /**
     * Elimina una recensione dal sistema.
     *
     * @param review La recensione da eliminare.
     * @param detailsView La vista dei dettagli da aggiornare dopo l'eliminazione.
     */
    private void deleteReview(dev.theknife.app.model.Review review, RestaurantDetailsView detailsView) {
        try {
            IReviewService reviewService = container.get(IReviewService.class);
            String email = sessionContext != null && sessionContext.getCurrentUser() != null
                ? sessionContext.getCurrentUser().getEmail() : null;
            boolean success = reviewService.deleteReview(review.getId(), email);
            if (success) {
                javafx.application.Platform.runLater(() -> {
                    detailsView.refresh();
                    String userName = sessionContext != null ? sessionContext.getCurrentUserName() : null;
                    detailsView.loadRestaurantDetails(review.getRestaurantName(), userName);
                });
            } else {
                ModalManager.getInstance().showError("Errore", "Impossibile eliminare la recensione. Riprova.");
            }
        } catch (Exception e) {
            dev.theknife.app.util.Logger.getLogger(RestaurantListView.class).error("Delete review failed", e);
            ModalManager.getInstance().showError("Errore", "Si è verificato un errore: " + e.getMessage());
        }
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
            final String selected = comboBox.getSelectionModel().getSelectedItem();
            
            // Se il testo è lo stesso dell'elemento selezionato, non è necessario filtrare
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
                
                // Se il dropdown è nascosto e si ha testo, mostra il dropdown
                // Mostra solo se si hanno risultati
                if (!filteredList.isEmpty() && !comboBox.isShowing()) {
                     comboBox.show();
                }
            });
        });
    }
}
