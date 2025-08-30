package dev.theknife.app;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.IOException;

public class RegisterScreen {
    private Scene scene;
    private TextField nameField;
    private TextField surnameField;
    private PasswordField passwordField;
    private DatePicker dateOfBirthPicker;
    private TextField latitudeField;
    private TextField longitudeField;
    private ComboBox<String> roleComboBox;
    private Label statusLabel;

    public RegisterScreen(Stage primaryStage, Scene homeScene) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        // Title
        Label titleLabel = new Label("User Registration");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // Form Grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Name
        Label nameLabel = new Label("Name:");
        nameField = new TextField();
        nameField.setPrefWidth(200);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);

        // Surname
        Label surnameLabel = new Label("Surname:");
        surnameField = new TextField();
        surnameField.setPrefWidth(200);
        grid.add(surnameLabel, 0, 1);
        grid.add(surnameField, 1, 1);

        // Password
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPrefWidth(200);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);

        // Date of Birth (Optional)
        Label dobLabel = new Label("Date of Birth (Optional):");
        dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setPrefWidth(200);
        grid.add(dobLabel, 0, 3);
        grid.add(dateOfBirthPicker, 1, 3);

        // Latitude
        Label latLabel = new Label("Latitude:");
        latitudeField = new TextField();
        latitudeField.setPrefWidth(200);
        latitudeField.setPromptText("e.g., 40.7128");
        grid.add(latLabel, 0, 4);
        grid.add(latitudeField, 1, 4);

        // Longitude
        Label lonLabel = new Label("Longitude:");
        longitudeField = new TextField();
        longitudeField.setPrefWidth(200);
        longitudeField.setPromptText("e.g., -74.0060");
        grid.add(lonLabel, 0, 5);
        grid.add(longitudeField, 1, 5);

        // Role
        Label roleLabel = new Label("Role:");
        roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Client", "Restaurateur");
        roleComboBox.setValue("Client");
        roleComboBox.setPrefWidth(200);
        grid.add(roleLabel, 0, 6);
        grid.add(roleComboBox, 1, 6);

        // Status Label
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: red;");

        // Buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button registerBtn = new Button("Register");
        registerBtn.setPrefSize(100, 35);
        registerBtn.setOnAction(e -> handleRegistration());

        Button backBtn = new Button("Back to Home");
        backBtn.setPrefSize(100, 35);
        backBtn.setOnAction(e -> primaryStage.setScene(homeScene));

        buttonBox.getChildren().addAll(registerBtn, backBtn);

        root.getChildren().addAll(titleLabel, grid, statusLabel, buttonBox);
        scene = new Scene(root, 500, 600);
    }

    private void handleRegistration() {
        // Validate inputs
        if (nameField.getText().trim().isEmpty() || 
            surnameField.getText().trim().isEmpty() || 
            passwordField.getText().trim().isEmpty() ||
            latitudeField.getText().trim().isEmpty() ||
            longitudeField.getText().trim().isEmpty()) {
            statusLabel.setText("Please fill in all required fields!");
            return;
        }

        // Validate coordinates
        try {
            double lat = Double.parseDouble(latitudeField.getText().trim());
            double lon = Double.parseDouble(longitudeField.getText().trim());
            
            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                statusLabel.setText("Invalid coordinates! Latitude: -90 to 90, Longitude: -180 to 180");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Invalid coordinate format!");
            return;
        }

        // Hash password
        String hashedPassword = PasswordHasher.hashPassword(passwordField.getText());

        // Create user object
        User user = new User(
            nameField.getText().trim(),
            surnameField.getText().trim(),
            hashedPassword,
            dateOfBirthPicker.getValue(),
            Double.parseDouble(latitudeField.getText().trim()),
            Double.parseDouble(longitudeField.getText().trim()),
            roleComboBox.getValue()
        );

        // Save to CSV
        try {
            CSVManager.saveUser(user);
            statusLabel.setText("Registration successful!");
            statusLabel.setStyle("-fx-text-fill: green;");
            clearFields();
        } catch (IOException e) {
            statusLabel.setText("Registration failed! Please try again.");
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }



    private void clearFields() {
        nameField.clear();
        surnameField.clear();
        passwordField.clear();
        dateOfBirthPicker.setValue(null);
        latitudeField.clear();
        longitudeField.clear();
        roleComboBox.setValue("Client");
    }

    public Scene getScene() {
        return scene;
    }
}
