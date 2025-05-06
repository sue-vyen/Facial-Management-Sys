package com.example.spa_sys.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AppointmentCardController {

    @FXML private Label nameLabel;
    @FXML private Label serviceLabel;
    @FXML private Label facialistLabel;
    @FXML private Label remarksLabel;

    public void setAppointmentData(String name, String service, String facialist, String remarks) {
        nameLabel.setText(name);
        serviceLabel.setText(service);
        facialistLabel.setText(facialist);
        remarksLabel.setText(remarks);
    }
}
