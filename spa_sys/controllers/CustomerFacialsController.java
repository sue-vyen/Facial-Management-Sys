package com.example.spa_sys.controllers;

import com.example.spa_sys.database.DBConnection;
import com.example.spa_sys.models.Facial;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerFacialsController {

    @FXML private VBox facialsContainer;

    public void initialize() {
        loadFacials();
    }

//    Loads in all the facials available from the database
    private void loadFacials() {
        List<Facial> facials = fetchFacialsFromDatabase();

        for (Facial facial : facials) {
            try {
//                Loads individual facial cards fxml
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/spa_sys/customer_facial_cards.fxml"));
                Parent card = loader.load();

//                And then it sets it in the card controller
                CustomerFacialCardController cardController = loader.getController();
                cardController.setFacialData(facial); // Pass the facial object to the card controller

//                Each card is then added into the VBox
                facialsContainer.getChildren().add(card);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


//    Gets the facial entries from the db
    private List<Facial> fetchFacialsFromDatabase() {
        List<Facial> facials = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM services")) { // assuming your table is "services"

            while (rs.next()) {
                String name = rs.getString("name");
                String description = rs.getString("description");
                String price = rs.getString("price");
                String duration = rs.getString("duration");
                String ingredients = rs.getString("ingredients");
                String benefits = rs.getString("benefits");
                String image = rs.getString("image_path");

                Facial facial = new Facial(name, description, price, duration, ingredients, benefits, image );
                facials.add(facial);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return facials;
    }
}
