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
import java.io.IOException;

public class LoginScreen {
    private Scene scene;
    private TextField nameField;
    private PasswordField passwordField;
    private Label statusLabel;

    public LoginScreen(Stage primaryStage, Scene homeScene) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        // Title
        Label titleLabel = new Label("User Login");
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

        // Password
        Label passwordLabel = new Label("Password:");
        passwordField = new PasswordField();
        passwordField.setPrefWidth(200);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        // Status Label
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: red;");

        // Buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);

        Button loginBtn = new Button("Login");
        loginBtn.setPrefSize(100, 35);
        loginBtn.setOnAction(e -> handleLogin());

        Button backBtn = new Button("Back to Home");
        backBtn.setPrefSize(100, 35);
        backBtn.setOnAction(e -> primaryStage.setScene(homeScene));

        buttonBox.getChildren().addAll(loginBtn, backBtn);

        root.getChildren().addAll(titleLabel, grid, statusLabel, buttonBox);
        scene = new Scene(root, 400, 300);
    }

    private void handleLogin() {
        String name = nameField.getText().trim();
        String password = passwordField.getText().trim();

        if (name.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both name and password!");
            return;
        }

        String hashedPassword = PasswordHasher.hashPassword(password);

        try {
            if (CSVManager.validateCredentials(name, hashedPassword)) {
                statusLabel.setText("Login successful!");
                statusLabel.setStyle("-fx-text-fill: green;");
                clearFields();
            } else {
                statusLabel.setText("Invalid credentials! Please try again.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        } catch (IOException e) {
            statusLabel.setText("Login failed! Please try again.");
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }



    private void clearFields() {
        nameField.clear();
        passwordField.clear();
    }

    public Scene getScene() {
        return scene;
    }
}
