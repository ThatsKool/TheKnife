package dev.theknife.app;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVManager {
    private static String CSV_FILE = "users.csv";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    public static void saveUser(User user) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CSV_FILE, true))) {
            writer.println(user.toString());
        }
    }

    public static List<User> loadUsers() throws IOException {
        List<User> users = new ArrayList<>();
        File file = new File(CSV_FILE);
        
        if (!file.exists()) {
            return users;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                User user = parseUserFromCSV(line);
                if (user != null) {
                    users.add(user);
                }
            }
        }
        return users;
    }

    public static User findUserByName(String name) throws IOException {
        List<User> users = loadUsers();
        return users.stream()
                   .filter(user -> user.getName().equals(name))
                   .findFirst()
                   .orElse(null);
    }

    public static boolean validateCredentials(String name, String hashedPassword) throws IOException {
        User user = findUserByName(name);
        return user != null && user.getPassword().equals(hashedPassword);
    }

    private static User parseUserFromCSV(String csvLine) {
        try {
            String[] parts = csvLine.split(",");
            if (parts.length >= 7) {
                String name = parts[0];
                String surname = parts[1];
                String password = parts[2];
                LocalDate dateOfBirth = parts[3].isEmpty() ? null : 
                    LocalDate.parse(parts[3], DATE_FORMATTER);
                double latitude = Double.parseDouble(parts[4]);
                double longitude = Double.parseDouble(parts[5]);
                String role = parts[6];

                return new User(name, surname, password, dateOfBirth, latitude, longitude, role);
            }
        } catch (Exception e) {
            System.err.println("Error parsing CSV line: " + csvLine);
            e.printStackTrace();
        }
        return null;
    }

    public static void createCSVHeader() throws IOException {
        File file = new File(CSV_FILE);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("Name,Surname,Password,DateOfBirth,Latitude,Longitude,Role");
            }
        }
    }

    // Test helper methods
    public static String getCsvFilePath() {
        return CSV_FILE;
    }

    public static void setCsvFilePath(String path) {
        CSV_FILE = path;
    }
}
