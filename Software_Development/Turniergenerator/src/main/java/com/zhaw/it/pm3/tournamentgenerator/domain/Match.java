package com.zhaw.it.pm3.tournamentgenerator.domain;


import com.zhaw.it.pm3.tournamentgenerator.persons.Team;

/**
 * This class represents a match between two teams.
 */
public class Match {

    private Team team1;
    private Team team2;
    private int team1Score;
    private int team2Score;
    private boolean isPlayed;
    private Match nextMatch;

    /**
     * Constructor to create a match with two participating teams.
     *
     * @param participant1 the first team participating in the match.
     * @param participant2 the second team participating in the match.
     */
    public Match(Team participant1, Team participant2) {
        this.team1 = participant1;
        this.team2 = participant2;
        this.team1Score = 0;
        this.team2Score = 0;
        this.isPlayed = false;
    }

    /**
     * Default constructor to create a match with placeholder teams.
     * Both teams are initialized with "TBD" names.
     */
    public Match() {
        this.team1 = new Team("TBD"); // Muss noch geändert werden
        this.team2 = new Team("TBD"); // Muss noch geändert werden
        this.team1Score = 0;
        this.team2Score = 0;
        this.isPlayed = false;
    }

    /**
     * Gets the first team in the match.
     *
     * @return the first team.
     */
    public Team getTeam1() {
        return team1;
    }

    /**
     * Gets the second team in the match.
     *
     * @return the second team.
     */
    public Team getTeam2() {
        return team2;
    }

    /**
     * Sets the first team in the match.
     *
     * @param team1 the first team to set.
     */
    public void setTeam1(Team team1) {
        this.team1 = team1;
    }

    /**
     * Sets the second team in the match.
     *
     * @param team2 the second team to set.
     */
    public void setTeam2(Team team2) {
        this.team2 = team2;
    }

    /**
     * Gets the score of the first team.
     *
     * @return the score of the first team.
     */
    public int getTeam1Score() {
        return team1Score;
    }

    /**
     * Gets the score of the second team.
     *
     * @return the score of the second team.
     */
    public int getTeam2Score() {
        return team2Score;
    }

    /**
     * Checks if the match has been played.
     *
     * @return true if the match has been played, false otherwise.
     */
    public boolean isPlayed() {
        return isPlayed;
    }

    /**
     * Sets the match as played or not played.
     *
     * @param isPlayed true if the match has been played, false otherwise.
     */
    public void setPlayed(boolean isPlayed) {
        this.isPlayed = isPlayed;
    }

    /**
     * Plays the match and sets the scores for both teams.
     * Marks the match as played.
     *
     * @param participant1Score the score of the first team.
     * @param participant2Score the score of the second team.
     */
    public void playMatch(int participant1Score, int participant2Score) {
        this.team1Score = participant1Score;
        this.team2Score = participant2Score;
        this.isPlayed = true;
    }

    /**
     * Gets the winner of the match.
     *
     * @return the winning team, or null if the match has not been played.
     */
    public Team getWinner() {
        if (isPlayed) {
            if (team1Score > team2Score) {
                return team1;
            } else {
                return team2;
            }
        } else {
            return null;
        }
    }

    /**
     * Gets the loser of the match.
     *
     * @return the losing team, or null if the match has not been played.
     */
    public Team getLoser() {
        if (isPlayed) {
            if (team1Score < team2Score) {
                return team1;
            } else {
                return team2;
            }
        } else {
            return null;
        }
    }

    /**
     * Checks if the match resulted in a draw.
     *
     * @return true if the match is a draw, false otherwise.
     */
    public boolean isDraw() {
        if (isPlayed) {
            return team1Score == team2Score;
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of the match, including team names and scores.
     *
     * @return a string representing the match.
     */
    public String toString() {
        return team1.getName() + " " + team1Score + " - " + team2Score + " " + team2.getName();
    }

    /**
     * Gets the next match in the tournament bracket.
     *
     * @return the next match, or null if no next match is set.
     */
    public Match getNextMatch() {
        return nextMatch;
    }

    /**
     * Sets the next match in the tournament bracket.
     *
     * @param nextMatch the next match to set.
     */
    public void setNextMatch(Match nextMatch) {
        this.nextMatch = nextMatch;
    }
}
