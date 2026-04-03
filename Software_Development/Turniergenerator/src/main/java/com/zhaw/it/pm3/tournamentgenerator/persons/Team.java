package com.zhaw.it.pm3.tournamentgenerator.persons;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Represents a team in the tournament system.
 * A team consists of a name, a list of players, and a score (points).
 */
public class Team {

    private int points;

    private String name;

    private ObservableList<Player> players;

    /**
     * Constructs a {@code Team} object with the specified name.
     * The team is initialized with no players and zero points.
     *
     * @param name the name of the team.
     */
    public Team(String name) {
        this.name = name;
        this.players = FXCollections.observableArrayList();
    }

    /**
     * Gets the name of the team.
     *
     * @return the name of the team.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the list of players in the team.
     *
     * @return an {@link ObservableList} of {@link Player} objects in the team.
     */
    public ObservableList<Player> getPlayers() {
        return players;
    }

    /**
     * Sets the name of the team.
     *
     * @param name the new name of the team.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the list of players in the team.
     *
     * @param players an {@link ObservableList} of {@link Player} objects to set as the team's players.
     */
    public void setPlayers(ObservableList<Player> players) {
        this.players = players;
    }

    /**
     * Adds a player to the team.
     *
     * @param player the {@link Player} to add to the team.
     */
    public void addPlayer(Player player) {
        players.add(player);
    }

    /**
     * Removes a player from the team.
     *
     * @param player the {@link Player} to remove from the team.
     */
    public void removePlayer(Player player) {
        players.remove(player);
    }

    /**
     * Gets the total points scored by the team.
     *
     * @return the total points of the team.
     */
    public int getPoints() {
        return points;
    }

    /**
     * Sets the total points of the team.
     *
     * @param points the new total points for the team.
     */
    public void setPoints(int points) {
        this.points = points;
    }

    /**
     * Adds points to the team's total score.
     *
     * @param points the number of points to add.
     */
    public void addPoints(int points) {
        this.points += points;
    }

    /**
     * Gets the current size of the team.
     *
     * @return the number of players in the team.
     */
    public int getTeamSize() {
        return players.size();
    }

    /**
     * Gets a string representing the team's current and maximum capacity.
     * Assumes a maximum capacity of 5 players.
     *
     * @return a string in the format "currentSize/5".
     */
    public String getTeamCapacity() {
        return players.size() + "/5";
    }

    /**
     * Returns a string representation of the team.
     * This is the team's name.
     *
     * @return the name of the team as a string.
     */
    public String toString() {
        return name;
    }
}
