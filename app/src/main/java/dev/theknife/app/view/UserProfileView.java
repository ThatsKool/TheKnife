/*
 * Autori:
 * - Federico Barbotti, 752545, Varese
 * - Oittijo Ahemmed Sarkar, 759646, Varese
 * - Bennajim Alì, 760125, Varese
 */
package dev.theknife.app.view;

import dev.theknife.app.model.User;
import dev.theknife.app.viewmodel.UserProfileViewModel;
import dev.theknife.app.viewmodel.UserProfileViewModel.LocationDetectOutcome;
import dev.theknife.app.viewmodel.UserProfileViewModel.LocationDetectStatus;
import dev.theknife.app.viewmodel.UserProfileViewModel.LocationUpdateResult;
import dev.theknife.app.viewmodel.UserProfileViewModel.LocationUpdateStatus;
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

/**
 * Vista per la gestione del profilo utente.
 * <p>
 * Visualizza le informazioni dell'account e delega logica e servizi a {@link UserProfileViewModel}.
 * </p>
 */
public class UserProfileView {
    private static final String PRIMARY_GREEN = "#2E7D32";
    private static final String BACKGROUND_WHITE = "#FFFFFF";
    private static final String TEXT_DARK = "#212121";
    private static final String TEXT_GRAY = "#757575";
    private static final String BORDER_GRAY = "#E0E0E0";
    private static final String ERROR_RED = "#D32F2F";
    private static final String INFO_BLUE = "#1976D2";

    private final UserProfileViewModel viewModel;

    /**
     * @param viewModel ViewModel del profilo utente.
     */
    public UserProfileView(UserProfileViewModel viewModel) {
        this.viewModel = viewModel;
    }

    /**
     * Crea il contenuto del modale profilo.
     */
    public Node createView() {
        User currentUser = viewModel.getCurrentUser();
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

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Il tuo Profilo");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web(PRIMARY_GREEN));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeButton = new Button("✕");
        closeButton.setStyle(
            "-fx-background-color: transparent; -fx-font-size: 18px; -fx-cursor: hand; -fx-text-fill: " + TEXT_GRAY + ";");
        closeButton.setOnAction(e -> ModalManager.getInstance().close());

        header.getChildren().addAll(titleLabel, spacer, closeButton);

        HBox userInfo = new HBox(20);
        userInfo.setAlignment(Pos.CENTER_LEFT);
        userInfo.setPadding(new Insets(10, 0, 20, 0));

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
        roleLabel.setStyle(
            "-fx-background-color: #E8F5E9; -fx-text-fill: " + PRIMARY_GREEN + "; "
                + "-fx-padding: 2 8; -fx-background-radius: 10;");
        roleLabel.setFont(Font.font("Segoe UI", 12));

        userDetails.getChildren().addAll(nameLabel, emailLabel, roleLabel);
        userInfo.getChildren().addAll(avatar, userDetails);

        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();

        VBox locationSection = new VBox(15);
        locationSection.setPadding(new Insets(10, 0, 0, 0));

        Label locTitle = new Label("Aggiorna Posizione");
        locTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        locTitle.setTextFill(Color.web(TEXT_DARK));

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        TextField latField = createStyledTextField(
            String.valueOf(currentUser.getLatitude()), "Latitudine");
        TextField lonField = createStyledTextField(
            String.valueOf(currentUser.getLongitude()), "Longitudine");

        grid.add(new Label("Latitudine:"), 0, 0);
        grid.add(latField, 0, 1);
        grid.add(new Label("Longitudine:"), 1, 0);
        grid.add(lonField, 1, 1);

        Label statusLabel = new Label();
        statusLabel.setWrapText(true);
        statusLabel.setVisible(false);

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
        locationBtn.setOnAction(e -> viewModel.detectLocationAsync(
            outcome -> applyLocationDetectOutcome(outcome, latField, lonField, statusLabel)));

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
        updateButton.setOnAction(e -> {
            statusLabel.setVisible(false);
            LocationUpdateResult result = viewModel.updateLocationFromText(
                latField.getText(), lonField.getText());

            if (result.isSuccess()) {
                ModalManager.getInstance().close();
                ModalManager.getInstance().showInfo("Successo", "Posizione aggiornata correttamente!");
                return;
            }

            if (result.getStatus() == LocationUpdateStatus.INVALID_NUMBER
                || result.getStatus() == LocationUpdateStatus.INVALID_COORDINATES
                || result.getStatus() == LocationUpdateStatus.SAVE_ERROR) {
                showStatus(statusLabel, result.getMessage(), ERROR_RED);
            }
        });

        locationSection.getChildren().addAll(locTitle, grid, locationBtn, statusLabel, updateButton);
        root.getChildren().addAll(header, userInfo, separator, locationSection);

        return root;
    }

    private void applyLocationDetectOutcome(LocationDetectOutcome outcome,
                                            TextField latitudeField,
                                            TextField longitudeField,
                                            Label statusLabel) {
        switch (outcome.status()) {
            case LOADING -> {
                showStatus(statusLabel, outcome.message(), INFO_BLUE);
            }
            case SUCCESS -> {
                latitudeField.setText(viewModel.formatCoordinate(outcome.latitude()));
                longitudeField.setText(viewModel.formatCoordinate(outcome.longitude()));
                showStatus(statusLabel, outcome.message(), PRIMARY_GREEN);
            }
            case NETWORK_ERROR -> {
                showStatus(statusLabel, outcome.message(), ERROR_RED);
            }
        }
    }

    private void showStatus(Label statusLabel, String message, String color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(Color.web(color));
        statusLabel.setVisible(true);
    }

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
}
