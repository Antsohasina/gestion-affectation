package com.gestion.gestion_affectation.controller;

import com.gestion.gestion_affectation.model.Place;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.gestion.gestion_affectation.model.Place;
import javafx.collections.ObservableList;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.google.gson.JsonObject;

public class EditLieuModalController {

    @FXML
    private TextField codePlaceField; // Aligné avec codePlace

    @FXML
    private TextField designationField;

    @FXML
    private TextField provinceField;

    private Stage stage;
    private Place lieuToEdit;
    private ObservableList<Place> lieuxList;

    // Méthode pour initialiser les données du lieu à modifier
    public void setLieuData(Place lieu, ObservableList<Place> lieuxList) {
        this.lieuToEdit = lieu;
        this.lieuxList = lieuxList;

        // Pré-remplir les champs avec les données existantes
        codePlaceField.setText(lieu.getCodePlace()); // Utilise getCodePlace
        designationField.setText(lieu.getDesignation());
        provinceField.setText(lieu.getProvince());
    }

    // Méthode pour définir le stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleUpdate() {
        String codePlace = codePlaceField.getText();
        String designation = designationField.getText();
        String province = provinceField.getText();

        if (!codePlace.isEmpty() && !designation.isEmpty() && !province.isEmpty()) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                JsonObject json = new JsonObject();
                json.addProperty("codePlace", codePlace); // Utilise codePlace comme identifiant
                json.addProperty("designation", designation);
                json.addProperty("province", province);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/places/" + lieuToEdit.getCodePlace())) // Endpoint avec l'identifiant original
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json.toString())) // Requête PUT
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 204) { // OK ou No Content
                    // Mettre à jour l'objet local uniquement si l'API réussit
                    lieuToEdit.setCodePlace(codePlace); // Utilise setCodePlace
                    lieuToEdit.setDesignation(designation);
                    lieuToEdit.setProvince(province);
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