package com.zhaw.it.pm3.tournamentgenerator.persons;

/**
 * Represents a ranked team in the tournament system.
 * A ranked team includes its position in the ranking, the team's name, and the points scored.
 */
public class RankedTeam {

    private int rank;

    private String teamName;

    private int points;

    /**
     * Constructs a {@code RankedTeam} object with the specified rank, team name, and points.
     *
     * @param rank     the rank of the team.
     * @param teamName the name of the team.
     * @param points   the points scored by the team.
     */
    public RankedTeam(int rank, String teamName, int points) {
        this.rank = rank;
        this.teamName = teamName;
        this.points = points;
    }

    public int getRank() {
        return rank;
    }

    public String getTeamName() {
        return teamName;
    }

    public int getPoints() {
        return points;
    }
}