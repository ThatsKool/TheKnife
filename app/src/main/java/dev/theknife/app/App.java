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
import javafx.scene.paint.Color;
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
        // Create a welcome screen with navigation to restaurant list
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #667eea 0%, #764ba2 100%);");
        
        // Title
        Label titleLabel = new Label("Welcome to TheKnife");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        titleLabel.setTextFill(Color.WHITE);
        
        // Subtitle
        Label subtitleLabel = new Label("Restaurant Management System");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.WHITE);
        
        // Main action button - Browse Restaurants
        Button browseRestaurantsBtn = new Button("Browse Restaurants");
        browseRestaurantsBtn.setPrefSize(250, 50);
        browseRestaurantsBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-background-radius: 25;");
        browseRestaurantsBtn.setOnAction(e -> {
            RestaurantList restaurantList = new RestaurantList(primaryStage, homeScene);
            primaryStage.setScene(restaurantList.getScene());
        });
        
        // Secondary buttons
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button registerBtn = new Button("Register");
        registerBtn.setPrefSize(150, 40);
        registerBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
        registerBtn.setOnAction(e -> primaryStage.setScene(registerScene));
        
        Button loginBtn = new Button("Login");
        loginBtn.setPrefSize(150, 40);
        loginBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
        loginBtn.setOnAction(e -> primaryStage.setScene(loginScene));
        
        buttonBox.getChildren().addAll(registerBtn, loginBtn);
        
        root.getChildren().addAll(titleLabel, subtitleLabel, browseRestaurantsBtn, buttonBox);
        homeScene = new Scene(root, 600, 400);
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
