package com.gestion.gestion_affectation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.gestion.gestion_affectation.model.Place;
import javafx.collections.ObservableList;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.google.gson.JsonObject;

public class AddLieuModalController {

    @FXML
    private TextField codePlaceField;

    @FXML
    private TextField designationField;

    @FXML
    private TextField provinceField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Stage stage;
    private ObservableList<Place> lieuxList;

    // Méthode pour initialiser le contrôleur avec le stage et la liste des lieux
    public void setStageAndLieux(Stage stage, ObservableList<Place> lieuxList) {
        this.stage = stage;
        this.lieuxList = lieuxList;
    }

    @FXML
    private void handleSave() {
        String codePlace = codePlaceField.getText();
        String designation = designationField.getText();
        String province = provinceField.getText();

        if (!codePlace.isEmpty() && !designation.isEmpty() && !province.isEmpty()) {
            Place newLieu = new Place(codePlace, designation, province);

            try {
                HttpClient client = HttpClient.newHttpClient();
                JsonObject json = new JsonObject();
                json.addProperty("codePlace", codePlace); // Include codePlace as the ID
                json.addProperty("designation", designation);
                json.addProperty("province", province);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/places"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 201) { // Created
                    lieuxList.add(newLieu); // Add to local list only if API succeeds
                    stage.close(); // Fermer le modal
                } else {
                    showErrorAlert("Erreur lors de l'ajout : " + response.body());
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
        stage.close(); // Fermer le modal sans ajouter
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.showAndWait();
    }
}