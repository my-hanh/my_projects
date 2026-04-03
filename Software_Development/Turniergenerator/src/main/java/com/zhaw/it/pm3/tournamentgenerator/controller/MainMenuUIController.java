package com.zhaw.it.pm3.tournamentgenerator.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * This class is the controller for the main menu view.
 */
public class MainMenuUIController extends BaseMainController {

    @FXML
    private Button loadTournamentButton;

    @FXML
    private Button startTournamentButton;

    @FXML
    private Label welcomeAccountLabel;

    @FXML
    private Button signInButton;

    /**
     * Initializes the main menu UI.
     * Sets up a timeline to periodically check the login state and update UI components
     * such as the welcome label, load tournament button, and start tournament button.
     */
    @FXML
    public void initialize() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.1), event -> {
            if (SignInController.currentAdminName != null) {
                welcomeAccountLabel.setText("Welcome " + SignInController.currentAdminName + "!");
                welcomeAccountLabel.setVisible(true);

                boolean isExistingAccount = SignInController.isLoginSuccessful;
                boolean isNewAccount = NewAccountController.isNewAccountLoginSuccesful;

                loadTournamentButton.setDisable(!isExistingAccount);
                startTournamentButton.setDisable(!(isExistingAccount || isNewAccount));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Opens the tournament configuration view.
     * Switches to the "Create New Tournament" view and sets the administrator details.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    public void openTournamentConfig(ActionEvent event) {
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
     * Enables or disables the "Load Tournament" button based on the provided value.
     *
     * @param enable true to enable the button, false to disable it.
     */
    public void changeLoadTournamentButton(boolean enable) {
        loadTournamentButton.setDisable(enable);
    }

    /**
     * Enables or disables the "Start Tournament" button based on the provided value.
     *
     * @param enable true to enable the button, false to disable it.
     */
    public void changeStartTournamentButton(boolean enable) {
        startTournamentButton.setDisable(enable);
    }

    /**
     * Opens the sign-in UI.
     * Loads the "Sign In" view in a new stage and disables the sign-in button in the main menu
     * until the sign-in window is closed.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void openSignInUI(ActionEvent event) {
        try {
            signInButton.setDisable(true);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/SignInUI.fxml"));
            Parent signInUIParent = loader.load();

            SignInController signInController = loader.getController();
            signInController.setSignInController(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(signInUIParent));
            stage.setResizable(false);
            stage.setOnCloseRequest(e -> enableSignInButton());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the "Load Tournament" page.
     * Switches to the "Load Tournament" view and sets the administrator details.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    void openLoadTournamentPage(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/loadTournamentUI.fxml"));
        try {
            Pane pageRoot = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(pageRoot);
            stage.setMinWidth(1000);
            stage.setMinHeight(402);
            stage.show();

            LoadTournamentController loadTournamentController = loader.getController();
            loadTournamentController.setAdmin(SignInController.currentAdmin);
            loadTournamentController.setLabelAdminID(SignInController.currentAdminName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Enables the "Sign In" button in the main menu.
     * Called when the sign-in window is closed.
     */
    public void enableSignInButton() {
        signInButton.setDisable(false);
    }
}
