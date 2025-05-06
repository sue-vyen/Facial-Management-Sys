package com.example.spa_sys.controllers;

import com.example.spa_sys.controllers.ServiceCardController;
import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.Services;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class ServicesController {

    @FXML private ScrollPane scrollPane;
    @FXML private VBox servicesContainer;
    @FXML private Button addServiceButton;

    @FXML
    private void initialize() {
        loadServices();
    }

    private void loadServices() {
        servicesContainer.getChildren().clear();

//        Add in the add service button
        servicesContainer.getChildren().add(addServiceButton);

        String sql = "SELECT * FROM services";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Services> servicesList = new ArrayList<>();
            while (rs.next()) {
                servicesList.add(new Services(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("duration"),
                        rs.getString("description"),
                        rs.getString("image_path"),
                        rs.getString("ingredients"),
                        rs.getString("benefits")
                ));
            }

            for (Services service : servicesList) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/spa_sys/service_card.fxml")
                );
                Node card = loader.load();
                ServiceCardController controller = loader.getController();

                // Edit existing service
                controller.setService(service);
                controller.setRefreshCallback(v -> loadServices());

                servicesContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddService() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/service_card.fxml"));
            Node newCard = loader.load();

            ServiceCardController serviceCardController = loader.getController();

            // Initialise as new service
            serviceCardController.initAsNew(v -> loadServices());

            // Add new card at top
            servicesContainer.getChildren().add(1, newCard);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
