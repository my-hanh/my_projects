package Domain;

import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MatchTest {

    private Match match;
    private Team team1;
    private Team team2;

    @BeforeEach
    void setUp() {
        team1 = new Team("Team 1");
        team2 = new Team("Team 2");
        match = new Match(team1, team2);
    }

    @Test
    void testInitialState() {
        assertEquals(team1, match.getTeam1(), "Team 1 should be correctly set");
        assertEquals(team2, match.getTeam2(), "Team 2 should be correctly set");
        assertEquals(0, match.getTeam1Score(), "Initial score for Team 1 should be 0");
        assertEquals(0, match.getTeam2Score(), "Initial score for Team 2 should be 0");
        assertFalse(match.isPlayed(), "Match should not be marked as played initially");
    }

    @Test
    void testPlayMatch() {
        match.playMatch(3, 2);
        assertEquals(3, match.getTeam1Score(), "Team 1 score should be updated to 3");
        assertEquals(2, match.getTeam2Score(), "Team 2 score should be updated to 2");
        assertTrue(match.isPlayed(), "Match should be marked as played after playMatch() is called");
    }

    @Test
    void testGetWinnerTeam1Wins() {
        match.playMatch(4, 2);
        assertEquals(team1, match.getWinner(), "Team 1 should be the winner with higher score");
        assertEquals(team2, match.getLoser(), "Team 2 should be the loser with lower score");
    }

    @Test
    void testGetWinnerTeam2Wins() {
        match.playMatch(1, 3);
        assertEquals(team2, match.getWinner(), "Team 2 should be the winner with higher score");
        assertEquals(team1, match.getLoser(), "Team 1 should be the loser with lower score");
    }

    @Test
    void testIsDraw() {
        match.playMatch(2, 2);
        assertTrue(match.isDraw(), "Match should be a draw if scores are equal");
        match.playMatch(2, 1);
        assertFalse(match.isDraw(), "Match should not be a draw ");
    }

}
