package com.zhaw.it.pm3.tournamentgenerator.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * This class is the controller for the register players view.
 */
public class RegisterPlayersUIController {

    /**
     * Opens the create new tournament view.
     *
     * @param event the event that triggered this method.
     */
    public void handleConfirm(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("createNewTournamentUI.fxml"));
            Parent settingsUIParent = loader.load();
            Scene settingsUIScene = new Scene(settingsUIParent);

            // Get the current stage and set the new scene
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(settingsUIScene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}