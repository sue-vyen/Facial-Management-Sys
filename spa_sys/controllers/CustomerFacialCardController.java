package com.example.spa_sys.controllers;

import com.example.spa_sys.models.Facial;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class CustomerFacialCardController {

    @FXML private Label serviceNameLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label priceLabel;
    @FXML private Label durationLabel;
    @FXML private Label ingredientsLabel;
    @FXML private Label benefitsLabel;
    @FXML private ImageView serviceImage;
    @FXML private Button bookButton;

    private Facial facial; // store facial object for use later

//    Sets the data for the facials available
    public void setFacialData(Facial facial) throws FileNotFoundException {
        this.facial = facial;

//        Text details about the facial
        serviceNameLabel.setText(facial.getName());
        descriptionLabel.setText(facial.getDescription());
        priceLabel.setText("RM" + facial.getPrice());
        durationLabel.setText(facial.getDuration() + " mins");
        ingredientsLabel.setText(facial.getIngredients());
        benefitsLabel.setText(facial.getBenefits());

//        The image to get for each facial
        String path = facial.getImagePath();
        if (path != null && !path.isBlank()) {
            try (FileInputStream inputStream = new FileInputStream(path)) {
                serviceImage.setImage(new Image(inputStream));
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

//        In the case there is no image, it sets a default background
        if (serviceImage.getImage() == null) {
            serviceImage.setImage(new Image(getClass().getResourceAsStream("/images/flower_meadow.jpg")));
        }
    }

//    The book button for customers
    @FXML
    private void onBookButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/BookingFacialPopup.fxml"));
            Parent root = loader.load();

//            Passes the facial details to the popup window
            BookingFacialPopupController popupController = loader.getController();
            popupController.setFacialDetails(facial);

            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Book Facial");
            popupStage.setScene(new Scene(root));
            popupStage.showAndWait(); // block until popup closes

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
