package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.Appointment;
import com.example.spa_sys.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.sql.*;
import java.time.*;
import java.util.*;

public class CustomerBookingsController {
    @FXML private VBox appointmentsContainer;

    @FXML
    public void initialize() {
        loadAppointments();
    }

    private void loadAppointments() {
        appointmentsContainer.getChildren().clear();
        int userId = User.getLoggedInUserId();
        List<Appointment> appointments = getCustomerAppointments(userId);

//        In the case the customer has no appts, it doesn't just show an empty page
        if (appointments.isEmpty()) {
            VBox emptyBox = new VBox(12);
            emptyBox.setAlignment(javafx.geometry.Pos.CENTER);

            Label noAppLabel = new Label("No Appointments...");
            noAppLabel.getStyleClass().add("no-app-label");

            emptyBox.getChildren().add(noAppLabel);
            appointmentsContainer.getChildren().add(emptyBox);
        }

//        Display each appt as a card (Node)
        for (Appointment a : getCustomerAppointments(userId)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/booking_card.fxml"));
                Node card = loader.load();
                CustomerBookingCardController controller = loader.getController();
                controller.setAppointment(a);
                controller.setOnEdit(this::onEditAppointment);
                appointmentsContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Loads the appointments (joins for all display fields)
    private List<Appointment> getCustomerAppointments(int userId) {
        List<Appointment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = """
                SELECT a.id, a.user_id, a.service_id, a.staff_id, a.remarks, a.appointment_date, a.status, a.payment_method_id,
                       s.name AS service_name, u2.name AS staff_name, pm.method_name AS payment_method_name
                FROM appointments a
                JOIN services s ON a.service_id = s.id
                JOIN users u2 ON a.staff_id = u2.id
                JOIN payment_methods pm ON a.payment_method_id = pm.id
                WHERE a.user_id = ?
                ORDER BY a.appointment_date DESC
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Appointment app = new Appointment(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("service_id"),
                        rs.getInt("staff_id"),
                        rs.getString("remarks"),
                        rs.getTimestamp("appointment_date").toLocalDateTime(),
                        rs.getString("status"),
                        rs.getInt("payment_method_id"),
                        rs.getString("service_name"),
                        rs.getString("staff_name"),
                        rs.getString("payment_method_name"),
                        null, // staffColour not needed here
                        null  // customerName not needed here
                );
                list.add(app);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Handles the edit request, opens a dialog if allowed
    private void onEditAppointment(Appointment appointment) {
        LocalDateTime now = LocalDateTime.now();
        if (appointment.getAppointmentDate().isBefore(now.plusHours(24))) {
            showAlert(Alert.AlertType.ERROR, "Edit Not Allowed", "You can only edit an appointment at least 24 hours in advance.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/edit_booking_popup.fxml"));
            Parent popupRoot = loader.load();
            EditBookingPopupController controller = loader.getController();
            controller.setData(appointment.getId(), appointment.getAppointmentDate());

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Appointment");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(popupRoot));
            dialogStage.showAndWait();

            // After closing, reload the list
            loadAppointments();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Failed", "Unable to open edit window.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
