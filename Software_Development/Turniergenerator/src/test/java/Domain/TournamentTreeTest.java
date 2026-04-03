package Domain;

import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.domain.TournamentTree;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TournamentTreeTest {

    private TournamentTree tournamentTree;
    private ArrayList<Team> teams;
    private ArrayList<ArrayList<Match>> rounds;

    @BeforeEach
    void setUp() {
        teams = new ArrayList<>();
        rounds = new ArrayList<>();

        // Setup test teams
        for (int i = 1; i <= 8; i++) {
            teams.add(new Team("Team " + i));
        }

        // Setup test matches in rounds
        ArrayList<Match> round1 = new ArrayList<>();
        round1.add(new Match(teams.get(0), teams.get(1)));
        round1.add(new Match(teams.get(2), teams.get(3)));
        round1.add(new Match(teams.get(4), teams.get(5)));
        round1.add(new Match(teams.get(6), teams.get(7)));

        ArrayList<Match> round2 = new ArrayList<>();
        round2.add(new Match(teams.get(0), teams.get(2)));
        round2.add(new Match(teams.get(4), teams.get(6)));

        ArrayList<Match> round3 = new ArrayList<>();
        round3.add(new Match(teams.get(0), teams.get(4)));

        rounds.add(round1);
        rounds.add(round2);
        rounds.add(round3);

        tournamentTree = new TournamentTree(teams, rounds);
    }

    @Test
    void testGetTeams() {
        assertEquals(8, tournamentTree.getTeams().size(), "Tournament should have 8 participants");
    }

    @Test
    void testGetNumberOfRounds() {
        assertEquals(3, tournamentTree.getNumberOfRounds(), "Tournament should have 3 rounds");
    }

    @Test
    void testGetMatchesForRound() {
        assertEquals(4, tournamentTree.getMatchesForRound(0).size(), "First round should have 4 matches");
        assertEquals(2, tournamentTree.getMatchesForRound(1).size(), "Second round should have 2 matches");
        assertEquals(1, tournamentTree.getMatchesForRound(2).size(), "Third round should have 1 match");
    }

    @Test
    void testIsRoundFinished() {
        // Mark all matches in round 0 as played
        for (Match match : tournamentTree.getMatchesForRound(0)) {
            match.setPlayed(true);
        }
        assertTrue(tournamentTree.isRoundFinished(0), "Round 0 should be marked as finished");

        assertFalse(tournamentTree.isRoundFinished(1), "Round 1 should not be finished yet");
    }

    @Test
    void testGetWinnersForRound() {
        Match match = tournamentTree.getMatchesForRound(0).get(0);
        match.playMatch(1,0);  // Team 1 wins the match
        ArrayList<Team> winners = tournamentTree.getWinnersForRound(0);

        assertEquals(1, winners.size(), "There should be 1 winner in round 0");
        assertEquals(teams.get(0), winners.get(0), "The winner of the first match in round 0 should be Team 1");
    }

    @Test
    void testGetLosersForRound() {
        Match match = tournamentTree.getMatchesForRound(0).get(1);
        match.playMatch(0,1);  // Team 3 loses the match
        ArrayList<Team> losers = tournamentTree.getLosersForRound(0);

        assertEquals(1, losers.size(), "There should be 1 loser in round 0");
        assertEquals(teams.get(2), losers.get(0), "The loser of the second match in round 0 should be Team 3");
    }

    @Test
    void testGetMatchesForParticipant() {
        ArrayList<Match> participantMatches1 = tournamentTree.getMatchesForParticipant(teams.get(0));
        ArrayList<Match> participantMatches2 = tournamentTree.getMatchesForParticipant(teams.get(1));


        assertEquals(3, participantMatches1.size(), "Team 1 should have participated in 3 matches");
        assertEquals(1, participantMatches2.size(), "Team 2 should have participated in 1 match");
    }

    @Test
    void testGetDrawsForRound() {
        Match match = tournamentTree.getMatchesForRound(0).get(0);
        match.playMatch(0,0);  // Set the match as a draw
        ArrayList<Match> draws = tournamentTree.getDrawsForRound(0);

        assertEquals(1, draws.size(), "There should be 1 draw in round 0");
        assertTrue(draws.contains(match), "The draw match should be included in the list");
    }
}
