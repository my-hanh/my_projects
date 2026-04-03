package Configurations.TournamentMode;

import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.SingleElimination;
import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.domain.Tournament;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class SingleEliminationTest {

    private ArrayList<Team> teams;
    private Tournament tournament;
    @BeforeEach
    void setUp() {
        teams = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {
            teams.add(new Team("Team " + i));
        }
        tournament = new Tournament();
        tournament.getConfig().setTournamentName("Test Tournament");
        tournament.getConfig().setTournamentMode(new SingleElimination());
        tournament.getConfig().setTeams(teams);
        tournament.generateTree();
    }

    @Test
    void testPlayNextRound() {
        tournament.getTournamentTree().getMatchesForRound(0).get(0).playMatch(2, 0);
        tournament.getTournamentTree().getMatchesForRound(0).get(1).playMatch(2, 0);
        tournament.getTournamentTree().getMatchesForRound(0).get(2).playMatch(2, 0);
        tournament.getTournamentTree().getMatchesForRound(0).get(3).playMatch(2, 0);
        tournament.startNextRound();

        ArrayList<Match> matches = tournament.getTournamentTree().getMatchesForRound(1);
        assertEquals(2, matches.size(), "Round 2 should contain 2 matches");
        assertEquals(teams.get(0), matches.get(0).getTeam1(), "Team 1 should be in match 1 of round 2");
        assertEquals(teams.get(2), matches.get(0).getTeam2(), "Team 3 should be in match 1 of round 2");
        assertEquals(teams.get(4), matches.get(1).getTeam1(), "Team 5 should be in match 2 of round 2");
        assertEquals(teams.get(6), matches.get(1).getTeam2(), "Team 7 should be in match 2 of round 2");
    }

}
