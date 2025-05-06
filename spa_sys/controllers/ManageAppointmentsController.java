package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.sql.*;
import java.sql.Date;
import java.time.*;
import java.util.*;

public class ManageAppointmentsController {
//    UI ELEMENTS
    @FXML private HBox statusBoard;
    @FXML private DatePicker datePicker;
    @FXML private Button showAllAppointmentsButton;

//     Editor fields
    @FXML private VBox editorPane;
    @FXML private TextField editCustomer;
    @FXML private ComboBox<String> editService;
    @FXML private ComboBox<String> editStaff;
    @FXML private DatePicker editDate;
    @FXML private ComboBox<LocalTime> editTime;
    @FXML private TextArea editRemarks;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button deleteButton;

    private int editingAppointmentId = -1;
    private final String[] statuses = {"on-time", "late", "in-session", "completed", "cancelled"};
    private final Map<String, VBox> statusColumns = new HashMap<>();

    @FXML
    private void initialize() {
//        Creates each status column
        buildStatusColumns();
//        Hides the editor first
        editorPane.setVisible(false);
        loadServices(); loadStaff(); loadPaymentMethods();
//        The available times
        editTime.getItems().setAll(
                LocalTime.of(9,0), LocalTime.of(10,0), LocalTime.of(11,0),
                LocalTime.of(14,0), LocalTime.of(15,0), LocalTime.of(16,0), LocalTime.of(17,0)
        );
        saveButton.setOnAction(e -> onSaveEdit());
        cancelButton.setOnAction(e -> onCancelEdit());
        deleteButton.setOnAction(e -> onDeleteEdit());
        datePicker.setOnAction(e -> onDateSelected());
        datePicker.setValue(null);

        automateStatuses();
        loadAppointments(null);
    }

    private void automateStatuses() {
        try (Connection conn = DBConnection.getConnection()) {
            // on-time -> late if the appt is late from their booking time
            String lateSql = "UPDATE appointments SET status='late' WHERE status='on-time' AND appointment_date < ?";
            PreparedStatement pl = conn.prepareStatement(lateSql);
            pl.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now())); pl.executeUpdate();

            // late -> cancelled if the appt is 1 day overdue
            String cancelSql = "UPDATE appointments SET status='cancelled' WHERE status='late' AND appointment_date < ?";
            PreparedStatement pc = conn.prepareStatement(cancelSql);
            pc.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now().minusDays(1))); pc.executeUpdate();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadServices() {
        editService.getItems().clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM services")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) editService.getItems().add(rs.getString("name"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadStaff() {
        editStaff.getItems().clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT name FROM users WHERE role='Admin' AND status='Available'")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) editStaff.getItems().add(rs.getString("name"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPaymentMethods() {
        paymentMethodCombo.getItems().clear();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT method_name FROM payment_methods")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) paymentMethodCombo.getItems().add(rs.getString("method_name"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void onDateSelected() {
        loadAppointments(datePicker.getValue());
    }

    @FXML private void onShowAll() {
        datePicker.setValue(null);
        loadAppointments(null);
    }


    private void buildStatusColumns() {
        statusBoard.getChildren().clear(); statusColumns.clear();
        for (String st : statuses) {
            VBox col = new VBox(10);
            col.setStyle("-fx-background-color:#f0f0f0; -fx-padding:10; -fx-border-color:grey;");
            col.setPrefWidth(200);
            Label hdr = new Label(st.toUpperCase()); hdr.setStyle("-fx-font-weight:bold;");
            col.getChildren().add(hdr);
            enableDrop(col, st); // <-- this enables the drag and drop feature
            statusColumns.put(st, col);
            statusBoard.getChildren().add(col);
        }
    }

    private void loadAppointments(LocalDate date) {
        automateStatuses();
        statusColumns.values().forEach(v -> v.getChildren().removeIf(n -> n instanceof HBox));

        boolean filt = date != null;
        String base =
                "SELECT a.id, u.name AS customer_name, s.name AS service, st.name AS facialist, st.colour_code, a.remarks, a.status " +
                        "FROM appointments a " +
                        "JOIN users u ON a.user_id = u.id " +
                        "JOIN services s ON a.service_id = s.id " +
                        "JOIN users st ON a.staff_id = st.id";
        String sql = filt ? base + " WHERE DATE(a.appointment_date)=?" : base;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (filt) ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int    id   = rs.getInt("id");
                String cust = rs.getString("customer_name");
                String svc  = rs.getString("service");
                String fac  = rs.getString("facialist");
                String col  = rs.getString("colour_code");
                String st   = rs.getString("status");
                statusColumns.get(st).getChildren().add(createCard(id, cust, svc, fac, col, st));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    Makes the appt card draggable with colour + event listeners
    private HBox createCard(int id, String cust, String svc, String fac, String colour, String status) {
        HBox card = new HBox(10);
        card.setUserData(id);
        card.setStyle("-fx-background-color:"+colour+"; -fx-padding:10; -fx-border-radius:5; -fx-background-radius:5;");
        card.getChildren().add(new Label(cust + " ("+svc+")"));

        card.setOnDragDetected(e -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent cc = new ClipboardContent();
            cc.putString(String.valueOf(id));
            db.setContent(cc);
            e.consume();
        });

        card.setOnMouseClicked(e -> loadAppointmentIntoEditor(id));

        if ("cancelled".equals(status)) {
            ContextMenu menu = new ContextMenu();
            MenuItem del = new MenuItem("Delete");
            del.setOnAction(ev -> { deleteAppointmentById(id); ((Pane)card.getParent()).getChildren().remove(card);} );
            menu.getItems().add(del);
            card.setOnContextMenuRequested(ev -> menu.show(card, ev.getScreenX(), ev.getScreenY()));
        }
        return card;
    }

    private void loadAppointmentIntoEditor(int id) {
        String sql = "SELECT u.name AS customer_name, s.name AS service, st.name AS facialist, " +
                "a.remarks, a.appointment_date, a.status, a.payment_method_id " +
                "FROM appointments a " +
                "JOIN users u ON a.user_id = u.id " +
                "JOIN services s ON a.service_id = s.id " +
                "JOIN users st ON a.staff_id = st.id " +
                "WHERE a.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                editingAppointmentId = id;
                editorPane.setVisible(true);
                editCustomer.setText(rs.getString("customer_name"));
                editService.setValue(rs.getString("service"));
                editStaff.setValue(rs.getString("facialist"));
                LocalDateTime dt = rs.getTimestamp("appointment_date").toLocalDateTime();
                editDate.setValue(dt.toLocalDate());
                editTime.setValue(dt.toLocalTime());
                editRemarks.setText(rs.getString("remarks"));
                paymentMethodCombo.setValue(lookupPaymentMethodName(rs.getInt("payment_method_id")));
                deleteButton.setVisible("cancelled".equals(rs.getString("status")));
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    private void onSaveEdit() {
        if (editingAppointmentId < 0) return;
        LocalDateTime newDt = LocalDateTime.of(editDate.getValue(), editTime.getValue());

        // conflict check...
        String chk = "SELECT COUNT(*) FROM appointments WHERE staff_id=? AND id<>? AND appointment_date BETWEEN ? AND ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(chk)) {
            int stfId = lookupStaffId(editStaff.getValue());
            ps.setInt(1, stfId);
            ps.setInt(2, editingAppointmentId);
            ps.setTimestamp(3, Timestamp.valueOf(newDt.minusMinutes(90)));
            ps.setTimestamp(4, Timestamp.valueOf(newDt.plusMinutes(90)));
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1)>0) {
                new Alert(Alert.AlertType.WARNING, editStaff.getValue()+
                        " is booked within 90 minutes.").showAndWait();
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error checking appointment conflict: " + e.getMessage());
            return;
        }

        String upd = "UPDATE appointments SET user_id=?, service_id=?, staff_id=?, remarks=?, appointment_date=?, payment_method_id=? WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(upd)) {
            ps.setInt(1, lookupUserId(editCustomer.getText()));
            ps.setInt(2, lookupServiceId(editService.getValue()));
            ps.setInt(3, lookupStaffId(editStaff.getValue()));
            ps.setString(4, editRemarks.getText());
            ps.setTimestamp(5, Timestamp.valueOf(newDt));
            ps.setInt(6, lookupPaymentMethodId(paymentMethodCombo.getValue()));
            ps.setInt(7, editingAppointmentId);
            ps.executeUpdate();
            editorPane.setVisible(false);
            loadAppointments(datePicker.getValue());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error saving appointment: " + e.getMessage());
        }
    }

    @FXML private void onCancelEdit() {
        editorPane.setVisible(false);
        editingAppointmentId = -1;
    }

    @FXML
    private void deleteAppointmentById(int id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM appointments WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void onDeleteAppointment(ActionEvent actionEvent) {
        // Handle the delete button in the editor pane
        int selectedAppointmentId = -1;
        if (selectedAppointmentId != -1) {
            deleteAppointmentById(selectedAppointmentId);
        }
    }

    private void onDeleteEdit() {
        if (editingAppointmentId >= 0) {
            deleteAppointmentById(editingAppointmentId);
            editorPane.setVisible(false);
            loadAppointments(datePicker.getValue());
            editingAppointmentId = -1;
        }
    }


//    Drag and drop feature
    private void enableDrop(VBox column, String newStatus) {
        column.setOnDragOver(e -> {
            if (e.getGestureSource()!=column && e.getDragboard().hasString())
                e.acceptTransferModes(TransferMode.MOVE);
            e.consume();
        });
        column.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                int apptId = Integer.parseInt(db.getString());
                updateAppointmentStatusInDB(apptId, newStatus);

                if ("completed".equals(newStatus) && !saleAlreadyExists(apptId)) {
                    // lookup the details then insert
                    try (Connection conn = DBConnection.getConnection();
                         PreparedStatement ps = conn.prepareStatement(
                                 "SELECT a.service_id, a.user_id, s.price, a.payment_method_id " +
                                         "FROM appointments a " +
                                         "JOIN services s ON a.service_id = s.id " +
                                         "WHERE a.id = ?")) {
                        ps.setInt(1, apptId);
                        ResultSet rs = ps.executeQuery();
                        if (rs.next()) {
                            createSaleFromAppointment(
                                    apptId,
                                    rs.getInt("service_id"),
                                    rs.getInt("user_id"),
                                    rs.getDouble("price"),
                                    rs.getInt("payment_method_id")
                            );
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                loadAppointments(datePicker.getValue());
                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    private Node findCardById(int id) {
        for (VBox col : statusColumns.values()) {
            for (Node n : col.getChildren()) {
                if (n instanceof HBox && id == (Integer)n.getUserData()) return n;
            }
        }
        return null;
    }

    private void updateAppointmentStatusInDB(int id, String status) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE appointments SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//  CHECKS FOR DUPLICATES
    private boolean saleAlreadyExists(int appointmentId) {
        String check = "SELECT 1 FROM sales WHERE appointment_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, appointmentId);
            return ps.executeQuery().next();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

//    Updates the sales table
    private void createSaleFromAppointment(int appointmentId,
                                           int serviceId,
                                           int userId,
                                           double amount,
                                           int paymentMethodId) {
        String insert =
                "INSERT INTO sales (service_id, user_id, appointment_id, amount, sale_date, sale_time, payment_method_id) " +
                        "VALUES (?,?,?,?,CURDATE(),CURTIME(),?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, serviceId);
            ps.setInt(2, userId);
            ps.setInt(3, appointmentId);
            ps.setDouble(4, amount);
            ps.setInt(5, paymentMethodId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

//  Lookups
    private int lookupUserId(String customerName) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id FROM users WHERE name=? AND role='Customer'")) {
            ps.setString(1, customerName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("No user found: " + customerName);
    }

    private int lookupServiceId(String serviceName) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM services WHERE name=?")) {
            ps.setString(1, serviceName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new SQLException("No service found: " + serviceName);
    }

    private int lookupStaffId(String staffName) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id FROM users WHERE name=? AND role='Admin'")) {
            ps.setString(1, staffName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("No staff found: " + staffName);
    }

    private int lookupPaymentMethodId(String methodName) throws Exception {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT id FROM payment_methods WHERE method_name=?")) {
            ps.setString(1, methodName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        throw new SQLException("No payment method found: " + methodName);
    }

    private String lookupPaymentMethodName(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT method_name FROM payment_methods WHERE id=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
