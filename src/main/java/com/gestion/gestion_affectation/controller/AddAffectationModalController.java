package com.gestion.gestion_affectation.controller;

import com.gestion.gestion_affectation.model.Affectation;
import com.gestion.gestion_affectation.model.Employe;
import com.gestion.gestion_affectation.model.Place;
import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddAffectationModalController {

    @FXML
    private TextField codeEmpField;

    @FXML
    private TextField codeLieuField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private Stage stage;
    private ObservableList<Affectation> affectationList;

    public void setStageAndAffectations(Stage stage, ObservableList<Affectation> affectationList) {
        this.stage = stage;
        this.affectationList = affectationList;
    }

    @FXML
    private void handleSave() {
        String codeEmp = codeEmpField.getText();
        String codeLieu = codeLieuField.getText();
        LocalDate localDate = datePicker.getValue();

        if (!codeEmp.isEmpty() && !codeLieu.isEmpty() && localDate != null) {
            try {
                // Convertir LocalDate en LocalDateTime (par défaut à minuit)
                LocalDateTime dateTime = localDate.atTime(LocalTime.MIDNIGHT);

                // Créer des objets Employe et Place minimaux
                Employe employe = new Employe();
                employe.setCodeEmp(codeEmp);

                Place place = new Place();
                place.setCodePlace(codeLieu);

                // Préparer la requête API
                HttpClient client = HttpClient.newHttpClient();
                JsonObject json = new JsonObject();
                JsonObject idJson = new JsonObject();
                idJson.addProperty("codeEmp", codeEmp);
                idJson.addProperty("codePlace", codeLieu);
                json.add("id", idJson);
                json.addProperty("date", dateTime.toString());

                JsonObject employeJson = new JsonObject();
                employeJson.addProperty("codeEmp", codeEmp);
                json.add("employe", employeJson);

                JsonObject placeJson = new JsonObject();
                placeJson.addProperty("codePlace", codeLieu);
                json.add("place", placeJson);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/assigned"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 201 || response.statusCode() == 200) { // Accept both 201 and 200
                    // Ajouter l'objet à la liste locale après succès de l'API
                    Affectation newAffectation = new Affectation(codeEmp, codeLieu, employe, place, dateTime);
                    affectationList.add(newAffectation);
                    stage.close(); // Fermer le modal
                } else {
                    showErrorAlert("Erreur lors de l'ajout : " + response.statusCode() + " - " + response.body());
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