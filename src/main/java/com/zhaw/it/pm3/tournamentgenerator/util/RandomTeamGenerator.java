package com.zhaw.it.pm3.tournamentgenerator.util;

import com.zhaw.it.pm3.tournamentgenerator.controller.PlayerAssignmentController;
import com.zhaw.it.pm3.tournamentgenerator.domain.DataModel;
import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import javafx.collections.ObservableList;

import java.util.Collections;

/**
 * Handles the generation of random teams and player assignment.
 */
public class RandomTeamGenerator {

    private PlayerAssignmentController playerAssignmentController;

    /**
     * Sets the controller used for managing player assignments.
     *
     * @param controller the PlayerAssignmentController to set
     */
    public void setPlayerAssignmentController(PlayerAssignmentController controller) {
        this.playerAssignmentController = controller;
    }

    /**
     * Generates random teams based on the specified number of teams
     * and assigns all available players randomly to those teams.
     *
     * @param numberOfTeams The number of teams to generate.
     */
    public void generateTeams(int numberOfTeams) {
        DataModel dataModel = DataModel.getInstance();
        ObservableList<Player> availablePlayers = dataModel.getPlayers();
        ObservableList<Team> teams = dataModel.getTeams();

        if (availablePlayers.isEmpty() || numberOfTeams <= 0) {
            // No players to assign or invalid number of teams
            return;
        }

        // Add all players from teams back to the available players list
        for (Team team : teams) {
            for (Player player : team.getPlayers()) {
                playerAssignmentController.addAvailablePlayer(player);
            }
            team.getPlayers().clear();
        }

        // Shuffle the list of available players
        Collections.shuffle(availablePlayers);

        // Assign players to teams in a round-robin fashion
        int teamIndex = 0;
        for (Player player : availablePlayers) {
            teams.get(teamIndex).getPlayers().add(player);
            teamIndex = (teamIndex + 1) % numberOfTeams;
        }

        // Clear the available players list after assignment
        availablePlayers.clear();
    }
}