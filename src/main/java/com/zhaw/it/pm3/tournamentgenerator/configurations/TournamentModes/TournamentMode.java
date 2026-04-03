package com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes;

import com.zhaw.it.pm3.tournamentgenerator.domain.TournamentTree;

/**
 * Represents a tournament mode.
 * This class serves as a base for specific tournament modes.
 */
public class TournamentMode {
    private String modeName;

    /**
     * Plays the next round of the tournament.
     * Subclasses should override this method to provide specific functionality.
     *
     * @param tournamentTree the tournament tree containing the matches.
     * @param round the current round to be played.
     */
    public void playNextRound(TournamentTree tournamentTree, int round) {
        // to be implemented by the subclasses
    }
}