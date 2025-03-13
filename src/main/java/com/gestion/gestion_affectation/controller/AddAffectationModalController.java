package com.gestion.gestion_affectation.controller;

import com.gestion.gestion_affectation.model.Affectation;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class AddAffectationModalController {

    @FXML
    private TextField codeEmpField;

    @FXML
    private TextField codeLieuField;

    @FXML
    private DatePicker datePicker;

    private ObservableList<Affectation> affectationList;
    private Stage stage;

    public void setStageAndAffectations(Stage stage, ObservableList<Affectation> affectationList) {
        this.stage = stage;
        this.affectationList = affectationList;
    }

    @FXML
    private void handleSave() {
        String codeEmp = codeEmpField.getText();
        String codeLieu = codeLieuField.getText();

        // Vérifier si une date a été sélectionnée
        if (datePicker.getValue() != null) {
            // Convertir LocalDate en java.util.Date
            LocalDate selectedDate = datePicker.getValue();
            Date date = Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Affectation affectation = new Affectation(codeEmp, codeLieu, date);
            affectationList.add(affectation);  // Ajouter l'affectation à la liste
            stage.close();  // Fermer la fenêtre après l'ajout
        } else {
            // Afficher une alerte si la date n'a pas été sélectionnée
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Erreur");
            alert.setHeaderText("Date manquante");
            alert.setContentText("Veuillez choisir une date.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();  // Fermer la fenêtre sans ajouter de nouvelle affectation
    }
}
