package Domain;

import com.zhaw.it.pm3.tournamentgenerator.configurations.Config;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.SingleElimination;

import com.zhaw.it.pm3.tournamentgenerator.domain.Tournament;

import com.zhaw.it.pm3.tournamentgenerator.domain.TournamentGenerator;
import com.zhaw.it.pm3.tournamentgenerator.domain.TournamentTree;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TournamentGeneratorTest {

    private TournamentGenerator generator;
    private Config config;

    @BeforeEach
    void setUp() {
        config = new Config();
        config.setTournamentMode(new SingleElimination());
        Team team1 = new Team("Team 1");
        Team team2 = new Team("Team 2");
        Team team3 = new Team("Team 3");
        Team team4 = new Team("Team 4");
        Team team5 = new Team("Team 5");
        Team team6 = new Team("Team 6");
        Team team7 = new Team("Team 7");
        Team team8 = new Team("Team 8");
        config.setTeams(new ArrayList<Team>() {{
            add(team1);
            add(team2);
            add(team3);
            add(team4);
            add(team5);
            add(team6);
            add(team7);
            add(team8);
        }});
        generator = new TournamentGenerator(config);
    }

    @Test
    void testGenerateSingleEliminationTree() {
        TournamentTree tree = generator.createTournamentTree();
        assertEquals(8, tree.getTeams().size());
        assertEquals(4, tree.getMatches().get(0).size());
        assertEquals(2, tree.getMatches().get(1).size());
        assertEquals(1, tree.getMatches().get(2).size());
    }

    @Test
    void testGenerateSingleEliminationTreeWithByes9Teams() {
        Team team1 = new Team("Team 9");
        config.getTeams().add(team1);
        TournamentTree tree = generator.createTournamentTree();
        assertEquals(9, tree.getTeams().size());
        assertEquals(8, tree.getMatches().get(0).size());
        assertEquals(4, tree.getMatches().get(1).size());
        assertEquals(2, tree.getMatches().get(2).size());
        assertEquals(1, tree.getMatches().get(3).size());

    }

    @Test
    void testSingleEliminationWithTwoTeams() {
        config.getTeams().clear();
        config.getTeams().add(new Team("Team 1"));
        config.getTeams().add(new Team("Team 2"));

        generator = new TournamentGenerator(config);
        TournamentTree tree= generator.createTournamentTree();
        assertEquals(2, tree.getTeams().size());
        assertEquals(1, tree.getMatches().get(0).size());
    }


}
