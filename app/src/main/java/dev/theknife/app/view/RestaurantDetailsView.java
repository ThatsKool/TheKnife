/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.view;

import dev.theknife.app.container.DependencyContainer;
import dev.theknife.app.model.Review;
import dev.theknife.app.service.IRestaurantService;
import dev.theknife.app.service.IReviewService;
import dev.theknife.app.service.IUserService;
import dev.theknife.app.service.IFavoriteService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.viewmodel.RestaurantDetailsViewModel;
import dev.theknife.app.viewmodel.RestaurantDetailsViewModel.AddReviewPermission;
import dev.theknife.app.viewmodel.RestaurantDetailsViewModel.FavoriteButtonStyle;
import dev.theknife.app.viewmodel.RestaurantDetailsViewModel.FavoriteToggleResult;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Color;

/**
 * View per la visualizzazione dei dettagli del ristorante e delle relative recensioni.
 * <p>
 * Implementa il pattern MVVM (Model-View-ViewModel) garantendo una netta separazione delle responsabilità.
 * Questa classe agisce come "Code-Behind" della vista, occupandosi esclusivamente della definizione
 * del layout grafico, del binding con il {@link RestaurantDetailsViewModel} e della gestione degli eventi UI.
 * </p>
 * <p>
 * <b>Componenti UI principali:</b>
 * <ul>
 *   <li>Visualizzazione dettagliata (nome, indirizzo, cucina, prezzo, contatti).</li>
 *   <li>Lista scorrevole delle recensioni con celle personalizzate.</li>
 *   <li>Controlli interattivi (pulsante "Aggiungi Recensione", "Preferiti").</li>
 *   <li>Indicatori di caricamento asincrono e stato "Nessuna recensione".</li>
 * </ul>
 * </p>
 * <p>
 * Utilizza una palette di colori ispirata a "The Fork" (sfumature di verde) per una UI moderna e coerente.
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.viewmodel.RestaurantDetailsViewModel
 * @see dev.theknife.app.model.Restaurant
 */
public class RestaurantDetailsView extends VBox {
    // CAMPI
    private final RestaurantDetailsViewModel viewModel;
    private final Label restaurantNameLabel;
    private final Label addressLabel;
    private final Label locationLabel;
    private final Label priceLabel;
    private final Label cuisineLabel;
    private final Label phoneLabel;
    private final Label websiteLabel;
    private final Label awardLabel;
    private final TextArea descriptionArea;
    private final Label averageRatingLabel;
    private final Label reviewCountLabel;
    private final ListView<Review> reviewsListView;
    private final Button addReviewButton;
    private final Button favoriteButton;
    private final Button backButton;
    private final ProgressIndicator loadingIndicator;
    private final Label noReviewsLabel;
    private final Label ratingDistributionLabel;
    private String currentRestaurantName;

    // COSTRUTTORI
    /**
     * Costruisce la vista dei dettagli del ristorante.
     * <p>
     * Inizializza la view con i servizi necessari e configura l'interfaccia utente.
     * </p>
     *
     * @param container Il contenitore di dipendenze per il recupero dei servizi.
     * @param sessionContext Il contesto di sessione per l'accesso all'utente corrente.
     */
    public RestaurantDetailsView(DependencyContainer container, SessionContext sessionContext) {
        IRestaurantService restaurantService = container.get(IRestaurantService.class);
        IReviewService reviewService = container.get(IReviewService.class);
        IUserService userService = container.get(IUserService.class);
        IFavoriteService favoriteService = container.get(IFavoriteService.class);
        this.viewModel = new RestaurantDetailsViewModel(
            restaurantService, reviewService, userService, favoriteService, sessionContext);
        this.restaurantNameLabel = new Label();
        this.addressLabel = new Label();
        this.locationLabel = new Label();
        this.priceLabel = new Label();
        this.cuisineLabel = new Label();
        this.phoneLabel = new Label();
        this.websiteLabel = new Label();
        this.awardLabel = new Label();
        this.descriptionArea = new TextArea();
        this.averageRatingLabel = new Label();
        this.reviewCountLabel = new Label();
        this.reviewsListView = new ListView<>();
        this.addReviewButton = new Button("Aggiungi Recensione");
        this.favoriteButton = new Button("★ Aggiungi ai Preferiti");
        this.backButton = new Button("← Torna alla Lista");
        this.loadingIndicator = new ProgressIndicator();
        this.noReviewsLabel = new Label("Nessuna recensione. Scrivi la prima recensione!");
        this.ratingDistributionLabel = new Label();
        
        setupUI();
        bindProperties();
        setupEventHandlers();
    }
    
    // The Fork inspired color palette
    private static final String PRIMARY_GREEN = "#2E7D32";
    private static final String LIGHT_GREEN = "#4CAF50";
    private static final String BACKGROUND_WHITE = "#FFFFFF";
    private static final String BACKGROUND_LIGHT = "#F5F5F5";
    private static final String TEXT_DARK = "#212121";
    private static final String TEXT_GRAY = "#757575";
    private static final String BORDER_GRAY = "#E0E0E0";
    
    /**
     * Genera lo stile CSS per il background con pattern sottile.
     *
     * @return Una stringa contenente le direttive CSS per il background-color.
     */
    private String getPatternBackgroundStyle() {
        // Use a visible green background color inspired by The Fork
        return "-fx-background-color: #C8E6C9;";
    }
    
    // METODI
    /**
     * Configura i componenti dell'interfaccia utente.
     * <p>
     * Definisce il layout principale (Header, Dettagli, Recensioni), applica gli stili CSS
     * e assembla la gerarchia dei nodi JavaFX.
     * </p>
     */
    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(25));
        setStyle(getPatternBackgroundStyle());
        
        // Header with back button and favorite button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);
        backButton.setPrefSize(140, 40);
        backButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;"
        );
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(backButton);
        
        favoriteButton.setPrefSize(180, 40);
        favoriteButton.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: " + PRIMARY_GREEN + ";" +
            "-fx-padding: 8 16;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 20px;" +
            "-fx-border-color: " + PRIMARY_GREEN + ";" +
            "-fx-border-width: 2px;" +
            "-fx-border-radius: 20px;" +
            "-fx-cursor: hand;"
        );
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(favoriteButton);
        
        headerBox.getChildren().addAll(backButton, favoriteButton);
        
        // Restaurant name (large title)
        restaurantNameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        restaurantNameLabel.setTextFill(Color.web(PRIMARY_GREEN));
        dev.theknife.app.util.AnimationUtils.fadeIn(restaurantNameLabel, 800);
        
        // Main content area
        HBox mainContent = new HBox();
        mainContent.setSpacing(30);
        mainContent.setAlignment(Pos.TOP_LEFT);
        
        // Left panel - Restaurant details
        VBox detailsPanel = createDetailsPanel();
        detailsPanel.setPrefWidth(400);
        dev.theknife.app.util.AnimationUtils.slideInFromLeft(detailsPanel, 600);
        
        // Right panel - Reviews
        VBox reviewsPanel = createReviewsPanel();
        reviewsPanel.setPrefWidth(500);
        dev.theknife.app.util.AnimationUtils.slideInFromRight(reviewsPanel, 600);
        
        mainContent.getChildren().addAll(detailsPanel, reviewsPanel);
        
        // Loading indicator
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(50, 50);
        
        getChildren().addAll(headerBox, restaurantNameLabel, mainContent, loadingIndicator);
    }
    
    /**
     * Crea il pannello contenente i dettagli informativi del ristorante.
     *
     * @return Un {@link VBox} configurato con i campi di dettaglio (indirizzo, contatti, ecc.).
     */
    private VBox createDetailsPanel() {
        VBox panel = new VBox();
        panel.setSpacing(15);
        panel.setPadding(new Insets(20));
        panel.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 2);" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );
        
        // Section title
        Label detailsTitle = new Label("Dettagli Ristorante");
        detailsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        detailsTitle.setTextFill(Color.web(PRIMARY_GREEN));
        
        // Details grid
        VBox detailsGrid = new VBox();
        detailsGrid.setSpacing(10);
        
        HBox row1 = createDetailRow("Indirizzo:", addressLabel);
        HBox row2 = createDetailRow("Posizione:", locationLabel);
        HBox row3 = createDetailRow("Fascia Prezzo:", priceLabel);
        HBox row4 = createDetailRow("Cucina:", cuisineLabel);
        HBox row5 = createDetailRow("Telefono:", phoneLabel);
        HBox row6 = createDetailRow("Sito Web:", websiteLabel);
        HBox row7 = createDetailRow("Premio:", awardLabel);
        
        detailsGrid.getChildren().addAll(row1, row2, row3, row4, row5, row6, row7);
        
        // Cascading animation for details rows
        dev.theknife.app.util.AnimationUtils.slideInFromLeft(row1, 400, 100);
        dev.theknife.app.util.AnimationUtils.slideInFromLeft(row2, 400, 150);
        dev.theknife.app.util.AnimationUtils.slideInFromLeft(row3, 400, 200);
        dev.theknife.app.util.AnimationUtils.slideInFromLeft(row4, 400, 250);
        dev.theknife.app.util.AnimationUtils.slideInFromLeft(row5, 400, 300);
        dev.theknife.app.util.AnimationUtils.slideInFromLeft(row6, 400, 350);
        dev.theknife.app.util.AnimationUtils.slideInFromLeft(row7, 400, 400);
        
        // Description section
        Label descTitle = new Label("Descrizione:");
        descTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        descTitle.setStyle("-fx-text-fill: #2c3e50;");
        dev.theknife.app.util.AnimationUtils.slideInFromLeft(descTitle, 400, 450);
        
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        dev.theknife.app.util.AnimationUtils.slideInFromLeft(descriptionArea, 400, 500);
        
        panel.getChildren().addAll(detailsTitle, detailsGrid, descTitle, descriptionArea);
        
        return panel;
    }
    
    /**
     * Crea il pannello dedicato alla visualizzazione e gestione delle recensioni.
     *
     * @return Un {@link VBox} contenente l'header delle recensioni, le statistiche e la lista.
     */
    private VBox createReviewsPanel() {
        VBox panel = new VBox();
        panel.setSpacing(15);
        panel.setPadding(new Insets(20));
        panel.setStyle(
            "-fx-background-color: " + BACKGROUND_WHITE + ";" +
            "-fx-background-radius: 12px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 12, 0, 0, 2);" +
            "-fx-border-color: " + BORDER_GRAY + ";" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 12px;"
        );
        
        // Reviews header
        HBox reviewsHeader = new HBox();
        reviewsHeader.setAlignment(Pos.CENTER_LEFT);
        reviewsHeader.setSpacing(10);
        
        Label reviewsTitle = new Label("Recensioni");
        reviewsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        reviewsTitle.setTextFill(Color.web(PRIMARY_GREEN));
        
        // Rating summary
        HBox ratingSummary = new HBox();
        ratingSummary.setSpacing(10);
        ratingSummary.setAlignment(Pos.CENTER_LEFT);
        
        averageRatingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        averageRatingLabel.setStyle("-fx-text-fill: #e67e22;");
        
        reviewCountLabel.setFont(Font.font("Arial", 14));
        reviewCountLabel.setStyle("-fx-text-fill: #6c757d;");
        
        ratingSummary.getChildren().addAll(averageRatingLabel, reviewCountLabel);
        
        reviewsHeader.getChildren().addAll(reviewsTitle, ratingSummary);
        
        // Add review button
        addReviewButton.setPrefSize(180, 40);
        addReviewButton.setStyle(
            "-fx-background-color: " + PRIMARY_GREEN + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: 600;" +
            "-fx-font-size: 14px;" +
            "-fx-background-radius: 20px;" +
            "-fx-cursor: hand;"
        );
        dev.theknife.app.util.AnimationUtils.applyButtonHoverAnimation(addReviewButton);
        
        // Rating distribution
        ratingDistributionLabel.setFont(Font.font("Arial", 12));
        ratingDistributionLabel.setStyle("-fx-text-fill: #6c757d;");
        ratingDistributionLabel.setWrapText(true);
        
        // Reviews list with edit/delete functionality
        reviewsListView.setCellFactory(listView -> new ReviewListCell(
            viewModel,
            this::onEditReview,
            this::onDeleteReview,
            this::onRestaurateurRespond,
            this::onClientRespond,
            () -> editReviewAction != null,
            () -> deleteReviewAction != null
        ));
        reviewsListView.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        
        // No reviews label
        noReviewsLabel.setFont(Font.font("Arial", 14));
        noReviewsLabel.setStyle("-fx-text-fill: #6c757d; -fx-text-alignment: center;");
        noReviewsLabel.setAlignment(Pos.CENTER);
        noReviewsLabel.setVisible(false);
        
        panel.getChildren().addAll(reviewsHeader, addReviewButton, ratingDistributionLabel, reviewsListView, noReviewsLabel);
        
        return panel;
    }
    
    /**
     * Crea una riga di dettaglio standardizzata (Etichetta + Valore).
     *
     * @param labelText Il testo dell'etichetta (es. "Indirizzo:").
     * @param valueLabel Il controllo Label che conterrà il valore (bindabile).
     * @return Un {@link HBox} che rappresenta la riga di dettaglio.
     */
    private HBox createDetailRow(String labelText, Label valueLabel) {
        HBox row = new HBox();
        row.setSpacing(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: #495057;");
        label.setMinWidth(80);
        
        valueLabel.setFont(Font.font("Arial", 12));
        valueLabel.setStyle("-fx-text-fill: #212529;");
        valueLabel.setWrapText(true);
        
        row.getChildren().addAll(label, valueLabel);
        return row;
    }
    
    /**
     * Bind properties to ViewModel
     */
    private void bindProperties() {
        // Restaurant details
        restaurantNameLabel.textProperty().bind(viewModel.restaurantNameProperty());
        addressLabel.textProperty().bind(viewModel.restaurantAddressProperty());
        locationLabel.textProperty().bind(viewModel.restaurantLocationProperty());
        priceLabel.textProperty().bind(viewModel.restaurantPriceProperty());
        cuisineLabel.textProperty().bind(viewModel.restaurantCuisineProperty());
        phoneLabel.textProperty().bind(viewModel.restaurantPhoneProperty());
        websiteLabel.textProperty().bind(viewModel.restaurantWebsiteProperty());
        awardLabel.textProperty().bind(viewModel.restaurantAwardProperty());
        descriptionArea.textProperty().bind(viewModel.restaurantDescriptionProperty());
        
        // Review statistics
        averageRatingLabel.textProperty().bind(viewModel.averageRatingProperty().concat("★"));
        reviewCountLabel.textProperty().bind(viewModel.reviewCountProperty().concat(" recensioni"));
        
        // Reviews list
        reviewsListView.setItems(viewModel.getReviews());
        
        // Loading state
        loadingIndicator.visibleProperty().bind(viewModel.isLoadingProperty());
        
        // Reviews visibility
        reviewsListView.visibleProperty().bind(viewModel.hasReviewsProperty());
        noReviewsLabel.visibleProperty().bind(viewModel.hasReviewsProperty().not());

        ratingDistributionLabel.textProperty().bind(viewModel.ratingDistributionTextProperty());

        addReviewButton.disableProperty().bind(viewModel.addReviewButtonDisabledProperty());
        addReviewButton.textProperty().bind(viewModel.addReviewButtonTextProperty());
        viewModel.addReviewButtonTooltipProperty().addListener((obs, oldTip, newTip) -> {
            if (newTip == null || newTip.isEmpty()) {
                addReviewButton.setTooltip(null);
            } else {
                addReviewButton.setTooltip(new Tooltip(newTip));
            }
            applyAddReviewButtonStyle();
        });
        viewModel.addReviewButtonTextProperty().addListener((obs, oldText, newText) ->
            applyAddReviewButtonStyle());

        favoriteButton.visibleProperty().bind(viewModel.favoriteButtonVisibleProperty());
        favoriteButton.disableProperty().bind(viewModel.favoriteButtonDisabledProperty());
        favoriteButton.textProperty().bind(viewModel.favoriteButtonTextProperty());
        viewModel.favoriteButtonStyleProperty().addListener((obs, oldStyle, newStyle) ->
            applyFavoriteButtonStyle(newStyle));

        viewModel.canAddReviewProperty().addListener((obs, oldVal, newVal) ->
            viewModel.refreshAddReviewButtonState());
    }

    private void applyAddReviewButtonStyle() {
        if (viewModel.isRestaurateur()) {
            addReviewButton.setStyle(
                "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-cursor: not-allowed;");
        } else {
            addReviewButton.setStyle(
                "-fx-background-color: " + PRIMARY_GREEN + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-weight: 600;" +
                "-fx-font-size: 14px;" +
                "-fx-background-radius: 20px;" +
                "-fx-cursor: hand;"
            );
        }
    }

    private void applyFavoriteButtonStyle(FavoriteButtonStyle style) {
        if (style == FavoriteButtonStyle.REMOVE) {
            favoriteButton.setStyle(
                "-fx-background-color: #dc3545; -fx-text-fill: white; -fx-padding: 8 16; -fx-font-weight: bold;");
        } else {
            favoriteButton.setStyle(
                "-fx-background-color: #ffc107; -fx-text-fill: #212529; -fx-padding: 8 16; -fx-font-weight: bold;");
        }
    }
    
    /**
     * Configura i gestori degli eventi (listeners) per le interazioni utente.
     * <p>
     * Mappa i click sui pulsanti (Back, Add Review, Favorite) alle rispettive azioni.
     * </p>
     */
    private void setupEventHandlers() {
        backButton.setOnAction(e -> onBackToRestaurantList());
        addReviewButton.setOnAction(e -> onAddReview());
        favoriteButton.setOnAction(e -> onToggleFavorite());
    }

    /**
     * Load restaurant details
     */
    public void loadRestaurantDetails(String restaurantName, String userName) {
        this.currentRestaurantName = restaurantName;
        viewModel.loadRestaurantDetails(restaurantName, userName);
        Platform.runLater(() -> {
            viewModel.refreshAddReviewButtonState();
            viewModel.refreshFavoriteButtonState();
            applyFavoriteButtonStyle(viewModel.favoriteButtonStyleProperty().get());
            applyAddReviewButtonStyle();
        });
    }

    private void onToggleFavorite() {
        FavoriteToggleResult result = viewModel.toggleFavorite();
        switch (result) {
            case RESTAURATEUR_BLOCKED -> ModalManager.getInstance().showWarning(
                "Non Consentito",
                "I Ristoratori non possono aggiungere ristoranti ai preferiti\n"
                    + "Solo i clienti possono aggiungere ristoranti alla loro lista preferiti."
            );
            case FAILED -> ModalManager.getInstance().showError(
                "Errore",
                "Impossibile aggiornare i preferiti\n"
                    + "Si è verificato un errore durante l'aggiornamento dei preferiti. Riprova."
            );
            default -> { }
        }
    }
    
    /**
     * Handle back button click
     */
    private void onBackToRestaurantList() {
        // The back button action is set externally via setBackButtonAction
        // This method is called by the default back button handler
        if (backButton.getOnAction() != null) {
            backButton.getOnAction().handle(null);
        }
    }
    
    /**
     * Handle add review button click
     */
    private void onAddReview() {
        if (!checkAddReviewPermissionAndWarn()) {
            return;
        }
        if (addReviewButtonAction != null) {
            addReviewButtonAction.run();
        }
    }

    private boolean checkAddReviewPermissionAndWarn() {
        return switch (viewModel.checkAddReviewPermission()) {
            case NOT_LOGGED_IN -> {
                ModalManager.getInstance().showWarning(
                    "Login Richiesto",
                    "Devi essere loggato per aggiungere una recensione\n"
                        + "Accedi o registrati per aggiungere una recensione."
                );
                yield false;
            }
            case RESTAURATEUR_BLOCKED -> {
                ModalManager.getInstance().showWarning(
                    "Azione Non Consentita",
                    "I Ristoratori non possono aggiungere recensioni.\n"
                        + "L'unica azione consentita è rispondere alle recensioni dei propri ristoranti."
                );
                yield false;
            }
            case ALLOWED -> true;
        };
    }
    
    private Runnable addReviewButtonAction;
    
    /**
     * Set back button action
     */
    public void setBackButtonAction(Runnable action) {
        backButton.setOnAction(e -> action.run());
    }
    
    /**
     * Set add review button action
     */
    public void setAddReviewButtonAction(Runnable action) {
        this.addReviewButtonAction = action;
        addReviewButton.setOnAction(e -> {
            if (!checkAddReviewPermissionAndWarn()) {
                return;
            }
            if (action != null) {
                action.run();
            }
        });
        viewModel.refreshAddReviewButtonState();
        applyAddReviewButtonStyle();
    }

    /**
     * Aggiorna la vista ricaricando le recensioni dal ViewModel.
     */
    public void refresh() {
        viewModel.refreshReviews();
    }
    
    /**
     * Gestisce l'azione di modifica di una recensione.
     *
     * @param review La recensione da modificare.
     */
    private void onEditReview(Review review) {
        if (editReviewAction != null) {
            editReviewAction.accept(review);
        }
    }
    
    /**
     * Gestisce l'azione di eliminazione di una recensione.
     * <p>
     * Mostra un dialogo di conferma prima di procedere con l'eliminazione.
     * </p>
     *
     * @param review La recensione da eliminare.
     */
    private void onDeleteReview(Review review) {
        ModalManager.getInstance().showConfirmation(
            "Elimina Recensione",
            "Sei sicuro di voler eliminare questa recensione? Questa azione non può essere annullata.",
            () -> {
                if (deleteReviewAction != null) {
                    deleteReviewAction.accept(review);
                }
            }
        );
    }
    
    private java.util.function.Consumer<Review> editReviewAction;
    private java.util.function.Consumer<Review> deleteReviewAction;
    
    /**
     * Imposta il callback per l'azione di modifica recensione.
     *
     * @param action Il consumer che gestisce la modifica della recensione.
     */
    public void setEditReviewAction(java.util.function.Consumer<Review> action) {
        this.editReviewAction = action;
    }
    
    /**
     * Imposta il callback per l'azione di eliminazione recensione.
     *
     * @param action Il consumer che gestisce l'eliminazione della recensione.
     */
    public void setDeleteReviewAction(java.util.function.Consumer<Review> action) {
        this.deleteReviewAction = action;
    }
    
    /**
     * Handle restaurateur response to a review
     */
    private void onRestaurateurRespond(Review review) {
        ModalManager.getInstance().showTextAreaDialog(
            "Rispondi alla Recensione",
            "Rispondi alla recensione di " + viewModel.getReviewAuthorDisplayName(review),
            "La tua risposta...",
            response -> {
                // Validation: Length and Format
                if (response == null || response.trim().length() < 10) {
                    ModalManager.getInstance().showError(
                        "Risposta Troppo Breve", 
                        "La risposta deve contenere almeno 10 caratteri."
                    );
                    return;
                }
                
                if (response.trim().length() > 1000) {
                    ModalManager.getInstance().showError(
                        "Risposta Troppo Lunga", 
                        "La risposta non può superare i 1000 caratteri."
                    );
                    return;
                }

                ModalManager.getInstance().showLoading("Invio risposta in corso...");
                
                new Thread(() -> {
                    try {
                        boolean success = viewModel.addRestaurateurResponse(review.getId(), response.trim());
                        Platform.runLater(() -> {
                            ModalManager.getInstance().close();
                            if (success) {
                                ModalManager.getInstance().showInfo("Successo", "Risposta aggiunta con successo.");
                                viewModel.refreshReviews();
                            } else {
                                ModalManager.getInstance().showError("Errore", "Impossibile aggiungere la risposta. Verifica di essere il proprietario del ristorante.");
                            }
                        });
                    } catch (Exception e) {
                        dev.theknife.app.util.Logger.getLogger(RestaurantDetailsView.class).error("Add restaurateur response failed", e);
                        Platform.runLater(() -> {
                            ModalManager.getInstance().close();
                            ModalManager.getInstance().showError("Errore", "Si è verificato un errore: " + e.getMessage());
                        });
                    }
                }).start();
            }
        );
    }
    
    /**
     * Handle client response to restaurateur's response
     */
    private void onClientRespond(Review review) {
        ModalManager.getInstance().showTextAreaDialog(
            "Rispondi al Ristorante",
            "Rispondi alla risposta del ristorante",
            "La tua risposta...",
            response -> {
                ModalManager.getInstance().showLoading("Invio risposta in corso...");
                
                new Thread(() -> {
                    try {
                        boolean success = viewModel.addClientResponse(review.getId(), response.trim());
                        Platform.runLater(() -> {
                            ModalManager.getInstance().close();
                            if (success) {
                                ModalManager.getInstance().showInfo("Successo", "Risposta aggiunta con successo.");
                                viewModel.refreshReviews();
                            } else {
                                ModalManager.getInstance().showError("Errore", "Impossibile aggiungere la risposta. Riprova.");
                            }
                        });
                    } catch (Exception e) {
                        dev.theknife.app.util.Logger.getLogger(RestaurantDetailsView.class).error("Add client response failed", e);
                        Platform.runLater(() -> {
                            ModalManager.getInstance().close();
                            ModalManager.getInstance().showError("Errore", "Si è verificato un errore: " + e.getMessage());
                        });
                    }
                }).start();
            }
        );
    }
}
