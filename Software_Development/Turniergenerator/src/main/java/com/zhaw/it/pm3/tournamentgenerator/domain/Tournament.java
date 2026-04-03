package com.zhaw.it.pm3.tournamentgenerator.domain;

import com.zhaw.it.pm3.tournamentgenerator.configurations.Config;

/**
 * This class represents a tournament.
 */
public class Tournament {

    private TournamentTree tournamentTree;
    private final Config config = new Config();

    private int roundsPlayed = 0;

    /**
     * Default constructor for the Tournament class.
     * Initializes an empty tournament.
     */
    public Tournament() {
    }

    /**
     * Constructor to create a Tournament with a specific creator ID and name.
     *
     * @param id the ID of the creator of the tournament.
     * @param name the name of the tournament.
     */
    public Tournament(String id, String name) {
        config.setCreatorId(id);
        config.setTournamentName(name);
    }

    /**
     * Generates the tournament tree based on the current configuration.
     * Ensures that all necessary configurations (name, teams, mode) are set.
     * Throws an exception if any required configuration is missing or invalid.
     */
    public void generateTree() {
        if (config.getTournamentName() == null || config.getTournamentName().isEmpty()) {
            throw new IllegalStateException("Tournament name not set.");
        }
        if (config.getTeams() == null || config.getTeams().isEmpty()) {
            throw new IllegalStateException("No teams set.");
        }
        if (config.getTournamentMode() == null) {
            throw new IllegalStateException("Tournament mode not set.");
        }

        System.out.println("Creating Tournament: " + config.getTournamentName() + " with game mode " + config.getTournamentMode());

        TournamentGenerator tournamentGenerator = new TournamentGenerator(config);
        this.tournamentTree = tournamentGenerator.createTournamentTree();
        if (tournamentTree == null) {
            throw new IllegalStateException("Failed to generate the tournament tree.");
        }
    }

    /**
     * Advances the tournament to the next round.
     * Ensures the current round is finished before progressing.
     * Ends the tournament if the last round has been completed.
     *
     * @throws IllegalStateException if the current round is not finished.
     */
    public void startNextRound() {
        if (roundsPlayed == tournamentTree.getNumberOfRounds() - 1 &&
                tournamentTree.getMatches().getLast().get(0).isPlayed()) {
            endTournament();
        } else if (tournamentTree.isRoundFinished(roundsPlayed)) {
            roundsPlayed++;
            config.getTournamentMode().playNextRound(tournamentTree, roundsPlayed);
            // Notify participants and save the round if necessary
        } else {
            throw new IllegalStateException("Round is not finished yet");
        }
    }

    /**
     * Ends the tournament and performs necessary cleanup or final notifications.
     * Prints a message indicating the tournament has ended.
     */
    public void endTournament() {
        System.out.println("Tournament " + config.getTournamentName() + " has ended.");
    }

    /**
     * Gets the tournament tree, which contains the structure and matches of the tournament.
     *
     * @return the TournamentTree object.
     */
    public TournamentTree getTournamentTree() {
        return tournamentTree;
    }

    /**
     * Sets the tournament tree.
     *
     * @param tournamentTree the TournamentTree object to set.
     */
    public void setTournamentTree(TournamentTree tournamentTree) {
        this.tournamentTree = tournamentTree;
    }

    /**
     * Gets the name of the tournament.
     *
     * @return the name of the tournament.
     */
    public String getName() {
        return config.getTournamentName();
    }

    /**
     * Gets the ID of the creator of the tournament.
     *
     * @return the creator ID.
     */
    public String getId() {
        return config.getCreatorId();
    }

    /**
     * Gets the number of rounds played so far in the tournament.
     *
     * @return the number of rounds played.
     */
    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    /**
     * Gets the configuration of the tournament.
     * The configuration includes details like name, creator ID, teams, and mode.
     *
     * @return the Config object containing the tournament configuration.
     */
    public Config getConfig() {
        return config;
    }
}
