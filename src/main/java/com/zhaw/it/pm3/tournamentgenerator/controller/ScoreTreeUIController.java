// ScoreTreeUIController.java
package com.zhaw.it.pm3.tournamentgenerator.controller;

import com.zhaw.it.pm3.tournamentgenerator.configurations.TournamentModes.SingleElimination;
import com.zhaw.it.pm3.tournamentgenerator.domain.Match;
import com.zhaw.it.pm3.tournamentgenerator.domain.Tournament;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

/**
 * This class is the controller for the score tree view.
 */
public class ScoreTreeUIController extends BaseMainController{

    @FXML
    public ToolBar toolBar;
    @FXML
    public Button nextRoundButton;
    @FXML
    private Pane tournamentPane = new Pane();
    @FXML
    public HBox buttonContainer;
    @FXML
    private Label warningLabel;
    @FXML
    private Button podestButton;

    private double paneWidth = tournamentPane.getPrefWidth();
    private double paneHeight = tournamentPane.getPrefHeight();

    private final double BOXWIDTH = 100;
    private final double BOXHEIGHT = 50;

    private Match finalMatch;
    private Tournament tournament;
    private Stage stage;
    private ArrayList<Team> sortedList = new ArrayList<>();
    private HashMap<Team, Integer> rankedTeams = new HashMap<>();

    private final SingleElimination singleElimination = new SingleElimination();

    /**
     * Initializes the controller with a given tournament.
     * If the tournament is null, displays an error message.
     *
     * @param tournament the Tournament object to initialize the controller with.
     */
    public void init(Tournament tournament){
        this.tournament = tournament;

        if(this.tournament==null){
            updateErrorMessage("Tournament data is missing.");
            return;
        }


        initComponents();
        drawTournamentTree();
    }

    /**
     * Initializes UI components based on the number of teams in the tournament.
     * Adjusts the size of the pane and stage to fit the tournament tree.
     */
    public void initComponents(){
        int numberOfTeams = this.tournament.getTournamentTree().getTeams().size();
        if (numberOfTeams <= 4) {
            this.paneHeight = 500;
            this.paneWidth = 700;
        } else if (numberOfTeams <=8) {
            this.paneHeight = 750;
            this.paneWidth = 1000;
        } else if (numberOfTeams <= 16) {
            this.paneHeight = 1000;
            this.paneWidth = 1500;
        } else {
            this.paneHeight = 1100;
            this.paneWidth = 1700;
        }

        stage.setMaxWidth(paneWidth);
        double TOOLBARHEIGHT = 60;
        stage.setMaxHeight(paneHeight + TOOLBARHEIGHT);
        stage.setHeight(paneHeight + TOOLBARHEIGHT);
        stage.setWidth(paneWidth);

        tournamentPane.setPrefWidth(paneWidth);
        tournamentPane.setPrefHeight(paneHeight);
        tournamentPane.setLayoutY(TOOLBARHEIGHT);



        toolBar.setPrefHeight(TOOLBARHEIGHT);
        toolBar.setLayoutY(0);
        toolBar.prefWidthProperty().bind(tournamentPane.prefWidthProperty());
    }

    /**
     * Draws the tournament tree on the pane.
     * Clears the pane and iterates through the rounds to draw matches and connections.
     */
    private void drawTournamentTree(){
        tournamentPane.getChildren().clear();
        ArrayList<ArrayList<Match>> rounds = this.tournament.getTournamentTree().getMatches();

        Map<Match, double[]> matchPositions = new HashMap<>();

        double centerX = paneWidth / 2;
        double centerY = paneHeight / 2;

        int totalRounds = rounds.size();
        double xSpacing = paneWidth / (2 * rounds.size() - 1);

        // Zeichnet Matches für jede Runde
        drawRounds(rounds, matchPositions, centerX, centerY, totalRounds, xSpacing);

        // Verbindet Matches mit Linien
        connectMatches(rounds, matchPositions, centerX);
    }

    /**
     * Draws matches for each round on the tournament tree.
     * Positions matches on the left and right sides of the tree and handles the final match separately.
     *
     * @param rounds the list of rounds to draw.
     * @param matchPositions a map to store positions of matches.
     * @param centerX the center x-coordinate of the pane.
     * @param centerY the center y-coordinate of the pane.
     * @param totalRounds the total number of rounds in the tournament.
     * @param xSpacing the horizontal spacing between rounds.
     */
    private void drawRounds(ArrayList<ArrayList<Match>> rounds, Map<Match, double[]> matchPositions,
                            double centerX, double centerY, int totalRounds, double xSpacing){
        for(int roundIndex = 0; roundIndex < totalRounds; roundIndex++){
            List<Match> matches = rounds.get(roundIndex);
            int matchesInRound = matches.size();

            double xDifferencePerRound = xSpacing * ((totalRounds - 1) - roundIndex);

            double xLeft = centerX - xDifferencePerRound - BOXWIDTH / 2;
            double xRight = centerX + xDifferencePerRound - BOXWIDTH / 2;

            double ySpacing = paneHeight / ((double) matchesInRound / 2);

            if(roundIndex==totalRounds - 1){
                // Zeichnet das Finalspiel
                drawFinalMatch(matches.get(0), centerX, centerY, matchPositions);
            } else{
                // Zeichnet die Matches auf der linken und rechten Seite
                drawLeftMatches(matches, matchPositions, xLeft, ySpacing, matchesInRound);
                drawRightMatches(matches, matchPositions, xRight, ySpacing, matchesInRound);
            }
        }
    }


    /**
     * Draws the final match in the tournament.
     *
     * @param finalMatch the final match to draw.
     * @param centerX the center x-coordinate of the pane.
     * @param centerY the center y-coordinate of the pane.
     * @param matchPositions a map to store the position of the final match.
     */
    private void drawFinalMatch(Match finalMatch, double centerX, double centerY, Map<Match, double[]> matchPositions){
        double x = centerX - BOXWIDTH / 2;
        double y = centerY - BOXHEIGHT / 2;

        drawMatch(finalMatch, x, y);
        matchPositions.put(finalMatch, new double[]{x, y});
        this.finalMatch = finalMatch;
    }

    /**
     * Draws a winner label above the match box for the winning team.
     *
     * @param winner the winning team.
     * @param boxX the x-coordinate of the match box.
     * @param boxY the y-coordinate of the match box.
     */
    private void drawWinnerLabel(Team winner, double boxX, double boxY) {
        Label winnerLabel = new Label("Winner: " + winner.getName() + "!");
        winnerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: green; -fx-font-size: 20px;");

        // Measure the width of the label
        new Scene(new Group(winnerLabel));
        winnerLabel.applyCss();
        double labelWidth = winnerLabel.prefWidth(-1);

        double labelX = boxX + (BOXWIDTH - labelWidth) / 2; // Center the label above the box
        double labelY = boxY - BOXHEIGHT;

        winnerLabel.setLayoutX(labelX);
        winnerLabel.setLayoutY(labelY);
        tournamentPane.getChildren().add(winnerLabel);
    }

    /**
     * Draws matches on the left side of the tree for a given round.
     *
     * @param matches the list of matches in the round.
     * @param matchPositions a map to store positions of matches.
     * @param xLeft the x-coordinate for the left side.
     * @param ySpacing the vertical spacing between matches.
     * @param matchesInRound the number of matches in the round.
     */
    private void drawLeftMatches(List<Match> matches, Map<Match, double[]> matchPositions,
                                 double xLeft, double ySpacing, int matchesInRound){
        for(int matchIndex = 0; matchIndex < matchesInRound / 2; matchIndex++){
            Match match = matches.get(matchIndex);
            double y = (- ySpacing / 2) + ySpacing * (matchIndex + 1);

            drawMatch(match, xLeft, y);
            matchPositions.put(match, new double[]{xLeft, y});
        }
    }

    /**
     * Draws matches on the right side of the tree for a given round.
     *
     * @param matches the list of matches in the round.
     * @param matchPositions a map to store positions of matches.
     * @param xRight the x-coordinate for the right side.
     * @param ySpacing the vertical spacing between matches.
     * @param matchesInRound the number of matches in the round.
     */
    private void drawRightMatches(List<Match> matches, Map<Match, double[]> matchPositions,
                                  double xRight, double ySpacing, int matchesInRound){
        for(int matchIndex = matchesInRound / 2; matchIndex < matchesInRound; matchIndex++){
            Match match = matches.get(matchIndex);
            double y = (- ySpacing / 2) + ySpacing * ((matchIndex - (double) matchesInRound / 2) + 1);

            drawMatch(match, xRight, y);
            matchPositions.put(match, new double[]{xRight, y});
        }
    }


    /**
     * Connects matches from one round to the next using lines.
     *
     * @param rounds the list of rounds in the tournament.
     * @param matchPositions a map of match positions.
     * @param centerX the center x-coordinate of the pane.
     */
    private void connectMatches(ArrayList<ArrayList<Match>> rounds, Map<Match, double[]> matchPositions, double centerX){
        for(ArrayList<Match> round : rounds){
            for(Match match : round){
                Match nextMatch = match.getNextMatch();
                if(nextMatch!=null){
                    drawConnection(match, nextMatch, matchPositions, centerX);
                }
            }
        }
    }

    /**
     * Draws a connection line between two matches.
     *
     * @param match the starting match.
     * @param nextMatch the next match to connect to.
     * @param matchPositions a map of match positions.
     * @param centerX the center x-coordinate of the pane.
     */
    private void drawConnection(Match match, Match nextMatch, Map<Match, double[]> matchPositions, double centerX){
        double[] startPos = matchPositions.get(match);
        double[] endPos = matchPositions.get(nextMatch);

        if(startPos!=null && endPos!=null){
            double startX, startY, endX, endY;

            if(startPos[0] < centerX){
                startX = startPos[0] + BOXWIDTH;
                startY = startPos[1] + BOXHEIGHT / 2;
            } else if(startPos[0] > centerX){
                startX = startPos[0];
                startY = startPos[1] + BOXHEIGHT / 2;
            } else{
                return;
            }

            if(nextMatch.equals(finalMatch)){
                if(startPos[0] < centerX){
                    endX = endPos[0];
                } else{
                    endX = endPos[0] + BOXWIDTH;
                }
                endY = endPos[1] + BOXHEIGHT / 2;
            } else{
                if(endPos[0] < centerX){
                    endX = endPos[0];
                    endY = endPos[1] + BOXHEIGHT / 2;
                } else if(endPos[0] > centerX){
                    endX = endPos[0] + BOXWIDTH;
                    endY = endPos[1] + BOXHEIGHT / 2;
                } else{
                    endX = endPos[0];
                    endY = endPos[1] + BOXHEIGHT / 2;
                }
            }

            drawLine(startX, startY, endX, endY);
        }
    }

    /**
     * Draws a match box with team names and score fields.
     *
     * @param match the Match object to draw.
     * @param x the x-coordinate of the match box.
     * @param y the y-coordinate of the match box.
     */
    private void drawMatch(Match match, double x, double y){
        VBox matchBox = new VBox();
        matchBox.setId("match-" + match.hashCode());
        matchBox.setLayoutX(x);
        matchBox.setLayoutY(y);
        matchBox.setPrefSize(BOXWIDTH, BOXHEIGHT);
        matchBox.setStyle("-fx-border-color: black; -fx-alignment: center;");

        Label team1Label = new Label(match.getTeam1()!=null ? match.getTeam1().getName() : "TBD");
        team1Label.setId("team1-" + match.hashCode());
        team1Label.setWrapText(true);
        team1Label.setPrefWidth(BOXWIDTH - 20);

        Label team2Label = new Label(match.getTeam2()!=null ? match.getTeam2().getName() : "TBD");
        team2Label.setId("team2-" + match.hashCode());
        team2Label.setWrapText(true);
        team2Label.setPrefWidth(BOXWIDTH - 20);

        HBox team1Box = new HBox(team1Label);
        team1Box.setSpacing(10);
        team1Box.setAlignment(Pos.CENTER);

        HBox team2Box = new HBox(team2Label);
        team2Box.setSpacing(10);
        team2Box.setAlignment(Pos.CENTER);

        if(! "TBD".equals(team1Label.getText()) && ! "Bye".equals(team2Label.getText()) ){
            TextField team1Score = new TextField();
            team1Score.setId("score1-" + match.hashCode());
            team1Score.setPrefWidth(50);
            team1Box.getChildren().add(team1Score);
        }

        if(! "TBD".equals(team2Label.getText()) && ! "Bye".equals(team2Label.getText()) ){
            TextField team2Score = new TextField();
            team2Score.setId("score2-" + match.hashCode());
            team2Score.setPrefWidth(50);
            team2Box.getChildren().add(team2Score);
        }



        matchBox.getChildren().addAll(team1Box, team2Box);
        tournamentPane.getChildren().add(matchBox);
    }

    /**
     * Draws a line connecting two points.
     *
     * @param startX the starting x-coordinate.
     * @param startY the starting y-coordinate.
     * @param endX the ending x-coordinate.
     * @param endY the ending y-coordinate.
     */
    private void drawLine(double startX, double startY, double endX, double endY){
        Line line = new Line();
        line.setStartX(startX);
        line.setStartY(startY);
        line.setEndX(endX);
        line.setEndY(endY);
        tournamentPane.getChildren().add(line);
    }

    /**
     * Sets the stage for this controller.
     *
     * @param stage the Stage object to set.
     */
    public void setStage(Stage stage){
        this.stage = stage;
    }

    /**
     * Sets the tournament for this controller.
     *
     * @param tournament the Tournament object to set.
     */
    public void setTournament(Tournament tournament){
        this.tournament = tournament;
    }

    /**
     * Generates the next round of matches in the tournament.
     * Validates scores, processes matches, and updates the tournament tree.
     */
    @FXML
    public void generateNextRound() {
        warningLabel.setVisible(false);
        ArrayList<Team> roundRankingList = new ArrayList<>();
        ArrayList<ArrayList<Match>> rounds = this.tournament.getTournamentTree().getMatches();
        int currentRound = this.tournament.getRoundsPlayed();

        if (currentRound >= rounds.size()) {
            updateErrorMessage("All rounds have been played.");
            return;
        }

        ArrayList<Match> matches = rounds.get(currentRound);

        // Check for draws and invalid inputs first
        for (Match match : matches) {
            if (match.getTeam1() == null || match.getTeam2() == null) {
                continue;
            }

            VBox matchBox = (VBox) tournamentPane.lookup("#match-" + match.hashCode());
            if (matchBox == null) {
                continue;
            }

            Node team1ScoreNode = matchBox.lookup("#score1-" + match.hashCode());
            Node team2ScoreNode = matchBox.lookup("#score2-" + match.hashCode());

            if (team1ScoreNode instanceof TextField team1ScoreField && team2ScoreNode instanceof TextField team2ScoreField) {
                try {
                    String team1ScoreText = team1ScoreField.getText();
                    String team2ScoreText = team2ScoreField.getText();

                    if (team1ScoreText.isEmpty() || team2ScoreText.isEmpty()) {
                        warningLabel.setText("Scores cannot be empty. Please enter valid scores.");
                        warningLabel.setVisible(true);
                        return;
                    }

                    int team1Score = Integer.parseInt(team1ScoreText);
                    int team2Score = Integer.parseInt(team2ScoreText);

                    if (team1Score == team2Score) {
                        warningLabel.setText("Draw not allowed. Please enter different scores.");
                        warningLabel.setVisible(true);
                        return;
                    }
                    if (team1Score < 0 || team2Score < 0) {
                        warningLabel.setText("Scores cannot be negative. Please enter valid scores.");
                        warningLabel.setVisible(true);
                        return;
                    }
                } catch (NumberFormatException e) {
                    warningLabel.setText("Invalid score input. Please enter valid numbers.");
                    warningLabel.setVisible(true);
                    return;
                }
            }
        }

        // Process matches if no draws or invalid inputs are found
        for (Match match : matches) {
            if (match.getTeam1() == null || match.getTeam2() == null) {
                continue;
            }

            VBox matchBox = (VBox) tournamentPane.lookup("#match-" + match.hashCode());
            if (matchBox == null) {
                continue;
            }

            Node team1ScoreNode = matchBox.lookup("#score1-" + match.hashCode());
            Node team2ScoreNode = matchBox.lookup("#score2-" + match.hashCode());

            if (team1ScoreNode instanceof TextField team1ScoreField && team2ScoreNode instanceof TextField team2ScoreField) {
                try {
                    int team1Score = Integer.parseInt(team1ScoreField.getText());
                    int team2Score = Integer.parseInt(team2ScoreField.getText());

                    match.playMatch(team1Score, team2Score);

                    // Update team points
                    match.getTeam1().addPoints(team1Score);
                    match.getTeam2().addPoints(team2Score);

                    if (team1Score > team2Score) {
                        roundRankingList.add(match.getTeam2());
                    } else {
                        roundRankingList.add(match.getTeam1());
                    }
                    if (this.finalMatch != null && this.finalMatch.isPlayed()) {
                        roundRankingList.add(this.finalMatch.getWinner());
                    }

                    if (match.getNextMatch() != null) {
                        if (team1Score > team2Score) {
                            match.getNextMatch().setTeam1(match.getTeam1());
                        } else {
                            match.getNextMatch().setTeam1(match.getTeam2());
                        }
                    }

                    replaceTextFieldWithLabel(team1ScoreField, String.valueOf(team1Score),
                            "score1-" + match.hashCode(), match.getTeam1().getName());
                    replaceTextFieldWithLabel(team2ScoreField, String.valueOf(team2Score),
                            "score2-" + match.hashCode(), match.getTeam2().getName());
                } catch (NumberFormatException e) {
                    updateErrorMessage("Invalid score input. Please enter valid numbers.");
                    return;
                }
            }
        }

        // Sort the roundRankingList based on points in descending order
        roundRankingList.sort(Comparator.comparingInt(Team::getPoints));

        // Add sorted teams to rankedList
        for (Team team : roundRankingList) {
            sortedList.addFirst(team);
        }

        // Handle the final match explicitly
        if (currentRound == rounds.size() - 1 && this.finalMatch != null) {
            VBox matchBox = (VBox) tournamentPane.lookup("#match-" + this.finalMatch.hashCode());
            if (matchBox != null) {
                updateMatchBox(matchBox, this.finalMatch);
            }

            // Check if the final match is played
            if (this.finalMatch.isPlayed()) {
                podestButton.setDisable(false);
                nextRoundButton.setDisable(true);
                for (int i = 0; i < sortedList.size(); i++) {
                    rankedTeams.put(sortedList.get(i), i + 1);
                }
                this.tournament.startNextRound();
                drawWinnerLabel(this.finalMatch.getWinner(), paneWidth / 2 - BOXWIDTH / 2, paneHeight / 2 - BOXHEIGHT * 1.5);
                return;
            }
        }

        this.tournament.startNextRound();
        singleElimination.playNextRound(this.tournament.getTournamentTree(), currentRound + 1);
        updateTournamentTree();

    }

    /**
     * Replaces a text field with a label displaying the score and team name.
     *
     * @param textField the TextField to replace.
     * @param value the score value to display.
     * @param labelId the ID for the new label.
     * @param teamName the name of the team.
     */
    private void replaceTextFieldWithLabel(TextField textField, String value, String labelId, String teamName){
        HBox parentBox = (HBox) textField.getParent();
        if(parentBox!=null){
            parentBox.getChildren().clear();

            Label teamLabel = new Label(teamName + ": " + value);
            teamLabel.setId(labelId);
            teamLabel.setPrefWidth(100);

            parentBox.getChildren().add(teamLabel);
        }
    }

    /**
     * Updates the tournament tree by refreshing the match boxes with current data.
     */
    private void updateTournamentTree(){
        for(Node node : tournamentPane.getChildren()){
            if(node instanceof VBox matchBox){
                String matchId = matchBox.getId();
                if(matchId!=null && matchId.startsWith("match-")){
                    Match match = findMatchById(matchId);
                    if(match!=null){
                        matchBox.getChildren().clear();
                        updateMatchBox(matchBox, match);
                    }
                }
            }
        }
    }

    /**
     * Finds a match by its ID in the tournament tree.
     *
     * @param matchId the ID of the match to find.
     * @return the Match object, or null if not found.
     */
    private Match findMatchById(String matchId){
        for(ArrayList<Match> round : this.tournament.getTournamentTree().getMatches()){
            for(Match match : round){
                if(("match-" + match.hashCode()).equals(matchId)){
                    return match;
                }
            }
        }
        return null;
    }

    /**
     * Updates a match box with the latest match data.
     *
     * @param matchBox the VBox representing the match.
     * @param match the Match object to update the box with.
     */
    private void updateMatchBox(VBox matchBox, Match match){
        matchBox.getChildren().clear(); // Clear the current content of the matchBox

        String team1Text = match.getTeam1()!=null && ! "TBD".equals(match.getTeam1().getName()) ? match.getTeam1().getName() + ": " : "TBD";
        String team2Text = match.getTeam2()!=null && ! "TBD".equals(match.getTeam2().getName()) ? match.getTeam2().getName() + ": " : "TBD";

        Label team1Label = new Label(team1Text);
        team1Label.setId("team1-" + match.hashCode());
        team1Label.setWrapText(true);
        team1Label.setPrefWidth(BOXWIDTH - 20);

        Label team2Label = new Label(team2Text);
        team2Label.setId("team2-" + match.hashCode());
        team2Label.setWrapText(true);
        team2Label.setPrefWidth(BOXWIDTH - 20);

        HBox team1Box = new HBox(team1Label);
        team1Box.setSpacing(10);
        team1Box.setAlignment(Pos.CENTER);

        HBox team2Box = new HBox(team2Label);
        team2Box.setSpacing(10);
        team2Box.setAlignment(Pos.CENTER);

        // Only add text fields if both teams are present and not "TBD"
        if(match.getTeam1()!=null && match.getTeam2()!=null &&
                ! "TBD".equals(match.getTeam1().getName()) && ! "TBD".equals(match.getTeam2().getName())){
            if(match.isPlayed()){
                // If the match has been played, show the result
                Label team1ScoreLabel = new Label(String.valueOf(match.getTeam1Score()));
                team1ScoreLabel.setId("score1-" + match.hashCode());
                team1ScoreLabel.setPrefWidth(50);
                team1Box.getChildren().add(team1ScoreLabel);

                Label team2ScoreLabel = new Label(String.valueOf(match.getTeam2Score()));
                team2ScoreLabel.setId("score2-" + match.hashCode());
                team2ScoreLabel.setPrefWidth(50);
                team2Box.getChildren().add(team2ScoreLabel);

                // Set colors based on the match result
                if(match.getTeam1Score() > match.getTeam2Score()){
                    team1Label.setStyle("-fx-text-fill: green;");
                    team2Label.setStyle("-fx-text-fill: red;");
                } else{
                    team1Label.setStyle("-fx-text-fill: red;");
                    team2Label.setStyle("-fx-text-fill: green;");
                }
            } else{
                // If the match has not been played, add text fields
                TextField team1Score = new TextField();
                team1Score.setId("score1-" + match.hashCode());
                team1Score.setPrefWidth(50);
                team1Box.getChildren().add(team1Score);

                TextField team2Score = new TextField();
                team2Score.setId("score2-" + match.hashCode());
                team2Score.setPrefWidth(50);
                team2Box.getChildren().add(team2Score);
            }
        }

        matchBox.getChildren().addAll(team1Box, team2Box);
    }

    /**
     * Gets the sorted list of teams based on their ranking.
     *
     * @return the sorted list of teams.
     */
    public ArrayList<Team> getSortedList(){
        return sortedList;
    }

    /**
     * Navigates to the winner podium view to display the final rankings.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    void goToWinnerPodest(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/finalWinnerPodestUI.fxml"));

            // Create a new instance of WinnerPodestController
            WinnerPodestController controller = new WinnerPodestController();

            // Set the rankedTeams before loading the FXML
            controller.setRankedTeams(rankedTeams);

            // Set the controller factory to return the pre-configured controller
            loader.setControllerFactory(param -> controller);

            Pane pageRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(pageRoot);
            stage.setWidth(800);
            stage.setHeight(700);
            stage.setMaxWidth(800);
            stage.setMinWidth(800);
            stage.setMinHeight(700);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load the FXML file.", e);
        }
    }
}
