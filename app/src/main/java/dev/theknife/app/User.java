package dev.theknife.app;

import java.time.LocalDate;

public class User {
    private String name;
    private String surname;
    private String password;
    private LocalDate dateOfBirth;
    private double latitude;
    private double longitude;
    private String role;

    public User(String name, String surname, String password, LocalDate dateOfBirth, 
                double latitude, double longitude, String role) {
        this.name = name;
        this.surname = surname;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.latitude = latitude;
        this.longitude = longitude;
        this.role = role;
    }

    // Getters
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getPassword() { return password; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getRole() { return role; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setSurname(String surname) { this.surname = surname; }
    public void setPassword(String password) { this.password = password; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s,%s,%s,%s",
            name, surname, password, 
            dateOfBirth != null ? dateOfBirth.toString() : "",
            latitude, longitude, role);
    }
}
