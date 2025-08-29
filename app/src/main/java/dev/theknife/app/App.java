package dev.theknife.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.io.IOException;

public class App extends Application {
    private Stage primaryStage;
    private Scene homeScene;
    private Scene registerScene;
    private Scene loginScene;

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("TheKnife - Restaurant Management System");
        
        // Initialize CSV file
        try {
            CSVManager.createCSVHeader();
        } catch (IOException e) {
            System.err.println("Failed to create CSV file: " + e.getMessage());
        }
        
        // Create scenes
        createHomeScene();
        createRegisterScene();
        createLoginScene();
        
        // Start with home scene
        primaryStage.setScene(homeScene);
        primaryStage.show();
    }
    
    private void createHomeScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        
        // Title
        Label titleLabel = new Label("Welcome to TheKnife");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Buttons
        Button registerBtn = new Button("Register");
        registerBtn.setPrefSize(200, 40);
        registerBtn.setOnAction(e -> primaryStage.setScene(registerScene));
        
        Button loginBtn = new Button("Login");
        loginBtn.setPrefSize(200, 40);
        loginBtn.setOnAction(e -> primaryStage.setScene(loginScene));
        
        root.getChildren().addAll(titleLabel, registerBtn, loginBtn);
        homeScene = new Scene(root, 400, 300);
    }
    
    private void createRegisterScene() {
        RegisterScreen registerScreen = new RegisterScreen(primaryStage, homeScene);
        registerScene = registerScreen.getScene();
    }
    
    private void createLoginScene() {
        LoginScreen loginScreen = new LoginScreen(primaryStage, homeScene);
        loginScene = loginScreen.getScene();
    }
}
