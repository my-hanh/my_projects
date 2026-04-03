package com.zhaw.it.pm3.tournamentgenerator.controller;

import com.zhaw.it.pm3.tournamentgenerator.domain.DataModel;
import com.zhaw.it.pm3.tournamentgenerator.persistence.UserDataManager;
import com.zhaw.it.pm3.tournamentgenerator.persons.Administrator;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * This class is the controller for the sign in view.
 */
public class SignInController extends BaseMainController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField userPasswordField;

    @FXML
    private TextField senderEmailTextField;

    @FXML
    private TextField AppPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmButton;

    @FXML
    private Label noAccountAvailable;

    public static boolean isLoginSuccessful = false;

    public static Administrator currentAdmin;

    public static String currentAdminName;

    private MainMenuUIController mainMenuUIController;

    /**
     * Sets the MainMenuUIController for this controller.
     * Allows interaction with the main menu to enable or disable buttons.
     *
     * @param controller the MainMenuUIController to associate with this controller.
     */
    public void setSignInController(MainMenuUIController controller) {
        this.mainMenuUIController = controller;
    }

    /**
     * Confirms the user's credentials and handles the sign-in process.
     * Validates the username and password, checks the credentials against stored data,
     * and updates the UI upon success or failure.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void confirmUser(ActionEvent event) {
        String username = usernameTextField.getText();
        String password = userPasswordField.getText();

        if(isInputValid(username, password)){
            if(UserDataManager.validateCredentials(username, password)){
                handleSuccessfulLogin(username, password);
                closeStageAfterDelay(event);
            } else {
                showError("Invalid username or password");
            }
        } else {
            showError("Please enter a valid username and password");
        }
    }

    /**
     * Opens the "New Account" UI to allow the user to create a new account.
     * Closes the current stage and loads the new account creation UI.
     */
    @FXML
    private void openNewAccountUI() {
        Stage oldStage = (Stage) noAccountAvailable.getScene().getWindow();
        oldStage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/NewAccountUI.fxml"));
            Parent loginUIParent = loader.load();

            NewAccountController loginController = loader.getController();
            loginController.setNewAccountController(this);
            loginController.setMainMenuUIController(mainMenuUIController);

            Stage stage = new Stage();
            stage.setScene(new Scene(loginUIParent));
            stage.setOnCloseRequest(e -> mainMenuUIController.enableSignInButton());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Error loading the UI", e.getMessage());
        }
    }

    /**
     * Returns to the main menu.
     * Closes the current stage and re-enables the sign-in button in the main menu.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void backToMainMenu(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        mainMenuUIController.enableSignInButton();
    }

    /**
     * Validates the input fields for the username and password.
     *
     * @param username the username input to validate.
     * @param password the password input to validate.
     * @return true if both inputs are non-null and non-empty, false otherwise.
     */
    private boolean isInputValid(String username, String password) {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }

    /**
     * Handles the UI and data updates after a successful login.
     * Updates the current admin information, enables necessary buttons in the main menu,
     * and provides visual feedback for the successful login.
     *
     * @param username the username of the successfully logged-in user.
     * @param password the password of the successfully logged-in user.
     */
    private void handleSuccessfulLogin(String username, String password){
        mainMenuUIController.changeLoadTournamentButton(false);
        mainMenuUIController.changeStartTournamentButton(false);
        cancelButton.setDisable(true);
        confirmButton.setDisable(true);
        noAccountAvailable.setDisable(true);
        errorLabel.setStyle("-fx-text-fill: green");
        errorLabel.setText("Sign In successful");
        errorLabel.setVisible(true);

        isLoginSuccessful = true;
        NewAccountController.isNewAccountLoginSuccesful = false;

        currentAdminName = username;
        currentAdmin = new Administrator(username);
        currentAdmin.setPassword(password);

        DataModel.getInstance().setCreatorId(username);
    }

    /**
     * Closes the current stage after a short delay.
     * Ensures the main menu's sign-in button is re-enabled.
     *
     * @param event the ActionEvent that triggered this method.
     */
    private void closeStageAfterDelay(ActionEvent event) {
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(e -> {
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            mainMenuUIController.enableSignInButton();
        });
        pause.play();
    }

    /**
     * Displays an error message in the error label.
     *
     * @param message the error message to display.
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
