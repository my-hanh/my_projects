package Domain;

import com.zhaw.it.pm3.tournamentgenerator.configurations.Config;
import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.TournamentMode;
import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.domain.Tournament;
import com.zhaw.it.pm3.tournamentgenerator.domain.TournamentTree;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TournamentTest {

    private Tournament tournament;

    @BeforeEach
    public void setUp() {
        tournament = new Tournament("123", "Test Tournament");
    }


    @Test
    public void testGenerateTreeFailureNoTeams() {
        // Arrange
        Config config = tournament.getConfig();
        TournamentMode mockMode = mock(TournamentMode.class);
        config.setTournamentMode(mockMode);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, tournament::generateTree);
        assertEquals("No teams set.", exception.getMessage());
    }

    @Test
    public void testGenerateTreeFailureNoMode() {
        // Arrange
        Config config = tournament.getConfig();
        config.setTeams(new ArrayList<>());
        config.getTeams().add(new Team("Team 1"));
        config.getTeams().add(new Team("Team 2"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, tournament::generateTree);
        assertEquals("Tournament mode not set.", exception.getMessage());
    }

    @Test
    public void testStartNextRoundSuccess() {
        // Arrange
        TournamentTree mockTree = mock(TournamentTree.class);
        when(mockTree.getNumberOfRounds()).thenReturn(2);
        when(mockTree.isRoundFinished(0)).thenReturn(true);

        tournament.setTournamentTree(mockTree);

        TournamentMode mockMode = mock(TournamentMode.class);
        Config config = tournament.getConfig();
        config.setTournamentMode(mockMode);

        // Act
        tournament.startNextRound();

        // Assert
        assertEquals(1, tournament.getRoundsPlayed(), "The number of rounds played should be incremented");
        verify(mockMode, times(1)).playNextRound(mockTree, 1);
    }

    @Test
    public void testStartNextRoundFailureNotFinished() {
        // Arrange
        TournamentTree mockTree = mock(TournamentTree.class);
        when(mockTree.isRoundFinished(0)).thenReturn(false);

        tournament.setTournamentTree(mockTree);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, tournament::startNextRound);
        assertEquals("Round is not finished yet", exception.getMessage());
    }

    @Test
    public void testStartNextRoundEndsTournament() {
        // Arrange
        TournamentTree mockTree = mock(TournamentTree.class);
        when(mockTree.getNumberOfRounds()).thenReturn(1);

        // Use an ArrayList with test data
        ArrayList<Match> lastRoundMatches = new ArrayList<>();
        Match mockMatch = mock(Match.class);
        when(mockMatch.isPlayed()).thenReturn(true);
        lastRoundMatches.add(mockMatch);

        ArrayList<ArrayList<Match>> matches = new ArrayList<>();
        matches.add(lastRoundMatches);

        // Return the correctly initialized ArrayList
        doReturn(matches).when(mockTree).getMatches();
        when(mockTree.isRoundFinished(0)).thenReturn(true);

        tournament.setTournamentTree(mockTree);

        // Act
        tournament.startNextRound();

        // Assert
        assertEquals(0, tournament.getRoundsPlayed(), "No additional rounds should be played if the tournament ends");
    }


    @Test
    public void testEndTournament() {
        // Act & Assert
        assertDoesNotThrow(tournament::endTournament, "Ending the tournament should not throw an exception");
    }

    @Test
    public void testGetTournamentTree() {
        // Arrange
        TournamentTree mockTree = mock(TournamentTree.class);
        tournament.setTournamentTree(mockTree);

        // Act
        TournamentTree result = tournament.getTournamentTree();

        // Assert
        assertEquals(mockTree, result, "The retrieved tournament tree should match the set tree");
    }

    @Test
    public void testGetName() {
        // Act
        String name = tournament.getName();

        // Assert
        assertEquals("Test Tournament", name, "The tournament name should match the one set in the constructor");
    }

    @Test
    public void testGetId() {
        // Act
        String id = tournament.getId();

        // Assert
        assertEquals("123", id, "The tournament ID should match the one set in the constructor");
    }

    @Test
    public void testGetRoundsPlayed() {
        // Act
        int roundsPlayed = tournament.getRoundsPlayed();

        // Assert
        assertEquals(0, roundsPlayed, "Initially, no rounds should be played");
    }

    @Test
    public void testGetConfig() {
        // Act
        Config config = tournament.getConfig();

        // Assert
        assertNotNull(config, "The tournament should always have a config object");
    }
}
