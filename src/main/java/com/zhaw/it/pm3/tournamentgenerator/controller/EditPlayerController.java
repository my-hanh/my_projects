package com.zhaw.it.pm3.tournamentgenerator.controller;

import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * This class is the controller for the edit player view.
 */
public class EditPlayerController extends BaseMainController {

    @FXML
    private TextField playerName;
    @FXML
    private TextField playerEmail;
    @FXML
    private Label errorLabel;
    @FXML
    private Button saveButton;

    private Player player;

    /**
     * Sets the player to be edited and pre-fills the input fields with the player's details.
     *
     * @param player the Player object to be edited.
     */
    public void setPlayer(Player player) {
        this.player = player;
        playerName.setText(player.getName());
    }

    /**
     * Saves the updated player details.
     * Validates the player's name and email before saving the changes.
     * If the input is invalid, an appropriate error message is displayed.
     * If valid, updates the player's name and email and closes the current window.
     */
    @FXML
    private void savePlayer() {
        String name = playerName.getText();
        String email = playerEmail.getText();
        if (name == null || name.isEmpty()) {
            errorLabel.setText("Player name cannot be empty");
            errorLabel.setVisible(true);
        } else if (email.isEmpty() || email == null) {
            player.setName(name);
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        } else if (!playerEmail.getText().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            errorLabel.setText("Please enter a valid email address");
            errorLabel.setVisible(true);
        } else {
            player.setName(name);
            player.setEmail(email);
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Closes the current window and returns to the previous screen.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void backToCreateTournament(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}
