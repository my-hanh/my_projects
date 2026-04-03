package com.zhaw.it.pm3.tournamentgenerator.controller;

import com.zhaw.it.pm3.tournamentgenerator.domain.DataModel;
import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import com.zhaw.it.pm3.tournamentgenerator.util.RandomTeamGenerator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * This class is the controller for the player assignment view.
 */
public class PlayerAssignmentController extends BaseMainController {

    @FXML
    private TableView<Player> availablePlayersTableView;
    @FXML
    private TableColumn<Player, String> playerNameColumn;

    @FXML
    private TableView<Team> teamsTableView;
    @FXML
    private TableColumn<Team, String> teamNameColumn;

    @FXML
    private TableView<Player> teamPlayersTableView;
    @FXML
    private TableColumn<Player, String> teamPlayerNameColumn;

    private ObservableList<Player> availablePlayers;
    private ObservableList<Team> teams;

    private DataModel dataModel;

    /**
     * Initializes the player assignment view.
     * Sets up the available players and teams table views and their data bindings.
     * Initializes teams if they are not already initialized.
     */
    @FXML
    public void initialize() {
        dataModel = DataModel.getInstance();

        // Initialize available players from DataModel
        availablePlayers = dataModel.getPlayers();
        availablePlayersTableView.setItems(availablePlayers);
        playerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Initialize teams from DataModel
        teams = dataModel.getTeams();
        teamsTableView.setItems(teams);
        teamNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Set up selection listener for teams
        teamsTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                teamPlayersTableView.setItems((ObservableList<Player>) newSelection.getPlayers());
            } else {
                teamPlayersTableView.setItems(null);
            }
        });

        teamPlayerNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Initialize teams if they are not already initialized
        if (teams.isEmpty() && dataModel.getTeamCount() > 0) {
            initializeTeams();
        }
    }

    /**
     * Initializes the teams based on the team count from the DataModel.
     * Displays an error alert if the team count is invalid.
     */
    private void initializeTeams() {
        int teamCount = dataModel.getTeamCount();
        if (teamCount > 0) {
            teams = FXCollections.observableArrayList(generateTeams(teamCount));
            dataModel.setTeams(teams);
            teamsTableView.setItems(teams);
            teamNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        } else {
            // Handle error: teamCount not set properly
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Team Count");
            alert.setContentText("Team count must be greater than zero.");
            alert.showAndWait();
        }
    }

    /**
     * Generates a list of teams with placeholder names based on the specified team count.
     *
     * @param teamCount the number of teams to generate.
     * @return an ObservableList of Team objects.
     */
    private ObservableList<Team> generateTeams(int teamCount) {
        ObservableList<Team> generatedTeams = FXCollections.observableArrayList();
        for (int i = 1; i <= teamCount; i++) {
            generatedTeams.add(new Team("Team " + i));
        }
        return generatedTeams;
    }

    /**
     * Adds a selected player from the available players table to the selected team.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void addPlayerToTeam(ActionEvent event) {
        Player selectedPlayer = availablePlayersTableView.getSelectionModel().getSelectedItem();
        Team selectedTeam = teamsTableView.getSelectionModel().getSelectedItem();

        if (selectedPlayer != null && selectedTeam != null) {
            selectedTeam.getPlayers().add(selectedPlayer);
            availablePlayers.remove(selectedPlayer);
        }
    }

    /**
     * Removes a selected player from the selected team and returns them to the available players list.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void removePlayerFromTeam(ActionEvent event) {
        Player selectedPlayer = teamPlayersTableView.getSelectionModel().getSelectedItem();
        Team selectedTeam = teamsTableView.getSelectionModel().getSelectedItem();

        if (selectedPlayer != null && selectedTeam != null) {
            selectedTeam.getPlayers().remove(selectedPlayer);
            availablePlayers.add(selectedPlayer);
        }
    }

    /**
     * Opens the "Add Player" view to create a new player.
     * Sets the PlayerAssignmentController as a reference for the AddPlayerController.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void addNewPlayer(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/AddPlayer.fxml"));
            Parent addPlayerUIParent = loader.load();

            AddPlayerController addPlayerController = loader.getController();
            addPlayerController.setPlayerAssignmentController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(addPlayerUIParent));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the "Edit Player" view to edit the details of the selected player.
     * Displays a warning alert if no player is selected.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void editPlayer(ActionEvent event) {
        Player selectedPlayer = availablePlayersTableView.getSelectionModel().getSelectedItem();

        if (selectedPlayer != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/EditPlayer.fxml"));
                Parent editPlayerUIParent = loader.load();

                EditPlayerController editPlayerController = loader.getController();
                editPlayerController.setPlayer(selectedPlayer);

                Stage stage = new Stage();
                stage.setScene(new Scene(editPlayerUIParent));
                stage.showAndWait();

                // Refresh the table view
                availablePlayersTableView.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Display an alert if no player is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Player Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a player to edit.");
            alert.showAndWait();
        }
    }

    /**
     * Deletes the selected player after confirming the action with the user.
     * Displays a warning alert if no player is selected.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void deletePlayer(ActionEvent event) {
        Player selectedPlayer = availablePlayersTableView.getSelectionModel().getSelectedItem();

        if (selectedPlayer != null) {
            // Confirmation dialog
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Player");
            alert.setHeaderText("Are you sure you want to delete this player?");
            alert.setContentText("Player: " + selectedPlayer.getName());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                availablePlayers.remove(selectedPlayer);
            }
        } else {
            // Display an alert if no player is selected
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Player Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a player to delete.");
            alert.showAndWait();
        }
    }

    /**
     * Adds a new player to the list of available players.
     *
     * @param player the Player object to add.
     */
    public void addAvailablePlayer(Player player) {
        availablePlayers.add(player);
    }

    /**
     * Generates random teams and assigns players to them using the RandomTeamGenerator.
     * Updates the UI to reflect the new teams and player assignments.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void generateRandomTeams(ActionEvent event) {
        int teamCount = dataModel.getTeamCount();
        RandomTeamGenerator randomTeamGenerator = new RandomTeamGenerator();
        randomTeamGenerator.setPlayerAssignmentController(this);
        randomTeamGenerator.generateTeams(teamCount);

        // Refresh the UI
        teamsTableView.setItems(dataModel.getTeams());
        availablePlayersTableView.setItems(dataModel.getPlayers());

        // Update team players view if a team is selected
        Team selectedTeam = teamsTableView.getSelectionModel().getSelectedItem();
        if (selectedTeam != null) {
            teamPlayersTableView.setItems((ObservableList<Player>) selectedTeam.getPlayers());
        } else {
            teamPlayersTableView.setItems(null);
        }
    }


    /**
     * Navigates back to the "Create New Tournament" view.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void backToCreateTournament(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/createNewTournamentUI.fxml"));

        try {
            Pane pageRoot = loader.load();
            openNextPage(loader, event, pageRoot);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens the "Edit Team Name" view to rename the selected team.
     * Displays a warning alert if no team is selected.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void renameTeam(ActionEvent event) {
        Team selectedTeam = teamsTableView.getSelectionModel().getSelectedItem();
        if (selectedTeam != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/EditTeamName.fxml"));
                Parent root = loader.load();

                EditTeamNameController controller = loader.getController();
                controller.setTeam(selectedTeam);

                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.showAndWait();

                teamsTableView.refresh(); // Refresh to show updated team name
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Show alert: No team selected
        }
    }
}
