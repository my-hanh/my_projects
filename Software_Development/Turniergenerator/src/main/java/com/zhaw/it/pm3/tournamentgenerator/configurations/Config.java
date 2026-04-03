package com.zhaw.it.pm3.tournamentgenerator.configurations;

import com.zhaw.it.pm3.tournamentgenerator.configurations.Games.Game;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.TournamentMode;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;

import java.util.ArrayList;

/**
 * This class represents the configuration of a tournament.
 */
public class Config {

    private String tournamentName;
    private String creatorId;
    private String password;
    private TournamentMode tournamentMode;
    private ArrayList<Team> teams;
    private Game game;

    /**
     * Default constructor for the Config class.
     */
    public Config() {
    }

    /**
     * Gets the name of the tournament.
     *
     * @return the name of the tournament.
     */
    public String getTournamentName() {
        return tournamentName;
    }

    /**
     * Sets the name of the tournament.
     *
     * @param tournamentName the name of the tournament to set.
     */
    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    /**
     * Gets the ID of the tournament creator.
     *
     * @return the creator's ID.
     */
    public String getCreatorId() {
        return creatorId;
    }

    /**
     * Sets the ID of the tournament creator.
     *
     * @param creatorId the creator's ID to set.
     */
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    /**
     * Gets the password for the tournament.
     *
     * @return the tournament password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for the tournament.
     *
     * @param password the password to set for the tournament.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the tournament mode.
     *
     * @return the current tournament mode.
     */
    public TournamentMode getTournamentMode() {
        return tournamentMode;
    }

    /**
     * Sets the tournament mode.
     *
     * @param tournamentMode the tournament mode to set.
     */
    public void setTournamentMode(TournamentMode tournamentMode) {
        this.tournamentMode = tournamentMode;
    }

    /**
     * Gets the list of teams participating in the tournament.
     *
     * @return the list of teams.
     */
    public ArrayList<Team> getTeams() {
        return teams;
    }

    /**
     * Sets the list of teams participating in the tournament.
     *
     * @param teams the list of teams to set.
     */
    public void setTeams(ArrayList<Team> teams) {
        this.teams = teams;
    }

    /**
     * Gets the game configuration for the tournament.
     *
     * @return the game configuration.
     */
    public Game getGame() {
        return game;
    }

    /**
     * Sets the game configuration for the tournament.
     *
     * @param game the game configuration to set.
     */
    public void setGame(Game game) {
        this.game = game;
    }
}
