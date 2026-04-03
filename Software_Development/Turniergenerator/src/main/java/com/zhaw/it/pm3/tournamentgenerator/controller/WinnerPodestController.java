package com.zhaw.it.pm3.tournamentgenerator.controller;

import com.zhaw.it.pm3.tournamentgenerator.domain.Tournament;
import com.zhaw.it.pm3.tournamentgenerator.persons.RankedTeam;
import com.zhaw.it.pm3.tournamentgenerator.persons.Team;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.zhaw.it.pm3.tournamentgenerator.controller.BaseMainController.openNextPage;

/**
 * This class is the controller for the winner podest view.
 */
public class WinnerPodestController {

    @FXML
    private TableColumn<RankedTeam, Object> columnRank;

    @FXML
    private TableColumn<RankedTeam, Integer> columnScore;

    @FXML
    private TableColumn<RankedTeam, String> columnTeam;

    @FXML
    private Button returnButton;

    @FXML
    private TableView<RankedTeam> tableViewRanking;

    @FXML
    private Text textFirstPlace;

    @FXML
    private Text textSecondPlace;

    @FXML
    private Text textThirdPlace;

    private Tournament tournament;

    private HashMap<Team, Integer> rankedTeams;

    private final ObservableList<RankedTeam> allData = FXCollections.observableArrayList();

    private HashMap<Team, Integer> top3Teams = new HashMap<>();

    /**
     * Sets the tournament associated with this controller.
     *
     * @param tournament the Tournament object to associate with this controller.
     */
    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    /**
     * Sets the ranked teams for this controller.
     * The ranked teams are used to populate the ranking table and display the top 3 teams.
     *
     * @param rankedTeams a HashMap containing teams and their respective ranks.
     */
    public void setRankedTeams(HashMap<Team, Integer> rankedTeams) {
        this.rankedTeams = rankedTeams;
    }

    /**
     * Initializes the winner podium view.
     * Populates the ranking table with team data and updates the display for the top 3 teams.
     */
    @FXML
    public void initialize() {
        if (rankedTeams != null && !rankedTeams.isEmpty()) {
            columnRank.setCellValueFactory(new PropertyValueFactory<>("rank"));
            columnTeam.setCellValueFactory(new PropertyValueFactory<>("teamName"));
            columnScore.setCellValueFactory(new PropertyValueFactory<>("points"));

            convertRankingToObservableList();
            tableViewRanking.setItems(allData);

            top3Teams.clear();
            top3Teams.putAll(rankedTeams);
            updateTop3TeamsDisplay();
        }
    }

    /**
     * Returns to the main menu.
     * Closes the current stage and loads the main menu UI.
     *
     * @param actionEvent the ActionEvent that triggered this method.
     */
    @FXML
    public void returnToMainMenu(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/menuUI.fxml"));

        try {
            Pane pageRoot = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.getScene().setRoot(pageRoot);

            // Set the desired size of the stage
            stage.setWidth(600);
            stage.setHeight(400);
            stage.setMaxWidth(600);
            stage.setMinWidth(600);
            stage.setMinHeight(400);

            if (SignInController.isLoginSuccessful) {
                MainMenuUIController mainMenuUIController = loader.getController();
                mainMenuUIController.changeLoadTournamentButton(true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the ranked teams into an observable list to populate the ranking table.
     * Sorts the ranked teams by rank before adding them to the observable list.
     */
    private void convertRankingToObservableList() {
        rankedTeams.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> {
                    int rank = entry.getValue();
                    Team team = entry.getKey();
                    allData.add(new RankedTeam(rank, team.getName(), team.getPoints()));
                });
    }

    /**
     * Updates the display for the top 3 teams.
     * Sets the text and alignment for the first, second, and third place teams.
     */
    private void updateTop3TeamsDisplay() {
        int countFirstPlace = 0;
        int countSecondPlace = 0;
        int countThirdPlace = 0;
        StringBuilder firstPlace = new StringBuilder();
        StringBuilder secondPlace = new StringBuilder();
        StringBuilder thirdPlace = new StringBuilder();

        for (Map.Entry<Team, Integer> entry : top3Teams.entrySet()) {
            if (entry.getValue() == 1) {
                firstPlace.append(entry.getKey().getName()).append("\n");
                countFirstPlace++;
            } else if (entry.getValue() == 2) {
                secondPlace.append(entry.getKey().getName()).append("\n");
                countSecondPlace++;
            } else if (entry.getValue() == 3) {
                thirdPlace.append(entry.getKey().getName()).append("\n");
                countThirdPlace++;
            }
        }

        adjustTextPosition(textFirstPlace, countFirstPlace);
        adjustTextPosition(textSecondPlace, countSecondPlace);
        adjustTextPosition(textThirdPlace, countThirdPlace);

        textFirstPlace.setText(firstPlace.toString());
        textFirstPlace.setTextAlignment(TextAlignment.CENTER);
        textSecondPlace.setText(secondPlace.toString());
        textSecondPlace.setTextAlignment(TextAlignment.CENTER);
        textThirdPlace.setText(thirdPlace.toString());
        textThirdPlace.setTextAlignment(TextAlignment.CENTER);
    }

    /**
     * Adjusts the vertical position of the text element based on the number of entries.
     *
     * @param text the Text element to adjust.
     * @param count the number of entries for the text.
     */
    private void adjustTextPosition(Text text, int count) {
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                text.setY(text.getY() - 10);
            }
        }
    }
}
