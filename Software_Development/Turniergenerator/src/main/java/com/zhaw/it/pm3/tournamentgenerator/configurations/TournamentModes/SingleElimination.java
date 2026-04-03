package com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes;

import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.domain.TournamentTree;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;

import java.util.ArrayList;

/**
 * This class represents the single elimination mode of a tournament.
 */
public class SingleElimination extends TournamentMode {

    /**
     * This method is used to play the next round of a single elimination tournament.
     * It gets the winners of the previous round and sets them as the teams for the next round.
     * @param tournamentTree The tournament tree that is used to play the next round.
     * @param round The round that is going to be played.
     */
    @Override
    public void playNextRound(TournamentTree tournamentTree, int round) {
        ArrayList<Team> winners = tournamentTree.getWinnersForRound(round - 1);
        ArrayList<Match> matches = tournamentTree.getMatchesForRound(round);
        for (int i = 0; i < matches.size(); i++) {
            matches.get(i).setTeam1(winners.get(i * 2));
            matches.get(i).setTeam2(winners.get(i * 2 + 1));
        }
    }

    @Override public String toString(){
        return "Single Elimination";
    }
}
