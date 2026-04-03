package Persons;

import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeamTest {

    private Team team;

    @BeforeEach
    void setUp() {
        team = new Team("Team A");
    }

    @Test
    void testGetName() {
        assertEquals("Team A", team.getName());
    }

    @Test
    void testSetName() {
        team.setName("Team B");
        assertEquals("Team B", team.getName());
    }

    @Test
    void testAddPoints() {
        team.addPoints(10);
        assertEquals(10, team.getPoints());
    }

    @Test
    void testGetPoints() {
        assertEquals(0, team.getPoints());
        team.addPoints(5);
        assertEquals(5, team.getPoints());
    }

    @Test
    void testSetPoints() {
        team.setPoints(20);
        assertEquals(20, team.getPoints());
    }

    @Test
    void testAddPlayer() {
        Player player = new Player("Player 1");
        team.addPlayer(player);

        ObservableList<Player> players = team.getPlayers();
        assertEquals(1, players.size());
        assertTrue(players.contains(player));
    }

    @Test
    void testRemovePlayer() {
        Player player = new Player("Player 1");
        team.addPlayer(player);
        team.removePlayer(player);

        ObservableList<Player> players = team.getPlayers();
        assertEquals(0, players.size());
        assertFalse(players.contains(player));
    }

    @Test
    void testGetTeamSize() {
        assertEquals(0, team.getTeamSize());

        team.addPlayer(new Player("Player 1"));
        team.addPlayer(new Player("Player 2"));

        assertEquals(2, team.getTeamSize());
    }

    @Test
    void testGetTeamCapacity() {
        assertEquals("0/5", team.getTeamCapacity());

        team.addPlayer(new Player("Player 1"));
        team.addPlayer(new Player("Player 2"));

        assertEquals("2/5", team.getTeamCapacity());
    }

    @Test
    void testToString() {
        assertEquals("Team A", team.toString());

        team.setName("Team B");
        assertEquals("Team B", team.toString());
    }

    @Test
    void testSetPlayers() {
        ObservableList<Player> players = FXCollections.observableArrayList();
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));

        team.setPlayers(players);

        ObservableList<Player> teamPlayers = team.getPlayers();
        assertEquals(2, teamPlayers.size());
        assertEquals("Player 1", teamPlayers.get(0).getName());
        assertEquals("Player 2", teamPlayers.get(1).getName());
    }
}
