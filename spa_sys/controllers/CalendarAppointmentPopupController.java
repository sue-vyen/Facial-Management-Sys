package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.Appointment;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class CalendarAppointmentPopupController {
    @FXML private ComboBox<String> customerCombo;
    @FXML private ComboBox<String> serviceCombo;
    @FXML private ComboBox<String> staffCombo;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> timeCombo;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private TextArea remarksArea;
    @FXML private Button bookButton;

    private Map<String, String> staffColourMap = new LinkedHashMap<>();
//    Purely just to refresh the calendar after booking has been made (Callback)
    private Runnable onAppointmentBooked;

    public void initialize() {
        initialiseFormComponents();
    }


//    Loads in the comboboxes using the db w/ rules
    private void initialiseFormComponents() {
        // Load customer names
        customerCombo.getItems().clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM users WHERE role = 'Customer'")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                customerCombo.getItems().add(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load customers");
        }

        // Load services
        serviceCombo.getItems().clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM services")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                serviceCombo.getItems().add(rs.getString("name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load services");
        }

        // Load available staff
        staffCombo.getItems().clear();
        staffColourMap.clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name, colour_code FROM users WHERE role='Admin' AND status = 'Available'")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("name");
                String colour = rs.getString("colour_code");
                staffCombo.getItems().add(name);
                staffColourMap.put(name, colour);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load staff");
        }

        // Load payment methods
        paymentMethodCombo.getItems().clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT method_name FROM payment_methods")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                paymentMethodCombo.getItems().add(rs.getString("method_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load payment methods");
        }


        // Time slots
        timeCombo.getItems().setAll(
                LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
                LocalTime.of(14, 0), LocalTime.of(15, 0), LocalTime.of(16, 0), LocalTime.of(17,0)
        );

        // Disable past dates
        datePicker.setDayCellFactory((Callback<DatePicker, DateCell>) picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #EEEEEE;");
                }
            }
        });
        datePicker.setValue(LocalDate.now());

        // Form validation
        ChangeListener<Object> formValidator = (obs, oldVal, newVal) -> validateForm();
        customerCombo.valueProperty().addListener(formValidator);
        serviceCombo.valueProperty().addListener(formValidator);
        staffCombo.valueProperty().addListener(formValidator);
        datePicker.valueProperty().addListener(formValidator);
        timeCombo.valueProperty().addListener(formValidator);
        paymentMethodCombo.valueProperty().addListener(formValidator);

        // Disable bookButton initially
        bookButton.setDisable(true);
    }

//    Callback is set to run automatically after appt is booked
    public void setOnAppointmentBooked(Runnable callback) {
        this.onAppointmentBooked = callback;
    }

//    Prevent customer from booking another appt on the same day
    private boolean hasCustomerBookingOnDate(Connection conn, int userId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE user_id = ? AND DATE(appointment_date) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }


    @FXML private void handleCreateAppointment() {
        if (bookButton.isDisable()) return;

        try {
            int userId = lookupUserId(customerCombo.getValue());
            int serviceId = lookupServiceId(serviceCombo.getValue());
            int staffId = lookupStaffId(staffCombo.getValue());
            int paymentMethodId = lookupPaymentMethodId(paymentMethodCombo.getValue());

//            validates incase the customer has another appt on that day
            if (hasCustomerBookingOnDate(DBConnection.getConnection(), userId, datePicker.getValue())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Booking Error");
                alert.setHeaderText(null);
                alert.setContentText("This customer already has an appointment on the selected date.");
                alert.showAndWait();
                return;
            }

            String staffColor = staffColourMap.getOrDefault(staffCombo.getValue(), "#AAAAAA");
            String status = "on-time";
            String remarks = remarksArea.getText();
            LocalDateTime appointmentDate = LocalDateTime.of(datePicker.getValue(), timeCombo.getValue());
            String serviceName = serviceCombo.getValue();
            String staffName = staffCombo.getValue();
            String paymentMethodName = paymentMethodCombo.getValue();
            String customerName = customerCombo.getValue();

            Appointment appt = new Appointment(
                    0, userId, serviceId, staffId, remarks, appointmentDate, status, paymentMethodId,
                    serviceName, staffName, paymentMethodName, staffColor, customerName
            );

            if (saveAppointmentToDB(appt)) {
                clearForm();
                showSuccess("Appointment booked successfully!");
                if (onAppointmentBooked != null) onAppointmentBooked.run();
                ((Stage) bookButton.getScene().getWindow()).close();
            }
        }
        catch (Exception e) {
            showError("Failed to create appointment: " + e.getMessage());
        }
    }

//    Appointment booked is inserted into the appointments table
    private boolean saveAppointmentToDB(Appointment a) {
        String sql = """
      INSERT INTO appointments
        (user_id, service_id, staff_id, remarks, appointment_date, status, payment_method_id)
      VALUES (?,       ?,          ?,        ?,       ?,                ?,      ?)
      """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

//            Convert form values (names) to IDs
            int userId   = lookupUserId(customerCombo.getValue());
            int serviceId= lookupServiceId(serviceCombo.getValue());
            int staffId  = lookupStaffId(staffCombo.getValue());
            int pmId     = lookupPaymentMethodId(paymentMethodCombo.getValue());

//            Bind parameters
            ps.setInt   (1, userId);
            ps.setInt   (2, serviceId);
            ps.setInt   (3, staffId);
            ps.setString(4, a.getRemarks());
            ps.setTimestamp(5, Timestamp.valueOf(a.getAppointmentDate()));
            ps.setString(6, a.getStatus());
            ps.setInt   (7, pmId);

            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            showError("Database error: " + e.getMessage());
            return false;
        }
    }

//    Makes sure all parameters have been filled
    private void validateForm() {
        boolean allFilled = customerCombo.getValue() != null
                && serviceCombo.getValue()!=null
                && staffCombo.getValue()!=null
                && datePicker.getValue()!=null
                && timeCombo.getValue()!=null
                && paymentMethodCombo.getValue()!=null;
        bookButton.setDisable(!allFilled);
    }

    private void clearForm() {
        customerCombo.getSelectionModel().clearSelection();
        serviceCombo.getSelectionModel().clearSelection();
        staffCombo.getSelectionModel().clearSelection();
        datePicker.setValue(LocalDate.now());
        timeCombo.getSelectionModel().clearSelection();
        remarksArea.clear();
        bookButton.setDisable(true);
    }

    private void showError(String msg) {
        System.out.println("Error: " + msg);
    }
    private void showSuccess(String msg) {
        System.out.println("Success: " + msg);
    }

    //    TO LOOK UP THE NAMES FROM THE TABLE'S ID
    private int lookupUserId(String customerName) throws SQLException {
        String sql = "SELECT id FROM users WHERE name = ? AND role = 'Customer'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
            throw new SQLException("No user found for " + customerName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int lookupServiceId(String serviceName) throws Exception {
        String sql = "SELECT id FROM services WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, serviceName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
            throw new SQLException("No service found for " + serviceName);
        }
    }


    private int lookupStaffId(String staffName) throws SQLException {
        String sql = "SELECT id FROM users WHERE name = ? AND role = 'Admin'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, staffName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
            throw new SQLException("No staff found for " + staffName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int lookupPaymentMethodId(String methodName) throws SQLException {
        String sql = "SELECT id FROM payment_methods WHERE method_name = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, methodName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
            throw new SQLException("No payment method found for " + methodName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
