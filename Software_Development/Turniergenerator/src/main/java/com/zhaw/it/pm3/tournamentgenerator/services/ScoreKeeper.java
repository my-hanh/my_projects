package com.zhaw.it.pm3.tournamentgenerator.services;

import com.zhaw.it.pm3.tournamentgenerator.persons.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScoreKeeper {

    public static ArrayList<Team> allTeams = new ArrayList<>();
    public static HashMap<Team, Integer> ranking = new HashMap<>();

    public ScoreKeeper() {
    }

    public static void sortAndRank() {
        insertionSortTeamsByScore();
        rankTeamsByScore();
    }

    public static HashMap<Team, Integer> getTop3Teams() {
        HashMap<Team, Integer> top3Teams = new HashMap<>();
        for (Map.Entry<Team, Integer> entry : ranking.entrySet()) {
            if (entry.getValue() <= 3) {
                top3Teams.put(entry.getKey(), entry.getValue());
            }
        }
        return top3Teams;
    }

    private static void insertionSortTeamsByScore() {
        for (int currentUnsortedIndex = 1; currentUnsortedIndex < allTeams.size(); currentUnsortedIndex++) {
            Team toInsertTeam = allTeams.get(currentUnsortedIndex);
            int currentSortedIndex;
            for (currentSortedIndex = currentUnsortedIndex; (currentSortedIndex > 0) && (allTeams.get(currentSortedIndex - 1).getPoints() < toInsertTeam.getPoints()); currentSortedIndex--) {
                allTeams.set(currentSortedIndex, allTeams.get(currentSortedIndex - 1));
            }
            allTeams.set(currentSortedIndex, toInsertTeam);
        }
    }

    private static void rankTeamsByScore() {
        ranking.clear();
        int rankNumber = 0;
        for (int index = 0; index < allTeams.size(); index++) {
            if (index > 0 && allTeams.get(index - 1).getPoints() == allTeams.get(index).getPoints()) {
                ranking.put(allTeams.get(index), rankNumber);
            } else {
                ranking.put(allTeams.get(index), ++rankNumber);
            }
        }
    }
}
