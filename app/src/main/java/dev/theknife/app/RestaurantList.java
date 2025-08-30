package dev.theknife.app;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RestaurantList {
    private Scene scene;
    private Stage primaryStage;
    private Scene homeScene;
    private List<Restaurant> allRestaurants;
    private List<Restaurant> displayedRestaurants;
    private GridPane restaurantGrid;
    private ScrollPane scrollPane;
    private int currentPage = 0;
    private final int RESTAURANTS_PER_PAGE = 10;
    private boolean isLoading = false;
    
    public RestaurantList(Stage primaryStage, Scene homeScene) {
        this.primaryStage = primaryStage;
        this.homeScene = homeScene;
        this.allRestaurants = loadRestaurants();
        this.displayedRestaurants = new ArrayList<>();
        createScene();
    }
    
    public Scene getScene() {
        return scene;
    }
    
    private void createScene() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Header
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Button backButton = new Button("← Back");
        backButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        backButton.setOnAction(e -> primaryStage.setScene(homeScene));
        
        Label titleLabel = new Label("Restaurant Directory");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#2c3e50"));
        
        header.getChildren().addAll(backButton, titleLabel);
        
        // Search bar
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        
        TextField searchField = new TextField();
        searchField.setPromptText("Search restaurants...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-background-radius: 20; -fx-border-radius: 20;");
        
        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        
        searchBox.getChildren().addAll(searchField, searchButton);
        
        // Restaurant grid
        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        restaurantGrid = new GridPane();
        restaurantGrid.setHgap(20);
        restaurantGrid.setVgap(20);
        restaurantGrid.setAlignment(Pos.TOP_CENTER);
        restaurantGrid.setPadding(new Insets(20));
        
        scrollPane.setContent(restaurantGrid);
        
        // Load initial restaurants
        loadNextPage();
        
        // Scroll listener for lazy loading
        scrollPane.vvalueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() >= 0.8 && !isLoading && currentPage * RESTAURANTS_PER_PAGE < allRestaurants.size()) {
                loadNextPage();
            }
        });
        
        // Search functionality
        searchButton.setOnAction(e -> {
            String searchTerm = searchField.getText().toLowerCase();
            performSearch(searchTerm);
        });
        
        searchField.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                String searchTerm = searchField.getText().toLowerCase();
                performSearch(searchTerm);
            }
        });
        
        root.getChildren().addAll(header, searchBox, scrollPane);
        scene = new Scene(root, 1200, 800);
    }
    
    private void performSearch(String searchTerm) {
        if (searchTerm.isEmpty()) {
            // Reset to show all restaurants
            currentPage = 0;
            displayedRestaurants.clear();
            restaurantGrid.getChildren().clear();
            loadNextPage();
        } else {
            // Filter restaurants
            List<Restaurant> filteredRestaurants = filterRestaurants(searchTerm);
            currentPage = 0;
            displayedRestaurants.clear();
            restaurantGrid.getChildren().clear();
            
            // Load filtered results
            int endIndex = Math.min(RESTAURANTS_PER_PAGE, filteredRestaurants.size());
            displayedRestaurants.addAll(filteredRestaurants.subList(0, endIndex));
            updateGrid();
        }
    }
    
    private void loadNextPage() {
        if (isLoading) return;
        
        isLoading = true;
        
        // Simulate loading delay for better UX
        new Thread(() -> {
            try {
                Thread.sleep(100); // Small delay to show loading
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Load next batch of restaurants
            int startIndex = currentPage * RESTAURANTS_PER_PAGE;
            int endIndex = Math.min(startIndex + RESTAURANTS_PER_PAGE, allRestaurants.size());
            
            if (startIndex < allRestaurants.size()) {
                List<Restaurant> newRestaurants = allRestaurants.subList(startIndex, endIndex);
                
                // Update UI on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    displayedRestaurants.addAll(newRestaurants);
                    updateGrid();
                    currentPage++;
                    isLoading = false;
                });
            } else {
                isLoading = false;
            }
        }).start();
    }
    
    private void updateGrid() {
        restaurantGrid.getChildren().clear();
        
        int column = 0;
        int row = 0;
        int maxColumns = 3; // Responsive: 3 columns for larger screens
        
        for (Restaurant restaurant : displayedRestaurants) {
            VBox card = createRestaurantCard(restaurant);
            restaurantGrid.add(card, column, row);
            
            column++;
            if (column >= maxColumns) {
                column = 0;
                row++;
            }
        }
        
        // Add loading indicator if there are more restaurants to load
        if (currentPage * RESTAURANTS_PER_PAGE < allRestaurants.size()) {
            VBox loadingCard = createLoadingCard();
            restaurantGrid.add(loadingCard, column, row);
        }
    }
    
    private VBox createLoadingCard() {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(350);
        card.setPrefHeight(400);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        // Loading placeholder
        Rectangle loadingPlaceholder = new Rectangle(320, 150);
        loadingPlaceholder.setFill(Color.web("#ecf0f1"));
        loadingPlaceholder.setArcWidth(10);
        loadingPlaceholder.setArcHeight(10);
        
        // Loading text
        Label loadingLabel = new Label("Loading more restaurants...");
        loadingLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        loadingLabel.setTextFill(Color.web("#7f8c8d"));
        loadingLabel.setTextAlignment(TextAlignment.CENTER);
        
        // Progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(30, 30);
        
        card.getChildren().addAll(loadingPlaceholder, loadingLabel, progressIndicator);
        
        return card;
    }
    
    private VBox createRestaurantCard(Restaurant restaurant) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(350);
        card.setPrefHeight(400);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        // Restaurant image placeholder
        Rectangle imagePlaceholder = new Rectangle(320, 150);
        imagePlaceholder.setFill(Color.web("#ecf0f1"));
        imagePlaceholder.setArcWidth(10);
        imagePlaceholder.setArcHeight(10);
        
        // Restaurant name
        Label nameLabel = new Label(restaurant.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#2c3e50"));
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setMaxWidth(320);
        
        // Award badge
        HBox awardBox = new HBox(5);
        awardBox.setAlignment(Pos.CENTER);
        
        if (restaurant.getAward() != null && !restaurant.getAward().isEmpty()) {
            Label awardLabel = new Label(restaurant.getAward());
            awardLabel.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10; -fx-background-radius: 15;");
            awardBox.getChildren().add(awardLabel);
        }
        
        // Price
        if (restaurant.getPrice() != null && !restaurant.getPrice().isEmpty()) {
            Label priceLabel = new Label(restaurant.getPrice());
            priceLabel.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 10;");
            awardBox.getChildren().add(priceLabel);
        }
        
        // Cuisine
        Label cuisineLabel = new Label(restaurant.getCuisine());
        cuisineLabel.setFont(Font.font("Arial", 12));
        cuisineLabel.setTextFill(Color.web("#7f8c8d"));
        cuisineLabel.setWrapText(true);
        cuisineLabel.setTextAlignment(TextAlignment.CENTER);
        cuisineLabel.setMaxWidth(320);
        
        // Location
        Label locationLabel = new Label(restaurant.getLocation());
        locationLabel.setFont(Font.font("Arial", 11));
        locationLabel.setTextFill(Color.web("#95a5a6"));
        locationLabel.setWrapText(true);
        locationLabel.setTextAlignment(TextAlignment.CENTER);
        locationLabel.setMaxWidth(320);
        
        // Description (truncated)
        String description = restaurant.getDescription();
        if (description != null && description.length() > 100) {
            description = description.substring(0, 97) + "...";
        }
        Label descLabel = new Label(description);
        descLabel.setFont(Font.font("Arial", 10));
        descLabel.setTextFill(Color.web("#34495e"));
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setMaxWidth(320);
        descLabel.setMaxHeight(60);
        
        // View details button
        Button detailsButton = new Button("View Details");
        detailsButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
        detailsButton.setPrefWidth(120);
        detailsButton.setOnAction(e -> showRestaurantDetails(restaurant));
        
        card.getChildren().addAll(imagePlaceholder, nameLabel, awardBox, cuisineLabel, locationLabel, descLabel, detailsButton);
        
        return card;
    }
    
    private void showRestaurantDetails(Restaurant restaurant) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Restaurant Details");
        alert.setHeaderText(restaurant.getName());
        
        StringBuilder content = new StringBuilder();
        content.append("Name: ").append(restaurant.getName()).append("\n\n");
        content.append("Address: ").append(restaurant.getAddress()).append("\n\n");
        content.append("Location: ").append(restaurant.getLocation()).append("\n\n");
        content.append("Price: ").append(restaurant.getPrice()).append("\n\n");
        content.append("Cuisine: ").append(restaurant.getCuisine()).append("\n\n");
        content.append("Award: ").append(restaurant.getAward()).append("\n\n");
        if (restaurant.getPhoneNumber() != null && !restaurant.getPhoneNumber().isEmpty()) {
            content.append("Phone: ").append(restaurant.getPhoneNumber()).append("\n\n");
        }
        if (restaurant.getWebsiteUrl() != null && !restaurant.getWebsiteUrl().isEmpty()) {
            content.append("Website: ").append(restaurant.getWebsiteUrl()).append("\n\n");
        }
        content.append("Description: ").append(restaurant.getDescription());
        
        alert.setContentText(content.toString());
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(600);
        alert.getDialogPane().setPrefHeight(500);
        
        alert.showAndWait();
    }
    
    private List<Restaurant> filterRestaurants(String searchTerm) {
        if (searchTerm.isEmpty()) {
            return allRestaurants;
        }
        
        List<Restaurant> filtered = new ArrayList<>();
        for (Restaurant restaurant : allRestaurants) {
            if (restaurant.getName().toLowerCase().contains(searchTerm) ||
                restaurant.getCuisine().toLowerCase().contains(searchTerm) ||
                restaurant.getLocation().toLowerCase().contains(searchTerm) ||
                (restaurant.getAward() != null && restaurant.getAward().toLowerCase().contains(searchTerm))) {
                filtered.add(restaurant);
            }
        }
        return filtered;
    }
    
    private List<Restaurant> loadRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        String[] possiblePaths = {
            "app\\michelin_my_maps.csv",
            "app/michelin_my_maps.csv",
            "michelin_my_maps.csv"
        };
        
        BufferedReader br = null;
        for (String csvFile : possiblePaths) {
            try {
                System.out.println("Trying to open: " + csvFile);
                br = new BufferedReader(new FileReader(csvFile));
                System.out.println("Successfully opened: " + csvFile);
                break; // If we successfully open the file, break out of the loop
            } catch (IOException e) {
                System.out.println("Tried path: " + csvFile + " - not found: " + e.getMessage());
                continue;
            }
        }
        
        if (br == null) {
            System.err.println("Could not find CSV file in any of the expected locations");
            // Add sample data
            restaurants.add(new Restaurant("Sample Restaurant", "123 Main St", "New York, USA", "$$$", "Italian", "0", "0", "+1234567890", "", "", "1 Star", "0", "A sample restaurant for demonstration purposes."));
            return restaurants;
        }
        
        try (BufferedReader reader = br) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                
                // Parse CSV line (handling quoted fields)
                String[] fields = parseCSVLine(line);
                if (fields.length >= 13) {
                    Restaurant restaurant = new Restaurant(
                        fields[0], // Name
                        fields[1], // Address
                        fields[2], // Location
                        fields[3], // Price
                        fields[4], // Cuisine
                        fields[5], // Longitude
                        fields[6], // Latitude
                        fields[7], // PhoneNumber
                        fields[8], // Url
                        fields[9], // WebsiteUrl
                        fields[10], // Award
                        fields[11], // GreenStar
                        fields[12]  // Description
                    );
                    restaurants.add(restaurant);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            // Add some sample data if CSV reading fails
            restaurants.add(new Restaurant("Sample Restaurant", "123 Main St", "New York, USA", "$$$", "Italian", "0", "0", "+1234567890", "", "", "1 Star", "0", "A sample restaurant for demonstration purposes."));
        }
        
        return restaurants;
    }
    
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString().trim());
        return fields.toArray(new String[0]);
    }
    
    // Inner class to represent restaurant data
    private static class Restaurant {
        private String name, address, location, price, cuisine, longitude, latitude;
        private String phoneNumber, url, websiteUrl, award, greenStar, description;
        
        public Restaurant(String name, String address, String location, String price, String cuisine,
                         String longitude, String latitude, String phoneNumber, String url, 
                         String websiteUrl, String award, String greenStar, String description) {
            this.name = name;
            this.address = address;
            this.location = location;
            this.price = price;
            this.cuisine = cuisine;
            this.longitude = longitude;
            this.latitude = latitude;
            this.phoneNumber = phoneNumber;
            this.url = url;
            this.websiteUrl = websiteUrl;
            this.award = award;
            this.greenStar = greenStar;
            this.description = description;
        }
        
        // Getters
        public String getName() { return name; }
        public String getAddress() { return address; }
        public String getLocation() { return location; }
        public String getPrice() { return price; }
        public String getCuisine() { return cuisine; }
        public String getLongitude() { return longitude; }
        public String getLatitude() { return latitude; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getUrl() { return url; }
        public String getWebsiteUrl() { return websiteUrl; }
        public String getAward() { return award; }
        public String getGreenStar() { return greenStar; }
        public String getDescription() { return description; }
    }
}
