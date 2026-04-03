package Configurations;

import com.zhaw.it.pm3.tournamentgenerator.configurations.Config;
import com.zhaw.it.pm3.tournamentgenerator.configurations.Games.Game;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.TournamentMode;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConfigTest {

    private Config config;
    private Team teamA;
    private Team teamB;
    private ArrayList<Team> teams;
    private Game game;
    private TournamentMode tournamentMode;

    @BeforeEach
    public void setUp() {
        config = new Config();
        teamA = new Team("Team A");
        teamB = new Team("Team B");
        teams = new ArrayList<>();
        teams.add(teamA);
        teams.add(teamB);
        game = new Game("Soccer");
        tournamentMode = new TournamentMode() {
            public String getName() {
                return "Single Elimination";
            }
        };
    }

    @Test
    public void testSetAndGetTournamentName() {
        config.setTournamentName("Champions League");
        assertEquals("Champions League", config.getTournamentName());
    }

    @Test
    public void testSetAndGetCreatorId() {
        config.setCreatorId("admin123");
        assertEquals("admin123", config.getCreatorId());
    }

    @Test
    public void testSetAndGetPassword() {
        config.setPassword("password");
        assertEquals("password", config.getPassword());
    }

    @Test
    public void testSetAndGetTournamentMode() {
        config.setTournamentMode(tournamentMode);
        assertEquals(tournamentMode, config.getTournamentMode());
    }

    @Test
    public void testSetAndGetTeams() {
        config.setTeams(teams);
        assertEquals(teams, config.getTeams());
    }

    @Test
    public void testSetAndGetGame() {
        config.setGame(game);
        assertEquals(game, config.getGame());
    }

    @Test
    public void testConfigInitialization() {
        assertNotNull(config);
    }
}