package com.zhaw.it.pm3.tournamentgenerator.controller;

import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * This class is the controller for the edit team name view.
 */
public class EditTeamNameController {
    @FXML
    private TextField teamNameField;

    private Team team;

    /**
     * Sets the team to be edited and pre-fills the input field with the team's current name.
     *
     * @param team the Team object to be edited.
     */
    public void setTeam(Team team) {
        this.team = team;
        teamNameField.setText(team.getName());
    }

    /**
     * Saves the new name for the team.
     * Validates the input to ensure it is not null or empty before updating the team's name.
     * Closes the current window after saving.
     */
    @FXML
    private void saveTeamName() {
        String newName = teamNameField.getText();
        if (newName != null && !newName.isEmpty()) {
            team.setName(newName);
            ((Stage) teamNameField.getScene().getWindow()).close();
        }
    }

    /**
     * Closes the current window without saving any changes.
     */
    @FXML
    private void cancel() {
        ((Stage) teamNameField.getScene().getWindow()).close();
    }
}
