package com.example.spa_sys.controllers;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class MainPageController {
    @FXML private StackPane splashOverlay;
    @FXML private Label whenCalmLabel;
    @FXML private Label vCalmLabel;

    private Timeline splashTimeline;

    public void initialize() {
        playSplashAnimation();
    }

    @FXML
    private void showMainPage() {
        playSplashAnimation();
    }

//    Inside the splash container, its a loop and the animations fade in and out
    private void playSplashAnimation() {
        splashOverlay.setVisible(true);
        whenCalmLabel.setOpacity(0);
        vCalmLabel.setOpacity(0);
        vCalmLabel.setVisible(true);

        if (splashTimeline != null) {
            splashTimeline.stop();
        }

        splashTimeline = new Timeline(
//                Makes both labels invisible first
                new KeyFrame(Duration.ZERO, evt -> {
                    whenCalmLabel.setOpacity(0);
                    vCalmLabel.setOpacity(0);
                }),

//                Fade in at specific durations
                new KeyFrame(Duration.seconds(0.5), new KeyValue(whenCalmLabel.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(1.4), new KeyValue(vCalmLabel.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(3.0)), // hold
//                Fade both labels out
                new KeyFrame(Duration.seconds(3.3), new KeyValue(whenCalmLabel.opacityProperty(), 0)),
                new KeyFrame(Duration.seconds(3.3), new KeyValue(vCalmLabel.opacityProperty(), 0))
        );

        splashTimeline.setCycleCount(Animation.INDEFINITE); // Loop forever
        splashTimeline.play();
    }
}
