import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.persons.Participant;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.KO;
import main.java.com.zhaw.it.pm3.tournamentgenerator.Persons.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestKO {

    private KO koMode;
    private ArrayList<Participant> participants;
    private ArrayList<Match> matches;

    @BeforeEach
    public void setUp() {
        koMode = new KO();
        participants = new ArrayList<>();
        matches = new ArrayList<>();

        // Add participants
        participants.add(new Player("Participant 1"));
        participants.add(new Player("Participant 2"));
        participants.add(new Player("Participant 3"));
        participants.add(new Player("Participant 4"));

        // Create initial matches
        koMode.createMatches(participants, matches);
    }

    @Test
    public void testCreateMatches() {
        assert(matches.size() == 2);
    }

    @Test
    public void testPlayNextRound() {
        // Set scores for the first round
        matches.get(0).setParticipant1Score(3);
        matches.get(0).setParticipant2Score(1);
        matches.get(1).setParticipant1Score(2);
        matches.get(1).setParticipant2Score(4);

        // Play the next round
        koMode.playNextRound(participants, matches);

        // Verify winners
        ArrayList<Participant> winners = koMode.getWinners();
        assertEquals(2, winners.size());
        assertTrue(winners.contains(participants.get(0)));
        assertTrue(winners.contains(participants.get(3)));

        // Verify new matches
        ArrayList<Match> matchesWinners = koMode.getMatchesWinners();
        assertEquals(1, matchesWinners.size());
        assertEquals(participants.get(0), matchesWinners.get(0).getParticipant1());
        assertEquals(participants.get(3), matchesWinners.get(0).getParticipant2());
    }
}