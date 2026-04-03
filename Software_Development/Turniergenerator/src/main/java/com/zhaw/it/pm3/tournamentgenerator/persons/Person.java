package com.zhaw.it.pm3.tournamentgenerator.persons;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * This class represents a person.
 */
public abstract class Person {

    private String name;

    private Long Id;

    protected UserRights userRights;

    private ObservableList<Player> players;

    /**
     * Represents a generic person in the tournament system.
     * This is an abstract class serving as a base for specific types of users, such as administrators or players.
     */
    public Person(String name) {
        this.name = name;
        this.players = FXCollections.observableArrayList();
        this.Id = System.currentTimeMillis();
    }

    /**
     * Gets the user rights associated with this person.
     *
     * @return the {@link UserRights} of the person.
     */
    public UserRights getUserRights() {
        return userRights;
    }

    /**
     * Gets the name of the person.
     *
     * @return the name of the person.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the person.
     *
     * @param name the new name of the person.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the unique ID of the person.
     *
     * @return the unique ID of the person.
     */
    public Long getId() {
        return this.Id;
    }

    /**
     * Sets the unique ID of the person.
     *
     * @param id the new ID for the person.
     */
    public void setId(Long id) {
        this.Id = id;
    }

    /**
     * Returns a string representation of the person.
     *
     * @return the name of the person as a string.
     */
    public String toString() {
        return name;
    }

    /**
     * Gets the list of players associated with this person.
     * This can be used for cases where the person is responsible for managing players, such as a team leader.
     *
     * @return an {@link ObservableList} of {@link Player} objects associated with the person.
     */
    public ObservableList<Player> getPlayers() {
        return players;
    }
}

