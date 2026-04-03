package com.zhaw.it.pm3.tournamentgenerator.ui;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The {@code UIHandler} class serves as the main entry point for the JavaFX application.
 * It handles the initialization and display of the main user interface.
 */
public class UIHandler extends Application {

    @FXML
    private Stage stage;

    /**
     * Constructs a new {@code UIHandler} object.
     * Currently, this constructor does not perform any specific initialization.
     */
    public UIHandler() {
    }

    /**
     * The main entry point for the JavaFX application.
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the main scene will be set.
     * @throws Exception if an error occurs during application startup.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        mainWindow(primaryStage);

    }

    /**
     * Loads and displays the main window of the application.
     *
     * @param stage the {@link Stage} on which the main scene will be set.
     * @throws RuntimeException if the FXML file cannot be loaded.
     */
    private void mainWindow(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menuUI.fxml"));
            Pane rootNode = loader.load();

            //StartPageController startController = loader.getController();

            Scene scene = new Scene(rootNode);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
