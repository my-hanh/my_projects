package com.zhaw.it.pm3.tournamentgenerator.controller;

import com.zhaw.it.pm3.tournamentgenerator.configurations.Config;
import com.zhaw.it.pm3.tournamentgenerator.domain.DataModel;
import com.zhaw.it.pm3.tournamentgenerator.persistence.PersistenceTournaments;
import com.zhaw.it.pm3.tournamentgenerator.persons.Administrator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.zhaw.it.pm3.tournamentgenerator.controller.BaseMainController.openNextPage;

/**
 * This class is the controller for the load tournament view.
 */
public class LoadTournamentController {

    @FXML
    private TableColumn<Config, String> columnID;

    @FXML
    private TableColumn<Config, String> columnMode;

    @FXML
    private TableColumn<Config, String> columnName;

    @FXML
    private Button loadJSONFileButton;

    @FXML
    private Button returnButton;

    @FXML
    private Button searchFileButton;

    @FXML
    private Button selectButton;

    @FXML
    private Button searchIDButton;

    @FXML
    private Button resetButton;

    @FXML
    private TableView<Config> tableViewTournament;

    @FXML
    private TextField textfieldFile;

    @FXML
    private Label labelAdminID;

    private Administrator admin;


    public String readFilepath;
    private ObservableList<Config> configs = FXCollections.observableArrayList();
    private String initialFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources";

    /**
     * Loads the JSON file containing tournament configurations and populates the table view.
     * Validates the file path before loading.
     *
     * @param event the ActionEvent that triggered this method.
     * @throws FileNotFoundException if the file cannot be found.
     */
    @FXML
    void loadJSONFile(ActionEvent event) throws FileNotFoundException {
        readFilepath = textfieldFile.getText();

        if (readFilepath.isEmpty() || readFilepath.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warnung");
            alert.setHeaderText("Bitte einen gueltigen Dateipfad angeben.");
            alert.showAndWait();
            return;
        }

        configs = FXCollections.observableArrayList(PersistenceTournaments.loadConfigs());


        columnName.setCellValueFactory(new PropertyValueFactory<>("tournamentName"));
        columnID.setCellValueFactory(new PropertyValueFactory<>("creatorId"));
        columnMode.setCellValueFactory(new PropertyValueFactory<>("tournamentMode"));
        tableViewTournament.setItems(configs);

    }

    /**
     * Returns the user to the main menu.
     * Loads the main menu UI and resets the stage dimensions.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    void returnToMenu(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/menuUI.fxml"));

        try {
            Pane pageRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(pageRoot);


            stage.setWidth(600);
            stage.setHeight(400);
            stage.setMaxWidth(600); // Optional, falls Einschränkungen gewünscht
            stage.setMinWidth(600);
            stage.setMinHeight(400);

            MainMenuUIController mainMenuUIController = loader.getController();
            if (SignInController.isLoginSuccessful) {
                mainMenuUIController.changeLoadTournamentButton(true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens a file chooser dialog for selecting a JSON file.
     * Sets the selected file path to the text field.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    void searchFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON Files", "*.json");
        fileChooser.getExtensionFilters().add(jsonFilter);

        File initialDirectory = new File(initialFilePath);
        if (initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        Stage stage = (Stage) loadJSONFileButton.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            readFilepath = selectedFile.getAbsolutePath();
            textfieldFile.setText(readFilepath);
        }

    }

    /**
     * Selects a tournament configuration from the table and loads it into the DataModel.
     * Switches to the "Create New Tournament" view with the selected configuration.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    void selectTournament(ActionEvent event) {
        Config chosenTournament = tableViewTournament.getSelectionModel().getSelectedItem();
        DataModel.getInstance().setTournamentName(chosenTournament.getTournamentName());
        DataModel.getInstance().setCreatorId(chosenTournament.getCreatorId());
        DataModel.getInstance().setTeamCount(chosenTournament.getTeams().size());
        DataModel.getInstance().setSelectedTournamentMode(chosenTournament.getTournamentMode().toString());

        DataModel.getInstance().setTeams(FXCollections.observableArrayList(chosenTournament.getTeams()));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/createNewTournamentUI.fxml"));


        try {
            Pane pageRoot = loader.load();
            openNextPage(loader, event, pageRoot);

            CreateNewTournamentUIController createNewTournamentUIController = loader.getController();
            createNewTournamentUIController.setAdmin(SignInController.currentAdmin);
            createNewTournamentUIController.setAdminLabel(SignInController.currentAdminName);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Searches for tournaments by the administrator ID.
     * Loads the matching configurations into the table.
     *
     * @param event the ActionEvent that triggered this method.
     * @throws FileNotFoundException if the configurations cannot be loaded.
     */
    @FXML
    void searchTournamentByID(ActionEvent event) throws FileNotFoundException {
        configs = FXCollections.observableArrayList(PersistenceTournaments.loadConfigs());

        if (configs.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warnung");
            alert.setHeaderText("Keine Turniere gefunden");
            alert.setContentText("Es wurden keine Turniere für die angegebene Admin-ID gefunden.");
            alert.showAndWait();
            SignInController.isLoginSuccessful = false;
            return;
        } else {
            SignInController.isLoginSuccessful = true;
        }

        resetButton.setVisible(true);
        searchIDButton.setVisible(false);
    }

    /**
     * Resets the tournament search and reloads all configurations.
     * Restores the visibility of the search and reset buttons.
     *
     * @param event the ActionEvent that triggered this method.
     * @throws FileNotFoundException if the configurations cannot be loaded.
     */
    @FXML
    void resetSearch(ActionEvent event) throws FileNotFoundException {
        configs = FXCollections.observableArrayList(PersistenceTournaments.loadConfigs());

        resetButton.setVisible(false);
        searchIDButton.setVisible(true);
    }

    /**
     * Sets the administrator for this controller.
     *
     * @param admin the Administrator object to set.
     */
    public void setAdmin(Administrator admin) {
        this.admin = admin;
    }

    /**
     * Updates the admin ID label in the UI with the specified ID.
     *
     * @param id the ID of the administrator to display.
     */
    void setLabelAdminID(String id) {
        labelAdminID.setText(id);
    }
}
