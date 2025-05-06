package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private Label greetingLabel;
    @FXML private Text greetingRole;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Label loginsuccessful;

    @FXML private Button registerButton;

    private boolean isAdminMode = false;

//    Changes the mode from admin to customer
    @FXML
    private void handleFlip() {
        isAdminMode = !isAdminMode;
        greetingRole.setText(isAdminMode ? "Admin" : "Customer");
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

//         Clear previous messages
        errorLabel.setText("");
        loginsuccessful.setText("");

//        Validation for both fields to be filled
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        String expectedRole = isAdminMode ? "Admin" : "Customer";

//        Checks with the db of details
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM login_users WHERE username = ? AND password = ? AND role = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, expectedRole);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("user_id");
//                Tells the program that its user id xx on
                User.setLoggedInUserId(userId);

                loginsuccessful.setText("Login successful!");
                errorLabel.setText("");

//                Load into admin/customer dashboard
                Stage stage = (Stage) usernameField.getScene().getWindow();
                Parent root;
                if (isAdminMode) {
                    root = FXMLLoader.load(getClass().getResource("/com/example/spa_sys/admin_dashboard.fxml"));
                }
                else {
                    root = FXMLLoader.load(getClass().getResource("/com/example/spa_sys/customer_dashboard.fxml"));
                }
                stage.setScene(new Scene(root));
                stage.setMaximized(true); //make it full screen
                stage.centerOnScreen(); // make it centered
                stage.setResizable(true); // but make the navigation buttons visble
            }
            else {
                errorLabel.setText("Incorrect credentials or role.");
                loginsuccessful.setText("");
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Database error occurred.");
            loginsuccessful.setText("");
        }
    }


//    Registration for new users
    @FXML
    private void handleRegister(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/registration.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
