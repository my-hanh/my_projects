package com.zhaw.it.pm3.tournamentgenerator.controller;

import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.domain.Tournament;
import com.zhaw.it.pm3.tournamentgenerator.persons.Administrator;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import com.zhaw.it.pm3.tournamentgenerator.services.MessageCaster;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Handles the display and interaction with the tournament tree UI.
 */
public class TreeUIController extends BaseMainController {

    @FXML
    public ToolBar toolBar;
    @FXML
    public AnchorPane anchorPane;
    @FXML
    private Pane tournamentPane = new Pane();
    @FXML
    public Button generateNewTreeButton;
    @FXML
    public Button startTournamentButton;
    @FXML
    public HBox buttonContainer;

    private Administrator admin;

    private double paneWidth = tournamentPane.getPrefWidth();
    private double paneHeight = tournamentPane.getPrefHeight();

    private final double BOXWIDTH = 120;
    private final double BOXHEIGHT = 60;
    private final double TOOLBARHEIGHT = 60;

    private ObservableList<Team> observableAllTeams;
    private final HashMap<Team, Boolean> teamStatus = new HashMap<>();
    private Map<ComboBox<Team>, Team> comboBoxTeamMap = new HashMap<>();
    private Set<Team> selectedTeams = new HashSet<>();
    private Match finalMatch;
    private Tournament tournament;
    private Stage stage;

    private boolean updatingComboBoxes = false;

    public void init(Tournament tournament) {
        this.tournament = tournament;

        if (this.tournament == null) {
            updateErrorMessage("Tournament data is missing.");
            return;
        }
        Team unassigned = new Team("Nicht zugewiesen");
        ArrayList<Team> unassignedTeams = new ArrayList<>();
        unassignedTeams.add(unassigned);

        this.tournament.getConfig().getTeams().forEach(team -> teamStatus.put(team, true));

        this.observableAllTeams = FXCollections.observableArrayList(this.tournament.getConfig().getTeams());
        this.observableAllTeams.add(unassigned);

        initComponents();
        drawTournamentTree();
    }

    /**
     * Updates the list of teams.
     */
    public void updateTeams(){
        // Method placeholder if needed for future team updates.
    }

    /**
     * Initializes components such as pane size and layout.
     */
    public void initComponents() {
        int numberOfTeams = this.tournament.getTournamentTree().getTeams().size();
        if (numberOfTeams <= 4) {
            this.paneHeight = 500;
            this.paneWidth = 700;
        } else if (numberOfTeams <= 8) {
            this.paneHeight = 650;
            this.paneWidth = 900;
        } else if (numberOfTeams <= 16) {
            this.paneHeight = 800;
            this.paneWidth = 1100;
        } else {
            this.paneHeight = 1000;
            this.paneWidth = 1300;
        }

        // Ensure a minimum initial width
        this.paneWidth = Math.max(this.paneWidth, 1200);

        tournamentPane.setPrefSize(paneWidth, paneHeight);

        // Increase initial window size
        stage.setWidth(paneWidth);
        stage.setHeight(paneHeight + TOOLBARHEIGHT);
    }

    /**
     * Draws the tournament tree on the UI pane.
     */
    private void drawTournamentTree() {
        tournamentPane.getChildren().clear(); // Clear previous contents
        ArrayList<ArrayList<Match>> rounds = this.tournament.getTournamentTree().getMatches();

        Map<Match, double[]> matchPositions = new HashMap<>();

        double centerX = paneWidth / 2;
        double centerY = paneHeight / 2;

        int totalRounds = rounds.size();
        double xSpacing = paneWidth / (2 * rounds.size() - 1);

        for (int roundIndex = 0; roundIndex < totalRounds; roundIndex++) {
            List<Match> matches = rounds.get(roundIndex);
            int matchesInRound = matches.size();
            double xDifferencePerRound = xSpacing * ((totalRounds - 1) - roundIndex);

            // Calculate horizontal positions for left and right
            double xLeft = centerX - xDifferencePerRound - BOXWIDTH / 2;
            double xRight = centerX + xDifferencePerRound - BOXWIDTH / 2;

            // Calculate vertical spacing between matches
            double ySpacing = paneHeight / ((double) matchesInRound / 2);

            // Final Round
            if (roundIndex == totalRounds - 1) {
                // Final round, draw the match in the center
                double x = centerX - BOXWIDTH / 2;
                double y = centerY - BOXHEIGHT / 2;
                Match finalMatch = matches.get(0);
                drawMatch(finalMatch, x, y);
                matchPositions.put(finalMatch, new double[]{x, y});
                this.finalMatch = finalMatch; // Store reference to the final match
                continue;
            }

            // Draw Left Side
            for (int matchIndex = 0; matchIndex < matchesInRound / 2; matchIndex++) {
                Match match = matches.get(matchIndex);
                double y = (-ySpacing / 2) + ySpacing * (matchIndex + 1);
                double x = xLeft;

                // Draw match box
                drawMatch(match, x, y);
                matchPositions.put(match, new double[]{x, y});
            }

            // Draw Right Side
            for (int matchIndex = matchesInRound / 2; matchIndex < matchesInRound; matchIndex++) {
                Match match = matches.get(matchIndex);
                double y = (-ySpacing / 2) + ySpacing * ((matchIndex - matchesInRound / 2) + 1);
                double x = xRight;

                // Draw match box
                drawMatch(match, x, y);
                matchPositions.put(match, new double[]{x, y});
            }
        }

        // Now, draw lines between matches
        for (ArrayList<Match> round : rounds) {
            for (Match match : round) {
                Match nextMatch = match.getNextMatch();
                if (nextMatch != null) {
                    double[] startPos = matchPositions.get(match);
                    double[] endPos = matchPositions.get(nextMatch);
                    if (startPos != null && endPos != null) {
                        double startX, startY, endX, endY;

                        // Determine side of the current match
                        if (startPos[0] < centerX) {
                            // Left side match
                            startX = startPos[0] + BOXWIDTH; // Right edge of current box
                            startY = startPos[1] + BOXHEIGHT / 2;
                        } else if (startPos[0] > centerX) {
                            // Right side match
                            startX = startPos[0]; // Left edge of current box
                            startY = startPos[1] + BOXHEIGHT / 2;
                        } else {
                            // Center match (final match)
                            continue; // No lines starting from the center match
                        }

                        // Determine end position
                        if (nextMatch.equals(finalMatch)) {
                            // Next match is the final match
                            if (startPos[0] < centerX) {
                                // Coming from left side
                                endX = endPos[0]; // Left edge of final box
                                endY = endPos[1] + BOXHEIGHT / 2;
                            } else {
                                // Coming from right side
                                endX = endPos[0] + BOXWIDTH; // Right edge of final box
                                endY = endPos[1] + BOXHEIGHT / 2;
                            }
                        } else {
                            // Next match is not the final match
                            if (endPos[0] < centerX) {
                                // Next match is on the left side
                                endX = endPos[0]; // Left edge of next box
                                endY = endPos[1] + BOXHEIGHT / 2;
                            } else if (endPos[0] > centerX) {
                                // Next match is on the right side
                                endX = endPos[0] + BOXWIDTH; // Right edge of next box
                                endY = endPos[1] + BOXHEIGHT / 2;
                            } else {
                                // Next match is center (should not happen here)
                                endX = endPos[0]; // Left edge of next box
                                endY = endPos[1] + BOXHEIGHT / 2;
                            }
                        }

                        // Draw the line
                        drawLine(startX, startY, endX, endY);
                    }
                }
            }
        }
    }

    /**
     * Draws a single match box in the UI.
     *
     * @param match the match to draw
     * @param x     the x-coordinate for the box
     * @param y     the y-coordinate for the box
     */
    private void drawMatch(Match match, double x, double y) {
        VBox matchBox = new VBox();
        matchBox.setLayoutX(x);
        matchBox.setLayoutY(y);
        matchBox.setPrefSize(BOXWIDTH, BOXHEIGHT);
        matchBox.setStyle("-fx-border-color: #4C566A; -fx-background-color: #D8DEE9; -fx-alignment: center; -fx-spacing: 5; -fx-padding: 5;");

        Team team1 = match.getTeam1();
        Team team2 = match.getTeam2();

        VBox teamContainer = new VBox(5);
        teamContainer.setPrefWidth(BOXWIDTH);

        // Handle Team 1
        if (team1.getName().equals("TBD") || team1.getName().equals("Bye")) {
            Label team1Label = new Label(team1.getName());
            team1Label.setStyle("-fx-text-fill: #2E3440;");
            teamContainer.getChildren().add(team1Label);
        } else {
            ComboBox<Team> team1ComboBox = new ComboBox<>();
            team1ComboBox.setMaxWidth(100);
            comboBoxTeamMap.put(team1ComboBox, team1);

            if (!team1.getName().equals("Nicht zugewiesen")) {
                selectedTeams.add(team1);
            }

            team1ComboBox.setOnAction(e -> {
                Team oldTeam = comboBoxTeamMap.get(team1ComboBox);
                Team newTeam = team1ComboBox.getValue();
                if (oldTeam != null && !oldTeam.getName().equals("Nicht zugewiesen")) {
                    selectedTeams.remove(oldTeam);
                }
                if (newTeam != null && !newTeam.getName().equals("Nicht zugewiesen")) {
                    selectedTeams.add(newTeam);
                }
                comboBoxTeamMap.put(team1ComboBox, newTeam);
                match.setTeam1(newTeam);
                updateAllComboBoxes();
            });

            team1ComboBox.setValue(team1);
            teamContainer.getChildren().add(team1ComboBox);
        }

        // Handle Team 2
        if (team2.getName().equals("TBD") || team2.getName().equals("Bye")) {
            Label team2Label = new Label(team2.getName());
            team2Label.setStyle("-fx-text-fill: #2E3440;");
            teamContainer.getChildren().add(team2Label);
        } else {
            ComboBox<Team> team2ComboBox = new ComboBox<>();
            team2ComboBox.setMaxWidth(100);
            comboBoxTeamMap.put(team2ComboBox, team2);

            if (!team2.getName().equals("Nicht zugewiesen")) {
                selectedTeams.add(team2);
            }

            team2ComboBox.setOnAction(e -> {
                Team oldTeam = comboBoxTeamMap.get(team2ComboBox);
                Team newTeam = team2ComboBox.getValue();
                if (oldTeam != null && !oldTeam.getName().equals("Nicht zugewiesen")) {
                    selectedTeams.remove(oldTeam);
                }
                if (newTeam != null && !newTeam.getName().equals("Nicht zugewiesen")) {
                    selectedTeams.add(newTeam);
                }
                comboBoxTeamMap.put(team2ComboBox, newTeam);
                match.setTeam2(newTeam);
                updateAllComboBoxes();
            });

            team2ComboBox.setValue(team2);
            teamContainer.getChildren().add(team2ComboBox);
        }

        // After adding both teams, we can update the combo boxes
        updateAllComboBoxes();

        matchBox.getChildren().add(teamContainer);
        tournamentPane.getChildren().add(matchBox);
    }

    /**
     * Updates the disable state of ComboBox elements for team selection.
     * Ensures that selected teams cannot be chosen again in other matches.
     */
    private void updateAllComboBoxes() {
        if (updatingComboBoxes) return;
        updatingComboBoxes = true;
        try {
            for (ComboBox<Team> comboBox : comboBoxTeamMap.keySet()) {
                Team currentlySelectedTeam = comboBox.getValue();

                List<Team> availableTeams = new ArrayList<>();
                for (Team t : observableAllTeams) {
                    // Skip the "Bye" team entirely for the dropdown
                    if (t.getName().equals("Bye")) {
                        continue;
                    }

                    // Conditions to include a team:
                    // - It's "Nicht zugewiesen"
                    // - It's the currently selected team (to not remove it)
                    // - It's not currently selected by any other combo box
                    if (t.getName().equals("Nicht zugewiesen")
                            || Objects.equals(t, currentlySelectedTeam)
                            || !selectedTeams.contains(t)) {
                        if (!availableTeams.contains(t)) {
                            availableTeams.add(t);
                        }
                    }
                }

                comboBox.setItems(FXCollections.observableArrayList(availableTeams));

                if (currentlySelectedTeam != null && availableTeams.contains(currentlySelectedTeam)) {
                    comboBox.setValue(currentlySelectedTeam);
                } else {
                    // If previously selected team is no longer available, set to "Nicht zugewiesen"
                    Optional<Team> unassigned = availableTeams.stream()
                            .filter(team -> team.getName().equals("Nicht zugewiesen"))
                            .findFirst();
                    unassigned.ifPresent(comboBox::setValue);
                }
            }
        } finally {
            updatingComboBoxes = false;
        }
    }

    /**
     * Draws a connecting line between two match boxes.
     *
     * @param startX the starting x-coordinate
     * @param startY the starting y-coordinate
     * @param endX   the ending x-coordinate
     * @param endY   the ending y-coordinate
     */
    private void drawLine(double startX, double startY, double endX, double endY) {
        Line line = new Line();
        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);
        line.setStrokeWidth(2);
        line.setStyle("-fx-stroke: #4C566A;");
        tournamentPane.getChildren().add(line);
    }

    public void setStage(Stage stage){
        this.stage = stage;
    }


    /**
     * Generates a new tournament tree and redraws it.
     *
     * @param actionEvent the event triggered by clicking the generate button
     */
    public void generateNewTree(ActionEvent actionEvent) {
        tournament.generateTree();
        this.tournament.getConfig().getTeams().forEach(team -> teamStatus.put(team, true));
        selectedTeams.clear();
        comboBoxTeamMap.clear();
        drawTournamentTree();
    }

    /**
     * Opens the score tree UI and sends emails if configured.
     *
     * @param event the event triggered by clicking the start button
     */
    @FXML
    void openPlayTournament(ActionEvent event) {
        for (ComboBox<Team> comboBox : comboBoxTeamMap.keySet()) {
            Team selectedTeam = comboBox.getValue();
            if (selectedTeam == null || selectedTeam.getName().equals("Nicht zugewiesen")) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fehler");
                alert.setHeaderText(null);
                alert.setContentText("Alle Teams müssen zugewiesen sein, um fortzufahren.");
                alert.showAndWait();
                return;
            }
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/scoreTreeUI.fxml"));
            Pane pageRoot = loader.load();
            ScoreTreeUIController controller = loader.getController();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            controller.setStage(stage);
            controller.setTournament(tournament);
            controller.init(tournament);

            stage.getScene().setRoot(pageRoot);
            if (admin != null) {
                if (admin.getSenderEmail() != null || admin.getSenderPassword() != null ) {
                    MessageCaster messageCaster = new MessageCaster(admin.getSenderEmail(),admin.getSenderPassword());
                    messageCaster.sendEmailToAllPlayers(pageRoot, Paths.get("tournament_tree.png").toString(), tournament.getConfig().getTeams());
                }
            }

            stage.setMinWidth(1000);
            stage.setMinHeight(402);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the FXML file.", e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the administrator for this controller.
     *
     * @param admin the administrator to set
     */
    public void setAdmin(Administrator admin){
        this.admin = admin;
    }
}
