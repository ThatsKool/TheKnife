# TheKnife - Restaurant Management System

A JavaFX application for restaurant management with user registration and login functionality.

## Features

### Home Screen
- Clean and modern interface
- "Register" button to navigate to registration page
- "Login" button to navigate to login page

### Registration Page
- **Required Fields:**
  - Name
  - Surname
  - Password (encrypted with SHA-256)
  - Latitude (place of residence coordinate)
  - Longitude (place of residence coordinate)
  - Role selection (Client/Restaurateur)

- **Optional Fields:**
  - Date of Birth

- **Features:**
  - Input validation for all fields
  - Coordinate validation (latitude: -90 to 90, longitude: -180 to 180)
  - Password encryption using SHA-256 hashing
  - Data storage in CSV format
  - Success/error feedback messages

### Login Page
- **Fields:**
  - Name
  - Password

- **Features:**
  - Credential validation against stored CSV data
  - Password verification using SHA-256 hashing
  - Success/error feedback messages

## Data Storage

User data is stored in a `users.csv` file with the following format:
```
Name,Surname,Password,DateOfBirth,Latitude,Longitude,Role
```

The CSV file is automatically created when the application starts.

## Security Features

- **Password Encryption:** All passwords are hashed using SHA-256 algorithm
- **Input Validation:** Comprehensive validation for all user inputs
- **Coordinate Validation:** Ensures valid geographic coordinates

## How to Run

### Prerequisites
- Java 11 or higher
- Gradle (included in the project)

### Running the Application

1. **Using Gradle:**
   ```bash
   ./gradlew run
   ```

2. **Using Gradle Wrapper (Windows):**
   ```cmd
   gradlew.bat run
   ```

3. **Build and Run:**
   ```bash
   ./gradlew build
   java -jar app/build/libs/app.jar
   ```

## Project Structure

```
app/src/main/java/theknife/
├── App.java              # Main application class
├── RegisterScreen.java   # Registration screen UI and logic
├── LoginScreen.java      # Login screen UI and logic
├── User.java            # User data model
├── CSVManager.java      # CSV file operations
└── PasswordHasher.java  # Password encryption utilities
```

## Dependencies

- **JavaFX:** UI framework (version 24.0.2)
- **JUnit Jupiter:** Testing framework
- **Google Guava:** Utility library

## File Locations

- **CSV File:** `users.csv` (created in the project root directory)
- **Build Output:** `app/build/`
- **Executable JAR:** `app/build/libs/app.jar`

## Usage Instructions

1. **Registration:**
   - Click "Register" on the home screen
   - Fill in all required fields
   - Enter valid coordinates for your location
   - Select your role (Client or Restaurateur)
   - Click "Register" to save your account

2. **Login:**
   - Click "Login" on the home screen
   - Enter your name and password
   - Click "Login" to authenticate

3. **Navigation:**
   - Use "Back to Home" buttons to return to the main screen
   - All screens provide clear feedback for user actions

## Technical Details

- **UI Framework:** JavaFX with modern styling
- **Data Persistence:** CSV file storage
- **Password Security:** SHA-256 hashing
- **Input Validation:** Client-side validation with user feedback
- **Error Handling:** Comprehensive exception handling with user-friendly messages

## Development

This project uses Gradle for build management and dependency resolution. The JavaFX plugin is configured to handle the JavaFX modules automatically.

To modify the application:
1. Edit the Java files in `app/src/main/java/theknife/`
2. Update the build configuration in `app/build.gradle.kts` if needed
3. Run `./gradlew build` to compile
4. Run `./gradlew run` to test changes