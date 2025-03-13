package com.gestion.gestion_affectation.controller;

import com.gestion.gestion_affectation.model.Employe;
import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EditEmployeModalController {

    @FXML
    private TextField codeEmpField; // Champ pour le code de l'employé

    @FXML
    private TextField firstNameField; // Champ pour le prénom

    @FXML
    private TextField lastNameField; // Champ pour le nom de famille

    @FXML
    private TextField jobField; // Champ pour le poste

    private Stage stage;
    private Employe employeToEdit; // Objet Employe au lieu de Lieu
    private ObservableList<Employe> employeList; // Liste des employés

    // Méthode pour initialiser les données de l'employé à modifier
    public void setEmployeData(Employe employe, ObservableList<Employe> employeList) {
        this.employeToEdit = employe;
        this.employeList = employeList;

        // Pré-remplir les champs avec les données existantes
        codeEmpField.setText(employe.getCodeEmp());
        firstNameField.setText(employe.getFirstName());
        lastNameField.setText(employe.getLastName());
        jobField.setText(employe.getJob());
    }

    // Méthode pour définir le stage
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleUpdate() {
        String codeEmp = codeEmpField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String job = jobField.getText();

        if (!codeEmp.isEmpty() && !firstName.isEmpty() && !lastName.isEmpty() && !job.isEmpty()) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                JsonObject json = new JsonObject();
                json.addProperty("codeEmp", codeEmp); // Identifiant de l'employé
                json.addProperty("firstName", firstName);
                json.addProperty("lastName", lastName);
                json.addProperty("job", job);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/employes/" + employeToEdit.getCodeEmp())) // Endpoint avec codeEmp original
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json.toString())) // Requête PUT
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200 || response.statusCode() == 204) { // OK ou No Content
                    // Mettre à jour l'objet local uniquement si l'API réussit
                    employeToEdit.setCodeEmp(codeEmp);
                    employeToEdit.setFirstName(firstName);
                    employeToEdit.setLastName(lastName);
                    employeToEdit.setJob(job);
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