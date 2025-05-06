package com.example.spa_sys.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.List;

import java.io.IOException;

public class AdminDashboardController {

    @FXML private StackPane contentArea;

    public void initialize() {
        showMainPage();
    }

//     Load FXML for each section
    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/" + fxmlFile));

//          Gets the parent root of the .fxml file to use as layout
            Parent view = loader.load();
//            Clears the current content with the new content from called .fxml file
            contentArea.getChildren().setAll(view);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load FXML file: " + fxmlFile);
        }
    }

//     Navigation Handlers
//    .fxml files will be loaded into the StackPane of the admin dashboard
    @FXML
    private void showMainPage() { loadView("main_page.fxml"); }
    @FXML
    private void showAppointments() { loadView("calendar_view.fxml"); }
    @FXML
    private void showManageAppointments() { loadView("manage_appointments.fxml"); }
    @FXML
    private void showManageServices() { loadView("manage_services.fxml"); }
    @FXML
    private void showSalesReport() { loadView("sales_report.fxml"); }
    @FXML
    private void showStaffInfo() { loadView("staff_info.fxml"); }
    @FXML
    private void handleLogout() {
        try {
//             Load logout screen
            StackPane logoutPane = FXMLLoader.load(getClass().getResource("/com/example/spa_sys/logout_screen.fxml"));
            Scene currentScene = contentArea.getScene();
            currentScene.setRoot(logoutPane);

//             Fade out animation
            FadeTransition fade = new FadeTransition(Duration.seconds(2), logoutPane);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            fade.setOnFinished(e -> {
                try {
//                     Return to log in screen
                    Stage stage = (Stage) logoutPane.getScene().getWindow();
                    Parent loginRoot = FXMLLoader.load(getClass().getResource("/com/example/spa_sys/login.fxml"));
                    stage.setScene(new Scene(loginRoot));
                    stage.centerOnScreen();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

//             Start animation after a short delay (1s)
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(e -> fade.play());
            delay.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}