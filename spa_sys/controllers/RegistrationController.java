package com.example.spa_sys.controllers;

import com.example.spa_sys.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;

import com.example.spa_sys.database.DBConnection;

public class RegistrationController {

    @FXML private TextField newUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField; // <-- New field
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Button registerNewUserButton;
    @FXML private Button backToLoginButton;

    @FXML
    private void initialize() {
        registerNewUserButton.setOnAction(event -> registerUser());
        backToLoginButton.setOnAction(event -> goToLogin());
    }

    private void registerUser() {
        String username = newUsernameField.getText().trim();
        String password = newPasswordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

//         Username: only letters, digits, underscore + min 3 chars
        if (!username.matches("^[a-zA-Z0-9_]{3,}$")) {
            showAlert(Alert.AlertType.ERROR, "Username must be at least 3 characters and can only contain letters, digits, or underscores.");
            return;
        }
        if (username.contains(" ")) {
            showAlert(Alert.AlertType.ERROR, "Username cannot contain spaces.");
            return;
        }

        // Email: basic regex check
        if (!email.matches("^[\\w-.]+@[\\w-]+(\\.[\\w-]+)+$")) {
            showAlert(Alert.AlertType.ERROR, "Please enter a valid email address.");
            return;
        }

        // Phone: digits only, length 8-15
        if (!phone.matches("^\\d{8,15}$")) {
            showAlert(Alert.AlertType.ERROR, "Phone number must be 8–15 digits.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Passwords do not match.");
            return;
        }

        // Check empty
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill in all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            // Username already exists?
            String checkUserSQL = "SELECT * FROM login_users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkUserSQL);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showAlert(Alert.AlertType.ERROR, "Username already exists. Please choose another.");
                return;
            }

            // Email already exists?
            String checkEmailSQL = "SELECT * FROM users WHERE email = ?";
            PreparedStatement emailStmt = conn.prepareStatement(checkEmailSQL);
            emailStmt.setString(1, email);
            ResultSet emailRs = emailStmt.executeQuery();
            if (emailRs.next()) {
                showAlert(Alert.AlertType.ERROR, "This email is already registered.");
                return;
            }

            // Phone already exists?
            String checkPhoneSQL = "SELECT * FROM users WHERE phone = ?";
            PreparedStatement phoneStmt = conn.prepareStatement(checkPhoneSQL);
            phoneStmt.setString(1, phone);
            ResultSet phoneRs = phoneStmt.executeQuery();
            if (phoneRs.next()) {
                showAlert(Alert.AlertType.ERROR, "This phone number is already registered.");
                return;
            }

            // Insert user
            String insertUserSQL = "INSERT INTO users (name, email, phone, role) VALUES (?, ?, ?, ?)";
            PreparedStatement userStmt = conn.prepareStatement(insertUserSQL, PreparedStatement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, username);
            userStmt.setString(2, email);
            userStmt.setString(3, phone);
            userStmt.setString(4, "Customer");
            userStmt.executeUpdate();

            var generatedKeys = userStmt.getGeneratedKeys();
            int userId = -1;
            if (generatedKeys.next()) {
                userId = generatedKeys.getInt(1);
            }

            // Insert login
            if (userId != -1) {
                String insertLoginSQL = "INSERT INTO login_users (user_id, username, password, role) VALUES (?, ?, ?, ?)";
                PreparedStatement loginStmt = conn.prepareStatement(insertLoginSQL);
                loginStmt.setInt(1, userId);
                loginStmt.setString(2, username);
                loginStmt.setString(3, password);
                loginStmt.setString(4, "Customer");
                loginStmt.executeUpdate();

                User.setLoggedInUserId(userId);
                System.out.println("Logged in user ID: " + userId);
            }

            showAlert(Alert.AlertType.INFORMATION, "Registration Successful! Please log in.");
            goToCustomerDashboard();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "An error occurred while registering. Please try again.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void goToCustomerDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/customer_dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) registerNewUserButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) backToLoginButton.getScene().getWindow();

            Scene loginScene = new Scene(loginRoot);
            stage.setScene(loginScene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
