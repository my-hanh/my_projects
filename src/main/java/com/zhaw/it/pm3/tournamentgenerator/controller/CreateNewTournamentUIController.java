package com.zhaw.it.pm3.tournamentgenerator.controller;

import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.*;
import com.zhaw.it.pm3.tournamentgenerator.domain.DataModel;
import com.zhaw.it.pm3.tournamentgenerator.domain.Tournament;
import com.zhaw.it.pm3.tournamentgenerator.persistence.PersistenceTournaments;
import com.zhaw.it.pm3.tournamentgenerator.persons.Administrator;
import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

/**
 * This class is the controller for the create new tournament view.
 */
public class CreateNewTournamentUIController {

    @FXML
    private TextField teamCountTextField;

    @FXML
    private Label creatorIdLabel;

    @FXML
    private TextField tournamentNameTextField;

    private Administrator admin;

    private DataModel dataModel;

    @FXML
    private Button confirmButton;

    @FXML
    private TextArea summaryTextArea;

    @FXML
    private ComboBox<String> tournamentTypeComboBox;

    @FXML
    private ComboBox<String> gameComboBox;

    /**
     * Initializes the controller.
     * Sets up default values for the combo boxes, populates fields from the DataModel,
     * and adds listeners to update the summary and confirm button state.
     */
    @FXML
    public void initialize() {
        dataModel = DataModel.getInstance();

        // Populate combo boxes
        tournamentTypeComboBox.getItems().clear();
        tournamentTypeComboBox.getItems().addAll(new SingleElimination().toString());
        tournamentTypeComboBox.setValue(new SingleElimination().toString());
        gameComboBox.getItems().clear();
        gameComboBox.getItems().addAll("Football", "Basketball", "Volleyball");
        gameComboBox.setValue("Football");

        if (dataModel.getSelectedTournamentMode() != null) {
            tournamentTypeComboBox.setValue(dataModel.getSelectedTournamentMode());
        }
        if (dataModel.getSelectedGame() != null) {
            gameComboBox.setValue(dataModel.getSelectedGame());
        }
        if (dataModel.getCreatorId() != null) {
            creatorIdLabel.setText(dataModel.getCreatorId());
        }

        // When the user selects a tournament type, store it in DataModel
        tournamentTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            dataModel.setSelectedTournamentMode(newVal);
            updateConfirmButtonState();
        });

        // Similarly for game selection
        gameComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            dataModel.setSelectedGame(newVal);
        });

        tournamentNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            dataModel.setTournamentName(newValue);
            updateConfirmButtonState();
        });

        teamCountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int newTeamCount = Integer.parseInt(newValue);
                if (newTeamCount != dataModel.getTeamCount()) {
                    dataModel.setTeamCount(newTeamCount);

                    // Move players from old teams to available players list
                    ObservableList<Player> availablePlayers = dataModel.getPlayers();
                    for (Team team : dataModel.getTeams()) {
                        availablePlayers.addAll(team.getPlayers());
                        team.getPlayers().clear();
                    }

                    // Reinitialize teams
                    dataModel.setTeams(FXCollections.observableArrayList(generateTeams(newTeamCount)));
                }
            } catch (NumberFormatException e) {
                dataModel.setTeamCount(0);
                dataModel.setTeams(FXCollections.observableArrayList()); // Clear teams
            }
        });

        // Continue with your existing logic for teamCount and tournamentName
        if (dataModel.getTournamentName() != null) {
            tournamentNameTextField.setText(dataModel.getTournamentName());
        }
        if (dataModel.getTeamCount() > 0) {
            teamCountTextField.setText(String.valueOf(dataModel.getTeamCount()));
        }

        confirmButton.setDisable(true);

        // Add listeners for DataModel changes as before
        dataModel.getPlayers().addListener((ListChangeListener<Player>) c -> {
            updateConfirmButtonState();
            updateSummary();
        });
        dataModel.getTeams().addListener((ListChangeListener<Team>) c -> {
            updateConfirmButtonState();
            updateSummary();
        });

        updateSummary();
        updateConfirmButtonState();
    }

    /**
     * Updates the summary text area with details about teams, players, and unassigned players.
     */
    private void updateSummary() {
        StringBuilder summary = new StringBuilder();

        // Count total players as those still in the unassigned list plus those in all teams
        int totalPlayers = dataModel.getPlayers().size();
        for (Team team : dataModel.getTeams()) {
            totalPlayers += team.getPlayers().size();
        }

        // Summarize Teams
        summary.append("Teams:\n");
        for (Team team : dataModel.getTeams()) {
            summary.append(" - ").append(team.getName()).append(": ");
            if (team.getPlayers().isEmpty()) {
                summary.append("No players assigned.\n");
            } else {
                summary.append(team.getPlayers().size()).append(" players\n");
                for (Player player : team.getPlayers()) {
                    summary.append("    * ").append(player.getName()).append("\n");
                }
            }
        }

        summary.append("\nTotal Players: ").append(totalPlayers).append("\n");

        // List unassigned players if there are any
        if (!dataModel.getPlayers().isEmpty()) {
            summary.append("Unassigned Players:\n");
            for (Player player : dataModel.getPlayers()) {
                summary.append(" - ").append(player.getName()).append("\n");
            }
        }

        summaryTextArea.setText(summary.toString());
    }

    /**
     * Updates the state of the confirm button based on the current input validation.
     * The button is enabled only if all required fields are valid.
     */
    private void updateConfirmButtonState() {
        String name = tournamentNameTextField.getText();
        String teamCountStr = teamCountTextField.getText();
        String modeSelected = tournamentTypeComboBox.getValue();

        boolean nameValid = (name != null && !name.isEmpty());
        boolean teamCountValid = false;
        try {
            int teamCount = Integer.parseInt(teamCountStr);
            teamCountValid = teamCount > 0;
        } catch (NumberFormatException ignored) {}

        boolean modeValid = (modeSelected != null);

        boolean hasTeams = !dataModel.getTeams().isEmpty();
        boolean allTeamsHavePlayers = dataModel.getTeams().stream().allMatch(team -> !team.getPlayers().isEmpty());

        boolean enable = nameValid && teamCountValid && modeValid && hasTeams && allTeamsHavePlayers;
        confirmButton.setDisable(!enable);
    }

    /**
     * Switches to the player assignment view.
     * Validates the team count and tournament name before proceeding.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void switchToPlayerAssignment(ActionEvent event) {
        // Validate inputs
        String teamCountText = teamCountTextField.getText();
        if (teamCountText == null || teamCountText.isEmpty()) {
            showAlert("Invalid Input", "Number of Teams is required.");
            return;
        }

        int teamCount;
        try {
            teamCount = Integer.parseInt(teamCountText);
            if (teamCount <= 1) {
                showAlert("Invalid Input", "Number of Teams must be greater than one.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Number of Teams must be a valid integer.");
            return;
        }

        dataModel.setTournamentName(tournamentNameTextField.getText());
        dataModel.setTeamCount(teamCount);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/PlayerAssignment.fxml"));
            Parent playerAssignmentUIParent = loader.load();

            Scene playerAssignmentUIScene = new Scene(playerAssignmentUIParent);

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(playerAssignmentUIScene);
            window.setWidth(900);
            window.setHeight(600);
            window.setMaxWidth(900); // Optional, falls Einschränkungen gewünscht
            window.setMinWidth(900);
            window.setMinHeight(600);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows an alert dialog with the specified title and message.
     *
     * @param title the title of the alert dialog.
     * @param message the message to display in the alert dialog.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Sets the admin label in the UI with the specified administrator name.
     *
     * @param adminName the name of the administrator.
     */
    public void setAdminLabel(String adminName) {
        creatorIdLabel.setText(adminName);
    }

    /**
     * Sets the current administrator for the controller.
     *
     * @param currentAdmin the Administrator object to set.
     */
    public void setAdmin(Administrator currentAdmin) {
        this.admin = currentAdmin;
    }

    /**
     * Shows the tournament tree view.
     * Validates the team-player distribution and optionally prompts the user for confirmation.
     *
     * @param event the ActionEvent that triggered this method.
     * @throws FileNotFoundException if the tournament tree cannot be generated.
     */
    @FXML
    private void showTournamentTree(ActionEvent event) throws FileNotFoundException {
        // Wenn die Verteilung ungleichmäßig ist, zeige den Dialog
        if (validateTeamPlayerDistribution()) {
            showConfirmationDialog(event);
            return;
        }
        // Andernfalls fahre fort
        continueShowTournamentTree(event);
    }

    /**
     * Navigates back to the main menu.
     * Resets the DataModel and loads the main menu UI.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void backToMainMenu(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/menuUI.fxml"));
            Parent mainMenuUIParent = loader.load();
            Scene mainMenuUIScene = new Scene(mainMenuUIParent);

            dataModel.reset();

            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(mainMenuUIScene);
            window.setWidth(600);
            window.setHeight(400);
            window.setMaxWidth(600);
            window.setMinWidth(600);
            window.setMinHeight(400);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Navigation Error", "Could not return to main menu.");
        }
    }

    /**
     * Converts a tournament mode name to its corresponding TournamentMode object.
     *
     * @param modeName the name of the tournament mode.
     * @return the corresponding TournamentMode object, or null if the mode name is invalid.
     */
    private TournamentMode getTournamentModeFromName(String modeName) {
        return switch (modeName) {
            case "Single Elimination" -> new SingleElimination();
            case "Double Elimination" -> new DoubleElimination();
            case "Round Robin" -> new RoundRobin();
            case "Swiss" -> new Swiss();
            default -> null;
        };
    }

    /**
     * Validates the distribution of players across teams.
     * Ensures that the difference between the team with the most players and the team with the least players is less than 1.
     *
     * @return true if the distribution is unbalanced, false otherwise.
     */
    private boolean validateTeamPlayerDistribution() {
        // Initialisiere Min- und Max-Werte
        int minPlayers = Integer.MAX_VALUE;
        int maxPlayers = Integer.MIN_VALUE;

        // Iteriere durch die Teams und finde min/max Spieleranzahl
        for (Team team : dataModel.getTeams()) {
            int playerCount = team.getPlayers().size();
            minPlayers = Math.min(minPlayers, playerCount);
            maxPlayers = Math.max(maxPlayers, playerCount);
        }

        // Prüfe, ob die Differenz zwischen max und min >= 1 ist
        return (maxPlayers - minPlayers) >= 1;
    }

    /**
     * Shows a confirmation dialog if the team distribution is not balanced.
     * If the user confirms, continues to show the tournament tree.
     *
     * @param event the ActionEvent that triggered this method.
     * @throws FileNotFoundException if the tournament tree cannot be generated.
     */
    private void showConfirmationDialog(ActionEvent event) throws FileNotFoundException{
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Team Distribution");
        alert.setHeaderText("The team distribution is not balanced.");
        alert.setContentText("Do you want to proceed anyway?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            continueShowTournamentTree(event);
        }
    }

    /**
     * Continues to show the tournament tree after confirming team distribution.
     *
     * @param event the ActionEvent that triggered this method.
     * @throws FileNotFoundException if the tournament tree cannot be generated.
     */
    private void continueShowTournamentTree(ActionEvent event) throws FileNotFoundException{
        String modeSelected = tournamentTypeComboBox.getValue();
        TournamentMode mode = getTournamentModeFromName(modeSelected);
        if (mode == null) {
            showAlert("Data Error", "Invalid tournament mode selected.");
            return;
        }

        Tournament tournament = new Tournament(dataModel.getCreatorId(), dataModel.getTournamentName());
        tournament.getConfig().setTeams(new ArrayList<>(dataModel.getTeams()));
        tournament.getConfig().setTournamentMode(mode);

        PersistenceTournaments.addConfig(tournament.getConfig());
        try {
            tournament.generateTree();
            dataModel.reset();
        } catch (IllegalStateException e) {
            showAlert("Data Error", e.getMessage());
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/treeUI.fxml"));
            Parent treeUIParent = loader.load();

            TreeUIController controller = loader.getController();
            controller.setStage((Stage)((Node) event.getSource()).getScene().getWindow());
            controller.init(tournament);

            Scene treeUIScene = new Scene(treeUIParent);
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(treeUIScene);
            window.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ObservableList<Team> generateTeams(int teamCount) {
        ObservableList<Team> generatedTeams = FXCollections.observableArrayList();
        for (int i = 1; i <= teamCount; i++) {
            generatedTeams.add(new Team("Team " + i));
        }
        return generatedTeams;
    }
}
