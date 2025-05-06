package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.Staff;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.*;
import java.util.function.Consumer;

public class StaffController {
    @FXML private ScrollPane scrollPane;
    @FXML private VBox staffContainer;
    @FXML private Button addStaffButton;

    @FXML
    private void initialize() {
        loadStaff();
    }

    private void loadStaff() {
        staffContainer.getChildren().clear();
        staffContainer.getChildren().add(addStaffButton);

        String sql = "SELECT * FROM users WHERE role='Admin'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Consumer<Void> refresher = v -> loadStaff();
            while (rs.next()) {
                Staff s = new Staff(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("gender"),
                        rs.getString("status"),
                        rs.getString("colour_code")
                );
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/spa_sys/staff_card.fxml"));
                Node card = loader.load();
                StaffCardController ctrl = loader.getController();
                ctrl.setStaff(s, refresher);
                staffContainer.getChildren().add(card);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onAddStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/spa_sys/staff_card.fxml"));
            Node newCard = loader.load();
            StaffCardController ctrl = loader.getController();
            ctrl.initAsNew(v -> loadStaff());
            staffContainer.getChildren().add(1, newCard);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
