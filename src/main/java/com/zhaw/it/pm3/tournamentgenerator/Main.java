package com.zhaw.it.pm3.tournamentgenerator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Main class to start the application.
 */
public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the application.
     *
     * @param stage The stage to start the application.
     * @throws Exception If the application cannot be started.
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/menuUI.fxml"));
        Parent mainMenuUIParent = loader.load();
        Scene mainMenuUIScene = new Scene(mainMenuUIParent);

        // Load the image from the resources directory
        Image icon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/WindowIcon.png")));

        // Get the current stage and set the new scene
        stage.setTitle("Tournament Generator");
        stage.getIcons().add(icon);
        stage.setScene(mainMenuUIScene);
        stage.setResizable(true);
        stage.show();
    }
}