package com.zhaw.it.pm3.tournamentgenerator.domain;

import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class represents the data model of the application.
 */
public class DataModel {

    private static DataModel instance = null;

    private ObservableList<Player> players;

    private ObservableList<Team> teams;

    private String tournamentName;

    private String creatorId;

    private int teamCount;

    private String selectedGame;

    private String selectedTournamentMode;

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the observable lists for players and teams.
     */
    private DataModel() {
        players = FXCollections.observableArrayList();
        teams = FXCollections.observableArrayList();
    }

    /**
     * Returns the singleton instance of the DataModel.
     * If no instance exists, a new one is created.
     *
     * @return the singleton instance of DataModel.
     */
    public static DataModel getInstance() {
        if (instance == null) {
            instance = new DataModel();
        }
        return instance;
    }

    /**
     * Resets the DataModel to its default state.
     * Clears all players, teams, and other tournament-related properties.
     */
    public void reset() {
        players.clear();
        teams.clear();
        tournamentName = null;
        teamCount = 0;
        selectedGame = null;
        selectedTournamentMode = null;
    }

    /**
     * Gets the tournament name.
     *
     * @return the name of the tournament.
     */
    public String getTournamentName() {
        return tournamentName;
    }

    /**
     * Sets the tournament name.
     *
     * @param tournamentName the name of the tournament to set.
     */
    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }


    /**
     * Gets the creator ID of the tournament.
     *
     * @return the ID of the creator.
     */
    public String getCreatorId() {
        return creatorId;
    }

    /**
     * Sets the creator ID of the tournament.
     *
     * @param creatorId the ID of the creator to set.
     */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * Gets the number of teams in the tournament.
     *
     * @return the number of teams.
     */
    public int getTeamCount() {
        return teamCount;
    }

    /**
     * Sets the number of teams in the tournament.
     *
     * @param teamCount the number of teams to set.
     */
    public void setTeamCount(int teamCount) {
        this.teamCount = teamCount;
    }

    /**
     * Gets the list of players.
     *
     * @return an observable list of players.
     */
    public ObservableList<Player> getPlayers() {
        return players;
    }

    /**
     * Gets the list of teams.
     *
     * @return an observable list of teams.
     */
    public ObservableList<Team> getTeams() {
        return teams;
    }

    /**
     * Sets the list of teams.
     *
     * @param teams the observable list of teams to set.
     */
    public void setTeams(ObservableList<Team> teams) {
        this.teams = teams;
    }

    /**
     * Gets the selected game for the tournament.
     *
     * @return the name of the selected game.
     */
    public String getSelectedGame() {
        return selectedGame;
    }

    /**
     * Sets the selected game for the tournament.
     *
     * @param selectedGame the name of the game to set.
     */
    public void setSelectedGame(String selectedGame) {
        this.selectedGame = selectedGame;
    }

    /**
     * Gets the selected tournament mode.
     *
     * @return the name of the selected tournament mode.
     */
    public String getSelectedTournamentMode() {
        return selectedTournamentMode;
    }

    /**
     * Sets the selected tournament mode.
     *
     * @param selectedTournamentMode the name of the tournament mode to set.
     */
    public void setSelectedTournamentMode(String selectedTournamentMode) {
        this.selectedTournamentMode = selectedTournamentMode;
    }
}
