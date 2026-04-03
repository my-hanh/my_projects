package Persons;

import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.persons.Administrator;
import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdministratorTest {

    private Administrator admin;
    private Team team1;
    private Team team2;
    private Player player1;
    private Player player2;
    private Match match;

    @BeforeEach
    void setUp() {
        admin = new Administrator("Admin");
        team1 = new Team("Team A");
        team2 = new Team("Team B");
        player1 = new Player("Player 1");
        player2 = new Player("Player 2");

        match = new Match(team1 ,team2 );
    }


    @Test
    void testRegisterParticipant() {
        team1.addPlayer(player1);
        assertTrue(team1.getPlayers().contains(player1), "Player 1 should be registered to Team A");
    }

    @Test
    void testChangeMatchResult() {
        match.playMatch(0, 0);
        match.playMatch(3, 5);
        assertTrue(match.isPlayed(), "Match should be marked as played");
        assertEquals(3, match.getTeam1Score(), "Team 1 score should be 3");
        assertEquals(5, match.getTeam2Score(), "Team 2 score should be 5");
    }

    @Test
    void testGetRegisteredPlayer() {
        team1.addPlayer(player1);
        assertTrue(team1.getPlayers().contains(player1), "Should retrieve the registered player correctly");
        assertFalse(team1.getPlayers().contains(player2), "Non-registered player should return null");
    }

    @Test
    void testRemovePlayer() {
        team1.addPlayer(player1);
        team1.removePlayer(player1);
        assertFalse(team1.getPlayers().contains(player1), "Player 1 should be removed from Team A");
    }

    @Test
    void testMoveParticipant() {
        team1.addPlayer(player1);
        team1.removePlayer(player1);
        team2.addPlayer(player1);
        assertFalse(team1.getPlayers().contains(player1), "Player 1 should be removed from Team A");
        assertTrue(team2.getPlayers().contains(player1), "Player 1 should be added to Team B");
    }

    @Test
    void testChangePlayerName() {
        player1.setName("New Player 1");
        assertEquals("New Player 1", player1.getName(), "Player's name should be changed to 'New Player 1'");
    }
}