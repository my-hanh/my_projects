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
 * This class is the controller for the add player view.
 */
public class AddPlayerController extends BaseMainController {

    @FXML
    private TextField playerName;
    @FXML
    private TextField playerEmail;
    @FXML
    private Label errorLabel;
    @FXML
    private Button addPlayerButton;

    private PlayerAssignmentController playerAssignmentController;

    /**
     * Sets the PlayerAssignmentController to handle player assignment logic.
     *
     * @param controller the PlayerAssignmentController to set.
     */
    public void setPlayerAssignmentController(PlayerAssignmentController controller) {
        this.playerAssignmentController = controller;
    }

    /**
     * Adds a new player to the player list.
     * Validates the input fields for player name and email before creating
     * a new Player object. If the input is invalid, displays an appropriate
     * error message. If valid, the player is added via the PlayerAssignmentController,
     * and the window is closed.
     */
    @FXML
    private void addPlayerToList() {
        String name = playerName.getText();
        String email = playerEmail.getText();
        if (name == null || name.isEmpty()) {
            errorLabel.setText("Player name cannot be empty");
            errorLabel.setVisible(true);
        } else if (!name.matches("[a-zA-Z\\s]+")) {
            errorLabel.setText("Please enter a valid name");
            errorLabel.setVisible(true);
        } else if (email.isEmpty()) {
            Player newPlayer = new Player(name);
            playerAssignmentController.addAvailablePlayer(newPlayer);

            addPlayerButton.getScene().getWindow().hide();
        } else if (!email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            errorLabel.setText("Please enter a valid email address");
            errorLabel.setVisible(true);
        } else {
            Player newPlayer = new Player(name, email);
            playerAssignmentController.addAvailablePlayer(newPlayer);

            addPlayerButton.getScene().getWindow().hide();
        }
    }

    /**
     * Closes the player editor window.
     * This method is triggered by an event, such as clicking a cancel button,
     * and closes the current stage (window).
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void exitPlayerEditor(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}
