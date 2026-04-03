package com.zhaw.it.pm3.tournamentgenerator.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * This class is the base controller for the main view.
 */
public abstract class BaseMainController {

    @FXML
    private final TextArea errorTextArea;

    /**
     * Default constructor for the BaseMainController class.
     * Initializes the errorTextArea.
     */
    public BaseMainController() {
        errorTextArea = new TextArea();
    }

    /**
     * Updates the error message displayed in the error text area.
     *
     * @param errorMessage the error message to display.
     */
    public void updateErrorMessage(String errorMessage) {
        errorTextArea.setText(errorMessage);
    }

    /**
     * Opens the next page by setting the provided {@code pageRoot} as the new root
     * of the current stage's scene. Adjusts the stage's dimensions to match the new content.
     *
     * @param loader the FXMLLoader used to load the FXML file.
     * @param actionEvent the event that triggered this method.
     * @param pageRoot the root node of the new page.
     */
    @FXML
    protected static void openNextPage(FXMLLoader loader, ActionEvent actionEvent, Pane pageRoot) {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        stage.getScene().setRoot(pageRoot);
        stage.setHeight(pageRoot.getPrefHeight());
        stage.setWidth(pageRoot.getPrefWidth());
        stage.show();
    }

    /**
     * Displays a confirmation dialog to the user with a "Yes" or "No" choice.
     *
     * @return {@code true} if the user confirmed, {@code false} otherwise.
     */
    boolean showConfirmationDialog() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure?");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Displays an error dialog with the specified header and content.
     *
     * @param header the header text for the error dialog.
     * @param content the content text for the error dialog.
     */
    protected void showErrorDialog(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
