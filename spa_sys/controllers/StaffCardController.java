package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.Staff;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import java.sql.*;
import java.util.function.Consumer;

public class StaffCardController {
    @FXML TextField nameField, phoneField, emailField;
    @FXML ComboBox<String> genderBox, statusBox;
    @FXML ColorPicker colourPicker;
    @FXML Button saveButton, deleteButton;

    private Staff staff;
    private Consumer<Void> refreshCallback;
    private boolean isNew;

    public void initAsNew(Consumer<Void> cb) {
        isNew = true;
        refreshCallback = cb;
        deleteButton.setVisible(false);
        saveButton.setText("Create");

        genderBox.getItems().setAll("Male","Female","Other");
        genderBox.getSelectionModel().select("Other");
        statusBox.getItems().setAll("Available","On-Leave","Unavailable");
        statusBox.getSelectionModel().select("Available");
    }

    public void setStaff(Staff staff, Consumer<Void> cb) {
        isNew = false;
        this.staff = staff;
        this.refreshCallback = cb;

        nameField.setText(staff.getFullName());
        phoneField.setText(staff.getPhone());
        emailField.setText(staff.getEmail());
        genderBox.getItems().setAll("Male","Female","Other");
        genderBox.getSelectionModel().select(staff.getGender());
        statusBox.getItems().setAll("Available","On-Leave","Unavailable");
        statusBox.getSelectionModel().select(staff.getStatus());
        colourPicker.setValue(Color.web(staff.getColourCode()));

        saveButton.setText("Update");
    }

    @FXML
    private void onSave() {

//        Staff Info Validation
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String gender = genderBox.getValue();
        String status = statusBox.getValue();

        // Empty field checks
        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || gender == null || status == null) {
            showError("Please fill in all fields.");
            return;
        }

        // Phone: should be all digits
        if (!phone.matches("\\d+")) {
            showError("Phone number must contain only digits.");
            return;
        }

        // Email: should have a @ and .
        if (!email.matches("^[\\w-.]+@[\\w-]+(\\.[\\w-]+)+$")) {
            showError("Invalid email address.");
            return;
        }

        // Check for unique phone and email to ensure none are repeated
        try (Connection conn = DBConnection.getConnection()) {
            // Email
            String emailSql = isNew
                    ? "SELECT COUNT(*) FROM users WHERE email = ?"
                    : "SELECT COUNT(*) FROM users WHERE email = ? AND id <> ?";
            try (PreparedStatement ps = conn.prepareStatement(emailSql)) {
                ps.setString(1, email);
                if (!isNew) ps.setInt(2, staff.getId());
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showError("Email already exists.");
                    return;
                }
            }
            // Phone
            String phoneSql = isNew
                    ? "SELECT COUNT(*) FROM users WHERE phone = ?"
                    : "SELECT COUNT(*) FROM users WHERE phone = ? AND id <> ?";
            try (PreparedStatement ps = conn.prepareStatement(phoneSql)) {
                ps.setString(1, phone);
                if (!isNew) ps.setInt(2, staff.getId());
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    showError("Phone number already exists.");
                    return;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Database error during validation.");
            return;
        }

//        Insert the new data inside the database
        String sql = isNew
                ? "INSERT INTO users(name,phone,email,gender,status,colour_code,role) VALUES(?,?,?,?,?,?,?)"
                : "UPDATE users SET name=?,phone=?,email=?,gender=?,status=?,colour_code=?,role='Admin' WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nameField.getText());
            ps.setString(2, phoneField.getText());
            ps.setString(3, emailField.getText());
            ps.setString(4, genderBox.getValue());
            ps.setString(5, statusBox.getValue());
            ps.setString(6, toHex(colourPicker.getValue()));

            if (isNew) {
                // IF new insert the role column
                ps.setString(7, "Admin");
            } else {
                // IF updating use the staff's current ID
                // guard against null just in case
                if (staff == null) {
                    throw new IllegalStateException("Admin is null in UPDATE");
                }
                ps.setInt(7, staff.getId());
            }

            ps.executeUpdate();
            if (refreshCallback != null) refreshCallback.accept(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onDelete() {
        if (staff == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete this staff member?", ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        String sql = "DELETE FROM users WHERE id=? AND role='Admin'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, staff.getId());
            ps.executeUpdate();
            if (refreshCallback != null) refreshCallback.accept(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

//    For Staff to pick their colour rep
    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int)(c.getRed()*255),
                (int)(c.getGreen()*255),
                (int)(c.getBlue()*255));
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}
