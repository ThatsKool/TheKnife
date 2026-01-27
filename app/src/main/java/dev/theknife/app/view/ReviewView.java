/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.view;

import dev.theknife.app.service.IReviewService;
import dev.theknife.app.session.SessionContext;
import dev.theknife.app.viewmodel.ReviewViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Color;
import dev.theknife.app.util.Logger;

/**
 * View per la creazione e modifica delle recensioni.
 * <p>
 * Questa classe implementa l'interfaccia utente per il form di recensione,
 * permettendo all'utente di assegnare un voto (stelle) e scrivere un commento.
 * Segue il pattern MVVM per la validazione e la sottomissione dei dati.
 * </p>
 * <p>
 * <b>Funzionalità:</b>
 * <ul>
 *   <li>Rating interattivo a stelle con effetti hover.</li>
 *   <li>Validazione in tempo reale della lunghezza del commento.</li>
 *   <li>Gestione dello stato di caricamento e degli errori.</li>
 *   <li>Supporto per la modalità "Nuova Recensione" e "Modifica".</li>
 * </ul>
 * </p>
 *
 * @author Federico Barbotti, Oittijo Ahemmed Sarkar, Bennajim Alì
 * @version 1.0
 * @since 1.0
 * @see dev.theknife.app.viewmodel.ReviewViewModel
 * @see dev.theknife.app.model.Review
 */
public class ReviewView extends VBox {
    // CAMPI
    private final Logger logger = Logger.getLogger(ReviewView.class);
    private final ReviewViewModel viewModel;
    private final Label restaurantNameLabel;
    private final Label userNameLabel;
    private final HBox starRatingBox;
    private final Label[] starLabels;
    private final TextArea commentArea;
    private final Label commentLengthLabel;
    private final Button submitButton;
    private final Button cancelButton;
    private final Label errorLabel;
    private final Label successLabel;
    private final ProgressIndicator loadingIndicator;

    // COSTRUTTORI
    /**
     * Costruttore della ReviewView.
     * <p>
     * Inizializza la view con i servizi necessari e configura l'interfaccia utente.
     * </p>
     *
     * @param reviewService Il servizio per la gestione delle recensioni.
     * @param sessionContext Il contesto di sessione per l'accesso all'utente corrente.
     */
    public ReviewView(IReviewService reviewService, SessionContext sessionContext) {
        this.viewModel = new ReviewViewModel(reviewService, sessionContext);
        this.restaurantNameLabel = new Label();
        this.userNameLabel = new Label();
        this.starRatingBox = new HBox();
        this.starLabels = new Label[5];
        this.commentArea = new TextArea();
        this.commentLengthLabel = new Label();
        this.submitButton = new Button("Invia Recensione");
        this.cancelButton = new Button("Cancel");
        this.errorLabel = new Label();
        this.successLabel = new Label();
        this.loadingIndicator = new ProgressIndicator();
        
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
     * Configura e assembla i componenti dell'interfaccia utente.
     * <p>
     * Definisce il layout del container principale, applica ombre ed effetti grafici,
     * e organizza le sezioni (Titolo, Rating, Commento, Pulsanti).
     * </p>
     */
    private void setupUI() {
        setSpacing(20);
        setPadding(new Insets(30));
        setStyle(getPatternBackgroundStyle());
        setMaxWidth(600);
        setAlignment(Pos.CENTER);
        
        // Main container with shadow
        VBox mainContainer = new VBox();
        mainContainer.setSpacing(20);
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
        Label titleLabel = new Label("Scrivi una Recensione");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));
        titleLabel.setAlignment(Pos.CENTER);
        
        // Restaurant and user info
        VBox infoBox = new VBox();
        infoBox.setSpacing(5);
        infoBox.setAlignment(Pos.CENTER);
        
        restaurantNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        restaurantNameLabel.setStyle("-fx-text-fill: #e67e22;");
        restaurantNameLabel.setAlignment(Pos.CENTER);
        
        userNameLabel.setFont(Font.font("Arial", 14));
        userNameLabel.setStyle("-fx-text-fill: #6c757d;");
        userNameLabel.setAlignment(Pos.CENTER);
        
        infoBox.getChildren().addAll(restaurantNameLabel, userNameLabel);
        
        // Rating section
        VBox ratingSection = createRatingSection();
        
        // Comment section
        VBox commentSection = createCommentSection();
        
        // Buttons
        HBox buttonBox = createButtonBox();
        
        // Status messages
        VBox statusBox = createStatusBox();
        
        mainContainer.getChildren().addAll(
            titleLabel, infoBox, ratingSection, commentSection, buttonBox, statusBox
        );
        
        getChildren().add(mainContainer);
    }
    
    /**
     * Crea la sezione di valutazione con le stelle interattive.
     * <p>
     * Le stelle reagiscono al passaggio del mouse (hover) e al click per impostare il voto.
     * </p>
     *
     * @return Un {@link VBox} contenente il selettore del voto.
     */
    private VBox createRatingSection() {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setAlignment(Pos.CENTER);
        
        Label ratingLabel = new Label("Voto:");
        ratingLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        ratingLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        starRatingBox.setSpacing(5);
        starRatingBox.setAlignment(Pos.CENTER);
        
        // Create star labels
        for (int i = 0; i < 5; i++) {
            final int starIndex = i;
            Label starLabel = new Label("☆");
            starLabel.setFont(Font.font("Arial", 32));
            starLabel.setStyle("-fx-text-fill: #dee2e6; -fx-cursor: hand;");
            
            // Add hover effects
            starLabel.setOnMouseEntered(e -> {
                for (int j = 0; j <= starIndex; j++) {
                    starLabels[j].setText("★");
                    starLabels[j].setStyle("-fx-text-fill: #e67e22; -fx-cursor: hand;");
                }
            });
            
            starLabel.setOnMouseExited(e -> {
                updateStarDisplay();
            });
            
            starLabel.setOnMouseClicked(e -> {
                viewModel.setRating(starIndex + 1);
                updateStarDisplay();
            });
            
            starLabels[i] = starLabel;
            starRatingBox.getChildren().add(starLabel);
        }
        
        section.getChildren().addAll(ratingLabel, starRatingBox);
        return section;
    }
    
    /**
     * Crea la sezione per l'inserimento del commento testuale.
     * <p>
     * Include l'area di testo e il contatore dei caratteri.
     * </p>
     *
     * @return Un {@link VBox} contenente l'area di input del commento.
     */
    private VBox createCommentSection() {
        VBox section = new VBox();
        section.setSpacing(10);
        section.setAlignment(Pos.CENTER_LEFT);
        
        Label commentLabel = new Label("Commento:");
        commentLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        commentLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        commentArea.setPromptText("Condividi la tua esperienza in questo ristorante...");
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(5);
        commentArea.setMaxWidth(500);
        commentArea.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-radius: 8; -fx-padding: 10;");
        
        HBox commentFooter = new HBox();
        commentFooter.setAlignment(Pos.CENTER_RIGHT);
        commentLengthLabel.setFont(Font.font("Arial", 12));
        commentLengthLabel.setStyle("-fx-text-fill: #6c757d;");
        commentFooter.getChildren().add(commentLengthLabel);
        
        section.getChildren().addAll(commentLabel, commentArea, commentFooter);
        return section;
    }
    
    /**
     * Crea la barra dei pulsanti (Invia, Annulla).
     *
     * @return Un {@link HBox} con i pulsanti di azione.
     */
    private HBox createButtonBox() {
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(15);
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
        
        submitButton.setText("Invia Recensione");
        cancelButton.setText("Annulla");
        
        buttonBox.getChildren().addAll(submitButton, cancelButton);
        return buttonBox;
    }
    
    /**
     * Crea l'area per la visualizzazione dei messaggi di stato (errori, successo, loading).
     *
     * @return Un {@link VBox} per i feedback all'utente.
     */
    private VBox createStatusBox() {
        VBox statusBox = new VBox();
        statusBox.setSpacing(10);
        statusBox.setAlignment(Pos.CENTER);
        
        errorLabel.setFont(Font.font("Arial", 12));
        errorLabel.setStyle("-fx-text-fill: #dc3545; -fx-text-alignment: center;");
        errorLabel.setAlignment(Pos.CENTER);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(500);
        
        successLabel.setFont(Font.font("Arial", 12));
        successLabel.setStyle("-fx-text-fill: #28a745; -fx-text-alignment: center;");
        successLabel.setAlignment(Pos.CENTER);
        successLabel.setWrapText(true);
        successLabel.setMaxWidth(500);
        
        loadingIndicator.setMaxSize(30, 30);
        loadingIndicator.setVisible(false);
        
        statusBox.getChildren().addAll(errorLabel, successLabel, loadingIndicator);
        return statusBox;
    }
    
    /**
     * Aggiorna visivamente le stelle in base al voto corrente.
     * <p>
     * Colora le stelle fino al voto selezionato e lascia vuote le altre.
     * </p>
     */
    private void updateStarDisplay() {
        int currentRating = viewModel.getRating();
        for (int i = 0; i < 5; i++) {
            if (i < currentRating) {
                starLabels[i].setText("★");
                starLabels[i].setStyle("-fx-text-fill: #e67e22; -fx-cursor: hand;");
            } else {
                starLabels[i].setText("☆");
                starLabels[i].setStyle("-fx-text-fill: #dee2e6; -fx-cursor: hand;");
            }
        }
    }
    
    /**
     * Collega le proprietà del ViewModel ai componenti della View.
     * <p>
     * Gestisce il binding bidirezionale per i campi di input e unidirezionale per
     * lo stato di abilitazione dei pulsanti e i messaggi di errore.
     * </p>
     */
    private void bindProperties() {
        // Restaurant and user info
        restaurantNameLabel.textProperty().bind(viewModel.restaurantNameProperty());
        userNameLabel.textProperty().bind(viewModel.userNameProperty());
        
        // Comment
        commentArea.textProperty().bindBidirectional(viewModel.commentProperty());
        commentLengthLabel.textProperty().bind(viewModel.commentLengthStatusProperty());
        
        // Button states
        submitButton.disableProperty().bind(viewModel.isValidProperty().not().or(viewModel.isSubmittingProperty()));
        cancelButton.disableProperty().bind(viewModel.isSubmittingProperty());
        
        // Status messages
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        errorLabel.visibleProperty().bind(viewModel.errorMessageProperty().isNotEmpty());
        successLabel.textProperty().bind(viewModel.successMessageProperty());
        successLabel.visibleProperty().bind(viewModel.successMessageProperty().isNotEmpty());
        
        // Loading indicator
        loadingIndicator.visibleProperty().bind(viewModel.isSubmittingProperty());
        
        // Comment length validation - handled in event handlers
    }
    
    /**
     * Configura i gestori degli eventi UI.
     * <p>
     * Gestisce i click sui pulsanti e la validazione in tempo reale del testo.
     * </p>
     */
    private void setupEventHandlers() {
        submitButton.setOnAction(e -> onSubmitReview());
        cancelButton.setOnAction(e -> onCancel());
        
        // Real-time validation
        commentArea.textProperty().addListener((obs, oldVal, newVal) -> {
            if (viewModel.isCommentTooLong()) {
                commentLengthLabel.setStyle("-fx-text-fill: #dc3545;");
            } else {
                commentLengthLabel.setStyle("-fx-text-fill: #6c757d;");
            }
        });
    }
    
    /**
     * Inizializza la vista per la creazione di una nuova recensione.
     *
     * @param restaurantName Il nome del ristorante da recensire.
     * @param userName Il nome dell'utente che scrive la recensione.
     */
    public void initialize(String restaurantName, String userName) {
        viewModel.initialize(restaurantName, userName);
        updateStarDisplay();
        submitButton.setText("Invia Recensione");
    }
    
    /**
     * Initialize the view for editing an existing review
     */
    public void initializeForEdit(dev.theknife.app.model.Review review) {
        viewModel.initializeForEdit(review);
        updateStarDisplay();
        submitButton.setText("Aggiorna Recensione");
    }
    
    /**
     * Gestisce l'azione di invio del form.
     * <p>
     * Delega al ViewModel la logica di sottomissione.
     * </p>
     */
    private void onSubmitReview() {
        viewModel.submitReview();
        // Form clearing is handled by the ViewModel through property bindings
    }
    
    /**
     * Gestisce l'azione di annullamento.
     */
    private void onCancel() {
        // This would typically close the dialog or navigate back
        logger.info("Cancel review");
    }
    
    /**
     * Imposta l'azione del pulsante di annullamento.
     *
     * @param action Il Runnable da eseguire quando viene premuto il pulsante Annulla.
     */
    public void setCancelButtonAction(Runnable action) {
        cancelButton.setOnAction(e -> action.run());
    }
    
    /**
     * Imposta il callback da eseguire in caso di successo dell'invio.
     *
     * @param callback Il Runnable da eseguire dopo il salvataggio.
     */
    public void setOnSubmitSuccess(Runnable callback) {
        // This would be called when review is successfully submitted
        // For now, we'll just print a message
        logger.info("Review submitted successfully");
    }
    
    /**
     * Restituisce il ViewModel per accesso esterno.
     *
     * @return Il ViewModel associato a questa vista.
     */
    public ReviewViewModel getViewModel() {
        return viewModel;
    }
    
    /**
     * Verifica se il form è valido.
     *
     * @return true se il form è valido, false altrimenti.
     */
    public boolean isValid() {
        return viewModel.isValid();
    }
    
    /**
     * Verifica se è in corso un'operazione di invio.
     *
     * @return true se il salvataggio è in corso, false altrimenti.
     */
    public boolean isSubmitting() {
        return viewModel.isSubmitting();
    }
}
