package com.example.spa_sys;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
//        Load in the fonts used for the app
        Font.loadFont(getClass().getResourceAsStream("/fonts/PlayfairDisplay-Regular.ttf"), 16);
        Font.loadFont(getClass().getResourceAsStream("/fonts/PlayfairDisplay-Bold.ttf"), 16);
        Font.loadFont(getClass().getResourceAsStream("/fonts/PlayfairDisplay-Italic.ttf"), 16);
        Font.loadFont(getClass().getResourceAsStream("/fonts/PlayfairDisplay-BoldItalic.ttf"), 16);

//        Load the FXML file and set the scene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
//        System.out.println(getClass().getResource("login.fxml"));
        Parent root = loader.load();

        stage.setTitle("V-Calm");
        stage.setScene(new Scene(root));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
