package com.zhaw.it.pm3.tournamentgenerator.services;

/**
 * Represents a placeholder for database operations in the tournament system.
 *
 * This class is designed for future implementation of database functionalities,
 * such as saving and retrieving tournament data.
 */
public class Database {

    /**
     * Constructs a {@code Database} object.
     * Currently, this constructor does not perform any specific initialization.
     */
    public Database() {
    }

    /**
     * Saves a player's score to the database.
     *
     * Note: This method is a placeholder and currently only prints the score
     * to the console. Actual database saving functionality will be implemented
     * in the future.
     *
     * @param name  the name of the player whose score is being saved.
     * @param score the score of the player to be saved.
     */
    public void saveScore(String name, int score) {
        System.out.println("Saving score to database: " + name + " " + score);
    }
}
