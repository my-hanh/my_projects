package com.zhaw.it.pm3.tournamentgenerator.configurations.Games;

/**
 * This class represents a game that can be played in a tournament.
 */
public class Game {
    private String name;

    /**
     * Constructor for a game.
     *
     * @param name The name of the game.
     */
    public Game(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the game.
     *
     * @return The name of the game.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the game.
     *
     * @param name The name of the game.
     */
    public void setName(String name) {
        this.name = name;
    }
}
