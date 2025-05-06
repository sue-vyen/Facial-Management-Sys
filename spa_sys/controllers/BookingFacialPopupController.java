package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.User;
import com.example.spa_sys.models.Facial;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class BookingFacialPopupController {

//    UI elements used in this page
    @FXML private Label facialNameLabel;
    @FXML private DatePicker bookingDatePicker;
//    @FXML private ComboBox<String> facialistComboBox;
    @FXML private ComboBox<String> timeComboBox;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private CheckBox termsCheckBox;
    @FXML private Button bookButton;
    @FXML private Button cancelButton;

//    FOR INPERSON CHANGES
    @FXML private VBox facialistRadioBox;


    //    object Facial is passed in from customer_facials
    private Facial selectedFacial;
    private ToggleGroup facialistToggleGroup;

    //    To set the facial details when card is clicked + facial's name will be set alr
    public void setFacialDetails(Facial facial) {
        this.selectedFacial = facial;
        facialNameLabel.setText(facial.getName());
    }

    @FXML
    private void initialize() {
//        Only allow today or future dates in the calendar
        bookingDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #F6FAFD; -fx-opacity: 0.7;");
                }
            }
        });

//        Sets the default day to today
        bookingDatePicker.setValue(LocalDate.now());

//        Facialist toggle area
        facialistToggleGroup = new ToggleGroup();


//         Populate combo boxes for facialist, payment methods, available times to pick
        loadFacialists();
        loadPaymentMethods();
        loadAvailableTimes();

        bookButton.setOnAction(e -> onBookFacial(e));
        cancelButton.setOnAction(e -> handleCancel());
    }

//    Fetches all facialist names from the database
//    private void loadFacialists() {
//        try (Connection conn = DBConnection.getConnection()) {
//            String sql = "SELECT name FROM users WHERE role = 'Admin'"; // assuming you tag facialists as 'staff' in users table
//            PreparedStatement stmt = conn.prepareStatement(sql);
//            ResultSet rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                facialistComboBox.getItems().add(rs.getString("name"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void loadFacialists() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT name FROM users WHERE role = 'Admin'"; // assuming you tag facialists as 'staff' in users table
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");

                RadioButton radioButton = new RadioButton(name);
                radioButton.setToggleGroup(facialistToggleGroup);
                facialistRadioBox.getChildren().add(radioButton);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //    fetches the payment methods from the db
    private void loadPaymentMethods() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT method_name FROM payment_methods";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                paymentMethodComboBox.getItems().add(rs.getString("method_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    Fetches the available times
    private void loadAvailableTimes() {
        timeComboBox.getItems().addAll(
                "9:00 AM", "10:00 AM", "11:00 AM",
                "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM"
        );
    }


//    BOOKING VALIDATIONS SO NOTHING CLASHES IRL
//    Checks whether a customer has an existing facial that day OR facialist is unavailable
    private boolean hasCustomerBookingOnDate(Connection conn, int userId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE user_id = ? AND DATE(appointment_date) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

//    Checks whether that same customer has a facial on that exact same time too
    private boolean hasCustomerTimeClash(Connection conn, int userId, LocalDateTime dateTime) throws SQLException {
        String sql = "SELECT COUNT(*) FROM appointments WHERE user_id = ? AND appointment_date = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(dateTime));
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

//    Checks if the facialist chosen is not available 90mins bfr/aft that slot
    private boolean isFacialistBooked(Connection conn, int staffId, LocalDateTime dateTime) throws SQLException {
//         90 min window before and after
        LocalDateTime start = dateTime.minusMinutes(90);
        LocalDateTime end = dateTime.plusMinutes(90);

        String sql = "SELECT COUNT(*) FROM appointments WHERE staff_id = ? AND appointment_date BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staffId);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(start));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(end));
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }


//    THE PROCESS AFTER THE BOOK BUTTON IS CLICKED (validation + inserting into DB)
//    checks if all parameters have been filled
    @FXML
    private void onBookFacial(ActionEvent event) {
        if (bookingDatePicker.getValue() == null ||
                timeComboBox.getValue() == null ||
//                facialistComboBox.getValue() == null ||
                facialistToggleGroup.getSelectedToggle() == null ||
                paymentMethodComboBox.getValue() == null ||
                !termsCheckBox.isSelected()) {

            showAlert(Alert.AlertType.ERROR, "Incomplete Form", "Please fill in all fields and agree to the Terms & Conditions.");
            return;
        }

        LocalDate selectedDate = bookingDatePicker.getValue();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        LocalTime selectedTime;

        try {
            selectedTime = LocalTime.parse(timeComboBox.getValue(), formatter);
        } catch (DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Time", "Please pick again.");
            return;
        }
        LocalDateTime dateTime = LocalDateTime.of(selectedDate, selectedTime);
        DateTimeFormatter sqlFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String sqlDateTime = dateTime.format(sqlFmt);

//        String selectedFacialist = facialistComboBox.getValue();
        String selectedFacialist = ((RadioButton) facialistToggleGroup.getSelectedToggle()).getText();
        String selectedPayment = paymentMethodComboBox.getValue();

        String facialName = selectedFacial.getName();
        int userId = User.getLoggedInUserId();
//        To check in the system if its detected the right user id
        System.out.println("Logged in User ID: " + User.getLoggedInUserId());
        String status = "on-time"; // default status

        try (Connection conn = DBConnection.getConnection()) {
//             Find service_id
            int serviceId = findServiceId(conn, facialName);

//             Find staff_id
            int staffId = findStaffId(conn, selectedFacialist);

//             Find payment_method_id
            int paymentMethodId = findPaymentMethodId(conn, selectedPayment);

//            Errors that will be shown if validation is triggered
            if (hasCustomerBookingOnDate(conn, userId, selectedDate)) {
                showAlert(Alert.AlertType.ERROR, "Booking Error", "You can only book one facial per day.");
                return;
            }
            if (hasCustomerTimeClash(conn, userId, dateTime)) {
                showAlert(Alert.AlertType.ERROR, "Booking Error", "You already have an appointment at this time.");
                return;
            }
            if (isFacialistBooked(conn, staffId, dateTime)) {
                showAlert(Alert.AlertType.ERROR, "Booking Error", "Selected facialist is already booked within 90 minutes of this slot.");
                return;
            }

//             Insert into appointments
            String sql = "INSERT INTO appointments (user_id, service_id, staff_id, appointment_date, status, payment_method_id) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, serviceId);
            stmt.setInt(3, staffId);
            stmt.setString(4, sqlDateTime);
            stmt.setString(5, status);
            stmt.setInt(6, paymentMethodId);

            stmt.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Booking Successful", "Your appointment has been booked!");

//             Close the popup
            Stage stage = (Stage) bookButton.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to book appointment. Please try again.");
        }
    }

    @FXML
    private void handleCancel() {
//         Close the popup
        ((Stage) cancelButton.getScene().getWindow()).close();
    }


//    HELPER METHODS SINCE IDS ARE USED THROUGHOUT DB SO, GETTING THE NAMES IS NEEDED!
    private int findServiceId(Connection conn, String serviceName) throws SQLException {
        String sql = "SELECT id FROM services WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, serviceName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Service not found: " + serviceName);
            }
        }
    }

    private int findStaffId(Connection conn, String staffName) throws SQLException {
        String sql = "SELECT id FROM users WHERE name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, staffName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Staff not found: " + staffName);
            }
        }
    }

    private int findPaymentMethodId(Connection conn, String paymentName) throws SQLException {
        String sql = "SELECT id FROM payment_methods WHERE method_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, paymentName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new SQLException("Payment Method not found: " + paymentName);
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
