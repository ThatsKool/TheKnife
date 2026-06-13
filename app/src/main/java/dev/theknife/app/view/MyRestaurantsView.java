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
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.IReviewService;
import dev.theknife.app.session.SessionContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import dev.theknife.app.viewmodel.MyRestaurantsViewModel;
import dev.theknife.app.viewmodel.MyRestaurantsViewModel.RestaurantsScreenState;

import javafx.application.Platform;

/**
 * View dedicata ai ristoratori per la gestione dei propri ristoranti.
 * <p>
 * Questa schermata permette agli utenti con ruolo {@code RESTAURATEUR} di visualizzare
 * l'elenco dei ristoranti di loro proprietà e accedere alle relative recensioni.
 * </p>
 * <p>
 * <b>Caratteristiche:</b>
 * <ul>
 *   <li>Filtro automatico: mostra solo i ristoranti associati all'email del ristoratore loggato.</li>
 *   <li>Caricamento asincrono per garantire la reattività dell'interfaccia.</li>
 *   <li>Visualizzazione delle recensioni ricevute per ogni ristorante.</li>
 *   <li>Pulsante di aggiornamento manuale ("Refresh") per ricaricare i dati.</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.service.IRestaurantService
 * @see dev.theknife.app.model.User
 */
public class MyRestaurantsView extends VBox {
    // CAMPI
    //Palette di colori ispirata a The Fork
    private static final String PRIMARY_GREEN = "#2E7D32";
    private static final String LIGHT_GREEN = "#4CAF50";
    private static final String BACKGROUND_WHITE = "#FFFFFF";
    private static final String BACKGROUND_LIGHT = "#F5F5F5";
    private static final String TEXT_DARK = "#212121";
    private static final String TEXT_GRAY = "#757575";
    private static final String BORDER_GRAY = "#E0E0E0";
    
    /**
     * ViewModel che incapsula la logica sui ristoranti del ristoratore.
     */
    private final MyRestaurantsViewModel viewModel;
    
    /**
     * Componente grafico per la visualizzazione della lista ristoranti.
     */
    private final ListView<Restaurant> restaurantsListView;
    
    /**
     * Etichetta per mostrare messaggi di stato o errori.
     */
    private final Label statusLabel;
    
    private final Stage primaryStage;
    private final Scene homeScene;
    private final DependencyContainer container;
    private final SessionContext sessionContext;
    
    /**
     * Indicatore di caricamento (spinner).
     */
    private final ProgressIndicator loadingIndicator;
    
    /**
     * Genera lo stile CSS per lo sfondo.
     * 
     * @return Stringa CSS.
     */
    private String getPatternBackgroundStyle() {
        // Usa un colore di sfondo verde visibile ispirato a The Fork
        return "-fx-background-color: #C8E6C9;";
    }
    
    // COSTRUTTORI
    /**
     * Costruisce la vista dei ristoranti del ristoratore.
     * <p>
     * Inizializza l'interfaccia e avvia il caricamento dei ristoranti associati all'utente.
     * </p>
     *
     * @param primaryStage Lo stage principale dell'applicazione.
     * @param homeScene La scena home per la navigazione.
     * @param container Il container per l'iniezione delle dipendenze.
     * @param sessionContext Il contesto della sessione utente.
     */
    public MyRestaurantsView(Stage primaryStage, Scene homeScene,
                             DependencyContainer container, SessionContext sessionContext) {
        this.primaryStage = primaryStage;
        this.homeScene = homeScene;
        this.container = container;
        this.sessionContext = sessionContext;
        this.viewModel = new MyRestaurantsViewModel(
            container.get(IRestaurantService.class),
            container.get(IReviewService.class),
            sessionContext
        );
        
        this.restaurantsListView = new ListView<>();
        this.statusLabel = new Label();
        this.loadingIndicator = new ProgressIndicator();
        
        setupUI();
        bindViewModel();
        viewModel.loadRestaurants();
    }
    
    // METODI
    /**
     * Configura l'interfaccia utente.
     * <p>
     * Inizializza i componenti grafici e imposta lo stile.
     * </p>
     */
    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(25));
        setStyle(getPatternBackgroundStyle());
        
        // Intestazione con titolo e pulsanti
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        Button backButton = new Button("← Torna alla Home");
        backButton.setPrefSize(150, 40);
        backButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;"
        );
        backButton.setOnMouseEntered(e -> backButton.setStyle(
            "-fx-background-color: " + LIGHT_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;"
        ));
        backButton.setOnMouseExited(e -> backButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;"
        ));
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(backButton);
        backButton.setOnAction(e -> primaryStage.setScene(homeScene));
        
        Label titleLabel = new Label("I Miei Ristoranti");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));
        dev.theknife.app.util.AnimationUtils.fadeIn(titleLabel, 800);
        
        Button refreshButton = new Button("🔄 Aggiorna");
        refreshButton.setPrefSize(130, 40);
        refreshButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 20px;" +
            "-fx-cursor: hand;"
        );
        refreshButton.setOnMouseEntered(e -> refreshButton.setStyle(
            "-fx-background-color: " + BACKGROUND_LIGHT + ";" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 20px;" +
            "-fx-cursor: hand;"
        ));
        refreshButton.setOnMouseExited(e -> refreshButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 20px;" +
            "-fx-cursor: hand;"
        ));
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(refreshButton);
        refreshButton.setOnAction(e -> viewModel.loadRestaurants());
        
        headerBox.getChildren().addAll(backButton, titleLabel, refreshButton);
        
        // Etichetta per i messaggi
        statusLabel.setFont(Font.font("Segoe UI", 14));
        statusLabel.setTextFill(Color.web(TEXT_GRAY));
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setVisible(false);
        
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(40, 40);
        loadingIndicator.setStyle("-fx-progress-color: " + PRIMARY_GREEN + ";");
        
        // Vista della lista con celle personalizzate
        restaurantsListView.setCellFactory(listView -> new RestaurantCell());
        restaurantsListView.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(restaurantsListView, Priority.ALWAYS);
        
        getChildren().addAll(headerBox, statusLabel, loadingIndicator, restaurantsListView);
        
        // Animazione di ingresso
        dev.theknife.app.util.AnimationUtils.slideInFromBottom(restaurantsListView, 600);
    }
    
    /**
     * Collega le property del ViewModel ai componenti UI.
     */
    private void bindViewModel() {
        restaurantsListView.setItems(viewModel.getRestaurants());
        loadingIndicator.progressProperty().bind(viewModel.loadProgressProperty());

        viewModel.loadingProperty().addListener((obs, wasLoading, isLoading) -> {
            loadingIndicator.setVisible(isLoading);
            if (isLoading) {
                dev.theknife.app.util.AnimationUtils.fadeIn(loadingIndicator);
            }
        });

        viewModel.statusMessageProperty().addListener((obs, oldMsg, newMsg) ->
            showStatus(newMsg, viewModel.isStatusError()));

        viewModel.screenStateProperty().addListener((obs, oldState, newState) ->
            updateUIForScreenState(newState));
    }

    /**
     * Aggiorna visibilità dei componenti in base allo stato del ViewModel.
     */
    private void updateUIForScreenState(RestaurantsScreenState state) {
        if (state == null) {
            return;
        }
        switch (state) {
            case LOADING -> restaurantsListView.setVisible(true);
            case LOADED_WITH_DATA -> restaurantsListView.setVisible(true);
            case LOGIN_REQUIRED, EMAIL_MISSING, LOADED_EMPTY, LOAD_ERROR ->
                restaurantsListView.setVisible(false);
            default -> { }
        }
    }
    /**
     * Mostra uno stato visivo (es. messaggio di errore o successo) per informare l'utente sull'operazione attuale.
     *
     * @param message Il messaggio da visualizzare.
     * @param isError true se è un messaggio di errore, false se è un messaggio di successo.
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle(isError ? 
            "-fx-text-fill: #dc3545; -fx-font-size: 14;" : 
            "-fx-text-fill: #28a745; -fx-font-size: 14;");
        statusLabel.setVisible(true);
    }
    /**
     * Crea la scena per la visualizzazione dei ristoranti.
     * <p>
     * La scena è composta da una lista di ristoranti e uno stato visivo
     * (es. messaggio di errore o successo) per informare l'utente sull'operazione attuale.
     * </p>
     *
     * @return La Scene configurata per questa vista.
     */
    public Scene createScene() {
        return App.createSceneWithModal(this, 1200, 800);
    }
    
    /**
     * Aggiorna la lista dei ristoranti visualizzata.
     * <p>
     * Questo metodo reimposta lo stato visivo e ricarica i ristoranti per l'utente corrente.
     * </p>
     */
    public void refresh() {
        viewModel.loadRestaurants();
    }
    
    /**
     * Cella personalizzata per la visualizzazione dei ristoranti con dettagli.
     */
    private class RestaurantCell extends ListCell<Restaurant> {
        /**
         * Aggiorna il contenuto della cella quando cambia l'elemento della lista.
         * <p>
         * Crea una card personalizzata per visualizzare i dettagli del ristorante.
         * </p>
         *
         * @param restaurant Il ristorante da visualizzare.
         * @param empty true se la cella è vuota, false altrimenti.
         */
        @Override
        protected void updateItem(Restaurant restaurant, boolean empty) {
            super.updateItem(restaurant, empty);
            
            if (empty || restaurant == null) {
                setGraphic(null);
                setText(null);
            } else {
                // Contenitore principale - Layout responsivo
                HBox cellContent = new HBox(15);
                cellContent.setAlignment(Pos.CENTER_LEFT);
                cellContent.setPadding(new Insets(15));
                cellContent.setStyle(
                    "-fx-background-color: " + BACKGROUND_WHITE + ";" +
                    "-fx-background-radius: 12px;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 2);" +
                    "-fx-border-color: " + BORDER_GRAY + ";" +
                    "-fx-border-width: 1px;" +
                    "-fx-border-radius: 12px;"
                );
                
                // Associa la larghezza alla larghezza della ListView per evitare lo scroll orizzontale
                cellContent.prefWidthProperty().bind(getListView().widthProperty().subtract(40));
                cellContent.setMaxWidth(Region.USE_PREF_SIZE);

                // --- Sezione Info a sinistra ---
                VBox infoBox = new VBox(8);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                
                // Nome del ristorante (grassetto, più grande)
                Label nameLabel = new Label(restaurant.getName());
                nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                nameLabel.setTextFill(Color.web(PRIMARY_GREEN));
                nameLabel.setWrapText(true);
                
                // Riga Dettagli 1: Location & Cuisine
                HBox detailsRow1 = new HBox(15);
                detailsRow1.setAlignment(Pos.CENTER_LEFT);
                
                // Posizione
                String location = (restaurant.getLocation() != null && !restaurant.getLocation().isEmpty()) 
                    ? restaurant.getLocation() 
                    : "Posizione non specificata";
                Label locationLabel = new Label("📍 " + location);
                locationLabel.setFont(Font.font("Segoe UI", 13));
                locationLabel.setStyle("-fx-text-fill: #6c757d;");
                
                // Cucina
                String cuisine = (restaurant.getCuisine() != null && !restaurant.getCuisine().isEmpty()) 
                    ? restaurant.getCuisine() 
                    : "Cucina non specificata";
                Label cuisineLabel = new Label("🍽️ " + cuisine);
                cuisineLabel.setFont(Font.font("Segoe UI", 13));
                cuisineLabel.setStyle("-fx-text-fill: #6c757d;");
                
                detailsRow1.getChildren().addAll(locationLabel, cuisineLabel);
                
                // Riga Dettagli 2: Award & Rating
                HBox detailsRow2 = new HBox(15);
                detailsRow2.setAlignment(Pos.CENTER_LEFT);
                
                // Award
                Label awardLabel = new Label();
                if (restaurant.getAward() != null && !restaurant.getAward().isEmpty()) {
                    String awardText = restaurant.getAward();
                     if (awardText.contains("Selected")) {
                         awardLabel.setText("Selezionato");
                         awardLabel.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11px;");
                     } else if (awardText.contains("Star")) {
                        String translatedAward = awardText.replace("Stars", "Stelle").replace("Star", "Stella");
                        awardLabel.setText(translatedAward);
                        awardLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11px;");
                    } else if (awardText.contains("Bib")) {
                        awardLabel.setText("Bib Gourmand");
                        awardLabel.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11px;");
                    } else {
                        awardLabel.setText(awardText);
                        awardLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11px;");
                    }
                } else {
                    awardLabel.setText("-");
                    awardLabel.setStyle("-fx-text-fill: #bdc3c7;");
                    awardLabel.setVisible(false);
                    awardLabel.setManaged(false);
                }
                
                // Valutazione
                double averageRating = viewModel.getAverageRating(restaurant.getName());
                int reviewCount = viewModel.getReviewCount(restaurant.getName());
                
                HBox ratingBox = new HBox(5);
                ratingBox.setAlignment(Pos.CENTER_LEFT);
                
                Label ratingLabel = new Label(String.format("%.1f", averageRating));
                ratingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                ratingLabel.setStyle("-fx-text-fill: #e67e22;");
                
                Label starLabel = new Label("★");
                starLabel.setFont(Font.font("Segoe UI", 14));
                starLabel.setStyle("-fx-text-fill: #e67e22;");
                
                Label countLabel = new Label("(" + reviewCount + ")");
                countLabel.setFont(Font.font("Segoe UI", 12));
                countLabel.setStyle("-fx-text-fill: #6c757d;");
                
                ratingBox.getChildren().addAll(ratingLabel, starLabel, countLabel);
                
                if (awardLabel.isVisible()) {
                    detailsRow2.getChildren().addAll(ratingBox, awardLabel);
                } else {
                    detailsRow2.getChildren().addAll(ratingBox);
                }
                
                infoBox.getChildren().addAll(nameLabel, detailsRow1, detailsRow2);
                HBox.setHgrow(infoBox, Priority.ALWAYS);
                
                // --- Sezione Azioni a destra ---
                Button viewDetailsButton = new Button("📝 Vedi Recensioni");
                viewDetailsButton.setPrefSize(140, 36);
                viewDetailsButton.setStyle(
                    "-fx-background-color: " + PRIMARY_GREEN + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 18px;" +
                    "-fx-cursor: hand;"
                );
                viewDetailsButton.setOnMouseEntered(e -> viewDetailsButton.setStyle(
                    "-fx-background-color: " + LIGHT_GREEN + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 18px;" +
                    "-fx-cursor: hand;"
                ));
                viewDetailsButton.setOnMouseExited(e -> viewDetailsButton.setStyle(
                    "-fx-background-color: " + PRIMARY_GREEN + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 18px;" +
                    "-fx-cursor: hand;"
                ));
                dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(viewDetailsButton);
                viewDetailsButton.setOnAction(e -> {
                    RestaurantDetailsView detailsView = new RestaurantDetailsView(container, sessionContext);
                    detailsView.setBackButtonAction(() -> {
                        MyRestaurantsView refreshedView = new MyRestaurantsView(primaryStage, homeScene, container, sessionContext);
                        primaryStage.setScene(refreshedView.createScene());
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
                    
                    detailsView.loadRestaurantDetails(restaurant.getName(), viewModel.getCurrentUserName());
                    
                    Scene detailsScene = App.createSceneWithModal(detailsView, 1000, 700);
                    primaryStage.setScene(detailsScene);
                });
                
                cellContent.getChildren().addAll(infoBox, viewDetailsButton);
                
                setGraphic(cellContent);
                setText(null);
                
                // Animazione della cella
                dev.theknife.app.util.AnimationUtils.slideInFromBottom(cellContent, 300, getIndex() * 50);
                dev.theknife.app.util.AnimationUtils.addHoverEffect(cellContent);
            }
        }
    }
    
    /**
     * Mostra il dialogo per aggiungere una nuova recensione.
     *
     * @param restaurant Il ristorante per cui aggiungere la recensione.
     * @param detailsView La vista dei dettagli del ristorante.
     */
    private void showAddReviewDialog(Restaurant restaurant, RestaurantDetailsView detailsView) {
        ReviewView reviewView = new ReviewView(container.get(IReviewService.class), sessionContext);
        
        // Configura la navigazione
        reviewView.setCancelButtonAction(() -> {
            // Vai indietro alla vista dei dettagli del ristorante
            showRestaurantDetails(restaurant);
        });
        
        // Configura la callback per il successo per aggiornare la vista dei dettagli
        reviewView.getViewModel().successMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                // Aggiorna la vista dei dettagli dopo il successo della submit
                javafx.application.Platform.runLater(() -> {
                    showRestaurantDetails(restaurant);
                });
            }
        });
        
        String userName = viewModel.getCurrentUserName();
        if (userName == null) {
            ModalManager.getInstance().showWarning(
                "Accesso Richiesto", 
                "Devi effettuare l'accesso per aggiungere una recensione"
            );
            return;
        }
        reviewView.initialize(restaurant.getName(), userName);
        
        // Crea la scena e mostra
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
    private void showEditReviewDialog(Restaurant restaurant, dev.theknife.app.model.Review review,
                                      RestaurantDetailsView detailsView) {
        ReviewView reviewView = new ReviewView(container.get(IReviewService.class), sessionContext);
        
        // Configura la navigazione
        reviewView.setCancelButtonAction(() -> {
            // Vai indietro alla vista dei dettagli del ristorante
            showRestaurantDetails(restaurant);
        });
        
        // Configura la callback per il successo per aggiornare la vista dei dettagli
        reviewView.getViewModel().successMessageProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                // Aggiorna la vista dei dettagli dopo il successo dell'aggiornamento
                javafx.application.Platform.runLater(() -> {
                    showRestaurantDetails(restaurant);
                });
            }
        });
        
        // Inizializza per la modifica della recensione
        reviewView.initializeForEdit(review);
        
        // Crea la scena e mostra
        Scene reviewScene = dev.theknife.app.App.createSceneWithModal(reviewView, 700, 600);
        primaryStage.setScene(reviewScene);
    }
    
    /**
     * Elimina una recensione dal sistema.
     *
     * @param review La recensione da eliminare.
     * @param detailsView La vista dei dettagli da aggiornare dopo l'eliminazione.
     */
    private void deleteReview(dev.theknife.app.model.Review review, RestaurantDetailsView detailsView) {
        if (viewModel.deleteReview(review)) {
            Platform.runLater(() -> {
                detailsView.refresh();
                detailsView.loadRestaurantDetails(review.getRestaurantName(), viewModel.getCurrentUserName());
            });
        } else {
            ModalManager.getInstance().showError("Errore", "Impossibile eliminare la recensione. Riprova.");
        }
    }
    
    /**
     * Mostra la vista dei dettagli di un ristorante.
     *
     * @param restaurant Il ristorante di cui mostrare i dettagli.
     */
    private void showRestaurantDetails(Restaurant restaurant) {
        RestaurantDetailsView detailsView = new RestaurantDetailsView(container, sessionContext);
        detailsView.setBackButtonAction(() -> {
            MyRestaurantsView refreshedView = new MyRestaurantsView(primaryStage, homeScene, container, sessionContext);
            primaryStage.setScene(refreshedView.createScene());
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
        
        detailsView.loadRestaurantDetails(restaurant.getName(), viewModel.getCurrentUserName());
        
        Scene detailsScene = App.createSceneWithModal(detailsView, 1000, 700);
        primaryStage.setScene(detailsScene);
    }
}
