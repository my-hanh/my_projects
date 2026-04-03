package com.zhaw.it.pm3.tournamentgenerator.domain;

import com.zhaw.it.pm3.tournamentgenerator.persons.Team;

import java.util.ArrayList;

/**
 * This class represents the tournament tree of a tournament.
 */
public class TournamentTree {

    private ArrayList<Team> participants;

    private ArrayList<ArrayList<Match>> matches;

    /**
     * Constructor to create a TournamentTree with specified participants and matches.
     *
     * @param participants the list of teams participating in the tournament.
     * @param matches      the list of matches organized by rounds.
     */
    public TournamentTree(ArrayList<Team> participants, ArrayList<ArrayList<Match>> matches) {
        this.participants = participants;
        this.matches = matches;
    }

    /**
     * Default constructor to create an empty TournamentTree.
     * Initializes empty lists for participants and matches.
     */
    public TournamentTree() {
        this.participants = new ArrayList<>();
        this.matches = new ArrayList<>();
    }

    /**
     * Gets the list of participants in the tournament.
     *
     * @return the list of participating teams.
     */
    public ArrayList<Team> getTeams() {
        return participants;
    }

    /**
     * Gets the matches organized by rounds.
     *
     * @return a list of rounds, where each round is a list of matches.
     */
    public ArrayList<ArrayList<Match>> getMatches() {
        return matches;
    }

    /**
     * Sets the list of participants in the tournament.
     *
     * @param participants the list of teams to set.
     */
    public void setParticipants(ArrayList<Team> participants) {
        this.participants = participants;
    }

    /**
     * Sets the matches organized by rounds.
     *
     * @param matches a list of rounds, where each round is a list of matches.
     */
    public void setMatches(ArrayList<ArrayList<Match>> matches) {
        this.matches = matches;
    }

    /**
     * Gets the matches for a specific round.
     *
     * @param round the round number (0-based index).
     * @return a list of matches for the specified round.
     */
    public ArrayList<Match> getMatchesForRound(int round) {
        return matches.get(round);
    }

    /**
     * Gets the winners of matches for a specific round.
     *
     * @param round the round number (0-based index).
     * @return a list of winning teams for the specified round.
     */
    public ArrayList<Team> getWinnersForRound(int round) {
        ArrayList<Team> winners = new ArrayList<>();
        for (Match match : matches.get(round)) {
            if (match.getWinner() != null) {
                winners.add(match.getWinner());
            }
        }
        return winners;
    }

    /**
     * Gets the losers of matches for a specific round.
     *
     * @param round the round number (0-based index).
     * @return a list of losing teams for the specified round.
     */
    public ArrayList<Team> getLosersForRound(int round) {
        ArrayList<Team> losers = new ArrayList<>();
        for (Match match : matches.get(round)) {
            if (match.getLoser() != null) {
                losers.add(match.getLoser());
            }
        }
        return losers;
    }

    /**
     * Checks if all matches in a specific round are finished.
     *
     * @param round the round number (0-based index).
     * @return true if all matches in the round are finished, false otherwise.
     */
    public boolean isRoundFinished(int round) {
        for (Match match : matches.get(round)) {
            if (!match.isPlayed()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the total number of rounds in the tournament.
     *
     * @return the number of rounds.
     */
    public int getNumberOfRounds() {
        return matches.size();
    }

    /**
     * Gets all matches involving a specific participant.
     *
     * @param participant the team whose matches are to be retrieved.
     * @return a list of matches involving the specified team.
     */
    public ArrayList<Match> getMatchesForParticipant(Team participant) {
        ArrayList<Match> participantMatches = new ArrayList<>();
        for (ArrayList<Match> round : matches) {
            for (Match match : round) {
                if (match.getTeam1().equals(participant) || match.getTeam2().equals(participant)) {
                    participantMatches.add(match);
                }
            }
        }
        return participantMatches;
    }

    /**
     * Gets all matches that resulted in a draw for a specific round.
     *
     * @param round the round number (0-based index).
     * @return a list of matches that ended in a draw for the specified round.
     */
    public ArrayList<Match> getDrawsForRound(int round) {
        ArrayList<Match> draws = new ArrayList<>();
        for (Match match : matches.get(round)) {
            if (match.isDraw()) {
                draws.add(match);
            }
        }
        return draws;
    }

    /**
     * Returns a string representation of the tournament tree.
     * Includes information about the matches.
     *
     * @return a string representing the tournament tree.
     */
    @Override
    public String toString() {
        return "TournamentTree{" + ", matches=" + matches + '}';
    }
}