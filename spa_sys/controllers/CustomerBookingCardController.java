package com.example.spa_sys.controllers;

import com.example.spa_sys.models.Appointment;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class CustomerBookingCardController {
    @FXML private Label serviceNameLabel;
    @FXML private Label appointmentDateLabel;
    @FXML private Label staffNameLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label statusLabel;
    @FXML private Label remarksLabel;
    @FXML private Button editButton;

//    A callback function for editing purposes
    private Consumer<Appointment> onEdit;
//    Model instance from Appointment
    private Appointment appointment;

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
        serviceNameLabel.setText(appointment.getServiceName());
        staffNameLabel.setText("Facialist: " + appointment.getStaffName());
        paymentMethodLabel.setText("Payment: " + appointment.getPaymentMethodName());
        remarksLabel.setText(
                (appointment.getRemarks() == null || appointment.getRemarks().isEmpty())
                        ? "" : "Remarks: " + appointment.getRemarks()
        );

//        Display the date aesthetically
        appointmentDateLabel.setText(
                appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a"))
        );

//        Displays the appt status and make it coloured
        statusLabel.setText(appointment.getStatus());
        statusLabel.setStyle("-fx-font-weight:bold; -fx-background-radius:8; -fx-padding:4 12 4 12;" +
                "-fx-text-fill:white;" +
                getStatusColour(appointment.getStatus())
        );
    }

//    Sets the callback action when edit button is clicked
    public void setOnEdit(Consumer<Appointment> onEdit) {
        this.onEdit = onEdit;
        editButton.setOnAction(e -> {
            if (onEdit != null) onEdit.accept(appointment);
        });
    }

//    The css colours for each status (case)
    private String getStatusColour(String status) {
        return switch (status.toLowerCase()) {
            case "cancelled" -> "-fx-background-color:#b22222;";
            case "completed" -> "-fx-background-color:#35774a;";
            case "in-session" -> "-fx-background-color:#265eb5;";
            case "on-time" -> "-fx-background-color:#ffbf47;";
            default -> "-fx-background-color:#6c757d;";
        };
    }
}
