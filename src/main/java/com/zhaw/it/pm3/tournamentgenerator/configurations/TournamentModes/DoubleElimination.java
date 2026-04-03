package com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes;

import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.domain.TournamentTree;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Class for the double elimination tournament mode.
 */
public class DoubleElimination extends TournamentMode {
    @Override
    public String toString() {
        return "Double Elimination";
    }

    /**
     * This method is used to play the next round of a double elimination tournament.
     * It gets the winners and losers of the previous round and sets them as the teams for the next round.
     * @param tournamentTree The tournament tree that is used to play the next round.
     * @param round The round that is going to be played.
     */
    @Override
    public void playNextRound(TournamentTree tournamentTree, int round) {
        ArrayList<Team> winners = tournamentTree.getWinnersForRound(round - 1);
        ArrayList<Team> losers = tournamentTree.getLosersForRound(round - 1);

        int numLoserRound = tournamentTree.getMatches().size() / 2 + round - 1;

        ArrayList<Match> matchesWinners = tournamentTree.getMatchesForRound(round);
        ArrayList<Match> matchesLosers = tournamentTree.getMatchesForRound(numLoserRound);
        if (!Objects.equals(matchesWinners.getFirst().getTeam1().getName(), "TBD")) {
            for (int i = 0; i < matchesWinners.size(); i++) {
                matchesWinners.get(i).setTeam1(winners.get(i * 2));
                matchesWinners.get(i).setTeam2(winners.get(i * 2 + 1));
            }
        }


        for (int i = 0; i < matchesLosers.size(); i++) {
            matchesLosers.get(i).setTeam1(losers.get(i * 2));
            matchesLosers.get(i).setTeam2(losers.get(i * 2 + 1));
        }

    }
}
