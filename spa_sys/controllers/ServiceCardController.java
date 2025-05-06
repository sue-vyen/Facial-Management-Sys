package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.Services;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

public class ServiceCardController {
    @FXML private ImageView serviceImage;
    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField priceField;
    @FXML private TextField durationField;
    @FXML private TextArea ingredientsArea;
    @FXML private TextArea benefitsArea;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button changeImageButton;

    private boolean isNew = false;
    private Services service;
    private Consumer<Void> refreshCallback;
    private String currentImagePath;


    public void initAsNew(Consumer<Void> refreshCallback) {
        this.isNew = true;
        this.refreshCallback = refreshCallback;
        this.service = null;

        // clear all UI fields
        nameField.clear();
        descriptionArea.clear();
        priceField.clear();
        durationField.clear();
        ingredientsArea.clear();
        benefitsArea.clear();
        serviceImage.setImage(null);

        deleteButton.setVisible(false);
        updateButton.setText("Save");
    }


    public void setService(Services service) {
        this.isNew = false;
        this.service = service;

        nameField.setText(service.getName());
        descriptionArea.setText(service.getDescription());
        priceField.setText(String.valueOf(service.getPrice()));
        durationField.setText(String.valueOf(service.getDuration()));
        ingredientsArea.setText(service.getIngredients());
        benefitsArea.setText(service.getBenefits());
        currentImagePath = service.getImagePath();
        loadImage(currentImagePath);

        deleteButton.setVisible(true);
        updateButton.setText("Update");
    }


    public void setRefreshCallback(Consumer<Void> refreshCallback) {
        this.refreshCallback = refreshCallback;
    }

    private void loadImage(String path) {
        if (path == null || path.isEmpty()) return;
        try (FileInputStream fis = new FileInputStream(path)) {
            Image img = new Image(fis);
            serviceImage.setImage(img);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onUpdate() {
        // Validate fields
        String priceText = priceField.getText();
        String durationText = durationField.getText();
        double price;
        int duration;

        // Price validation (integer or decimal)
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Invalid input", "Price must be a positive number.");
            priceField.requestFocus();
            return;
        }

        // Duration validation (integer only)
        try {
            duration = Integer.parseInt(durationText);
            if (duration < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Invalid input", "Duration must be a positive integer (minutes).");
            durationField.requestFocus();
            return;
        }

        String sql;
        if (isNew) {
            sql = "INSERT INTO services(name, description, price, duration, ingredients, benefits, image_path) VALUES (?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE services SET name=?, description=?, price=?, duration=?, ingredients=?, benefits=?, image_path=? WHERE id=?";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nameField.getText());
            ps.setString(2, descriptionArea.getText());
            ps.setDouble(3, price);
            ps.setInt(4, duration);
            ps.setString(5, ingredientsArea.getText());
            ps.setString(6, benefitsArea.getText());
            ps.setString(7, currentImagePath);
            if (!isNew) {
                ps.setInt(8, service.getId());
            }
            ps.executeUpdate();

            if (refreshCallback != null) {
                refreshCallback.accept(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to save service. " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    private void onDelete() {
        if (service == null) return;
        String sql = "DELETE FROM services WHERE id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, service.getId());
            ps.executeUpdate();

            if (refreshCallback != null) {
                refreshCallback.accept(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onChangeImage() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        Window win = serviceImage.getScene().getWindow();
        File file = chooser.showOpenDialog(win);
        if (file != null) {
            currentImagePath = file.getAbsolutePath();
            loadImage(currentImagePath);
        }
    }
}