package com.gestion.gestion_affectation.controller;

import com.gestion.gestion_affectation.model.Affectation;
import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EditAffectationModalController {

    @FXML
    private TextField codeEmpField; // Champ pour le code de l'employé

    @FXML
    private TextField codePlaceField; // Champ pour le code du lieu

    @FXML
    private DatePicker datePicker; // Champ pour la date

    private Stage stage;
    private Affectation affectationToEdit; // Objet Affectation à modifier
    private ObservableList<Affectation> affectationList; // Liste des affectations

    // Méthode pour initialiser les données de l'affectation à modifier
    public void setAffectationData(Affectation affectation, ObservableList<Affectation> affectationList) {
        this.affectationToEdit = affectation;
        this.affectationList = affectationList;

        // Pré-remplir les champs avec les données existantes
        codeEmpField.setText(affectation.getCodeEmp());
        codePlaceField.setText(affectation.getCodePlace());
        datePicker.setValue(affectation.getDate().toLocalDate()); // Assuming getDate() returns LocalDateTi
    }

    // Méthode pour définir le stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleUpdate() {
        String codeEmp = codeEmpField.getText();
        String codePlace = codePlaceField.getText(); // Assurez-vous que le nom correspond à l'FXML
        LocalDate date = datePicker.getValue(); // Récupérer la date depuis le DatePicker

        if (!codeEmp.isEmpty() && !codePlace.isEmpty() && date != null) {
            try {
                // Convertir LocalDate en LocalDateTime (par exemple, minuit)
                LocalDateTime dateTime = date.atStartOfDay();

                HttpClient client = HttpClient.newHttpClient();
                JsonObject json = new JsonObject();
                JsonObject idJson = new JsonObject();
                idJson.addProperty("codeEmp", codeEmp);
                idJson.addProperty("codePlace", codePlace);
                json.add("id", idJson);
                json.addProperty("date", dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // Format pour l'API

                // Inclure les objets employe et place existants pour éviter de les perdre
                JsonObject employeJson = new JsonObject();
                employeJson.addProperty("codeEmp", affectationToEdit.getEmploye().getCodeEmp());
                employeJson.addProperty("firstName", affectationToEdit.getEmploye().getFirstName());
                employeJson.addProperty("lastName", affectationToEdit.getEmploye().getLastName());
                employeJson.addProperty("job", affectationToEdit.getEmploye().getJob());
                json.add("employe", employeJson);

                JsonObject placeJson = new JsonObject();
                placeJson.addProperty("codePlace", affectationToEdit.getPlace().getCodePlace());
                placeJson.addProperty("designation", affectationToEdit.getPlace().getDesignation());
                placeJson.addProperty("province", affectationToEdit.getPlace().getProvince());
                json.add("place", placeJson);

                // Afficher le JSON pour débogage
                System.out.println("JSON envoyé : " + json.toString());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/assigned/" + affectationToEdit.getCodeEmp() + "/" + affectationToEdit.getCodePlace())) // Endpoint avec clés originales
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString())) // Changé de PUT à POST
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 204) { // OK ou No Content
                    // Mettre à jour l'objet local uniquement si l'API réussit
                    affectationToEdit.getId().setCodeEmp(codeEmp);
                    affectationToEdit.getId().setCodePlace(codePlace);
                    affectationToEdit.setDate(dateTime);
                    stage.close(); // Fermer le modal
                } else {
                    showErrorAlert("Erreur lors de la mise à jour : " + response.statusCode() + " - " + response.body());
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorAlert("Erreur lors de la connexion à l'API : " + e.getMessage());
            }
        } else {
            showErrorAlert("Veuillez remplir tous les champs.");
        }
    }
    @FXML
    private void handleCancel() {
        stage.close();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}