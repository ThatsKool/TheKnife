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
import dev.theknife.app.service.IFavoriteService;
import dev.theknife.app.service.IRestaurantService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import dev.theknife.app.view.ReviewView;

import java.util.List;
import javafx.concurrent.Task;
import javafx.application.Platform;
import dev.theknife.app.util.AnimationUtils;

/**
 * View per la visualizzazione dei ristoranti preferiti dell'utente.
 * <p>
 * Questa classe gestisce la schermata che mostra l'elenco dei ristoranti contrassegnati
 * come preferiti dall'utente loggato. Utilizza una {@link ListView} personalizzata per
 * visualizzare le card dei ristoranti e permette la rimozione rapida dai preferiti.
 * </p>
 * <p>
 * Implementa il pattern MVVM interagendo direttamente con il {@link IFavoriteService}
 * per il recupero e la gestione dei dati.
 * </p>
 * <p>
 * <b>Funzionalità principali:</b>
 * <ul>
 *   <li>Visualizzazione lista preferiti con card personalizzate.</li>
 *   <li>Rimozione di un ristorante dai preferiti tramite pulsante dedicato.</li>
 *   <li>Navigazione ai dettagli del ristorante al click sulla card.</li>
 *   <li>Aggiornamento automatico della vista alla rimozione di un elemento.</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.service.IFavoriteService
 * @see dev.theknife.app.model.FavoriteRestaurant
 */
public class FavoriteRestaurantsView extends VBox {
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
     * Servizio per la gestione dei ristoranti preferiti.
     */
    private final IFavoriteService favoriteService;
    
    /**
     * Servizio per l'accesso ai dettagli dei ristoranti.
     */
    private final IRestaurantService restaurantService;
    
    /**
     * Servizio per l'accesso alle recensioni (utilizzato per calcolare la media voti).
     */
    private final dev.theknife.app.service.IReviewService reviewService;
    
    /**
     * Componente grafico per la lista dei preferiti.
     */
    private final ListView<Restaurant> favoritesListView;
    
    /**
     * Pulsante per tornare alla schermata precedente.
     */
    private final Button backButton;
    
    /**
     * Etichetta del titolo della pagina.
     */
    private final Label titleLabel;
    
    /**
     * Etichetta mostrata quando la lista dei preferiti è vuota.
     */
    private final Label emptyLabel;
    
    /**
     * Contenitore per l'indicatore di caricamento.
     */
    private VBox loadingBox;
    
    private Stage primaryStage;
    private Scene homeScene;
    private final DependencyContainer container;
    private final dev.theknife.app.session.SessionContext sessionContext;
    
    /**
     * Genera lo stile CSS per lo sfondo a pattern.
     * 
     * @return Stringa contenente le regole CSS per il background.
     */
    private String getPatternBackgroundStyle() {
        // Usa un colore di sfondo verde visibile ispirato a The Fork
        return "-fx-background-color: #C8E6C9;";
    }
    
    // COSTRUTTORI
    /**
     * Costruisce la vista dei ristoranti preferiti.
     * <p>
     * Inizializza i componenti grafici e avvia il caricamento asincrono dei preferiti.
     * </p>
     *
     * @param primaryStage Lo stage principale dell'applicazione.
     * @param homeScene La scena home per la navigazione "Indietro".
     * @param container Il container per l'iniezione delle dipendenze.
     * @param sessionContext Il contesto della sessione utente.
     */
    public FavoriteRestaurantsView(Stage primaryStage, Scene homeScene,
                                   DependencyContainer container,
                                   dev.theknife.app.session.SessionContext sessionContext) {
        this.primaryStage = primaryStage;
        this.homeScene = homeScene;
        this.container = container;
        this.sessionContext = sessionContext;
        this.favoriteService = container.get(IFavoriteService.class);
        this.restaurantService = container.get(IRestaurantService.class);
        this.reviewService = container.get(dev.theknife.app.service.IReviewService.class);
        
        this.favoritesListView = new ListView<>();
        this.backButton = new Button("← Indietro");
        this.titleLabel = new Label("I Miei Ristoranti Preferiti");
        this.emptyLabel = new Label("Nessun ristorante preferito. Aggiungine alcuni dai dettagli del ristorante!");
        
        setupUI();
        loadFavorites();
    }
    
    /**
     * Configura l'interfaccia utente della vista dei preferiti.
     */
    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(25));
        setStyle(getPatternBackgroundStyle());
        
        // Intestazione
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 20, 0));
        
        backButton.setPrefSize(120, 40);
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
        
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));
        dev.theknife.app.util.AnimationUtils.fadeIn(titleLabel, 800);
        
        headerBox.getChildren().addAll(backButton, titleLabel);
        
        // Etichetta vuota
        emptyLabel.setFont(Font.font("Segoe UI", 16));
        emptyLabel.setTextFill(Color.web(TEXT_GRAY));
        emptyLabel.setAlignment(Pos.CENTER);
        emptyLabel.setVisible(false);
        
        // Vista della lista
        favoritesListView.setCellFactory(listView -> new FavoriteRestaurantCell());
        favoritesListView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        
        // Contenitore per l'indicatore di caricamento
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);
        Label loadingLabel = new Label("Caricamento preferiti...");
        loadingLabel.setFont(Font.font("Segoe UI", 14));
        loadingLabel.setTextFill(Color.web(TEXT_GRAY));
        
        loadingBox = new VBox(15, loadingIndicator, loadingLabel);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setVisible(false);
        // Animazione di ingresso per il contenitore di caricamento
        AnimationUtils.fadeIn(loadingBox);
        
        // Contenitore per il contenuto
        StackPane contentStack = new StackPane(favoritesListView, emptyLabel, loadingBox);
        VBox.setVgrow(contentStack, Priority.ALWAYS);
        
        getChildren().addAll(headerBox, contentStack);
    }
    
    /**
     * Carica i ristoranti preferiti dell'utente corrente in modo asincrono.
     * <p>
     * Se l'utente non è loggato o è un ristoratore, mostra un messaggio appropriato.
     * </p>
     */
    private void loadFavorites() {
        if (sessionContext == null) return;
        if (sessionContext.isLoggedIn() && sessionContext.getCurrentUser() != null && 
            ("Restaurateur".equalsIgnoreCase(sessionContext.getCurrentUser().getRole()) || "Ristoratore".equalsIgnoreCase(sessionContext.getCurrentUser().getRole()))) {
            emptyLabel.setText("I Ristoratori non possono aggiungere ristoranti ai preferiti.");
            emptyLabel.setVisible(true);
            favoritesListView.setVisible(false);
            if (loadingBox != null) loadingBox.setVisible(false);
            return;
        }
        
        if (!sessionContext.isLoggedIn()) {
            emptyLabel.setText("Accedi per vedere i tuoi ristoranti preferiti.");
            emptyLabel.setVisible(true);
            favoritesListView.setVisible(false);
            if (loadingBox != null) loadingBox.setVisible(false);
            return;
        }
        
        if (loadingBox != null) {
            loadingBox.setVisible(true);
            AnimationUtils.fadeIn(loadingBox);
        }
        favoritesListView.setVisible(false);
        emptyLabel.setVisible(false);
        
        String userEmail = sessionContext.getCurrentUser() != null ? sessionContext.getCurrentUser().getEmail() : null;
        if (userEmail == null) {
            if (loadingBox != null) loadingBox.setVisible(false);
            emptyLabel.setText("Effettua l'accesso per vedere i tuoi preferiti.");
            emptyLabel.setVisible(true);
            favoritesListView.setVisible(false);
            return;
        }
        
        Task<javafx.collections.ObservableList<Restaurant>> task = new Task<>() {
            @Override
            protected javafx.collections.ObservableList<Restaurant> call() throws Exception {
                List<dev.theknife.app.model.FavoriteRestaurant> favorites = favoriteService.getUserFavoriteRestaurants(userEmail);
                javafx.collections.ObservableList<Restaurant> restaurants = javafx.collections.FXCollections.observableArrayList();
                if (favorites != null) {
                    for (dev.theknife.app.model.FavoriteRestaurant fav : favorites) {
                        if (fav.getRestaurantId() != null) {
                            Restaurant r = restaurantService.findRestaurantById(fav.getRestaurantId());
                            if (r != null) restaurants.add(r);
                        }
                    }
                }
                return restaurants;
            }
        };
        
        task.setOnSucceeded(e -> {
            if (loadingBox != null) loadingBox.setVisible(false);
            
            javafx.collections.ObservableList<Restaurant> result = task.getValue();
            if (result == null || result.isEmpty()) {
                emptyLabel.setText("Nessun ristorante preferito. Aggiungine alcuni dai dettagli del ristorante!");
                emptyLabel.setVisible(true);
                AnimationUtils.fadeIn(emptyLabel);
                favoritesListView.setVisible(false);
                favoritesListView.getItems().clear();
            } else {
                emptyLabel.setVisible(false);
                favoritesListView.setItems(result);
                favoritesListView.setVisible(true);
                // Animazione per l'apparizione della lista
                AnimationUtils.slideInFromBottom(favoritesListView, 600);
            }
        });
        
        task.setOnFailed(e -> {
            if (loadingBox != null) loadingBox.setVisible(false);
            emptyLabel.setText("Errore nel caricamento dei preferiti.");
            emptyLabel.setVisible(true);
            favoritesListView.setVisible(false);
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace();
        });
        
        new Thread(task).start();
    }
    
    /**
     * Cella personalizzata per la visualizzazione dei ristoranti preferiti.
     */
    private class FavoriteRestaurantCell extends ListCell<Restaurant> {
        /**
         * Aggiorna il contenuto della cella quando cambia l'elemento della lista.
         * <p>
         * Crea una card personalizzata per visualizzare i dettagli del ristorante preferito.
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
                
                // --- Colonna a sinistra: Info (Nome, Cucina, Posizione, Statistiche) ---
                VBox infoBox = new VBox(8);
                infoBox.setAlignment(Pos.CENTER_LEFT);
                
                // Nome del ristorante
                Label nameLabel = new Label(restaurant.getName());
                nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
                nameLabel.setStyle("-fx-text-fill: " + PRIMARY_GREEN + ";");
                nameLabel.setWrapText(true);
                
                // Cucina
                String cuisine = restaurant.getCuisine() != null && !restaurant.getCuisine().isEmpty() 
                    ? restaurant.getCuisine() 
                    : "Cucina non specificata";
                Label cuisineLabel = new Label("🍽️ " + cuisine);
                cuisineLabel.setFont(Font.font("Segoe UI", 13));
                cuisineLabel.setTextFill(Color.web(TEXT_GRAY));
                
                // Location
                String location = restaurant.getLocation() != null && !restaurant.getLocation().isEmpty() 
                    ? restaurant.getLocation() 
                    : "Posizione non specificata";
                Label locationLabel = new Label("📍 " + location);
                locationLabel.setFont(Font.font("Segoe UI", 13));
                locationLabel.setTextFill(Color.web(TEXT_GRAY));
                
                // --- Riga Statistiche (Numero di recensioni, Valutazione, Prezzo, Premi) ---
                // Usa FlowPane per permettere il wrapping se lo schermo è stretto
                FlowPane statsRow = new FlowPane();
                statsRow.setHgap(15);
                statsRow.setVgap(5);
                statsRow.setAlignment(Pos.CENTER_LEFT);
                
                // Numero di recensioni
                int reviewCount = reviewService.getReviewCount(restaurant.getName());
                Label countLabel = new Label(reviewCount + (reviewCount == 1 ? " recensione" : " recensioni"));
                countLabel.setFont(Font.font("Segoe UI", 13));
                countLabel.setStyle("-fx-text-fill: " + TEXT_GRAY + ";");
                
                // Valutazione (Valutazione dell'utente)
                double averageRating = reviewService.getAverageRating(restaurant.getName());
                Label ratingLabel = new Label(String.format("%.1f", averageRating));
                ratingLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
                ratingLabel.setStyle("-fx-text-fill: #e67e22;");
                
                Label starLabel = new Label("★");
                starLabel.setFont(Font.font("Segoe UI", 14));
                starLabel.setStyle("-fx-text-fill: #e67e22;");
                
                HBox ratingBox = new HBox(2);
                ratingBox.setAlignment(Pos.CENTER_LEFT);
                ratingBox.getChildren().addAll(ratingLabel, starLabel);
                
                // Prezzo
                Label priceLabel = null;
                if (restaurant.getPrice() != null && !restaurant.getPrice().isEmpty()) {
                    priceLabel = new Label(restaurant.getPrice());
                    priceLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10; -fx-font-size: 11px;");
                }
                
                // Awards (Michelin Stars etc)
                Label awardLabel = null;
                if (restaurant.getAward() != null && !restaurant.getAward().isEmpty() && !restaurant.getAward().equals("0")) {
                    String awardText = restaurant.getAward();
                    awardLabel = new Label();
                    
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
                }
                
                // Aggiungere alla Riga Statistiche
                statsRow.getChildren().add(countLabel);
                statsRow.getChildren().add(ratingBox);
                if (priceLabel != null) statsRow.getChildren().add(priceLabel);
                if (awardLabel != null) statsRow.getChildren().add(awardLabel);
                
                infoBox.getChildren().addAll(nameLabel, cuisineLabel, locationLabel, statsRow);
                HBox.setHgrow(infoBox, Priority.ALWAYS);
                
                // --- Colonna a destra: Azioni (Pulsanti) ---
                VBox actionBox = new VBox(10);
                actionBox.setAlignment(Pos.CENTER_RIGHT);
                actionBox.setMinWidth(130);
                
                Button viewButton = new Button("Vedi Dettagli");
                viewButton.setMaxWidth(Double.MAX_VALUE);
                viewButton.setPrefHeight(32);
                viewButton.setStyle(
                    "-fx-background-color: " + PRIMARY_GREEN + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 16px;" +
                    "-fx-cursor: hand;"
                );
                viewButton.setOnMouseEntered(e -> viewButton.setStyle(
                    "-fx-background-color: " + LIGHT_GREEN + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 16px;" +
                    "-fx-cursor: hand;"
                ));
                viewButton.setOnMouseExited(e -> viewButton.setStyle(
                    "-fx-background-color: " + PRIMARY_GREEN + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 16px;" +
                    "-fx-cursor: hand;"
                ));
                AnimationUtils.applyButtonHoverAnimation(viewButton);
                viewButton.setOnAction(e -> {
                    RestaurantDetailsView detailsView = new RestaurantDetailsView(container, sessionContext);
                    detailsView.setBackButtonAction(() -> {
                        primaryStage.setScene(getScene());
                    });
                    
                    detailsView.setAddReviewButtonAction(() -> {
                        showAddReviewDialog(restaurant, detailsView);
                    });
                    
                    detailsView.setEditReviewAction((review) -> {
                        showEditReviewDialog(restaurant, review, detailsView);
                    });
                    
                    detailsView.setDeleteReviewAction((review) -> {
                        deleteReview(review, detailsView);
                    });
                    
                    String userName = sessionContext != null ? sessionContext.getCurrentUserName() : null;
                    detailsView.loadRestaurantDetails(restaurant.getName(), userName);
                    
                    Scene detailsScene = App.createSceneWithModal(detailsView, 1000, 700);
                    primaryStage.setScene(detailsScene);
                });
                
                Button removeButton = new Button("Rimuovi");
                removeButton.setMaxWidth(Double.MAX_VALUE);
                removeButton.setPrefHeight(32);
                removeButton.setStyle(
                    "-fx-background-color: #D32F2F;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 16px;" +
                    "-fx-cursor: hand;"
                );
                removeButton.setOnMouseEntered(e -> removeButton.setStyle(
                    "-fx-background-color: #C62828;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 16px;" +
                    "-fx-cursor: hand;"
                ));
                removeButton.setOnMouseExited(e -> removeButton.setStyle(
                    "-fx-background-color: #D32F2F;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-weight: 600;" +
                    "-fx-font-size: 13px;" +
                    "-fx-background-radius: 16px;" +
                    "-fx-cursor: hand;"
                ));
                AnimationUtils.applyButtonHoverAnimation(removeButton);
                removeButton.setOnAction(e -> {
                    String userEmail = sessionContext != null && sessionContext.getCurrentUser() != null ? sessionContext.getCurrentUser().getEmail() : null;
                    if (userEmail != null && restaurant.getId() != null && favoriteService.removeFavorite(userEmail, restaurant.getId())) {
                        loadFavorites();
                    }
                });
                
                actionBox.getChildren().addAll(viewButton, removeButton);
                
                cellContent.getChildren().addAll(infoBox, actionBox);
                setGraphic(cellContent);
                setText(null);
                
                // Aggiungere animazioni
                AnimationUtils.fadeIn(cellContent);
                AnimationUtils.addHoverEffect(cellContent);
            }
        }
    }
    
    /**
     * Mostra il dialogo per aggiungere una nuova recensione.
     *
     * @param restaurant Il ristorante per cui aggiungere la recensione.
     * @param detailsView La vista dei dettagli del ristorante.
     */
    private void showAddReviewDialog(dev.theknife.app.model.Restaurant restaurant, RestaurantDetailsView detailsView) {
        ReviewView reviewView = new ReviewView(reviewService, sessionContext);
        
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
        
        String userName = sessionContext != null ? sessionContext.getCurrentUserName() : null;
        if (userName == null) {
            // Mostra un errore - l'utente deve essere loggato
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
    private void showEditReviewDialog(dev.theknife.app.model.Restaurant restaurant, dev.theknife.app.model.Review review, RestaurantDetailsView detailsView) {
        ReviewView reviewView = new ReviewView(reviewService, sessionContext);
        
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
            dev.theknife.app.util.Logger.getLogger(FavoriteRestaurantsView.class).error("Delete review failed", e);
            ModalManager.getInstance().showError("Errore", "Si è verificato un errore: " + e.getMessage());
        }
    }
    
    /**
     * Mostra la vista dei dettagli di un ristorante.
     *
     * @param restaurant Il ristorante di cui mostrare i dettagli.
     */
    private void showRestaurantDetails(dev.theknife.app.model.Restaurant restaurant) {
        RestaurantDetailsView detailsView = new RestaurantDetailsView(container, sessionContext);
        detailsView.setBackButtonAction(() -> {
            primaryStage.setScene(getScene());
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
        
        Scene detailsScene = App.createSceneWithModal(detailsView, 1000, 700);
        primaryStage.setScene(detailsScene);
    }
}

