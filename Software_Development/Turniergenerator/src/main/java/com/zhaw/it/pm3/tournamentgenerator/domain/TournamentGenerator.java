package com.zhaw.it.pm3.tournamentgenerator.domain;


import com.zhaw.it.pm3.tournamentgenerator.configurations.Config;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;

import java.util.ArrayList;

import static java.util.Collections.shuffle;

/**
 * This class is used to generate a tournament tree based on the configuration.
 */
public class TournamentGenerator {

    private final Config config;

    /**
     * Constructor for TournamentGenerator.
     *
     * @param config the configuration object containing tournament details such as teams and mode.
     */
    public TournamentGenerator(Config config) {
        this.config = config;
    }

    /**
     * Creates the tournament tree based on the selected tournament mode.
     *
     * @return the generated TournamentTree, or null if the mode is invalid.
     */
    public TournamentTree createTournamentTree() {
        return switch (config.getTournamentMode().toString()) {
            case "Single Elimination" -> createSingleEliminationTree();
            case "Double Elimination" -> createDoubleEliminationTree();
            case "RoundRobin" -> createRoundRobinTree();
            case "Swiss" -> createSwissTree();
            default -> null;
        };
    }

    /**
     * Creates a single elimination tournament tree.
     * If the number of teams is not a power of two, byes are added to the first round.
     *
     * @return the TournamentTree for a single elimination tournament.
     */
    private TournamentTree createSingleEliminationTree() {
        TournamentTree tournamentTree = new TournamentTree();
        ArrayList<ArrayList<Match>> matches = new ArrayList<>();
        shuffle(config.getTeams());
        int power2 = 2;
        while (power2 < config.getTeams().size()) {
            power2 *= 2;
        }
        int totalParticipants = config.getTeams().size();
        int firstRoundByes = power2 - totalParticipants;

        ArrayList<Match> firstRound = new ArrayList<>();

        for (int i = 0; i < totalParticipants; i += 2) {
            if (firstRoundByes > 0) {
                Match match = new Match(config.getTeams().get(i), new Team("Bye"));
                match.playMatch(1, 0);
                firstRound.add(match);
                firstRoundByes--;
                i--;
            } else {
                Match match = new Match(config.getTeams().get(i), config.getTeams().get(i + 1));
                firstRound.add(match);
            }
        }
        matches.add(firstRound);

        int remainingParticipants = firstRound.size();
        while (remainingParticipants > 1) {
            ArrayList<Match> nextRound = new ArrayList<>();
            for (int i = 0; i < remainingParticipants / 2; i++) {
                nextRound.add(new Match());
            }
            matches.add(nextRound);
            remainingParticipants /= 2;
        }
        for (int i = 0; i < matches.size() - 1; i++) {
            for (Match match : matches.get(i)) {
                match.setNextMatch(matches.get(i + 1).get(matches.get(i).indexOf(match) / 2));
            }
        }
        tournamentTree.setMatches(matches);
        tournamentTree.setParticipants(config.getTeams());
        return tournamentTree;
    }

    /**
     * Creates a double elimination tournament tree.
     * Adds a winner bracket and a loser bracket, and links them with a final match.
     *
     * @return the TournamentTree for a double elimination tournament.
     */
    private TournamentTree createDoubleEliminationTree() {
        TournamentTree tournamentTreeDoubleElimination = new TournamentTree();
        TournamentTree winnerBracketTree = this.createSingleEliminationTree();
        ArrayList<ArrayList<Match>> winnerBracket = winnerBracketTree.getMatches();
        ArrayList<ArrayList<Match>> loserBracket = new ArrayList<>();

        //create loser bracket
        int numLoserBracketRounds = 0;
        numLoserBracketRounds = switch (this.config.getTeams().size()) {
            case 4 -> 2;
            case 8 -> 4;
            case 16 -> 6;
            default -> numLoserBracketRounds;
        };

        for (int i = 0; i < numLoserBracketRounds; i++) {
            loserBracket.add(new ArrayList<>());
        }
        for (int i = 0; i < numLoserBracketRounds; i++) {
            Match match = new Match(new Team("Loser TBD"), new Team("Loser TBD"));
            if (i < 2) {
                loserBracket.get(numLoserBracketRounds - i - 1).add(match);
            } else if (i < 4) {
                loserBracket.get(numLoserBracketRounds - i - 1).add(match);
                loserBracket.get(numLoserBracketRounds - i - 1).add(match);
            } else {
                loserBracket.get(numLoserBracketRounds - i - 1).add(match);
                loserBracket.get(numLoserBracketRounds - i - 1).add(match);
                loserBracket.get(numLoserBracketRounds - i - 1).add(match);
                loserBracket.get(numLoserBracketRounds - i - 1).add(match);
            }
        }

        for (int i = 0; i < loserBracket.size() - 1; i++) {
            for (Match match : loserBracket.get(i)) {
                match.setNextMatch(loserBracket.get(i + 1).get(loserBracket.get(i).indexOf(match) / 2));
            }
        }

        //add loser bracket to tournament tree
        tournamentTreeDoubleElimination.setMatches(winnerBracket);
        tournamentTreeDoubleElimination.getMatches().addAll(loserBracket);


        //create final round
        ArrayList<Match> finalRound = new ArrayList<>();
        finalRound.add(new Match(new Team("Final TBD"), new Team("Final TBD")));
        tournamentTreeDoubleElimination.getMatches().add(finalRound);
        winnerBracket.get(winnerBracket.size() - 1).get(0).setNextMatch(finalRound.get(0));
        loserBracket.get(loserBracket.size() - 1).get(0).setNextMatch(finalRound.get(0));
        return tournamentTreeDoubleElimination;

    }

    /**
     * Creates a Swiss tournament tree.
     * Currently not implemented.
     *
     * @return null as this method is not yet implemented.
     */
    private TournamentTree createSwissTree() {
        return null;
    }

    /**
     * Creates a Round Robin tournament tree.
     * Currently not implemented.
     *
     * @return null as this method is not yet implemented.
     */
    private TournamentTree createRoundRobinTree() {
        return null;
    }

}
