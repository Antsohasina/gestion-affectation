package com.gestion.gestion_affectation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.gestion.gestion_affectation.model.Employe;
import javafx.collections.ObservableList;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.google.gson.JsonObject;

public class AddEmployeModalController {

    @FXML
    private TextField codeEmpField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField jobField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private Stage stage;
    private ObservableList<Employe> employeList;

    // Initialisation du contrôleur avec le stage et la liste des employés
    public void setStageAndEmployes(Stage stage, ObservableList<Employe> employeList) {
        this.stage = stage;
        this.employeList = employeList;
    }

    @FXML
    private void handleSave() {
        String codeEmp = codeEmpField.getText();
        String lastName = lastNameField.getText();
        String firstName = firstNameField.getText();
        String job = jobField.getText();

        if (!codeEmp.isEmpty() && !lastName.isEmpty() && !firstName.isEmpty() && !job.isEmpty()) {
            Employe newEmploye = new Employe(codeEmp, firstName, lastName, job);

            try {
                HttpClient client = HttpClient.newHttpClient();
                JsonObject json = new JsonObject();
                json.addProperty("codeEmp", codeEmp); // Include codeEmp as the ID
                json.addProperty("firstName", firstName);
                json.addProperty("lastName", lastName);
                json.addProperty("job", job);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/employes"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 201) { // Created
                    employeList.add(newEmploye); // Add to local list
                    stage.close(); // Close the modal
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