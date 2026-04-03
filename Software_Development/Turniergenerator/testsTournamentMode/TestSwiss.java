import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.persons.Participant;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.Swiss;
import main.java.com.zhaw.it.pm3.tournamentgenerator.Persons.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSwiss {

    private Swiss swissMode;
    private ArrayList<Participant> participants;
    private ArrayList<Match> matches;

    @BeforeEach
    public void setUp() {
        swissMode = new Swiss();
        participants = new ArrayList<>();
        matches = new ArrayList<>();

        // Add participants
        participants.add(new Player("Participant 1"));
        participants.add(new Player("Participant 2"));
        participants.add(new Player("Participant 3"));
        participants.add(new Player("Participant 4"));

        // Create initial matches
        swissMode.createMatches(participants, matches);
    }

    @Test
    public void testCreateMatches() {
        assertEquals(2, matches.size());
    }

    @Test
    public void testPlayNextRound() {
        // Set scores for the first round
        matches.get(0).setParticipant1Score(3);
        matches.get(0).setParticipant2Score(1);
        matches.get(1).setParticipant1Score(2);
        matches.get(1).setParticipant2Score(4);

        // Play the next round
        swissMode.playNextRound(participants, matches);

        // Verify winners
        ArrayList<Participant> winners = swissMode.getWinners();
        assertEquals(2, winners.size());
        assertTrue(winners.contains(participants.get(0)));
        assertTrue(winners.contains(participants.get(3)));

        // Verify new matches
        ArrayList<Match> matchesWinners = swissMode.getMatchesWinners();
        assertEquals(1, matchesWinners.size());
        assertEquals(participants.get(0), matchesWinners.get(0).getParticipant1());
        assertEquals(participants.get(3), matchesWinners.get(0).getParticipant2());
    }
}