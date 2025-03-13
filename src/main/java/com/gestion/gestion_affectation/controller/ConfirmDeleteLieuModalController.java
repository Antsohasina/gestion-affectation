package com.gestion.gestion_affectation.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ConfirmDeleteLieuModalController {

    @FXML
    private Label confirmationLabel;

    private Stage stage;
    private boolean confirmed = false;

    // Méthode pour définir le stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // Méthode pour personnaliser le message (facultatif)
    public void setConfirmationMessage(String message) {
        confirmationLabel.setText(message);
    }

    // Retourne true si l'utilisateur confirme la suppression
    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        confirmed = true;
        stage.close();
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        confirmed = false;
        stage.close();
    }
}