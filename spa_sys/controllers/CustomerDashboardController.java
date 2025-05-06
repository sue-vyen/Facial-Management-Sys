package com.example.spa_sys.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;

import java.io.IOException;

public class CustomerDashboardController {

    @FXML private StackPane contentArea;
    @FXML private Label welcomeLabel;

    @FXML private Button homeButton;
    @FXML private Button facialButton;
    @FXML private Button bookingButton;
    @FXML private Button logoutButton;

    // Load FXML for each section
    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/" + fxmlFile));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML file: " + fxmlFile);
        }
    }

//     (Optional) method to set customer name
    public void setCustomerName(String customerName) {
        welcomeLabel.setText("Hello, " + customerName);
    }

    @FXML
    private void initialize() {
        showHomePage();
        logoutButton.setOnAction(event -> showLogoutPage());
    }

//    NAVIGATION BANNER

    @FXML
    private void showHomePage() {
        loadPage("main_page.fxml");
    }

    @FXML
    private void showFacialsPage() {
        loadPage("customer_facials.fxml");
    }

    @FXML
    private void showBookingsPage() {
        loadPage("customer_bookings.fxml");
    }

    @FXML
    private void showLogoutPage() {
        try {
            // Load logout screen
            StackPane logoutPane = FXMLLoader.load(getClass().getResource("/com/example/spa_sys/logout_screen.fxml"));
            Scene currentScene = contentArea.getScene();
            currentScene.setRoot(logoutPane);

            // Fade out animation
            FadeTransition fade = new FadeTransition(Duration.seconds(2), logoutPane);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            fade.setOnFinished(e -> {
                try {
                    // Return to log in screen
                    Stage stage = (Stage) logoutPane.getScene().getWindow();
                    Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/example/spa_sys/login.fxml"));
                    stage.setScene(new Scene(loginRoot));
                    stage.centerOnScreen();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            // Start animation after a short delay
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(e -> fade.play());
            delay.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
