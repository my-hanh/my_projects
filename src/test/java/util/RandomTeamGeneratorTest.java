package util;

import com.zhaw.it.pm3.tournamentgenerator.domain.DataModel;
import com.zhaw.it.pm3.tournamentgenerator.persons.Player;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import com.zhaw.it.pm3.tournamentgenerator.util.RandomTeamGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RandomTeamGeneratorTest {

    private DataModel dataModel;
    private RandomTeamGenerator randomTeamGenerator;

    @BeforeEach
    public void setUp() {
        dataModel = DataModel.getInstance();
        dataModel.getPlayers().clear();
        dataModel.getTeams().clear();

        randomTeamGenerator = new RandomTeamGenerator();

        // Prepopulate teams
        for (int i = 1; i <= 5; i++) {
            dataModel.getTeams().add(new Team("Team " + i));
        }
    }

    @Test
    public void testGenerateTeams() {
        // Arrange
        List<Player> players = dataModel.getPlayers();
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));
        players.add(new Player("Player 3"));
        players.add(new Player("Player 4"));

        // Act
        randomTeamGenerator.generateTeams(2);

        // Assert
        List<Team> teams = dataModel.getTeams();
        assertTrue(teams.stream().allMatch(team -> team.getPlayers().size() <= 2),
                "Each team should have at most 2 players");
        assertEquals(0, players.size(), "The players list should be empty after assignment");
    }

    @Test
    public void testGenerateTeamsWithZeroTeams() {
        // Arrange
        List<Player> players = dataModel.getPlayers();
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));

        // Act
        randomTeamGenerator.generateTeams(0);

        // Assert
        assertEquals(0, dataModel.getTeams().stream().mapToInt(team -> team.getPlayers().size()).sum(),
                "No players should be assigned when number of teams is 0");
        assertFalse(players.isEmpty(), "Players list should remain unchanged");
    }

    @Test
    public void testGenerateTeamsWithUnevenPlayers() {
        // Arrange
        List<Player> players = dataModel.getPlayers();
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));
        players.add(new Player("Player 3"));

        // Act
        randomTeamGenerator.generateTeams(2);

        // Assert
        assertEquals(3, dataModel.getTeams().stream().mapToInt(team -> team.getPlayers().size()).sum(),
                "All players should be assigned to teams");
        assertTrue(dataModel.getTeams().stream().anyMatch(team -> team.getPlayers().size() == 2),
                "One team should have 2 players");
        assertTrue(dataModel.getTeams().stream().anyMatch(team -> team.getPlayers().size() == 1),
                "One team should have 1 player");
    }

    @Test
    public void testGenerateTeamsWithMoreTeamsThanPlayers() {
        // Arrange
        List<Player> players = dataModel.getPlayers();
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));

        // Act
        randomTeamGenerator.generateTeams(5);

        // Assert
        assertEquals(2, dataModel.getTeams().stream().mapToInt(team -> team.getPlayers().size()).sum(),
                "All players should be assigned to teams");
        assertEquals(3, dataModel.getTeams().stream().filter(team -> team.getPlayers().isEmpty()).count(),
                "Three teams should have no players assigned");
    }

    @Test
    public void testGenerateTeamsWithNegativeTeams() {
        // Arrange
        List<Player> players = dataModel.getPlayers();
        players.add(new Player("Player 1"));
        players.add(new Player("Player 2"));

        // Act
        randomTeamGenerator.generateTeams(-1);

        // Assert
        assertEquals(0, dataModel.getTeams().stream().mapToInt(team -> team.getPlayers().size()).sum(),
                "Negative number of teams should result in no players being assigned");
        assertFalse(players.isEmpty(), "Players list should remain unchanged");
    }
}
