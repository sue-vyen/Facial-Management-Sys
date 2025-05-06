package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class EditBookingPopupController {

    @FXML private DatePicker newDatePicker;
    @FXML private ComboBox<String> newTimeComboBox;
    @FXML private CheckBox termsCheckBox;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private int appointmentId;
    private LocalDateTime originalAppointmentDateTime;

    public void setData(int appointmentId, LocalDateTime appointmentDateTime) {
        this.appointmentId = appointmentId;
        this.originalAppointmentDateTime = appointmentDateTime;

//        The available time slots
        newTimeComboBox.getItems().addAll(Arrays.asList(
                "9:00", "10:00", "11:00", "14:00", "15:00", "16:00", "17:00"
        ));

//         Pre-fill with original date & time
        newDatePicker.setValue(appointmentDateTime.toLocalDate());
        newTimeComboBox.setValue(appointmentDateTime.toLocalTime().toString().substring(0,5)); // "HH:mm"
    }

//    Updates the DB when changes are saved
    @FXML
    private void handleSave() {
        LocalDate date = newDatePicker.getValue();
        String time = newTimeComboBox.getValue();

//        Validation for filled fields
        if (date == null || time == null) {
            showAlert(Alert.AlertType.WARNING, "Please select both date and time.");
            return;
        }
        if (!termsCheckBox.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Please accept the terms and conditions.");
            return;
        }

        LocalDateTime newDateTime = LocalDateTime.of(date, LocalTime.parse(time));

//        Message to the customer if they try changing their appt 24h bfr
        if (Duration.between(LocalDateTime.now(), newDateTime).toHours() < 24) {
            showAlert(Alert.AlertType.ERROR, "You can only change appointments at least 24 hours in advance.");
            return;
        }

//        Message to customer if their current appt is already within 24h
        if (Duration.between(LocalDateTime.now(), originalAppointmentDateTime).toHours() < 24) {
            showAlert(Alert.AlertType.ERROR, "Cannot edit within 24 hours of your current appointment.");
            return;
        }

//        Updates the db of the changes made
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE appointments SET appointment_date = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, Timestamp.valueOf(newDateTime));
            stmt.setInt(2, appointmentId);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Appointment updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "No appointment was updated.");
            }

            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();

        }
        catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage());
        }
        catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
