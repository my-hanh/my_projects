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
 * This class is the controller for the new account view.
 */
public class NewAccountController extends BaseMainController {

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField userPasswordField;

    @FXML
    private TextField senderEmailField;

    @FXML
    private PasswordField appPasswordField;

    @FXML
    private PasswordField userConfirmPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmButton;

    private SignInController signInController;

    private MainMenuUIController mainMenuUIController;

    public static boolean isNewAccountLoginSuccesful = false;

    /**
     * Sets the SignInController for this controller.
     *
     * @param controller the SignInController to associate with this controller.
     */
    public void setNewAccountController(SignInController controller){
        this.signInController = controller;
    }

    /**
     * Sets the MainMenuUIController for this controller.
     *
     * @param controller the MainMenuUIController to associate with this controller.
     */
    public void setMainMenuUIController(MainMenuUIController controller) {
        this.mainMenuUIController = controller;
    }

    /**
     * Confirms and creates a new user account.
     * Validates the provided username, password, and optional email credentials.
     * If the input is valid, creates a new Administrator account and saves it in the DataModel and UserDataManager.
     * Updates the current login state and provides visual feedback on success or error.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void confirmLoginUser(ActionEvent event) {
        String username = usernameTextField.getText();
        String password = userPasswordField.getText();
        String confirmPassword = userConfirmPasswordField.getText();

        if ((username == null || username.isEmpty()) || (password == null || password.isEmpty()) || (confirmPassword == null || confirmPassword.isEmpty())) {
            errorLabel.setText("Please enter a valid username and password");
            errorLabel.setVisible(true);
        } else if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match!");
            errorLabel.setVisible(true);
        } else if (hasEmailAndPassword() && !hasValidEmail()) {
            errorLabel.setText("Please enter a valid email address");
            errorLabel.setVisible(true);
        } else {
            if (showConfirmationDialog()) {
                Administrator admin = new Administrator(username);
                admin.setPassword(password);
                DataModel.getInstance().setCreatorId(username);
                UserDataManager.addOrUpdateUser(admin);
                SignInController.currentAdmin = admin; // Update the currentAdmin
                SignInController.currentAdminName = username; // Update the currentAdminName
                if (hasEmailAndPassword()) {
                    admin.setSenderEmail(senderEmailField.getText());
                    admin.setSenderPassword(appPasswordField.getText());
                }
                cancelButton.setDisable(true);
                confirmButton.setDisable(true);
                errorLabel.setStyle("-fx-text-fill: green");
                errorLabel.setText("Login successful");
                errorLabel.setVisible(true);
                isNewAccountLoginSuccesful = true;
                SignInController.isLoginSuccessful = false;

                if (mainMenuUIController != null) {
                    mainMenuUIController.changeStartTournamentButton(false);
                    mainMenuUIController.changeLoadTournamentButton(true);
                    mainMenuUIController.enableSignInButton();
                }

                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(e -> ((Stage) ((Node) event.getSource()).getScene().getWindow()).close());
                pause.play();
            }
        }
    }

    /**
     * Checks if both email and app password fields are filled.
     *
     * @return true if both fields are non-empty, false otherwise.
     */
    private boolean hasEmailAndPassword() {
        return senderEmailField.getText() != null && !senderEmailField.getText().isEmpty() && appPasswordField.getText() != null && !appPasswordField.getText().isEmpty();
    }

    /**
     * Validates the format of the email address provided in the senderEmailField.
     *
     * @return true if the email address matches a valid format, false otherwise.
     */
    private boolean hasValidEmail() {
        return senderEmailField.getText().matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }

    /**
     * Returns to the sign-in UI.
     * Closes the current stage and opens the SignInUI window.
     *
     * @param event the ActionEvent that triggered this method.
     */
    @FXML
    private void backToSignInUI(ActionEvent event) {
        Stage oldStage = (Stage) cancelButton.getScene().getWindow();
        oldStage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com.zhaw.it.pm3.tournamentgenerator/SignInUI.fxml"));
            Parent signInUIParent = loader.load();

            SignInController signInController = loader.getController();
            signInController.setSignInController(mainMenuUIController);

            Stage stage = new Stage();
            stage.setScene(new Scene(signInUIParent));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Error loading the UI", e.getMessage());
        }
    }



}
